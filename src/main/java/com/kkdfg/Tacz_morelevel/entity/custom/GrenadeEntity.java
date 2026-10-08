package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

public class GrenadeEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Integer> DATA_FUSE_TICKS =
            SynchedEntityData.defineId(GrenadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FUSE_MAX_TICKS =
            SynchedEntityData.defineId(GrenadeEntity.class, EntityDataSerializers.INT);

    private static final double BOUNCE_FACTOR = 0.5D;
    private static final double BOUNCE_CONSTANT = 0.08D;
    private int fuseTicks = 100;
    private int fuseMaxTicks = 100;

    public GrenadeEntity(EntityType<? extends GrenadeEntity> type, Level level) {
        super(type, level);
    }

    public GrenadeEntity(Level level, LivingEntity owner) {
        super(ModEntities.GRENADE.get(), owner, level);
    }

    /** 子类指定 {@link EntityType}（如闪光弹）。 */
    protected GrenadeEntity(EntityType<? extends GrenadeEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    public void setFuseTicks(int ticks) {
        int clamped = Math.max(1, ticks);
        this.fuseTicks = clamped;
        this.fuseMaxTicks = clamped;
        this.syncFuseToClient();
    }

    public int getFuseTicks() {
        return this.entityData.get(DATA_FUSE_TICKS);
    }

    public int getFuseMaxTicks() {
        return this.entityData.get(DATA_FUSE_MAX_TICKS);
    }

    private void syncFuseToClient() {
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_FUSE_TICKS, this.fuseTicks);
            this.entityData.set(DATA_FUSE_MAX_TICKS, this.fuseMaxTicks);
        }
    }

    /** 服务端：爆炸结算期间置位，由 {@link com.kkdfg.Tacz_morelevel.event.GrenadeControlEvents} 取消投掷者自伤。 */
    public static final String TAG_SKIP_EXPLOSION_SELF_DAMAGE = "TaczMoreLevelSkipSelfGrenadeExplosion";

    /**
     * 手雷爆炸用：不破方块。投掷者自伤由 {@link #TAG_SKIP_EXPLOSION_SELF_DAMAGE} 与 {@code LivingHurtEvent} 排除
     * （1.20.1 的 {@link ExplosionDamageCalculator} 无 {@code shouldDamageEntity}）。
     * <p>
     * 原独立类 {@code GrenadeExplosionNoTerrainCalculator} 已停用，逻辑集中于此。
     *
     * @param unused 保留参数以便与旧调用兼容，当前未使用
     */
    public static ExplosionDamageCalculator grenadeExplosionDamageCalculator(@Nullable Entity unused) {
        return new ExplosionDamageCalculator() {
            @Override
            public boolean shouldBlockExplode(
                    Explosion explosion,
                    BlockGetter level,
                    BlockPos pos,
                    BlockState state,
                    float fluidExplosionPower) {
                return false;
            }
        };
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GRENADE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.fuseTicks--;
        this.syncFuseToClient();
        if (this.fuseTicks <= 0) {
            explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Vec3 vel = this.getDeltaMovement();
        if (vel.lengthSqr() < 1.0E-6D) {
            return;
        }
        // 几何碰撞近似：反向并衰减
        Vec3 bounced = vel.scale(-BOUNCE_FACTOR);
        this.setDeltaMovement(applyConstantDrag(bounced));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Vec3 vel = this.getDeltaMovement();
        if (vel.lengthSqr() < 1.0E-6D) {
            return;
        }
        Direction face = result.getDirection();
        Vec3 n = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        // 几何反射：v' = v - 2(v·n)n，然后执行“速度二分之一 - 常数”
        Vec3 reflected = vel.subtract(n.scale(2.0D * vel.dot(n)));
        Vec3 bounced = reflected.scale(BOUNCE_FACTOR);
        this.setDeltaMovement(applyConstantDrag(bounced));
    }

    private static Vec3 applyConstantDrag(Vec3 v) {
        double len = v.length();
        if (len <= BOUNCE_CONSTANT) {
            return Vec3.ZERO;
        }
        return v.scale((len - BOUNCE_CONSTANT) / len);
    }

    protected void explode() {
        if (this.level().isClientSide) {
            return;
        }
        ServerLevel sl = (ServerLevel) this.level();
        /*
         * ——— 旧实现（保留备忘）———
         * DamageSource blast = this.damageSources().explosion(null, null);
         * sl.explode(
         *         null,
         *         blast,
         *         GrenadeExplosionNoTerrainCalculator.INSTANCE,
         *         this.getX(),
         *         this.getY(0.0625D),
         *         this.getZ(),
         *         Config.grenadeExplosionPower,
         *         false,
         *         Level.ExplosionInteraction.MOB);
         * 说明：ExplosionInteraction.MOB 依赖计算器 shouldBlockExplode=false 才不破方块；
         * 爆炸源与伤害源为 null 时仍可能对投掷者结算爆炸伤害。
         * ———————————————————
         */
        Entity owner = this.getOwner();
        DamageSource blast = this.damageSources().explosion(this, owner);
        if (owner != null) {
            owner.getPersistentData().putBoolean(TAG_SKIP_EXPLOSION_SELF_DAMAGE, true);
        }
        try {
            sl.explode(
                    this,
                    blast,
                    grenadeExplosionDamageCalculator(null),
                    this.getX(),
                    this.getY(0.0625D),
                    this.getZ(),
                    Config.grenadeExplosionPower,
                    false,
                    Level.ExplosionInteraction.MOB);
        } finally {
            if (owner != null) {
                owner.getPersistentData().remove(TAG_SKIP_EXPLOSION_SELF_DAMAGE);
            }
        }
        this.discard();
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FUSE_TICKS, 100);
        this.entityData.define(DATA_FUSE_MAX_TICKS, 100);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FuseTicks", this.fuseTicks);
        tag.putInt("FuseMaxTicks", this.fuseMaxTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fuseTicks = Math.max(1, tag.getInt("FuseTicks"));
        this.fuseMaxTicks = Math.max(this.fuseTicks, tag.getInt("FuseMaxTicks"));
        if (this.fuseMaxTicks <= 0) {
            this.fuseMaxTicks = this.fuseTicks;
        }
        this.syncFuseToClient();
    }
}
