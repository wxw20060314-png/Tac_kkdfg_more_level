package com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon;

import com.kkdfg.Tacz_morelevel.armor.plugin.shoulder_cannon.AbstractShoulderCannonPluginItem;
import com.kkdfg.Tacz_morelevel.entity.ai.ShoulderCannonLookControl;
import com.kkdfg.Tacz_morelevel.event.ShoulderCannonKilledEvent;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.tacz.guns.entity.EntityKineticBullet;
import java.util.Collections;
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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

/**
 * 肩炮实体抽象：Mob 但不开寻路。移动只跟宿主；无目标时随机转头。
 */
public abstract class AbstractShoulderCannonEntity extends Mob {

    public static final float DEFAULT_MAX_HEALTH = 100.0F;
    public static final int REGEN_INTERVAL_TICKS = 20;
    public static final float REGEN_AMOUNT = 10.0F;

    /** 肩炮子弹标记：父类据此屏蔽对宿主的直击与爆炸。子类开火时调用 {@link #markShoulderBullet}。 */
    public static final String TAG_SHOULDER_CANNON_BULLET = "ShoulderCannonBullet";

    private static final String TAG_HOST = "Host";
    private static final String TAG_PLUGIN_SLOT = "PluginSlot";

    private static final EntityDataAccessor<Optional<UUID>> DATA_HOST_UUID =
            SynchedEntityData.defineId(AbstractShoulderCannonEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private int pluginSlot;
    private boolean specialDeathStarted;

    protected AbstractShoulderCannonEntity(
            EntityType<? extends AbstractShoulderCannonEntity> type, Level level) {
        super(type, level);
        this.lookControl = new ShoulderCannonLookControl(this);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.pluginSlot = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, DEFAULT_MAX_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /** 整炮跟头转，Gecko 用 {@code yBodyRot}。 */
    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        this.yBodyRot = this.yHeadRot;
        return animStep;
    }

    /**
     * 交战目标是否合法。子类选目标时调用；创造/旁观玩家、宿主、炮自身、同盟、超距均排除。
     */
    public boolean isValidCombatTarget(LivingEntity host, @Nullable LivingEntity target) {
        if (host == null || target == null || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        if (target == host || target == this) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (target.level() != host.level()) {
            return false;
        }
        if (host.isAlliedTo(target) || target.isAlliedTo(host)) {
            return false;
        }
        double range = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        return host.distanceToSqr(target) <= range * range;
    }

    /** 标记为肩炮子弹，供宿主免伤识别。 */
    public static void markShoulderBullet(EntityKineticBullet bullet) {
        if (bullet != null) {
            bullet.getPersistentData().putBoolean(TAG_SHOULDER_CANNON_BULLET, true);
        }
    }

    /** 这发肩炮子弹的 owner 是否就是受伤者（宿主自伤，含爆炸溅射）。 */
    public static boolean hurtsOwnHost(@Nullable Entity entity, LivingEntity victim) {
        return entity instanceof EntityKineticBullet bullet
                && bullet.getPersistentData().getBoolean(TAG_SHOULDER_CANNON_BULLET)
                && bullet.getOwner() == victim;
    }

    public void bind(LivingEntity host, int pluginSlot) {
        this.setHostUuid(host.getUUID());
        this.pluginSlot = pluginSlot;
    }

    public int getPluginSlot() {
        return this.pluginSlot;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HOST_UUID, Optional.empty());
    }

    public void setHostUuid(@Nullable UUID uuid) {
        this.entityData.set(DATA_HOST_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getHostUuid() {
        return this.entityData.get(DATA_HOST_UUID).orElse(null);
    }

    @Nullable
    public LivingEntity getHost() {
        UUID uuid = this.getHostUuid();
        if (uuid == null) {
            return null;
        }
        if (this.level() instanceof ServerLevel server) {
            var entity = server.getEntity(uuid);
            return entity instanceof LivingEntity living && living.isAlive() ? living : null;
        }
        var player = this.level().getPlayerByUUID(uuid);
        if (player != null && player.isAlive()) {
            return player;
        }
        for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
            if (uuid.equals(living.getUUID()) && living.isAlive()) {
                return living;
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.isBindingValid()) {
            this.retractAndDiscard();
            return;
        }
        LivingEntity host = this.getHost();
        if (host != null) {
            this.snapToHeadRight(host);
        }
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.tickRegen();
    }

    @Override
    public void travel(Vec3 travelVector) {
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    private void tickRegen() {
        if (this.tickCount % REGEN_INTERVAL_TICKS != 0) {
            return;
        }
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(REGEN_AMOUNT);
        }
    }

    public boolean isBindingValid() {
        LivingEntity host = this.getHost();
        if (host == null || !host.isAlive() || host.level() != this.level()) {
            return false;
        }
        ItemStack chest = host.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BulletproofArmor)) {
            return false;
        }
        ItemStack plugin = BulletproofArmor.getPlugin(chest, this.pluginSlot);
        if (!(plugin.getItem() instanceof AbstractShoulderCannonPluginItem)) {
            return false;
        }
        UUID bound = AbstractShoulderCannonPluginItem.getBoundCannonUuid(plugin);
        return this.getUUID().equals(bound);
    }

    /** 贴宿主头部右后方；只改位置，朝向由 LookControl / 目标决定。 */
    public void snapToHeadRight(LivingEntity host) {
        Vec3 pos = attachPos(host, this.getBbWidth(), this.getBbHeight(), 1.0F);
        this.moveTo(pos.x, pos.y, pos.z, this.getYRot(), this.getXRot());
        this.setDeltaMovement(Vec3.ZERO);
    }

    /** {@code partialTick} 为 1 用当前坐标；渲染用宿主 xo/yo/zo 插值。 */
    public static Vec3 attachPos(LivingEntity host, float cannonWidth, float cannonHeight, float partialTick) {
        double hx = Mth.lerp(partialTick, host.xo, host.getX());
        double hy = Mth.lerp(partialTick, host.yo, host.getY());
        double hz = Mth.lerp(partialTick, host.zo, host.getZ());
        float yaw = Mth.rotLerp(partialTick, host.yHeadRotO, host.yHeadRot);
        float yawRad = yaw * ((float) Math.PI / 180.0F);
        double rightX = -Mth.sin(yawRad + (float) (Math.PI / 2.0D));
        double rightZ = Mth.cos(yawRad + (float) (Math.PI / 2.0D));
        double forwardX = -Mth.sin(yawRad);
        double forwardZ = Mth.cos(yawRad);
        double rightDist = host.getBbWidth() * 0.5D + cannonWidth * 0.5D + 0.28D;
        double forwardDist = -0.28D;
        double eyeY = hy + host.getEyeHeight();
        return new Vec3(
                hx + rightX * rightDist + forwardX * forwardDist,
                eyeY ,
                hz + rightZ * rightDist + forwardZ * forwardDist);
    }

    /** 炮口朝向目标。不改 xo 角，留给客户端插值。 */
    public void faceToward(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }
        Vec3 from = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 to = target.getEyePosition();
        Vec3 dir = to.subtract(from);
        if (dir.lengthSqr() < 1.0E-8D) {
            return;
        }
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch =
                (float) (-(Mth.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * (180.0D / Math.PI)));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yHeadRot = yaw;
        this.yBodyRot = yaw;
    }

    /** 非法绑定：收回后直接消失（无特色死亡、无冷却）。 */
    public void retractAndDiscard() {
        if (this.level().isClientSide() || this.isRemoved()) {
            return;
        }
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isHostProjectile(source.getDirectEntity()) || isHostProjectile(source.getEntity())) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        this.hurtDuration = 0;
        this.hurtTime = 0;
        this.setDeltaMovement(Vec3.ZERO);
        return hurt;
    }

    private boolean isHostProjectile(Entity entity) {
        if (!(entity instanceof EntityKineticBullet bullet) || bullet.getOwner() == null) {
            return false;
        }
        UUID host = this.getHostUuid();
        return host != null && host.equals(bullet.getOwner().getUUID());
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 2 || id == 3) {
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void die(DamageSource source) {
        if (this.level().isClientSide() || this.specialDeathStarted) {
            return;
        }
        this.specialDeathStarted = true;
        this.playSpecialDeathAndDiscard(source);
    }

    /** 特色死亡：炸弹小车爆炸音 + 事件 + 消失。不走原版躺平。 */
    protected void playSpecialDeathAndDiscard(DamageSource source) {
        SoundEvent death = this.getDeathSound();
        if (death != null) {
            this.playSound(death, this.getSoundVolume(), this.getVoicePitch());
        }
        LivingEntity host = this.getHost();
        if (host != null) {
            MinecraftForge.EVENT_BUS.post(new ShoulderCannonKilledEvent(host, this, source));
        }
        this.discard();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.SOUND_WAR_MACHINE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID host = this.getHostUuid();
        if (host != null) {
            tag.putUUID(TAG_HOST, host);
        }
        tag.putInt(TAG_PLUGIN_SLOT, this.pluginSlot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(TAG_HOST)) {
            this.setHostUuid(tag.getUUID(TAG_HOST));
        }
        this.pluginSlot = tag.getInt(TAG_PLUGIN_SLOT);
    }
}
