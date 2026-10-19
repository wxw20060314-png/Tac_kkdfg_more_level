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

/** 超级特种兵：纯化档枪械（{@code *_refined}）。*/
public final class SuperSpecialSoldierEntity extends AbstractPlayerLikeSoldierEntity {
    private final MobGunCoverStrafeMoveGoal<SuperSpecialSoldierEntity> gunGoal =
            new MobGunCoverStrafeMoveGoal<>(
                    this,
                    Config.gunGoalSpeedModifierFor(SoldierSpawnArmorProfile.SUPER_SPECIAL),
                    Config.gunAttackIntervalMinFor(SoldierSpawnArmorProfile.SUPER_SPECIAL),
                    Config.gunRadiusFor(SoldierSpawnArmorProfile.SUPER_SPECIAL),
                    Config.gunMeleeFracFor(SoldierSpawnArmorProfile.SUPER_SPECIAL),
                    Config.seeTimeToStopMoveFor(SoldierSpawnArmorProfile.SUPER_SPECIAL),
                    Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.SUPER_SPECIAL),
                    Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.SUPER_SPECIAL),
                    Config.seeTimeToShootFor(SoldierSpawnArmorProfile.SUPER_SPECIAL));
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.meleeSpeedFor(SoldierSpawnArmorProfile.SUPER_SPECIAL), false) {
                @Override
                public void stop() {
                    super.stop();
                    SuperSpecialSoldierEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    SuperSpecialSoldierEntity.this.setAggressive(true);
                }
            };

    public SuperSpecialSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }

    @Override
    protected boolean allowsCrawlPathfinding() {
        return true;
    }

    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.SUPER_SPECIAL;
    }

    @Override
    public String spawnArmorPoolId() {
        return "super_special";
    }

    @Override
    protected String skinTextureFolder() {
        return "super_special_soldier";
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
        this.targetSelector.addGoal(1, new SoldierHurtByTargetGoal(this,SuperSpecialSoldierEntity.class).setAlertOthers());
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.npcCombatGoalsFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
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
                .add(Attributes.MAX_HEALTH, Config.maxHealthFor(SoldierSpawnArmorProfile.SUPER_SPECIAL))
                .add(Attributes.MOVEMENT_SPEED, Config.movementSpeedFor(SoldierSpawnArmorProfile.SUPER_SPECIAL))
                .add(Attributes.ATTACK_DAMAGE, Config.attackDamageFor(SoldierSpawnArmorProfile.SUPER_SPECIAL))
                .add(Attributes.FOLLOW_RANGE, Config.followRangeFor(SoldierSpawnArmorProfile.SUPER_SPECIAL));
    }

    @Override
    public int minJumpTier() {
        return JumpController.TIER_LOW;
    }

    @Override
    public int maxJumpTier() {
        return JumpController.TIER_HIGH;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasEffect(MobEffects.REGENERATION)) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 1, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 4, false, false));
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
