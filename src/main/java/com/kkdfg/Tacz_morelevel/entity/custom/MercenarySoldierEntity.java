package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.ai.*;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
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
/** 雇佣兵：改进档枪械（{@code *_improved}）。*/
public final class MercenarySoldierEntity extends AbstractPlayerLikeSoldierEntity {
    private final MobGunCoverStrafeMoveGoal<MercenarySoldierEntity> gunGoal =
            new MobGunCoverStrafeMoveGoal<>(
                    this,
                    Config.gunGoalSpeedModifierFor(SoldierSpawnArmorProfile.MERCENARY),
                    Config.gunAttackIntervalMinFor(SoldierSpawnArmorProfile.MERCENARY),
                    Config.gunRadiusFor(SoldierSpawnArmorProfile.MERCENARY),
                    Config.gunMeleeFracFor(SoldierSpawnArmorProfile.MERCENARY),
                    Config.seeTimeToStopMoveFor(SoldierSpawnArmorProfile.MERCENARY),
                    Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.MERCENARY),
                    Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.MERCENARY),
                    Config.seeTimeToShootFor(SoldierSpawnArmorProfile.MERCENARY));
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.meleeSpeedFor(SoldierSpawnArmorProfile.MERCENARY), false) {
                @Override
                public void stop() {
                    super.stop();
                    MercenarySoldierEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    MercenarySoldierEntity.this.setAggressive(true);
                }
            };

    public MercenarySoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }
    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.MERCENARY;
    }

    @Override
    public String spawnArmorPoolId() {
        return "mercenary";
    }

    @Override
    protected String skinTextureFolder() {
        return "mercenary";
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
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.92D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 9.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.npcCombatGoalsFor(SoldierSpawnArmorProfile.MERCENARY);
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
                .add(Attributes.MAX_HEALTH, Config.maxHealthFor(SoldierSpawnArmorProfile.MERCENARY))
                .add(Attributes.MOVEMENT_SPEED, Config.movementSpeedFor(SoldierSpawnArmorProfile.MERCENARY))
                .add(Attributes.ATTACK_DAMAGE, Config.attackDamageFor(SoldierSpawnArmorProfile.MERCENARY))
                .add(Attributes.FOLLOW_RANGE, Config.followRangeFor(SoldierSpawnArmorProfile.MERCENARY));
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
}
