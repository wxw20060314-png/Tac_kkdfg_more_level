package com.kkdfg.Tacz_morelevel.entity.companion;

import com.google.common.collect.ImmutableList;
import com.kkdfg.Tacz_morelevel.BodyguardProfile;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.CompanionMob;
import com.kkdfg.Tacz_morelevel.api.GunEquippedMob;
import com.kkdfg.Tacz_morelevel.api.JumpActingMob;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.api.MilitiaAcquireTargetVoice;
import com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob;
import com.kkdfg.Tacz_morelevel.api.TaczCrawlCapableMob;
import com.kkdfg.Tacz_morelevel.entity.ConfiguredLivingAttributes;
import com.kkdfg.Tacz_morelevel.entity.SoldierTaczCrawlState;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunSoldierRangedGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeBodyRotationControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierMoveControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierPlayerLikeLookControl;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierUnarmedMeleeGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionAvoidGrenadeAndFireGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionAttackMonsterGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionFollowOwnerGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithTargetInLongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionOwnerHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionOwnerHurtTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionTamedGunGoal;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierGroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.scores.Team;

/**
 * ???????????? / ?? / ??????
 * <p>
 * ???????????? stub????????? {@link AbstractFriendlyCompanionEntity} ? override ?????
 * ??????????????
 */
public abstract class AbstractFriendEntity extends PathfinderMob
        implements
                RangedAttackMob,
                GunEquippedMob,
                CompanionMob,
                TaczCrawlCapableMob,
                PlayerLikeHeadBodyYawMob,
                MilitiaAcquireTargetVoice,
                JumpActingMob {

    private static final EntityDataAccessor<Boolean> DATA_TAME =
            SynchedEntityData.defineId(AbstractFriendEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(AbstractFriendEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_TACZ_CRAWLING =
            SynchedEntityData.defineId(AbstractFriendEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private ResourceLocation persistedGunDataId;
    private boolean combatGoalsMounted;

    /** 下一次 {@link #getJumpPower()} 用的档；消费后归 {@link #defaultJumpTier()}。子类可细改。 */
    private int currentJumpTier;

    protected AbstractFriendEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.lookControl = new SoldierPlayerLikeLookControl(this);
        this.moveControl = new SoldierMoveControl(this);
        this.xpReward = 0;
        this.disableVanillaEquipmentDrops();
        if (this.getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
            ground.setCanPassDoors(true);
        }
        this.setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, 0.0F);
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.resetJumpTierToDefault();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SoldierGroundPathNavigation(this, level);
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int currentJumpTier() {
        return this.currentJumpTier;
    }

    @Override
    public void setCurrentJumpTier(int tier) {
        int last = JumpController.JUMP_TIERS.size() - 1;
        if (last < 0) {
            this.currentJumpTier = 0;
            return;
        }
        int i = tier;
        if (i < 0) {
            i = 0;
        } else if (i > last) {
            i = last;
        }
        this.currentJumpTier = i;
    }

    @Override
    protected float getJumpPower() {
        float base = this.jumpPowerAt(this.currentJumpTier(), false);
        float power = base * this.getBlockJumpFactor() + this.getJumpBoostPower();
        this.resetJumpTierToDefault();
        return power;
    }

    @Override
    public boolean requestJump(int tier) {
        if (!this.onGround() || this.isInWater() || this.isInLava()) {
            return false;
        }
        this.setCurrentJumpTier(tier);
        this.getJumpControl().jump();
        return true;
    }

    @Override
    public boolean performJump() {
        return this.requestJump(this.defaultJumpTier());
    }

    @Override
    public boolean performJumpTier(int tier) {
        return this.performJumpTier(tier, true);
    }

    @Override
    public boolean performJumpTier(int tier, boolean respectMin) {
        int i = tier;
        if (respectMin) {
            int[] w = this.normalizedWindow();
            if (i < w[0]) {
                i = w[0];
            }
            if (i > w[1]) {
                i = w[1];
            }
        }
        return this.requestJump(i);
    }

    @Override
    public boolean performJumpPower(float basePower) {
        return this.requestJump(JumpController.nearestTier(basePower));
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float multiplier) {
        return JumpController.calculateFallDamage(this, this, fallDistance, multiplier);
    }

    /** ??????????Helper ??????????????????*/
    private void disableVanillaEquipmentDrops() {
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                this.setDropChance(slot, 0.0F);
            }
        }
    }

    protected abstract Goal gunGoal();

    protected abstract BodyguardProfile bodyguardProfile();

    protected abstract MeleeAttackGoal meleeGoal();

    protected void applyConfiguredLivingAttributes(boolean fillHealth) {
        ConfiguredLivingAttributes.apply(
                this, Config.bodyguardAttributesFor(this.bodyguardProfile()), fillHealth);
    }

    public boolean canUseCompanionGunCombatGoal() {
        return this.isTame();
    }

    public boolean canUseUnarmedMeleeGoal() {
        return this.isTame() && !IGun.mainHandHoldGun(this);
    }

    protected final MeleeAttackGoal newTameMeleeGoal(double speed) {
        return new MeleeAttackGoal(this, speed, false) {
            @Override
            public boolean canUse() {
                return AbstractFriendEntity.this.isTame() && super.canUse();
            }

            @Override
            public void start() {
                super.start();
                AbstractFriendEntity.this.setAggressive(true);
            }

            @Override
            public void stop() {
                super.stop();
                AbstractFriendEntity.this.setAggressive(false);
            }
        };
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new SoldierPlayerLikeBodyRotationControl(this);
    }

    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        MobGunPlayerLikeAimHelper.tickHeadTurnBodyFollow(this, this.yHeadRot);
        return animStep;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TAME, false);
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_TACZ_CRAWLING, false);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnReason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag dataTag) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
        this.setLeftHanded(false);
        if (!level.isClientSide()) {
            this.applyConfiguredLivingAttributes(true);
        }
        return spawnData;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CompanionAvoidGrenadeAndFireGoal(this));
        this.goalSelector.addGoal(4, new CompanionFollowOwnerGoal(this, 1.0D, 25.0F, 10.0F, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new CompanionOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CompanionHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new CompanionOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new CompanionAttackMonsterGoal(this));

        Config.CombatGoalStats g = Config.bodyguardCombatGoalsFor(BodyguardProfile.ELITE);
        this.goalSelector.addGoal(
                1,
                new CompanionMobTaczCombatCrawlGoal(
                        this,
                        g.crawlRandomDenominator,
                        g.crawlExitRandomDenominator,
                        g.crawlMaxHealthExclusive,
                        g.crawlMinPlanarDistance,
                        g.crawlMinHeightToDistanceRatio,
                        g.crawlEnabled));
        this.goalSelector.addGoal(3, new CompanionMobRandomSneakWithNoTargetGoal(this));
        this.goalSelector.addGoal(
                1,
                new CompanionMobRandomSneakWithTargetInLongDistanceGoal(
                        this,
                        g.longSneakTimeDelay,
                        g.longSneakTime,
                        g.longSneakRandomTime,
                        g.longSneakDistance,
                        g.longSneakEnabled));
        this.goalSelector.addGoal(
                1,
                new CompanionMobRandomSneakWithTargetInShortDistanceGoal(
                        this,
                        g.shortSneakTimeDelay,
                        g.shortSneakTime,
                        g.shortSneakRandomTime,
                        g.shortSneakDistance,
                        g.shortSneakEnabled));
        this.registerCompanionGunReloadGoal();
    }

    protected void registerCompanionGunReloadGoal() {
        this.goalSelector.addGoal(1, new CompanionMobGunReloadGoal(this));
    }

    /** ????????????????/ ????*/
    public boolean mayUseCombatCrouchOrCrawl() {
        return this.isTame() && !this.isOrderedToSit();
    }

    public void reassessWeaponGoal() {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.mountPermanentCombatGoals();
        Goal gun = this.gunGoal();
        if (gun instanceof MobGunSoldierRangedGoal ranged) {
            ranged.setMinAttackInterval(this.bodyguardGunBurstIntervalTicks());
        }
    }

    protected void mountPermanentCombatGoals() {
        if (this.combatGoalsMounted) {
            return;
        }
        Goal gun = this.gunGoal();
        MeleeAttackGoal melee = this.meleeGoal();
        if (gun == null || melee == null) {
            return;
        }
        this.combatGoalsMounted = true;
        this.goalSelector.addGoal(2, new CompanionTamedGunGoal(this, gun));
        this.goalSelector.addGoal(2, new SoldierUnarmedMeleeGoal(this, melee));
    }

    protected int bodyguardGunBurstIntervalTicks() {
        return Config.mapNpcGunBurstInterval(5);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (IGun.mainHandHoldGun(this)) {
            ItemStack main = this.getMainHandItem();
            TaczGunMobHelper.normalizeFireModeIfInvalid(main);
            TaczGunMobHelper.preferAutoFireModeForMobMainHandGun(main);
            if (this instanceof AbstractFriendlyCompanionEntity companion) {
                CompanionBodyguardAmmoHelper.stripDummyAmmoTag(main);
                CompanionBodyguardAmmoHelper.applyCreativeMagazineTopUp(companion);
            } else {
                TaczGunMobHelper.tickInfiniteMobAmmo(this);
            }
        }
    }

    @Override
    public boolean isGunCombatAiming() {
        return IGun.mainHandHoldGun(this) && this.isAggressive();
    }

    @Override
    public boolean isTaczCrawlingSynced() {
        return this.entityData.get(DATA_TACZ_CRAWLING);
    }

    public void syncTaczCrawlingFlag(boolean crawling) {
        this.entityData.set(DATA_TACZ_CRAWLING, crawling);
    }

    @Override
    public boolean isTaczCrawling() {
        return CompanionTaczCrawlHelper.isCrawling(this);
    }

    @Override
    public SoldierTaczCrawlState getTaczCrawlState() {
        return CompanionTaczCrawlHelper.getCrawlState(this);
    }

    public boolean setTaczCrawling(boolean crawling) {
        return CompanionTaczCrawlHelper.applyCrawl(this, crawling);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.mayUseCombatCrouchOrCrawl() && this.isTaczCrawlingSynced()) {
            CompanionTaczCrawlHelper.setCrawlIntent(this, false);
        }
        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            if (target != null
                    && (!target.isAlive()
                            || (target instanceof Player player
                                    && (player.isCreative() || player.isSpectator())))) {
                this.setTarget(null);
            }
            CompanionTaczCrawlHelper.maintainCrawlFromSync(this);
        }
        CompanionTaczCrawlHelper.applyCrawlPose(this);
    }

    /**
     * 站/蹲/趴：对齐玩家碰撞降级（站不下→蹲→趴）；死/睡等姿势直通。
     */
    @Override
    public void setPose(Pose pose) {
        super.setPose(this.resolveCollidingPose(pose));
    }

    /** 站/蹲/趴碰撞降级；非移动姿势原样返回。 */
    private Pose resolveCollidingPose(Pose desired) {
        if (desired == null
                || (desired != Pose.STANDING && desired != Pose.CROUCHING && desired != Pose.SWIMMING)) {
            return desired;
        }
        if (this.level() == null || !this.isAddedToWorld() || this.isPassenger()) {
            return desired;
        }
        if (!this.canEnterPose(Pose.SWIMMING)) {
            return this.getPose();
        }
        if (this.canEnterPose(desired)) {
            return desired;
        }
        if (desired == Pose.STANDING) {
            if (this.canEnterPose(Pose.CROUCHING)) {
                return Pose.CROUCHING;
            }
            return Pose.SWIMMING;
        }
        if (desired == Pose.CROUCHING) {
            return Pose.SWIMMING;
        }
        return this.getPose();
    }

    @Override
    public double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (pose == Pose.SLEEPING) {
            return super.getDimensions(pose);
        }
        EntityDimensions likePlayer =
                switch (pose) {
                    case STANDING -> Player.STANDING_DIMENSIONS;
                    case CROUCHING -> EntityDimensions.scalable(0.6F, Player.CROUCH_BB_HEIGHT);
                    case SWIMMING, FALL_FLYING, SPIN_ATTACK ->
                            EntityDimensions.scalable(Player.SWIMMING_BB_WIDTH, Player.SWIMMING_BB_HEIGHT);
                    default -> null;
                };
        if (likePlayer != null) {
            return likePlayer.scale(this.getScale());
        }
        return super.getDimensions(pose);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return switch (pose) {
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> 0.4F;
            case CROUCHING -> 1.27F;
            case STANDING -> 1.62F;
            default -> super.getStandingEyeHeight(pose, dimensions);
        };
    }

    @Override
    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of(Pose.STANDING, Pose.CROUCHING, Pose.SWIMMING);
    }

    /**
     * ??????????????????????     * Friend stub ????false????override ??????true??     */
    public boolean shouldUseOrderedSitVisual() {
        return this.isOrderedToSit() && !this.isTaczCrawling() && !this.isPassenger();
    }

    @Override
    public boolean isTame() {
        return this.entityData.get(DATA_TAME);
    }

    public void setTame(boolean tame) {
        this.entityData.set(DATA_TAME, tame);
        if (!tame) {
            this.setOwnerUUID(null);
        }
        this.reassessWeaponGoal();
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    /** Stub?Friend ??????? {@link AbstractFriendlyCompanionEntity} override? */
    @Override
    public boolean isOrderedToSit() {
        return false;
    }

    /** Stub???????? */
    @Override
    public void setOrderedToSit(boolean orderedToSit) {}

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && amount > 0.0F && this.isOrderedToSit()) {
            this.setOrderedToSit(false);
        }
        return hurt;
    }

    @Override
    public boolean isTameItem(ItemStack stack) {
        return stack.is(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_4.get());
    }

    @Override
    public boolean tryTame(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTameItem(stack)) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (this.random.nextInt(3) != 0) {
            return false;
        }
        this.setTame(true);
        this.setOwnerUUID(player.getUUID());
        this.navigation.stop();
        this.setTarget(null);
        this.setOrderedToSit(true);
        this.level().broadcastEntityEvent(this, (byte) 7);
        this.onTameSuccess(player);
        return true;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            this.spawnTamingParticles(true);
        } else if (id == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    protected void spawnTamingParticles(boolean success) {
        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level()
                    .addParticle(
                            success ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.SMOKE,
                            this.getRandomX(1.0D),
                            this.getRandomY() + 0.5D,
                            this.getRandomZ(1.0D),
                            d0,
                            d1,
                            d2);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || this.isTameItem(stack) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (this.isTame()) {
            return super.mobInteract(player, hand);
        }
        if (this.isTameItem(stack)) {
            if (this.tryTame(player, hand)) {
                return InteractionResult.SUCCESS;
            }
            this.level().broadcastEntityEvent(this, (byte) 6);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target == null || owner == null) {
            return false;
        }

        if (target instanceof AbstractFriendEntity other && other.isTame() && other.isOwnedBy(owner)) {
            return false;
        }

        if (owner instanceof Player ownerPlayer) {
            if (target instanceof Player targetPlayer) {
                return ownerPlayer.canHarmPlayer(targetPlayer) && Config.canFightWithotherguard();
            }
            if (target instanceof AbstractFriendEntity otherFriend && otherFriend.isTame()) {
                LivingEntity otherOwner = otherFriend.getOwner();
                if (otherOwner instanceof Player otherPlayer) {
                    return ownerPlayer.canHarmPlayer(otherPlayer) && Config.canFightWithotherguard();
                }
            }
        }

        return true;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isOwnedBy(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (this.isTame()) {
            LivingEntity owner = this.getOwner();
            if (entity == owner) {
                return true;
            }
            if (owner != null) {
                return owner.isAlliedTo(entity);
            }
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public Team getTeam() {
        if (this.isTame()) {
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return super.getTeam();
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public void playAcquireTargetCombatVoice(
            Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide() || !this.isTame() || this.isOrderedToSit()) {
            return;
        }
        int pick = self.getRandom().nextInt(3);
        SoundEvent sound =
                switch (pick) {
                    case 0 -> ModSounds.SOUND_SOLDIER_FIGHT_2.get();
                    case 1 -> ModSounds.SOUND_SOLDIER_FIGHT1_1.get();
                    default -> ModSounds.SOUND_FIRE.get();
                };
        self.playSound(sound, 1.0F, self.getVoicePitch());
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void equipTaCZGunMainHand(ResourceLocation gunDataId) {
        if (this.level().isClientSide()) {
            return;
        }
        boolean ok;
        if (this instanceof AbstractFriendlyCompanionEntity) {
            ok = CompanionBodyguardAmmoHelper.equipCompanionMainHand(this, gunDataId);
        } else {
            ok = TaczGunMobHelper.equipMainHand(this, gunDataId);
        }
        this.persistedGunDataId = ok ? gunDataId : null;
        this.reassessWeaponGoal();
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.level().isClientSide() || !IGun.mainHandHoldGun(this)) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this);
        this.setSprinting(false);
        ShooterDataHolder dh = op.getDataHolder();
        if (dh != null) {
            dh.sprintTimeS = 0.0F;
        }
        ShootResult result = op.shoot(this::getXRot, this::getYRot);
        if (result == ShootResult.IS_SPRINTING && dh != null) {
            dh.sprintTimeS = 0.0F;
            this.setSprinting(false);
            result = op.shoot(this::getXRot, this::getYRot);
        }
        if (result == ShootResult.NOT_DRAW || result == ShootResult.IS_DRAWING) {
            op.draw(this::getMainHandItem);
        } else if (result == ShootResult.NEED_BOLT) {
            op.bolt();
        } else if (result == ShootResult.NO_AMMO) {
            ItemStack gunStack = this.getMainHandItem();
            if (this instanceof AbstractFriendlyCompanionEntity companion) {
                if (CompanionBodyguardAmmoHelper.hasReloadAmmoSupply(companion, gunStack)) {
                    CompanionBodyguardAmmoHelper.stripDummyAmmoTag(gunStack);
                    op.reload();
                }
            } else {
                op.reload();
            }
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!this.level().isClientSide() && slot == EquipmentSlot.MAINHAND) {
            this.reassessWeaponGoal();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getOwnerUUID() != null) {
            compound.putUUID("Owner", this.getOwnerUUID());
        }
        if (this.persistedGunDataId != null) {
            compound.putString("GunDataId", this.persistedGunDataId.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.setOwnerUUID(compound.getUUID("Owner"));
            this.setTame(true);
        }
        if (compound.contains("GunDataId", 8)) {
            ResourceLocation id = ResourceLocation.tryParse(compound.getString("GunDataId"));
            if (id != null && !this.level().isClientSide()) {
                this.equipTaCZGunMainHand(id);
            }
        }
        this.reassessWeaponGoal();
        this.applyConfiguredLivingAttributes(false);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide() && this.isTame()) {
            LivingEntity owner = this.getOwner();
            if (owner instanceof ServerPlayer player) {
                player.sendSystemMessage(this.getCombatTracker().getDeathMessage());
            }
        }
        super.die(source);
    }
}
