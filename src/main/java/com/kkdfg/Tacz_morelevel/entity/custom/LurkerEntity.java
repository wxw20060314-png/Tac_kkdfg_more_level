package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthHandler;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthCloakSkill;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunCoverStrafeMoveGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.FlashedStunGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunReloadGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobInvestigateGunSoundGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 潜伏者：独立 {@code [lurker]} 配置（默认同突击兵）；固定隐身斗篷套；有目标且冷却好则开隐身；
 * 默认不远距刷没；枪池 {@code troop_guns/lurker}。
 */
public final class LurkerEntity extends AbstractPlayerLikeSoldierEntity {

    private final MobGunCoverStrafeMoveGoal<LurkerEntity> gunGoal =
            new MobGunCoverStrafeMoveGoal<>(
                    this,
                    Config.lurkerGunGoalSpeedModifier,
                    Config.lurkerAttributes.gunAttackIntervalMin,
                    (float) Config.lurkerAttributes.gunRadius,
                    (float) Config.lurkerAttributes.gunMeleeFrac,
                    Config.lurkerAttributes.seeTimeToStopMove,
                    (float) Config.lurkerAttributes.gunStrafeOuterRingFrac,
                    (float) Config.lurkerAttributes.gunStrafeInnerRingFrac,
                    Config.lurkerAttributes.seeTimeToShoot);

    private final MeleeAttackGoal meleeGoal =
            new MeleeAttackGoal(this, Config.lurkerAttributes.meleeSpeed, false) {
                @Override
                public void stop() {
                    super.stop();
                    LurkerEntity.this.setAggressive(false);
                }

                @Override
                public void start() {
                    super.start();
                    LurkerEntity.this.setAggressive(true);
                }
            };

    public LurkerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.reassessWeaponGoal();
    }

    @Override
    protected SoldierSpawnArmorProfile spawnArmorWeightProfile() {
        return SoldierSpawnArmorProfile.ASSAULT;
    }

    @Override
    protected Config.NpcAttributeStats configuredAttributeStats() {
        return Config.lurkerAttributes;
    }

    @Override
    public String spawnArmorPoolId() {
        return "lurker";
    }

    @Override
    protected String troopGunPoolId() {
        return "lurker";
    }

    @Override
    protected String skinTextureFolder() {
        return "assault_soldier";
    }

    @Override
    protected void applyMilitiaSpawnArmor(RandomSource random) {
        equipStealthCloakSet();
    }

    private void equipStealthCloakSet() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.STEALTH_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.STEALTH_CLOAK.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.STEALTH_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.STEALTH_BOOTS.get()));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayerSq) {
        return Config.lurkerRemoveWhenFarAway;
    }

    @Override
    protected Goal gunGoal() {
        return this.gunGoal;
    }

    @Override
    protected int mobGunBurstIntervalTicks() {
        return Config.mapNpcGunBurstInterval(Config.lurkerAttributes.gunAttackIntervalMin);
    }

    @Override
    protected MeleeAttackGoal meleeGoal() {
        return this.meleeGoal;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(
                1, new SoldierHurtByTargetGoal(this, LurkerEntity.class).setAlertOthers());
        this.goalSelector.addGoal(0, new FlashedStunGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MobGunReloadGoal(this));
        this.goalSelector.addGoal(4, new MobInvestigateGunSoundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.95D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 9.5F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        Config.CombatGoalStats g = Config.lurkerCombatGoals;
        this.goalSelector.addGoal(
                1,
                new MobRandomSneakWithTargetInlongDistanceGoal(
                        this,
                        g.longSneakTimeDelay,
                        g.longSneakTime,
                        g.longSneakRandomTime,
                        g.longSneakDistance,
                        g.longSneakEnabled));
        this.goalSelector.addGoal(
                1,
                new MobRandomSneakWithTargetInShortDistanceGoal(
                        this,
                        g.shortSneakTimeDelay,
                        g.shortSneakTime,
                        g.shortSneakRandomTime,
                        g.shortSneakDistance,
                        g.shortSneakEnabled));
        this.goalSelector.addGoal(2, new MobRandomSneakWithNoTargetGoal(this));
        this.goalSelector.addGoal(
                1,
                new MobTaczCombatCrawlGoal(
                        this,
                        g.crawlRandomDenominator,
                        g.crawlExitRandomDenominator,
                        g.crawlMaxHealthExclusive,
                        g.crawlMinPlanarDistance,
                        g.crawlMinHeightToDistanceRatio,
                        g.crawlEnabled));
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
        if (!this.level().isClientSide()) {
            tickCombatStealth();
        }
    }

    /** 有存活目标且冷却好 → 开隐身；无目标 → 关开关（效果边沿自清）。 */
    private void tickCombatStealth() {
        LivingEntity target = this.getTarget();
        boolean hasTarget = target != null && target.isAlive();
        if (hasTarget) {
            if (!ArmorStealthHandler.isToggled(this)
                    && ArmorLegendarySkillRegistry.canActivate(this, StealthCloakSkill.ID)) {
                ArmorLegendarySkillRegistry.tryActivate(this, StealthCloakSkill.ID);
            }
            return;
        }
        if (ArmorStealthHandler.isToggled(this)) {
            ArmorStealthHandler.setToggled(this, false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.lurkerMaxHealth)
                .add(Attributes.MOVEMENT_SPEED, Config.lurkerAttributes.movementSpeed)
                .add(Attributes.ATTACK_DAMAGE, Config.lurkerAttackDamage)
                .add(Attributes.FOLLOW_RANGE, Config.lurkerAttributes.followRange);
    }
}
