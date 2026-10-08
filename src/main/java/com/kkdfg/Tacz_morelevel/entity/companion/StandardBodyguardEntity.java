package com.kkdfg.Tacz_morelevel.entity.companion;

import javax.annotation.Nullable;

import com.kkdfg.Tacz_morelevel.BodyguardProfile;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionAttackMonsterGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionAvoidGrenadeAndFireGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionFollowOwnerGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithTargetInLongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionMobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionOwnerHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionOwnerHurtTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionSitWhenOrderedGoal;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;


public final class StandardBodyguardEntity extends AbstractFriendlyCompanionEntity {

    private static final String TAG_SKIN_INDEX = "TaczMoreLevelStandardSkinIdx";

    
    public static final int STANDARD_SKIN_COUNT = 15;

    private static final EntityDataAccessor<Integer> DATA_SKIN_INDEX =
            SynchedEntityData.defineId(StandardBodyguardEntity.class, EntityDataSerializers.INT);

    private static final BodyguardProfile PROFILE = BodyguardProfile.STANDARD;

    @Override
    protected BodyguardProfile bodyguardProfile() {
        return PROFILE;
    }

    private final MobGunRangedPassthroughAttackGoal<StandardBodyguardEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.bodyguardGunGoalSpeedModifierFor(PROFILE),
                    Config.bodyguardGunAttackIntervalMinFor(PROFILE),
                    Config.bodyguardGunRadiusFor(PROFILE),
                    Config.bodyguardGunMeleeFracFor(PROFILE),
                    Config.bodyguardSeeTimeToStopMoveFor(PROFILE),
                    Config.bodyguardGunStrafeOuterRingFracFor(PROFILE),
                    Config.bodyguardGunStrafeInnerRingFracFor(PROFILE),
                    Config.bodyguardSeeTimeToShootFor(PROFILE));

    private final MeleeAttackGoal meleeGoal = newTameMeleeGoal(Config.bodyguardMeleeSpeedFor(PROFILE));

    public StandardBodyguardEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_INDEX, 0);
    }

    public int getStandardSkinIndex() {
        return Mth.clamp(this.entityData.get(DATA_SKIN_INDEX), 0, STANDARD_SKIN_COUNT - 1);
    }

    public ResourceLocation getStandardSkinTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID,
                "textures/entity/standard_bodyguard/skin_" + getStandardSkinIndex() + ".png");
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
        if (!level.isClientSide()) {
            this.entityData.set(DATA_SKIN_INDEX, level.getRandom().nextInt(STANDARD_SKIN_COUNT));
        }
        return spawnData;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_SKIN_INDEX, getStandardSkinIndex());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_SKIN_INDEX, Tag.TAG_INT)) {
            int idx = Mth.clamp(compound.getInt(TAG_SKIN_INDEX), 0, STANDARD_SKIN_COUNT - 1);
            this.entityData.set(DATA_SKIN_INDEX, idx);
        }
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected MeleeAttackGoal meleeGoal() {
        return this.meleeGoal;
    }

    @Override
    protected int bodyguardGunBurstIntervalTicks() {
        return Config.mapNpcGunBurstInterval(Config.bodyguardGunAttackIntervalMinFor(PROFILE));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, Config.bodyguardMovementSpeedFor(PROFILE))
                .add(Attributes.MAX_HEALTH, Config.bodyguardMaxHealthFor(PROFILE))
                .add(Attributes.FOLLOW_RANGE, Config.bodyguardFollowRangeFor(PROFILE))
                .add(Attributes.ATTACK_DAMAGE, Config.bodyguardAttackDamageFor(PROFILE));
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CompanionAvoidGrenadeAndFireGoal(this));
        this.goalSelector.addGoal(1, new CompanionSitWhenOrderedGoal(this));
        this.goalSelector.addGoal(4, new CompanionFollowOwnerGoal(this, 1.0D, 25.0F, 10.0F, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new CompanionOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CompanionHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new CompanionOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new CompanionAttackMonsterGoal(this));
        //__________________________________________________________________________________
        Config.CombatGoalStats g = Config.bodyguardCombatGoalsFor(PROFILE);
        this.goalSelector.addGoal(1, new CompanionMobTaczCombatCrawlGoal(this,
                g.crawlRandomDenominator, g.crawlExitRandomDenominator, g.crawlMaxHealthExclusive,
                g.crawlMinPlanarDistance, g.crawlMinHeightToDistanceRatio, g.crawlEnabled));
        this.goalSelector.addGoal(3, new CompanionMobRandomSneakWithNoTargetGoal(this));
        this.goalSelector.addGoal(1, new CompanionMobRandomSneakWithTargetInLongDistanceGoal(this,
                g.longSneakTimeDelay, g.longSneakTime, g.longSneakRandomTime, g.longSneakDistance, g.longSneakEnabled));
        this.goalSelector.addGoal(1, new CompanionMobRandomSneakWithTargetInShortDistanceGoal(this,
                g.shortSneakTimeDelay, g.shortSneakTime, g.shortSneakRandomTime, g.shortSneakDistance, g.shortSneakEnabled));
        this.goalSelector.addGoal(1, new CompanionMobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
        this.registerCompanionGunReloadGoal();
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_HIGH_MID;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 0, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 1, false, false));
        }
    }
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        int WhichToChoice=random.nextInt(10);
        return switch (WhichToChoice) {
            case 0 -> ModSounds.SOUND_MALE_BODYGUARD_I_STAND_READY.get();
            case 1 -> ModSounds.SOUND_MALE_BODYGUARD_YOUR_ORDERS.get();
            case 2 -> ModSounds.SOUND_MALE_BODYGUARD_AT_YOUR_COMMAND.get();
            case 3 -> ModSounds.SOUND_MALE_BODYGUARD_READY_FOR_THE_THINGS.get();
            case 4 -> ModSounds.SOUND_MALE_BODYGUARD_READY_TO_SIR.get();
            case 5 -> ModSounds.SOUND_MALE_BODYGUARD_WHAT_WOULD_MEDO.get();
            case 6 -> ModSounds.SOUND_MALE_BODYGUARD_YES_SIR .get();
            case 7 -> ModSounds.SOUND_MALE_BODYGUARD_YOUR_COMMANDSIR.get();
            case 8 -> ModSounds.SOUND_MALE_BODYGUARD_YOUR_WISHSIR.get();
            case 9 -> ModSounds.SOUND_MALE_BODYGUARD_HERE_I_STAND.get();
            default ->  ModSounds.SOUND_MALE_BODYGUARD_YOUR_WISHSIR.get();
        };
    }
    @Override
    public void playAcquireTargetCombatVoice(
        Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide() || !this.isTame() || this.isOrderedToSit()) {
            return;
        }
        int pick = self.getRandom().nextInt(4);
        SoundEvent sound =
            switch (pick) {
                case 0 -> ModSounds.SOUND_MALE_BODYGUARD_FORWARD_WE_GO.get();
                case 1 -> ModSounds.SOUND_MALE_BODYGUARD_NO_ENEMY_SHALL_STAND_BEFORE_US.get();
                case 2 -> ModSounds.SOUND_MALE_BODYGUARD_DEFEND_YOURSELF.get();
                case 3 -> ModSounds.SOUND_MALE_BODYGUARD_FOR_HONOR_AND_GLORY.get();
                default -> ModSounds.SOUND_MALE_BODYGUARD_FOR_HONOR_AND_GLORY.get();
            };
        self.playSound(sound, 1.0F, self.getVoicePitch());
    }
    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        int WhichChoose = random.nextInt(2);
        return switch (WhichChoose) {
            case 0 -> ModSounds.SOUND_SOLDIER_HURT_2.get();
            case 1 -> ModSounds.SOUND_SOLDIER_HURT_1.get();
            default -> ModSounds.SOUND_SOLDIER_HURT_2.get();
        };
    }

    
    @Override
    protected SoundEvent getDeathSound() {
        int WhichChoose = random.nextInt(4);
        return switch (WhichChoose) {
            case 0 -> ModSounds.SOUND_SOLDIER_DIED_1.get();
            case 1 -> ModSounds.SOUND_SOLDIER_DIED_2.get();
            case 2 -> ModSounds.SOUND_SOLDIER_DIED_5.get();
            case 3 -> ModSounds.SOUND_SOLDIER_DIED_4.get();
            default -> ModSounds.SOUND_SOLDIER_DIED_1.get();
        };
    }
}
