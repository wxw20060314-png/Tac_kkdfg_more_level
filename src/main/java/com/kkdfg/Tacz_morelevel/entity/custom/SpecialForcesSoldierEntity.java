package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.ai.*;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** 特种兵：完美档枪械（{@code *_perfect}）。*/
public final class SpecialForcesSoldierEntity extends AbstractPlayerLikeSoldierEntity {
    private final MobGunSimThreadCoverStrafeMoveGoal<SpecialForcesSoldierEntity> gunGoal =
            new MobGunSimThreadCoverStrafeMoveGoal<>(
                    this,
                    Config.gunGoalSpeedModifierFor(SoldierSpawnArmorProfile.SPECIAL_FORCES),
                    Config.gunAttackIntervalMinFor(SoldierSpawnArmorProfile.SPECIAL_FORCES),
                    Config.gunRadiusFor(SoldierSpawnArmorProfile.SPECIAL_FORCES),
                    Config.gunMeleeFracFor(SoldierSpawnArmorProfile.SPECIAL_FORCES),
                    Config.seeTimeToStopMoveFor(SoldierSpawnArmorProfile.SPECIAL_FORCES),
                    Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.SPECIAL_FORCES),
                    Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.SPECIAL_FORCES),
                    Config.seeTimeToShootFor(SoldierSpawnArmorProfile.SPECIAL_FORCES));
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.meleeSpeedFor(SoldierSpawnArmorProfile.SPECIAL_FORCES), false) {
                @Override
                public void stop() {
                    super.stop();
                    SpecialForcesSoldierEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    SpecialForcesSoldierEntity.this.setAggressive(true);
                }
            };

    public SpecialForcesSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
    }

    @Override
    public int getSimThreadUrgency() {
        return 40;
    }

    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.SPECIAL_FORCES;
    }

    @Override
    public String spawnArmorPoolId() {
        return "special_forces";
    }

    @Override
    protected String skinTextureFolder() {
        return "special_forces";
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
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new SoldierHurtByTargetGoal(this,SpecialForcesSoldierEntity.class).setAlertOthers());
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        Config.NpcAttributeStats coverRange = this.configuredAttributeStats();
        this.goalSelector.addGoal(
                3,
                new MobGunFindCoverRequestGoal<>(
                        this,
                        (float) coverRange.findCoverNearDistance,
                        (float) coverRange.findCoverFarDistance));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.98D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.npcCombatGoalsFor(SoldierSpawnArmorProfile.SPECIAL_FORCES);
        this.goalSelector.addGoal(1, new MobRandomSneakWithTargetInlongDistanceGoal(this,
                g.longSneakTimeDelay, g.longSneakTime, g.longSneakRandomTime, g.longSneakDistance, g.longSneakEnabled));
        this.goalSelector.addGoal(1, new MobRandomSneakWithTargetInShortDistanceGoal(this,
                g.shortSneakTimeDelay, g.shortSneakTime, g.shortSneakRandomTime, g.shortSneakDistance, g.shortSneakEnabled));
        this.goalSelector.addGoal(2, new MobRandomSneakWithNoTargetGoal(this));
        this.goalSelector.addGoal(1, new MobTaczCombatCrawlGoal(this,
                g.crawlRandomDenominator, g.crawlExitRandomDenominator, g.crawlMaxHealthExclusive,
                g.crawlMinPlanarDistance, g.crawlMinHeightToDistanceRatio, g.crawlEnabled));
        this.goalSelector.addGoal(2, new MobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.maxHealthFor(SoldierSpawnArmorProfile.SPECIAL_FORCES))
                .add(Attributes.MOVEMENT_SPEED, Config.movementSpeedFor(SoldierSpawnArmorProfile.SPECIAL_FORCES))
                .add(Attributes.ATTACK_DAMAGE, Config.attackDamageFor(SoldierSpawnArmorProfile.SPECIAL_FORCES))
                .add(Attributes.FOLLOW_RANGE, Config.followRangeFor(SoldierSpawnArmorProfile.SPECIAL_FORCES));
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_AMP2;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 1, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 3, false, false));
        }
    }
    @Override
    public void playAcquireTargetCombatVoice(
        Mob self, LivingEntity newTarget, @Nullable LivingEntity previousTarget) {
        if (self.level().isClientSide()) {
            return;
        }
        self.playSound(ModSounds.SOUND_FIRE_AT_WILL.get(), 1.0F,1);
    }
    @Override
    protected SoundEvent getAmbientSound() {
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (!hasTarget) {
            int WhichChoose = random.nextInt(7);
            return switch (WhichChoose) {
                case 0 -> ModSounds.SOUND_GOOD_WORK_SOLDIER.get();
                case 1 -> ModSounds.SOUND_HOLD_YOUR_POSITION.get();
                case 2 -> ModSounds.SOUND_SECURE_THE_PERIMETER.get();
                case 3 -> ModSounds.SOUND_WATCH_YOUR_AMMO.get();
                case 4 -> ModSounds.SOUND_WATCH_YOUR_SIX.get();
                case 5 -> ModSounds.SOUND_AREA_CLEAR.get();
                case 6 -> ModSounds.SOUND_ENTERING_HOSTILE_TERRITORY.get();
                default -> ModSounds.SOUND_ENTERING_HOSTILE_TERRITORY.get();
            };
        }
        int WhichChoose = random.nextInt(4);
        return switch (WhichChoose) {
            case 0 -> ModSounds.SOUND_HOSTILES_INBOUND.get();
            case 1 -> ModSounds.SOUND_OBJECTIVE_SECURED.get();
            case 2 -> ModSounds.SOUND_WERE_UNDER_ATTACK.get();
            case 3 -> ModSounds.SOUND_ENEMY_ON_THE_MOVE.get();
            default -> ModSounds.SOUND_ENEMY_ON_THE_MOVE.get();
        };
    }
}
