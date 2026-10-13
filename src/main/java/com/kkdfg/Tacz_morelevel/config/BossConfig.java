package com.kkdfg.Tacz_morelevel.config;

import com.kkdfg.Tacz_morelevel.Config;
import net.minecraftforge.common.ForgeConfigSpec;

/** Boss 配置（生成 {@code config/tacz_morelevel/bosses.toml}）。 */
public final class BossConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue GUN_BURST_INTERVAL_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_BURST_INTERVAL_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue GUN_MELEE_FRAC_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_MELEE_FRAC_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue FOLLOW_RANGE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue FOLLOW_RANGE_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue MELEE_SPEED_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue MELEE_SPEED_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue GUN_RADIUS_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_RADIUS_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue SEE_TIME_TO_STOP_MOVE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue SEE_TIME_TO_STOP_MOVE_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue GUN_STRAFE_OUTER_RING_FRAC_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_STRAFE_OUTER_RING_FRAC_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue GUN_STRAFE_INNER_RING_FRAC_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_STRAFE_INNER_RING_FRAC_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue SEE_TIME_TO_SHOOT_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue SEE_TIME_TO_SHOOT_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue GUN_GOAL_SPEED_MODIFIER_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_GOAL_SPEED_MODIFIER_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_TIME_DELAY_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_TIME_DELAY_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_TIME_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_TIME_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_RANDOM_TIME_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_RANDOM_TIME_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_DISTANCE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue LONG_SNEAK_DISTANCE_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_TIME_DELAY_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_TIME_DELAY_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_TIME_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_TIME_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_RANDOM_TIME_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_RANDOM_TIME_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_DISTANCE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue SHORT_SNEAK_DISTANCE_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue CRAWL_RANDOM_DENOMINATOR_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue CRAWL_RANDOM_DENOMINATOR_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue CRAWL_EXIT_RANDOM_DENOMINATOR_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue CRAWL_EXIT_RANDOM_DENOMINATOR_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue CRAWL_MAX_HEALTH_EXCLUSIVE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue CRAWL_MAX_HEALTH_EXCLUSIVE_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue CRAWL_MIN_PLANAR_DISTANCE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue CRAWL_MIN_PLANAR_DISTANCE_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue JUMP_CHANCE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue JUMP_CHANCE_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue JUMP_MAX_DISTANCE_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue JUMP_MAX_DISTANCE_SCALE_B;

    static {
        BUILDER.push("gunBurstIntervalScale");
        BUILDER.comment(
                "Maps each BossConfig unit's base gun burst interval x as floor((x + offsetA) * scaleB). Defaults leave intervals unchanged.");
        GUN_BURST_INTERVAL_OFFSET_A =
                BUILDER.comment("Additive offset a in floor((x + a) * b).")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_BURST_INTERVAL_SCALE_B =
                BUILDER.comment("Scale factor b in floor((x + a) * b).")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunMeleeFracScale");
        BUILDER.comment(
                "Maps each BossConfig unit's base gunMeleeFrac x as clamp((x + offsetA) * scaleB, 0.05, 1). Defaults leave frac unchanged.");
        GUN_MELEE_FRAC_OFFSET_A =
                BUILDER.comment("Additive offset a in clamp((x + a) * b, 0.05, 1).")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_MELEE_FRAC_SCALE_B =
                BUILDER.comment("Scale factor b in clamp((x + a) * b, 0.05, 1).")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("movementSpeedScale");
        BUILDER.comment(
                "Maps each BossConfig unit movementSpeed x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        MOVEMENT_SPEED_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        MOVEMENT_SPEED_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("followRangeScale");
        BUILDER.comment(
                "Maps each BossConfig unit followRange x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        FOLLOW_RANGE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        FOLLOW_RANGE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("meleeSpeedScale");
        BUILDER.comment(
                "Maps each BossConfig unit meleeSpeed x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        MELEE_SPEED_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        MELEE_SPEED_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunRadiusScale");
        BUILDER.comment(
                "Maps each BossConfig unit gunRadius x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_RADIUS_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_RADIUS_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("seeTimeToStopMoveScale");
        BUILDER.comment(
                "Maps each BossConfig unit seeTimeToStopMove x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SEE_TIME_TO_STOP_MOVE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SEE_TIME_TO_STOP_MOVE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunStrafeOuterRingFracScale");
        BUILDER.comment(
                "Maps each BossConfig unit gunStrafeOuterRingFrac x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_STRAFE_OUTER_RING_FRAC_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_STRAFE_OUTER_RING_FRAC_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunStrafeInnerRingFracScale");
        BUILDER.comment(
                "Maps each BossConfig unit gunStrafeInnerRingFrac x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_STRAFE_INNER_RING_FRAC_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_STRAFE_INNER_RING_FRAC_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("seeTimeToShootScale");
        BUILDER.comment(
                "Maps each BossConfig unit seeTimeToShoot x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SEE_TIME_TO_SHOOT_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SEE_TIME_TO_SHOOT_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunGoalSpeedModifierScale");
        BUILDER.comment(
                "Maps each BossConfig unit gunGoalSpeedModifier x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_GOAL_SPEED_MODIFIER_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_GOAL_SPEED_MODIFIER_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakTimeDelayScale");
        BUILDER.comment(
                "Maps each BossConfig unit longSneakTimeDelay x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        LONG_SNEAK_TIME_DELAY_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_TIME_DELAY_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakTimeScale");
        BUILDER.comment(
                "Maps each BossConfig unit longSneakTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        LONG_SNEAK_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakRandomTimeScale");
        BUILDER.comment(
                "Maps each BossConfig unit longSneakRandomTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        LONG_SNEAK_RANDOM_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_RANDOM_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakDistanceScale");
        BUILDER.comment(
                "Maps each BossConfig unit longSneakDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        LONG_SNEAK_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakTimeDelayScale");
        BUILDER.comment(
                "Maps each BossConfig unit shortSneakTimeDelay x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SHORT_SNEAK_TIME_DELAY_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_TIME_DELAY_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakTimeScale");
        BUILDER.comment(
                "Maps each BossConfig unit shortSneakTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SHORT_SNEAK_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakRandomTimeScale");
        BUILDER.comment(
                "Maps each BossConfig unit shortSneakRandomTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SHORT_SNEAK_RANDOM_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_RANDOM_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakDistanceScale");
        BUILDER.comment(
                "Maps each BossConfig unit shortSneakDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        SHORT_SNEAK_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlRandomDenominatorScale");
        BUILDER.comment(
                "Maps each BossConfig unit crawlRandomDenominator x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        CRAWL_RANDOM_DENOMINATOR_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_RANDOM_DENOMINATOR_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlExitRandomDenominatorScale");
        BUILDER.comment(
                "Maps each BossConfig unit crawlExitRandomDenominator x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        CRAWL_EXIT_RANDOM_DENOMINATOR_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_EXIT_RANDOM_DENOMINATOR_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlMaxHealthExclusiveScale");
        BUILDER.comment(
                "Maps each BossConfig unit crawlMaxHealthExclusive x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        CRAWL_MAX_HEALTH_EXCLUSIVE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_MAX_HEALTH_EXCLUSIVE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlMinPlanarDistanceScale");
        BUILDER.comment(
                "Maps each BossConfig unit crawlMinPlanarDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        CRAWL_MIN_PLANAR_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_MIN_PLANAR_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlMinHeightToDistanceRatioScale");
        BUILDER.comment(
                "Maps each BossConfig unit crawlMinHeightToDistanceRatio x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("jumpChanceScale");
        BUILDER.comment(
                "Maps each BossConfig unit jumpChance x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        JUMP_CHANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        JUMP_CHANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("jumpMaxDistanceScale");
        BUILDER.comment(
                "Maps each BossConfig unit jumpMaxDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        JUMP_MAX_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        JUMP_MAX_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();
    }

    /** 双手 Boss 普通开火（非偏好主/副）；默认超级特种兵射击数值。 */
    private static final NormalShootConfigEntry NORMAL_SHOOT = defineNormalShoot();

    private record NormalShootConfigEntry(
            ForgeConfigSpec.DoubleValue gunRadius,
            ForgeConfigSpec.DoubleValue gunMeleeFrac,
            ForgeConfigSpec.IntValue gunAttackIntervalMin,
            ForgeConfigSpec.IntValue seeTimeToShoot) {}

    private static NormalShootConfigEntry defineNormalShoot() {
        int i = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        double[] d = CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[i];
        BUILDER.push("normalShoot");
        BUILDER.comment(
                "Shared normal-fire tuning for dual-gun bosses (War Machine / Bomb Baby) when holding neither preferred gun. Defaults match superSpecial.");
        ForgeConfigSpec.DoubleValue gunRadius =
                BUILDER.defineInRange("gunRadius", d[3], 4.0D, 128.0D);
        ForgeConfigSpec.DoubleValue gunMeleeFrac =
                BUILDER.defineInRange("gunMeleeFrac", d[4], 0.05D, 1.0D);
        ForgeConfigSpec.IntValue gunAttackIntervalMin =
                BUILDER.defineInRange("gunAttackIntervalMin", (int) d[8], 0, 200);
        ForgeConfigSpec.IntValue seeTimeToShoot =
                BUILDER.defineInRange("seeTimeToShoot", (int) d[9], 0, 100);
        BUILDER.pop();
        return new NormalShootConfigEntry(gunRadius, gunMeleeFrac, gunAttackIntervalMin, seeTimeToShoot);
    }

    static final CombatUnitConfig.SpecEntry WAR_MACHINE = defineWarMachine();
    static final CombatUnitConfig.SpecEntry BOMB_BABY = defineBombBaby();
    static final CeilingDuelConfigEntry CEILING_DUEL = defineCeilingDuel();
    static final CombatUnitConfig.SpecEntry ANNIHILATOR = defineAnnihilator();
    static final CombatUnitConfig.SpecEntry PUNISHER = definePunisher();
    static final CombatUnitConfig.SpecEntry SCORPION = defineScorpion();
    static final CombatUnitConfig.SpecEntry SLIME_COMMANDER = defineSlimeCommander();
    static final CombatUnitConfig.SpecEntry SLIME_WARRIOR = defineSlimeWarrior();
    static final CombatUnitConfig.SpecEntry LEV_MARSHAL = defineLevMarshal();
    static final BombSightConfigEntry BOMB_SIGHT = defineBombSight();

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private BossConfig() {}

    private static CombatUnitConfig.SpecEntry defineWarMachine() {
        double[] attrs = {0.8D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.3D, 0.1D, 0.0D, 2.0D};
        // 无潜行/趴/跳 Goal → 不写 combatGoals 段
        return CombatUnitConfig.define(
                BUILDER,
                "warMachine",
                "War Machine boss combat tuning.",
                30.0D,
                2.0D,
                1.0D,
                attrs,
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX],
                null,
                0.2D,
                CombatGoalSection.none());
    }

    private static CombatUnitConfig.SpecEntry defineBombBaby() {
        int superSpecialIndex = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        return CombatUnitConfig.define(
                BUILDER,
                "bombBaby",
                "Bomb Baby boss; defaults match superSpecial soldier gun tuning.",
                20.0D,
                2.0D,
                1.2D,
                CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex],
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex],
                null,
                0.2D);
    }

    record CeilingDuelConfigEntry(
            CombatUnitConfig.SpecEntry unit, ForgeConfigSpec.DoubleValue helmetDropChance) {}

    private static CeilingDuelConfigEntry defineCeilingDuel() {
        double[] attrs = {0.64D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.3D, 0.1D, 0.0D, 2.0D};
        ForgeConfigSpec.DoubleValue[] helmetDropChance = new ForgeConfigSpec.DoubleValue[1];
        CombatUnitConfig.SpecEntry unit =
                CombatUnitConfig.define(
                        BUILDER,
                        "ceilingDuel",
                        "Ceiling Duel boss combat tuning.",
                        20.0D,
                        2.0D,
                        1.2D,
                        attrs,
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[
                                CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX],
                        null,
                        0.2D,
                        null,
                        () ->
                                helmetDropChance[0] =
                                        BUILDER.comment(
                                                        "Head slot only (Joker Type VII helmet); default 1.0 = always drop.")
                                                .defineInRange("helmetDropChance", 1.0, 0.0, 1.0));
        return new CeilingDuelConfigEntry(unit, helmetDropChance[0]);
    }

    private static CombatUnitConfig.SpecEntry defineAnnihilator() {
        int superSpecialIndex = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        return CombatUnitConfig.define(
                BUILDER,
                "annihilator",
                "Annihilator boss; attribute defaults match superSpecial soldier.",
                20.0D,
                2.0D,
                1.2D,
                CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex],
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex],
                null,
                0.2D);
    }

    private static CombatUnitConfig.SpecEntry definePunisher() {
        int superSpecialIndex = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        return CombatUnitConfig.define(
                BUILDER,
                "punisher",
                "Punisher boss; attribute defaults match superSpecial soldier (was hardcoded).",
                20.0D,
                2.0D,
                1.2D,
                CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex],
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex],
                null,
                0.2D);
    }

    private static CombatUnitConfig.SpecEntry defineScorpion() {
        int superSpecialIndex = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        // Melee/no-aim ≤20; engage 100; strafe outer≈80 / inner≈40 → (80/100)²=0.64, (40/100)²=0.16.
        double[] attrs = {0.64D, 108.0D, 1.25D, 100.0D, 0.2D, 3.0D, 0.64D, 0.16D, 0.0D, 2.0D};
        return CombatUnitConfig.define(
                BUILDER,
                "scorpion",
                "Scorpion boss; holster M107 excellent (draw on target); stealth seventh set; gunRadius 100, melee 20, strafe 80/40.",
                20.0D,
                2.0D,
                1.2D,
                attrs,
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex],
                null,
                0.2D);
    }

    private static CombatUnitConfig.SpecEntry defineSlimeCommander() {
        int superSpecialIndex = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        // 无趴 Goal → 不写 crawl* 字段
        return CombatUnitConfig.define(
                BUILDER,
                "slimeCommander",
                "Slime Commander: gun/move/combatGoals only. Entity HP/scale/split from code SPLIT_TABLE (gen0=300); maxHealth here is unused. No armor; always drops nothing.",
                300.0D,
                2.0D,
                1.2D,
                CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex],
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex],
                null,
                null,
                CombatGoalSection.sneakAndJump());
    }

    private static CombatUnitConfig.SpecEntry defineSlimeWarrior() {
        int specialForcesIndex = CombatUnitConfig.SPECIAL_FORCES_NPC_DEFAULT_INDEX;
        double[] attrs = CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[specialForcesIndex].clone();
        // Default burst interval 0 (matches prior hardcoded Hard value); adjustable via gunAttackIntervalMin.
        attrs[8] = 0.0D;
        return CombatUnitConfig.define(
                BUILDER,
                "slimeWarrior",
                "Slime Warrior: gun/move/combatGoals. Entity HP/scale/split from code SPLIT_TABLE; maxHealth here is unused. No armor.",
                CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[specialForcesIndex],
                CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[specialForcesIndex],
                CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[specialForcesIndex],
                attrs,
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[specialForcesIndex],
                null,
                null,
                CombatGoalSection.sneakAndJump());
    }

    private static CombatUnitConfig.SpecEntry defineLevMarshal() {
        double[] attrs = {0.4D, 108.0D, 1.18D, 80.0D, 0.5D, 5.0D, 0.5D, 0.2D, 3.0D, 5.0D};
        // 仅跳 Goal → 不写潜行/趴字段
        return CombatUnitConfig.define(
                BUILDER,
                "levMarshal",
                "Lev Marshal boss combat tuning.",
                20.0D,
                2.0D,
                1.08D,
                attrs,
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[CombatUnitConfig.MERCENARY_NPC_DEFAULT_INDEX],
                null,
                0.2D,
                CombatGoalSection.jumpOnly());
    }

    record BombSightConfigEntry(
            CombatUnitConfig.SpecEntry unit, ForgeConfigSpec.IntValue resistanceAmplifier) {}

    private static BombSightConfigEntry defineBombSight() {
        int superSpecialIndex = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        // Strafe rings ≈ 12 / 6 blocks at gunRadius 80: (12/80)²≈0.0225, (6/80)²≈0.005625 → 0.022 / 0.0056.
        double[] attrs = {0.64D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.022D, 0.0056D, 0.0D, 2.0D};
        ForgeConfigSpec.IntValue[] resistanceAmplifier = new ForgeConfigSpec.IntValue[1];
        CombatUnitConfig.SpecEntry unit =
                CombatUnitConfig.define(
                        BUILDER,
                        "bombSight",
                        "Bomb Sight boss; holster AA12 Shockwave + shockwave armor; AI like superSpecial with closer strafe rings.",
                        25.0D,
                        2.0D,
                        1.2D,
                        attrs,
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex],
                        null,
                        0.2D,
                        null,
                        () ->
                                resistanceAmplifier[0] =
                                        BUILDER.comment(
                                                        "Passive DAMAGE_RESISTANCE amplifier (0 = I … 3 = IV). Default 2 = Resistance III.")
                                                .defineInRange("resistanceAmplifier", 2, 0, 4),
                        CombatGoalSection.sneakAndJump());
        return new BombSightConfigEntry(unit, resistanceAmplifier[0]);
    }

    public static void applyLoadedValues() {
        Config.bossGunBurstIntervalOffsetA = GUN_BURST_INTERVAL_OFFSET_A.get();
        Config.bossGunBurstIntervalScaleB = GUN_BURST_INTERVAL_SCALE_B.get();
        Config.bossGunMeleeFracOffsetA = GUN_MELEE_FRAC_OFFSET_A.get();
        Config.bossGunMeleeFracScaleB = GUN_MELEE_FRAC_SCALE_B.get();

        Config.normalShootGunRadius =
                (float)
                        clampDouble(
                                (NORMAL_SHOOT.gunRadius().get() + GUN_RADIUS_OFFSET_A.get())
                                        * GUN_RADIUS_SCALE_B.get(),
                                4.0D,
                                128.0D);
        Config.normalShootGunMeleeFrac =
                (float)
                        clampDouble(
                                (NORMAL_SHOOT.gunMeleeFrac().get() + GUN_MELEE_FRAC_OFFSET_A.get())
                                        * GUN_MELEE_FRAC_SCALE_B.get(),
                                0.05D,
                                1.0D);
        Config.normalShootGunAttackIntervalMin = NORMAL_SHOOT.gunAttackIntervalMin().get();
        Config.normalShootSeeTimeToShoot =
                clampFloor(
                        (NORMAL_SHOOT.seeTimeToShoot().get() + SEE_TIME_TO_SHOOT_OFFSET_A.get())
                                * SEE_TIME_TO_SHOOT_SCALE_B.get(),
                        0,
                        100);

        applyBossUnit(WAR_MACHINE, true);
        applyBossUnit(BOMB_BABY, false);
        applyBossUnit(ANNIHILATOR, false);
        applyBossUnit(PUNISHER, false);
        applyBossUnit(SCORPION, false);
        applyBossUnit(SLIME_COMMANDER, false);
        applyBossUnit(SLIME_WARRIOR, false);
        applyBossUnit(LEV_MARSHAL, false);

        CombatUnitConfig.SpecEntry cdUnit = CEILING_DUEL.unit();
        Config.ceilingDuelMaxHealth = cdUnit.maxHealth().get();
        Config.ceilingDuelAttackDamage = cdUnit.attackDamage().get();
        CombatUnitConfig.copyIntoAttributeStats(cdUnit, Config.ceilingDuelAttributes);
        Config.applyBossGunMeleeFracScale(Config.ceilingDuelAttributes);
        applyAttributeScales(Config.ceilingDuelAttributes);
        Config.ceilingDuelGunGoalSpeedModifier = Config.ceilingDuelAttributes.gunGoalSpeedModifier;
        CombatUnitConfig.copyIntoCombatGoalStats(cdUnit.combatGoals(), Config.ceilingDuelCombatGoals);
        applyCombatGoalScales(Config.ceilingDuelCombatGoals);
        if (cdUnit.equipmentDropChance() != null) {
            Config.ceilingDuelEquipmentDropChance = cdUnit.equipmentDropChance().get().floatValue();
        }
        Config.ceilingDuelHelmetDropChance = CEILING_DUEL.helmetDropChance().get().floatValue();

        CombatUnitConfig.SpecEntry bsUnit = BOMB_SIGHT.unit();
        Config.bombSightMaxHealth = bsUnit.maxHealth().get();
        Config.bombSightAttackDamage = bsUnit.attackDamage().get();
        CombatUnitConfig.copyIntoAttributeStats(bsUnit, Config.bombSightAttributes);
        Config.applyBossGunMeleeFracScale(Config.bombSightAttributes);
        applyAttributeScales(Config.bombSightAttributes);
        Config.bombSightGunGoalSpeedModifier = Config.bombSightAttributes.gunGoalSpeedModifier;
        CombatUnitConfig.copyIntoCombatGoalStats(bsUnit.combatGoals(), Config.bombSightCombatGoals);
        applyCombatGoalScales(Config.bombSightCombatGoals);
        if (bsUnit.equipmentDropChance() != null) {
            Config.bombSightEquipmentDropChance = bsUnit.equipmentDropChance().get().floatValue();
        }
        Config.bombSightResistanceAmplifier = BOMB_SIGHT.resistanceAmplifier().get();
    }

    private static void finishBossAttrsAndGoals(
            CombatUnitConfig.SpecEntry entry,
            Config.NpcAttributeStats attrs,
            Config.CombatGoalStats goals) {
        CombatUnitConfig.copyIntoAttributeStats(entry, attrs);
        Config.applyBossGunMeleeFracScale(attrs);
        applyAttributeScales(attrs);
        CombatUnitConfig.copyIntoCombatGoalStats(entry.combatGoals(), goals);
        if (entry.combatGoals().hasAnySpec()) {
            applyCombatGoalScales(goals);
        }
    }

    private static void applyBossUnit(CombatUnitConfig.SpecEntry entry, boolean warMachine) {
        if (warMachine) {
            Config.warMachineMaxHealth = entry.maxHealth().get();
            Config.warMachineAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(entry, Config.warMachineAttributes, Config.warMachineCombatGoals);
            Config.warMachineGunGoalSpeedModifier = Config.warMachineAttributes.gunGoalSpeedModifier;
            if (entry.equipmentDropChance() != null) {
                Config.warMachineEquipmentDropChance = entry.equipmentDropChance().get().floatValue();
            }
            return;
        }
        if (entry == BOMB_BABY) {
            Config.bombBabyMaxHealth = entry.maxHealth().get();
            Config.bombBabyAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(entry, Config.bombBabyAttributes, Config.bombBabyCombatGoals);
            Config.bombBabyGunGoalSpeedModifier = Config.bombBabyAttributes.gunGoalSpeedModifier;
            if (entry.equipmentDropChance() != null) {
                Config.bombBabyEquipmentDropChance = entry.equipmentDropChance().get().floatValue();
            }
            return;
        }
        if (entry == ANNIHILATOR) {
            Config.annihilatorMaxHealth = entry.maxHealth().get();
            Config.annihilatorAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(entry, Config.annihilatorAttributes, Config.annihilatorCombatGoals);
            Config.annihilatorGunGoalSpeedModifier = Config.annihilatorAttributes.gunGoalSpeedModifier;
            if (entry.equipmentDropChance() != null) {
                Config.annihilatorEquipmentDropChance = entry.equipmentDropChance().get().floatValue();
            }
            return;
        }
        if (entry == PUNISHER) {
            Config.punisherMaxHealth = entry.maxHealth().get();
            Config.punisherAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(entry, Config.punisherAttributes, Config.punisherCombatGoals);
            Config.punisherGunGoalSpeedModifier = Config.punisherAttributes.gunGoalSpeedModifier;
            if (entry.equipmentDropChance() != null) {
                Config.punisherEquipmentDropChance = entry.equipmentDropChance().get().floatValue();
            }
            return;
        }
        if (entry == SCORPION) {
            Config.scorpionMaxHealth = entry.maxHealth().get();
            Config.scorpionAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(entry, Config.scorpionAttributes, Config.scorpionCombatGoals);
            Config.scorpionGunGoalSpeedModifier = Config.scorpionAttributes.gunGoalSpeedModifier;
            if (entry.equipmentDropChance() != null) {
                Config.scorpionEquipmentDropChance = entry.equipmentDropChance().get().floatValue();
            }
            return;
        }
        if (entry == SLIME_COMMANDER) {
            Config.slimeCommanderAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(
                    entry, Config.slimeCommanderAttributes, Config.slimeCommanderCombatGoals);
            Config.slimeCommanderGunGoalSpeedModifier = Config.slimeCommanderAttributes.gunGoalSpeedModifier;
            return;
        }
        if (entry == SLIME_WARRIOR) {
            Config.slimeWarriorAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(entry, Config.slimeWarriorAttributes, Config.slimeWarriorCombatGoals);
            Config.slimeWarriorGunGoalSpeedModifier = Config.slimeWarriorAttributes.gunGoalSpeedModifier;
            return;
        }
        if (entry == LEV_MARSHAL) {
            Config.levMarshalMaxHealth = entry.maxHealth().get();
            Config.levMarshalAttackDamage = entry.attackDamage().get();
            finishBossAttrsAndGoals(entry, Config.levMarshalAttributes, Config.levMarshalCombatGoals);
            Config.levMarshalGunGoalSpeedModifier = Config.levMarshalAttributes.gunGoalSpeedModifier;
            if (entry.equipmentDropChance() != null) {
                Config.levMarshalEquipmentDropChance = entry.equipmentDropChance().get().floatValue();
            }
        }
    }

    private static void applyAttributeScales(Config.NpcAttributeStats stats) {
        if (stats == null) {
            return;
        }
        stats.movementSpeed = clampDouble((stats.movementSpeed + MOVEMENT_SPEED_OFFSET_A.get()) * MOVEMENT_SPEED_SCALE_B.get(), 0.05D, 2.0D);
        stats.followRange = clampDouble((stats.followRange + FOLLOW_RANGE_OFFSET_A.get()) * FOLLOW_RANGE_SCALE_B.get(), 4.0D, 256.0D);
        stats.meleeSpeed = clampDouble((stats.meleeSpeed + MELEE_SPEED_OFFSET_A.get()) * MELEE_SPEED_SCALE_B.get(), 0.5D, 2.0D);
        stats.gunRadius = clampDouble((stats.gunRadius + GUN_RADIUS_OFFSET_A.get()) * GUN_RADIUS_SCALE_B.get(), 4.0D, 128.0D);
        stats.seeTimeToStopMove = clampFloor((stats.seeTimeToStopMove + SEE_TIME_TO_STOP_MOVE_OFFSET_A.get()) * SEE_TIME_TO_STOP_MOVE_SCALE_B.get(), 0, 100);
        stats.gunStrafeOuterRingFrac = clampDouble((stats.gunStrafeOuterRingFrac + GUN_STRAFE_OUTER_RING_FRAC_OFFSET_A.get()) * GUN_STRAFE_OUTER_RING_FRAC_SCALE_B.get(), 0.0D, 1.0D);
        stats.gunStrafeInnerRingFrac = clampDouble((stats.gunStrafeInnerRingFrac + GUN_STRAFE_INNER_RING_FRAC_OFFSET_A.get()) * GUN_STRAFE_INNER_RING_FRAC_SCALE_B.get(), 0.0D, 1.0D);
        stats.seeTimeToShoot = clampFloor((stats.seeTimeToShoot + SEE_TIME_TO_SHOOT_OFFSET_A.get()) * SEE_TIME_TO_SHOOT_SCALE_B.get(), 0, 100);
        stats.gunGoalSpeedModifier = clampDouble((stats.gunGoalSpeedModifier + GUN_GOAL_SPEED_MODIFIER_OFFSET_A.get()) * GUN_GOAL_SPEED_MODIFIER_SCALE_B.get(), 0.5D, 2.0D);
    }

    private static void applyCombatGoalScales(Config.CombatGoalStats stats) {
        if (stats == null) {
            return;
        }
        stats.longSneakTimeDelay = clampFloor((stats.longSneakTimeDelay + LONG_SNEAK_TIME_DELAY_OFFSET_A.get()) * LONG_SNEAK_TIME_DELAY_SCALE_B.get(), 0, 1000);
        stats.longSneakTime = clampFloor((stats.longSneakTime + LONG_SNEAK_TIME_OFFSET_A.get()) * LONG_SNEAK_TIME_SCALE_B.get(), 0, 1000);
        stats.longSneakRandomTime = clampFloor((stats.longSneakRandomTime + LONG_SNEAK_RANDOM_TIME_OFFSET_A.get()) * LONG_SNEAK_RANDOM_TIME_SCALE_B.get(), 0, 1000);
        stats.longSneakDistance = clampDouble((stats.longSneakDistance + LONG_SNEAK_DISTANCE_OFFSET_A.get()) * LONG_SNEAK_DISTANCE_SCALE_B.get(), 0.0D, 256.0D);
        stats.shortSneakTimeDelay = clampFloor((stats.shortSneakTimeDelay + SHORT_SNEAK_TIME_DELAY_OFFSET_A.get()) * SHORT_SNEAK_TIME_DELAY_SCALE_B.get(), 0, 1000);
        stats.shortSneakTime = clampFloor((stats.shortSneakTime + SHORT_SNEAK_TIME_OFFSET_A.get()) * SHORT_SNEAK_TIME_SCALE_B.get(), 0, 1000);
        stats.shortSneakRandomTime = clampFloor((stats.shortSneakRandomTime + SHORT_SNEAK_RANDOM_TIME_OFFSET_A.get()) * SHORT_SNEAK_RANDOM_TIME_SCALE_B.get(), 0, 1000);
        stats.shortSneakDistance = clampDouble((stats.shortSneakDistance + SHORT_SNEAK_DISTANCE_OFFSET_A.get()) * SHORT_SNEAK_DISTANCE_SCALE_B.get(), 0.0D, 256.0D);
        stats.crawlRandomDenominator = clampFloor((stats.crawlRandomDenominator + CRAWL_RANDOM_DENOMINATOR_OFFSET_A.get()) * CRAWL_RANDOM_DENOMINATOR_SCALE_B.get(), 1, 1000);
        stats.crawlExitRandomDenominator = clampFloor((stats.crawlExitRandomDenominator + CRAWL_EXIT_RANDOM_DENOMINATOR_OFFSET_A.get()) * CRAWL_EXIT_RANDOM_DENOMINATOR_SCALE_B.get(), 1, 1000);
        stats.crawlMaxHealthExclusive = (float) clampDouble((stats.crawlMaxHealthExclusive + CRAWL_MAX_HEALTH_EXCLUSIVE_OFFSET_A.get()) * CRAWL_MAX_HEALTH_EXCLUSIVE_SCALE_B.get(), 0.0D, 1000.0D);
        stats.crawlMinPlanarDistance = clampDouble((stats.crawlMinPlanarDistance + CRAWL_MIN_PLANAR_DISTANCE_OFFSET_A.get()) * CRAWL_MIN_PLANAR_DISTANCE_SCALE_B.get(), 0.0D, 256.0D);
        stats.crawlMinHeightToDistanceRatio = clampDouble((stats.crawlMinHeightToDistanceRatio + CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_OFFSET_A.get()) * CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_SCALE_B.get(), 0.0D, 100.0D);
        stats.jumpChance = clampFloor((stats.jumpChance + JUMP_CHANCE_OFFSET_A.get()) * JUMP_CHANCE_SCALE_B.get(), 1, 1000);
        stats.jumpMaxDistance = clampDouble((stats.jumpMaxDistance + JUMP_MAX_DISTANCE_OFFSET_A.get()) * JUMP_MAX_DISTANCE_SCALE_B.get(), 0.0D, 256.0D);
    }

    private static double clampDouble(double v, double min, double max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    private static int clampFloor(double v, int min, int max) {
        int i = (int) Math.floor(v);
        if (i < min) {
            return min;
        }
        if (i > max) {
            return max;
        }
        return i;
    }

}
