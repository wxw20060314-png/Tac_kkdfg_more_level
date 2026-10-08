package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.entity.ai.BombCartChaseTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.BombCartOwnerHurtTargetGoal;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.tacz.guns.entity.EntityKineticBullet;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 中立炸弹小车：有主时追主人仇恨目标并近距自爆；无主待机。
 * <p>TTL 为独立寿命保险丝（创建时设定，与认主无关）：每 tick 递减，清 0 一律大爆，保证不长期存活。
 * 每 tick 检测主人：已死或不存在则清 UUID。
 * 无跟随主人 Goal。爆炸走原版 TNT 交互（可破方块、距离衰减）。
 * 伤害：同主小车互免；非玩家主人与车互相免伤（车 {@code hurt}）；玩家主人可打坏自己的车。
 * 小车爆炸不对任何主人造成伤害（见 {@link com.kkdfg.Tacz_morelevel.event.BombCartOwnerHateEvents}）。
 *
 * <p>传说技能：爆炸小车 — 爆破七级全套召唤后 {@link #setOwner} 认主，可作为主人的炸弹。
 * <p>被打死：直接消失 + 半径 {@link #DEATH_EXPLOSION_RADIUS} 小爆炸（无死亡侧翻）。
 */
public class BombCartEntity extends PathfinderMob implements GeoEntity {

    /** 冲锋/TTL 自爆半径（原版爆炸威力）。 */
    public static final float EXPLOSION_RADIUS = 7.0F;
    /** 被打死时的小爆炸半径。 */
    public static final float DEATH_EXPLOSION_RADIUS = 1.5F;
    /** 默认最长存活 tick（寿命保险丝）；归零自爆。与是否有主无关。 */
    public static final int DEFAULT_TTL = 200;
    /** 距目标多近触发自爆（方块）。 */
    public static final double DETONATE_RANGE = 2.0D;

    private static final String TAG_OWNER = "Owner";
    private static final String TAG_TTL = "Ttl";

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(BombCartEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int ttl;
    private boolean exploded;

    /** 刷怪蛋 / {@link EntityType#create}：默认寿命 {@link #DEFAULT_TTL}。 */
    public BombCartEntity(EntityType<? extends BombCartEntity> type, Level level) {
        this(type, level, DEFAULT_TTL);
    }

    /** @param ttlTicks 最长存活 tick，到 0 必爆。 */
    public BombCartEntity(EntityType<? extends BombCartEntity> type, Level level, int ttlTicks) {
        super(type, level);
        this.ttl = Math.max(0, ttlTicks);
        this.xpReward = 0;
        // 仅允许走上半砖（0.5）；整砖靠跳跃跨过
        this.setMaxUpStep(0.5F);
    }

    /** 原版起跳力；半砖靠 {@code maxUpStep=0.5} 走上，整砖靠跳。 */
    @Override
    protected float getJumpPower() {
        return 0.42F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.65D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BombCartChaseTargetGoal(this, 1.0D));
        // 巡逻靠前，次于追击；无 target 时闲逛
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.targetSelector.addGoal(1, new BombCartOwnerHurtTargetGoal(this));
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.setOwnerUUID(owner == null ? null : owner.getUUID());
    }

    @Nullable
    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null || !(this.level() instanceof ServerLevel server)) {
            return null;
        }
        Entity entity = server.getEntity(uuid);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    public boolean isOwnedBy(LivingEntity living) {
        UUID uuid = this.getOwnerUUID();
        return uuid != null && living != null && uuid.equals(living.getUUID());
    }

    public int getTtl() {
        return this.ttl;
    }

    /** 显式设定剩余寿命（与认主无关）。 */
    public void setTtl(int ttl) {
        this.ttl = Math.max(0, ttl);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() || this.exploded || !this.isAlive()) {
            return;
        }

        // TTL：独立寿命保险丝，清 0 一律大爆
        this.ttl--;
        if (this.ttl <= 0) {
            this.detonate();
            return;
        }

        // 主人：死了或不存在则清 UUID
        if (this.getOwnerUUID() != null && this.getOwner() == null) {
            this.setOwnerUUID(null);
            this.setTarget(null);
            return;
        }
        if (this.getOwnerUUID() == null) {
            this.setTarget(null);
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null
                && target.isAlive()
                && this.distanceToSqr(target) <= DETONATE_RANGE * DETONATE_RANGE) {
            this.detonate();
        }
    }

    /** TTL / 近距冲锋大爆炸；不依赖是否有主。 */
    public void detonate() {
        this.explodeAndDiscard(EXPLOSION_RADIUS);
    }

    /** 被打死：小爆炸后直接消失（不播死亡侧翻）。 */
    @Override
    public void die(DamageSource source) {
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.exploded) {
            this.explodeAndDiscard(DEATH_EXPLOSION_RADIUS);
        } else {
            this.discard();
        }
    }

    /** 原版 TNT 交互爆炸；实体伤按距离衰减。同主/非玩家主人互免见 {@link #hurt}。 */
    private void explodeAndDiscard(float radius) {
        if (this.level().isClientSide() || this.exploded) {
            return;
        }
        this.exploded = true;
        this.level()
                .explode(
                        this,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        radius,
                        false,
                        Level.ExplosionInteraction.TNT);
        this.discard();
    }

    /**
     * 受伤屏蔽：同主小车互免；主人为非玩家（如炸弹宝宝）时屏蔽其伤害；玩家主人可打坏自己的车。
     * 含 TaCZ 子弹（射手为子弹 owner）。
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        LivingEntity attacker = resolveAttacker(source);
        if (shouldBlockDamageFrom(attacker)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        this.hurtDuration = 0;
        this.hurtTime = 0;
        return hurt;
    }

    /**
     * @return true 应屏蔽该攻击者对本车的伤害
     */
    public boolean shouldBlockDamageFrom(@Nullable LivingEntity attacker) {
        if (attacker == null || attacker == this) {
            return false;
        }
        if (attacker instanceof BombCartEntity otherCart) {
            UUID a = this.getOwnerUUID();
            UUID b = otherCart.getOwnerUUID();
            return a != null && a.equals(b);
        }
        if (!this.isOwnedBy(attacker)) {
            return false;
        }
        // 主人是玩家：不屏蔽；非玩家主人（炸弹宝宝等）：屏蔽
        return !(attacker instanceof Player);
    }

    @Nullable
    private static LivingEntity resolveAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof EntityKineticBullet bullet
                && bullet.getOwner() instanceof LivingEntity living) {
            return living;
        }
        Entity causing = source.getEntity();
        if (causing instanceof EntityKineticBullet bullet
                && bullet.getOwner() instanceof LivingEntity living) {
            return living;
        }
        if (causing instanceof LivingEntity living) {
            return living;
        }
        if (direct instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // 免击退
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (super.isAlliedTo(other)) {
            return true;
        }
        if (other instanceof LivingEntity living && this.isOwnedBy(living)) {
            return true;
        }
        if (other instanceof BombCartEntity otherCart) {
            UUID a = this.getOwnerUUID();
            UUID b = otherCart.getOwnerUUID();
            return a != null && a.equals(b);
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) {
            tag.putUUID(TAG_OWNER, this.getOwnerUUID());
        }
        tag.putInt(TAG_TTL, this.ttl);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(TAG_OWNER)) {
            this.setOwnerUUID(tag.getUUID(TAG_OWNER));
        } else {
            this.setOwnerUUID(null);
        }
        if (tag.contains(TAG_TTL)) {
            this.ttl = tag.getInt(TAG_TTL);
        }
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (!this.level().isClientSide() && this.getOwnerUUID() == null) {
            this.setOwner(player);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.random.nextInt(5)) {
            case 0 -> ModSounds.SOUND_BOMB_CART_IDLE_1.get();
            case 1 -> ModSounds.SOUND_BOMB_CART_IDLE_2.get();
            case 2 -> ModSounds.SOUND_BOMB_CART_IDLE_3.get();
            case 3 -> ModSounds.SOUND_BOMB_CART_IDLE_4.get();
            default -> ModSounds.SOUND_BOMB_CART_IDLE_5.get();
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SOUND_WAR_MACHINE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "move",
                        5,
                        state -> {
                            if (state.isMoving()) {
                                state.setAnimation(WALK);
                            } else {
                                state.setAnimation(IDLE);
                            }
                            return PlayState.CONTINUE;
                        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
