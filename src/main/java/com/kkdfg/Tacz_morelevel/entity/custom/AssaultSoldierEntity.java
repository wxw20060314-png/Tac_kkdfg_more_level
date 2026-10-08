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

/** 突击兵：卓越档枪械（{@code *_excellent}）。*/
public final class AssaultSoldierEntity extends AbstractPlayerLikeSoldierEntity {
    private final MobGunRangedPassthroughAttackGoal<AssaultSoldierEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.gunGoalSpeedModifierFor(SoldierSpawnArmorProfile.ASSAULT),
                    Config.gunAttackIntervalMinFor(SoldierSpawnArmorProfile.ASSAULT),
                    Config.gunRadiusFor(SoldierSpawnArmorProfile.ASSAULT),
                    Config.gunMeleeFracFor(SoldierSpawnArmorProfile.ASSAULT),
                    Config.seeTimeToStopMoveFor(SoldierSpawnArmorProfile.ASSAULT),
                    Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.ASSAULT),
                    Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.ASSAULT),
                    Config.seeTimeToShootFor(SoldierSpawnArmorProfile.ASSAULT));
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.meleeSpeedFor(SoldierSpawnArmorProfile.ASSAULT), false) {
                @Override
                public void stop() {
                    super.stop();
                    AssaultSoldierEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    AssaultSoldierEntity.this.setAggressive(true);
                }
            };

    public AssaultSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }
    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.ASSAULT;
    }

    @Override
    public String spawnArmorPoolId() {
        return "assault";
    }

    @Override
    protected String skinTextureFolder() {
        return "assault_soldier";
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
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.95D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 9.5F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.npcCombatGoalsFor(SoldierSpawnArmorProfile.ASSAULT);
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
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 999999, 0, false, false));
        }
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 2, false, false));
        }
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.maxHealthFor(SoldierSpawnArmorProfile.ASSAULT))
                .add(Attributes.MOVEMENT_SPEED, Config.movementSpeedFor(SoldierSpawnArmorProfile.ASSAULT))
                .add(Attributes.ATTACK_DAMAGE, Config.attackDamageFor(SoldierSpawnArmorProfile.ASSAULT))
                .add(Attributes.FOLLOW_RANGE, Config.followRangeFor(SoldierSpawnArmorProfile.ASSAULT));
    }
}
