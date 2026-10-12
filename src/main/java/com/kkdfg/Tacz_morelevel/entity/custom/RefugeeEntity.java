package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.ai.*;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
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

/**
 * 难民：普通档枪械，整体弱于民兵；生成盔甲见 {@code data/tacz_morelevel/troop_armor/refugee.json}。
 */
public final class RefugeeEntity extends AbstractPlayerLikeSoldierEntity {

    private final MobGunRangedAttackByRefugeePassthroughGoal<RefugeeEntity> gunGoal =
            new MobGunRangedAttackByRefugeePassthroughGoal<>(
                    this,
                    Config.gunGoalSpeedModifierFor(SoldierSpawnArmorProfile.REFUGEE),
                    Config.gunRadiusFor(SoldierSpawnArmorProfile.REFUGEE),
                    Config.gunMeleeFracFor(SoldierSpawnArmorProfile.REFUGEE),
                    Config.seeTimeToStopMoveFor(SoldierSpawnArmorProfile.REFUGEE),
                    Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.REFUGEE),
                    Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.REFUGEE),
                    Config.gunAttackIntervalMinFor(SoldierSpawnArmorProfile.REFUGEE),
                    Config.seeTimeToShootFor(SoldierSpawnArmorProfile.REFUGEE));
    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.meleeSpeedFor(SoldierSpawnArmorProfile.REFUGEE), false) {
                @Override
                public void stop() {
                    super.stop();
                    RefugeeEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    RefugeeEntity.this.setAggressive(true);
                }
            };

    public RefugeeEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }
    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.REFUGEE;
    }

    @Override
    public String spawnArmorPoolId() {
        return "refugee";
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

    /** 难民点射节奏：近战也走间隔。 */
    @Override
    protected Goal createGunFireGoal() {
        Config.NpcAttributeStats attrs = this.configuredAttributeStats();
        return new MobGunRangedFireGoal<>(
                this,
                (float) attrs.gunRadius,
                (float) attrs.gunMeleeFrac,
                attrs.gunAttackIntervalMin,
                attrs.seeTimeToShoot,
                GunCombatSight.COMBAT,
                true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        Config.CombatGoalStats g = Config.npcCombatGoalsFor(SoldierSpawnArmorProfile.REFUGEE);
        this.goalSelector.addGoal(1, new MobRandomSneakWithTargetInlongDistanceGoal(this,
                g.longSneakTimeDelay, g.longSneakTime, g.longSneakRandomTime, g.longSneakDistance, g.longSneakEnabled));
        this.goalSelector.addGoal(1, new MobRandomSneakWithTargetInShortDistanceGoal(this,
                g.shortSneakTimeDelay, g.shortSneakTime, g.shortSneakRandomTime, g.shortSneakDistance, g.shortSneakEnabled));
        this.goalSelector.addGoal(2, new MobRandomSneakWithNoTargetGoal(this));
        this.goalSelector.addGoal(1, new MobTaczCombatCrawlGoal(this,
                g.crawlRandomDenominator, g.crawlExitRandomDenominator, g.crawlMaxHealthExclusive,
                g.crawlMinPlanarDistance, g.crawlMinHeightToDistanceRatio, g.crawlEnabled));
        this.goalSelector.addGoal(2, new MobRandomJumpGoal(this, g.jumpChance, g.jumpEnabled, g.jumpMaxDistance));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.maxHealthFor(SoldierSpawnArmorProfile.REFUGEE))
                .add(Attributes.MOVEMENT_SPEED, Config.movementSpeedFor(SoldierSpawnArmorProfile.REFUGEE))
                .add(Attributes.ATTACK_DAMAGE, Config.attackDamageFor(SoldierSpawnArmorProfile.REFUGEE))
                .add(Attributes.FOLLOW_RANGE, Config.followRangeFor(SoldierSpawnArmorProfile.REFUGEE));
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
