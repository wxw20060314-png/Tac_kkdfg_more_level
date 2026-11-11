package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.entity.ai.MechanicalHorseShareOwnerTargetGoal;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 机械马：可骑的抽象马。玩家控马要鞍，NPC 直接骑。默认驯服、成年、不吃草。
 * 主人默认空，用原版 Owner。背上有活着的骑者就换成这个骑者。查不到或死亡满 5 秒清空。
 * 仇恨目标不能是主人。
 */
public class MechanicalHorseEntity extends AbstractHorse implements GeoEntity {

    private static final double MAX_HEALTH = 300.0D;
    private static final double ARMOR = 30.0D;
    private static final double MOVEMENT_SPEED = 0.5D;
    private static final double ATTACK_DAMAGE = 20.0D;
    /** {@link Attributes#JUMP_STRENGTH} 的默认值。生成时原版会重掷，这里钉回去。 */
    private static final double JUMP_STRENGTH = 0.7D;
    /** 四件七套各 0.2，齐套 0.8。 */
    private static final double KNOCKBACK_RESISTANCE = 0.8D;
    /** 防御显示等级。amplifier + 1。 */
    private static final int DEFENCE_LEVEL = 10;
    /** 主人查不到或已死亡持续这么多 tick 后清空。 */
    private static final int OWNER_LOST_TICKS = 100;

    /** 死亡爆炸半径。不破坏方块。 */
    private static final float DEATH_EXPLOSION_RADIUS = 15.0F;

    private boolean deathExploded;
    /** 主人缺失或死亡的连续 tick。不写存档，读档后重新计。 */
    private int ownerLostTicks;

    private static final float RUN_SWING_AMOUNT = 0.64F;
    private static final int BITE_INTERVAL_TICKS = 36;
    private static final int BITE_WINDUP_TICKS = 8;
    private static final String ATTACK_CONTROLLER = "attack";
    private static final String BITE_TRIGGER = "bite";
    private static final String MISSILE_CONTROLLER = "launcher";
    private static final String MISSILE_TRIGGER = "fire";
    private static final RawAnimation MISSILE_FIRE =
            RawAnimation.begin().thenPlay("animation.mechanical_horse.missile_fire");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.mechanical_horse.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.mechanical_horse.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.mechanical_horse.run");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.mechanical_horse.bite");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MechanicalHorseEntity(EntityType<? extends MechanicalHorseEntity> type, Level level) {
        super(type, level);
        this.setTamed(true);
        this.applyFixedStats();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes().add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BiteAttackGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new MechanicalHorseShareOwnerTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            // 把旧存档或命令写入胸甲槽的马甲退回为掉落物。
            ItemStack armor = this.getItemBySlot(EquipmentSlot.CHEST);
            if (armor.getItem() instanceof HorseArmorItem) {
                ItemEntity dropped = new ItemEntity(
                        this.level(), this.getX(), this.getY(), this.getZ(), armor.copy());
                dropped.setDefaultPickUpDelay();
                if (this.level().addFreshEntity(dropped)) {
                    this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                }
            }
        }
        if (!this.level().isClientSide()) {
            this.tickOwner();
            if (this.isOwner(this.getTarget())) {
                this.setTarget(null);
            }
        }
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 1, false, false));
        }
        MobEffectInstance defence = this.getEffect(ModMobEffects.MOD_DEFFENCE.get());
        if (defence == null || defence.getAmplifier() != DEFENCE_LEVEL - 1) {
            this.addEffect(
                    new MobEffectInstance(
                            ModMobEffects.MOD_DEFFENCE.get(),
                            999999,
                            DEFENCE_LEVEL - 1,
                            false,
                            false,
                            true));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        this.setAge(0);
        this.setTamed(true);
        this.applyFixedStats();
        return data;
    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.35D;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getRandom().nextBoolean()
                ? ModSounds.SOUND_MECHANICAL_HORSE_IDLE_1.get()
                : ModSounds.SOUND_MECHANICAL_HORSE_IDLE_2.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SOUND_WAR_MACHINE_HURT.get();
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void die(DamageSource source) {
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.deathExploded) {
            this.deathExploded = true;
            this.level()
                    .explode(
                            this,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            DEATH_EXPLOSION_RADIUS,
                            false,
                            Level.ExplosionInteraction.TNT);
            this.discard();
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isOwner(target)) {
            return;
        }
        super.setTarget(target);
    }

    private void tickOwner() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        UUID ownerId = this.getOwnerUUID();
        if (ownerId != null) {
            Entity owner = server.getEntity(ownerId);
            if (owner instanceof LivingEntity living && living.isAlive()) {
                this.ownerLostTicks = 0;
            } else if (++this.ownerLostTicks >= OWNER_LOST_TICKS) {
                this.setOwnerUUID(null);
                this.ownerLostTicks = 0;
            }
        }
        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof LivingEntity living && living.isAlive()) {
            this.ownerLostTicks = 0;
            this.setOwnerUUID(living.getUUID());
        }
    }

    private boolean isOwner(@Nullable LivingEntity entity) {
        UUID ownerId = this.getOwnerUUID();
        return entity != null && ownerId != null && ownerId.equals(entity.getUUID());
    }

    /** Fires at a fixed world position. The server accepts at most one volley per cooldown. */
    public boolean tryFireMissiles(Vec3 target) {
        return this.fireMissileVolley(target, null);
    }

    public boolean tryFireMissiles(BlockPos target) {
        return this.tryFireMissiles(Vec3.atCenterOf(target));
    }

    /** Fires a volley that tracks a living target. */
    public boolean tryFireMissiles(LivingEntity target) {
        return this.tryFireTrackingMissiles(target);
    }

    public boolean tryFireTrackingMissiles(LivingEntity target) {
        return this.fireMissileVolley(target.getBoundingBox().getCenter(), target);
    }

    public boolean canLockMissileTarget(LivingEntity target) {
        return target != this && target.isAlive() && target.level() == this.level()
                && !target.isPassengerOfSameVehicle(this) && !this.isOwner(target)
                && MechanicalHorseMissileLauncher.inRange(this.position(), target.getBoundingBox().getCenter());
    }

    private boolean fireMissileVolley(Vec3 target, @Nullable LivingEntity tracked) {
        if (!MechanicalHorseMissileLauncher.tryFire(this, target, tracked)) {
            return false;
        }
        this.triggerAnim(MISSILE_CONTROLLER, MISSILE_TRIGGER);
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 3, state -> {
            float swing = state.getLimbSwingAmount();
            if (swing < 0.06F || !this.onGround()) {
                state.getController().setAnimationSpeed(1.0D);
                return state.setAndContinue(IDLE);
            }
            if (swing >= RUN_SWING_AMOUNT) {
                state.getController().setAnimationSpeed(Mth.clampedMap(swing, RUN_SWING_AMOUNT, 1.0F, 1.0F, 1.85F));
                return state.setAndContinue(RUN);
            }
            state.getController().setAnimationSpeed(Mth.clampedMap(swing, 0.06F, RUN_SWING_AMOUNT, 0.65F, 1.65F));
            return state.setAndContinue(WALK);
        }));
        controllers.add(new AnimationController<>(this, ATTACK_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(BITE_TRIGGER, BITE));
        controllers.add(new AnimationController<>(this, MISSILE_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(MISSILE_TRIGGER, MISSILE_FIRE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private final class BiteAttackGoal extends MeleeAttackGoal {
        @Nullable private LivingEntity biteTarget;
        private long impactTick;
        private long nextBiteTick;

        private BiteAttackGoal() {
            super(MechanicalHorseEntity.this, 1.0D, false);
        }

        @Override
        public boolean canContinueToUse() {
            // Finish the windup even when navigation has already reached the target.
            return (this.biteTarget != null
                            && this.biteTarget == this.mob.getTarget()
                            && this.biteTarget.isAlive())
                    || super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target, double distanceSqr) {
            long now = this.mob.level().getGameTime();
            if (this.biteTarget != null) {
                if (now < this.impactTick) {
                    return;
                }
                if (target == this.biteTarget
                        && this.mob.canAttack(target)
                        && distanceSqr <= this.getAttackReachSqr(target)) {
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(target);
                }
                this.biteTarget = null;
                return;
            }

            if (now >= this.nextBiteTick && distanceSqr <= this.getAttackReachSqr(target)) {
                this.biteTarget = target;
                this.impactTick = now + BITE_WINDUP_TICKS;
                this.nextBiteTick = now + BITE_INTERVAL_TICKS;
                MechanicalHorseEntity.this.triggerAnim(ATTACK_CONTROLLER, BITE_TRIGGER);
            }
        }

        @Override
        public void stop() {
            this.biteTarget = null;
            super.stop();
        }
    }

    private void applyFixedStats() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        this.getAttribute(Attributes.ARMOR).setBaseValue(ARMOR);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ATTACK_DAMAGE);
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(JUMP_STRENGTH);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(KNOCKBACK_RESISTANCE);
        this.setHealth(this.getMaxHealth());
    }
}

