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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 民兵：基础档枪械。*/
public final class MilitiaEntity extends AbstractPlayerLikeSoldierEntity {
    private final MobGunRangedPassthroughAttackGoal<MilitiaEntity> gunGoal =
            new MobGunRangedPassthroughAttackGoal<>(
                    this,
                    Config.gunGoalSpeedModifierFor(SoldierSpawnArmorProfile.MILITIA),
                    Config.gunAttackIntervalMinFor(SoldierSpawnArmorProfile.MILITIA),
                    Config.gunRadiusFor(SoldierSpawnArmorProfile.MILITIA),
                    Config.gunMeleeFracFor(SoldierSpawnArmorProfile.MILITIA),
                    Config.seeTimeToStopMoveFor(SoldierSpawnArmorProfile.MILITIA),
                    Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.MILITIA),
                    Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.MILITIA),
                    Config.seeTimeToShootFor(SoldierSpawnArmorProfile.MILITIA));
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.meleeSpeedFor(SoldierSpawnArmorProfile.MILITIA), false) {
                @Override
                public void stop() {
                    super.stop();
                    MilitiaEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    MilitiaEntity.this.setAggressive(true);
                }
            };

    public MilitiaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }
    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.MILITIA;
    }

    @Override
    public String spawnArmorPoolId() {
        return "militia";
    }

    @Override
    protected String skinTextureFolder() {
        return "militia";
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
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.npcCombatGoalsFor(SoldierSpawnArmorProfile.MILITIA);
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
        return JumpController.TIER_MID;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 999999, 0, false, false));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.maxHealthFor(SoldierSpawnArmorProfile.MILITIA))
                .add(Attributes.MOVEMENT_SPEED, Config.movementSpeedFor(SoldierSpawnArmorProfile.MILITIA))
                .add(Attributes.ATTACK_DAMAGE, Config.attackDamageFor(SoldierSpawnArmorProfile.MILITIA))
                .add(Attributes.FOLLOW_RANGE, Config.followRangeFor(SoldierSpawnArmorProfile.MILITIA));
    }
    @Override
    protected SoundEvent getAmbientSound() {
        int WhichChoose = random.nextInt(2);
        return switch (WhichChoose) {
            case 0 -> ModSounds.SOUND_XIANGSHOU.get();
            case 1 -> ModSounds.SOUND_HENHEN.get();
            default -> ModSounds.SOUND_HENHEN.get();
        };
    }
}
