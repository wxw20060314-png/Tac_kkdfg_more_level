package com.kkdfg.Tacz_morelevel.config;

import com.kkdfg.Tacz_morelevel.BodyguardProfile;
import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import net.minecraftforge.common.ForgeConfigSpec;

/** NPC 配置（生成 {@code config/tacz_morelevel/npc.toml}）。 */
public final class NpcConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final String[] SOLDIER_KEYS = {
        "refugee", "militia", "mercenary", "assault", "specialForces", "superSpecial"
    };

    private static final SoldierSpawnArmorProfile[] SOLDIER_PROFILE_ORDER = {
        SoldierSpawnArmorProfile.REFUGEE,
        SoldierSpawnArmorProfile.MILITIA,
        SoldierSpawnArmorProfile.MERCENARY,
        SoldierSpawnArmorProfile.ASSAULT,
        SoldierSpawnArmorProfile.SPECIAL_FORCES,
        SoldierSpawnArmorProfile.SUPER_SPECIAL
    };

    private static final String[] BODYGUARD_KEYS = {
        "bodyguardBeauty", "bodyguardStandard", "bodyguardElite", "bodyguardDemonLord"
    };

    private static final BodyguardProfile[] BODYGUARD_PROFILE_ORDER = {
        BodyguardProfile.BEAUTY,
        BodyguardProfile.STANDARD,
        BodyguardProfile.ELITE,
        BodyguardProfile.DEMON_LORD
    };

    private static final ForgeConfigSpec.DoubleValue BODYGUARD_DEATH_ITEM_DROP_CHANCE;
    private static final ForgeConfigSpec.BooleanValue BODYGUARD_CAN_FIGHT_WITH_OTHERGUARD;
    private static final ForgeConfigSpec.DoubleValue GUN_BURST_INTERVAL_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_BURST_INTERVAL_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue GUN_MELEE_FRAC_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_MELEE_FRAC_SCALE_B;
    private static final ForgeConfigSpec.DoubleValue MAX_HEALTH_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue MAX_HEALTH_SCALE_B;
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
    private static final ForgeConfigSpec.DoubleValue GUN_STRAFE_SPEED_MODIFIER_OFFSET_A;
    private static final ForgeConfigSpec.DoubleValue GUN_STRAFE_SPEED_MODIFIER_SCALE_B;
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
                "Maps each NPC unit's base gun burst interval x as floor((x + offsetA) * scaleB). Defaults leave intervals unchanged.");
        GUN_BURST_INTERVAL_OFFSET_A =
                BUILDER.comment("Additive offset a in floor((x + a) * b).")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_BURST_INTERVAL_SCALE_B =
                BUILDER.comment("Scale factor b in floor((x + a) * b).")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunMeleeFracScale");
        BUILDER.comment(
                "Maps each NPC unit's base gunMeleeFrac x as clamp((x + offsetA) * scaleB, 0.05, 1). Defaults leave frac unchanged.");
        GUN_MELEE_FRAC_OFFSET_A =
                BUILDER.comment("Additive offset a in clamp((x + a) * b, 0.05, 1).")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_MELEE_FRAC_SCALE_B =
                BUILDER.comment("Scale factor b in clamp((x + a) * b, 0.05, 1).")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("maxHealthScale");
        BUILDER.comment(
                "Maps each NPC unit maxHealth x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        MAX_HEALTH_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        MAX_HEALTH_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("movementSpeedScale");
        BUILDER.comment(
                "Maps each NPC unit movementSpeed x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        MOVEMENT_SPEED_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        MOVEMENT_SPEED_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("followRangeScale");
        BUILDER.comment(
                "Maps each NPC unit followRange x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        FOLLOW_RANGE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        FOLLOW_RANGE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("meleeSpeedScale");
        BUILDER.comment(
                "Maps each NPC unit meleeSpeed x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        MELEE_SPEED_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        MELEE_SPEED_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunRadiusScale");
        BUILDER.comment(
                "Maps each NPC unit gunRadius x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_RADIUS_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_RADIUS_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("seeTimeToStopMoveScale");
        BUILDER.comment(
                "Maps each NPC unit seeTimeToStopMove x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SEE_TIME_TO_STOP_MOVE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SEE_TIME_TO_STOP_MOVE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunStrafeOuterRingFracScale");
        BUILDER.comment(
                "Maps each NPC unit gunStrafeOuterRingFrac x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_STRAFE_OUTER_RING_FRAC_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_STRAFE_OUTER_RING_FRAC_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunStrafeInnerRingFracScale");
        BUILDER.comment(
                "Maps each NPC unit gunStrafeInnerRingFrac x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_STRAFE_INNER_RING_FRAC_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_STRAFE_INNER_RING_FRAC_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("seeTimeToShootScale");
        BUILDER.comment(
                "Maps each NPC unit seeTimeToShoot x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SEE_TIME_TO_SHOOT_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SEE_TIME_TO_SHOOT_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunGoalSpeedModifierScale");
        BUILDER.comment(
                "Maps each NPC unit gunGoalSpeedModifier x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_GOAL_SPEED_MODIFIER_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_GOAL_SPEED_MODIFIER_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("gunStrafeSpeedModifierScale");
        BUILDER.comment(
                "Maps each NPC unit gunStrafeSpeedModifier x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        GUN_STRAFE_SPEED_MODIFIER_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        GUN_STRAFE_SPEED_MODIFIER_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakTimeDelayScale");
        BUILDER.comment(
                "Maps each NPC unit longSneakTimeDelay x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        LONG_SNEAK_TIME_DELAY_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_TIME_DELAY_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakTimeScale");
        BUILDER.comment(
                "Maps each NPC unit longSneakTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        LONG_SNEAK_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakRandomTimeScale");
        BUILDER.comment(
                "Maps each NPC unit longSneakRandomTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        LONG_SNEAK_RANDOM_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_RANDOM_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("longSneakDistanceScale");
        BUILDER.comment(
                "Maps each NPC unit longSneakDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        LONG_SNEAK_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        LONG_SNEAK_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakTimeDelayScale");
        BUILDER.comment(
                "Maps each NPC unit shortSneakTimeDelay x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SHORT_SNEAK_TIME_DELAY_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_TIME_DELAY_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakTimeScale");
        BUILDER.comment(
                "Maps each NPC unit shortSneakTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SHORT_SNEAK_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakRandomTimeScale");
        BUILDER.comment(
                "Maps each NPC unit shortSneakRandomTime x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        SHORT_SNEAK_RANDOM_TIME_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_RANDOM_TIME_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("shortSneakDistanceScale");
        BUILDER.comment(
                "Maps each NPC unit shortSneakDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        SHORT_SNEAK_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        SHORT_SNEAK_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlRandomDenominatorScale");
        BUILDER.comment(
                "Maps each NPC unit crawlRandomDenominator x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        CRAWL_RANDOM_DENOMINATOR_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_RANDOM_DENOMINATOR_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlExitRandomDenominatorScale");
        BUILDER.comment(
                "Maps each NPC unit crawlExitRandomDenominator x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        CRAWL_EXIT_RANDOM_DENOMINATOR_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_EXIT_RANDOM_DENOMINATOR_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlMaxHealthExclusiveScale");
        BUILDER.comment(
                "Maps each NPC unit crawlMaxHealthExclusive x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        CRAWL_MAX_HEALTH_EXCLUSIVE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_MAX_HEALTH_EXCLUSIVE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlMinPlanarDistanceScale");
        BUILDER.comment(
                "Maps each NPC unit crawlMinPlanarDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        CRAWL_MIN_PLANAR_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_MIN_PLANAR_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("crawlMinHeightToDistanceRatioScale");
        BUILDER.comment(
                "Maps each NPC unit crawlMinHeightToDistanceRatio x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        CRAWL_MIN_HEIGHT_TO_DISTANCE_RATIO_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("jumpChanceScale");
        BUILDER.comment(
                "Maps each NPC unit jumpChance x as (x + offsetA) * scaleB (int fields use floor). Defaults leave x unchanged.");
        JUMP_CHANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        JUMP_CHANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("jumpMaxDistanceScale");
        BUILDER.comment(
                "Maps each NPC unit jumpMaxDistance x as (x + offsetA) * scaleB. Defaults leave x unchanged.");
        JUMP_MAX_DISTANCE_OFFSET_A =
                BUILDER.comment("Additive offset a in (x + a) * b.")
                        .defineInRange("offsetA", 0.0D, -1000.0D, 1000.0D);
        JUMP_MAX_DISTANCE_SCALE_B =
                BUILDER.comment("Scale factor b in (x + a) * b.")
                        .defineInRange("scaleB", 1.0D, -1000.0D, 1000.0D);
        BUILDER.pop();

        BUILDER.push("bodyguardCommon");
        BUILDER.comment(
                "Bodyguard companion death loot and targeting rules shared by all bodyguard tiers.");
        BODYGUARD_DEATH_ITEM_DROP_CHANCE =
                BUILDER.comment(
                                "Each non-empty inventory/equipment stack rolls independently; 1.0 = 100%, 0.0 = destroy all.")
                        .defineInRange("itemDropChance", 1.0, 0.0, 1.0);
        BODYGUARD_CAN_FIGHT_WITH_OTHERGUARD =
                BUILDER.comment(
                                "When false, tamed bodyguards never target other players or other players' bodyguards.")
                        .define("canFightWithotherguard", true);
        BUILDER.pop();
    }

    static final CombatUnitConfig.SpecEntry[] SOLDIER_ENTRIES = defineSoldiers();
    static final CombatUnitConfig.SpecEntry[] BODYGUARD_ENTRIES = defineBodyguards();

    static final MysteriousMerchantConfigEntry MYSTERIOUS_MERCHANT = defineMysteriousMerchant();
    static final CombatUnitConfig.SpecEntry MARSHAL_GUARD = defineMarshalGuard();
    static final CombatUnitConfig.SpecEntry LURKER = defineLurker();
    static final CombatUnitConfig.SpecEntry POLICE = definePolice();

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /** Cloth：全局 maxHealth (x+a)*b 的 a。 */
    public static ForgeConfigSpec.DoubleValue maxHealthOffsetA() {
        return MAX_HEALTH_OFFSET_A;
    }

    /** Cloth：全局 maxHealth (x+a)*b 的 b。 */
    public static ForgeConfigSpec.DoubleValue maxHealthScaleB() {
        return MAX_HEALTH_SCALE_B;
    }

    /** Cloth / 调试：全局移速 (x+a)*b 的 a。 */
    public static ForgeConfigSpec.DoubleValue movementSpeedOffsetA() {
        return MOVEMENT_SPEED_OFFSET_A;
    }

    /** Cloth / 调试：全局移速 (x+a)*b 的 b。 */
    public static ForgeConfigSpec.DoubleValue movementSpeedScaleB() {
        return MOVEMENT_SPEED_SCALE_B;
    }

    /** Cloth：moveTo / gunGoalSpeedModifier 全局 a。 */
    public static ForgeConfigSpec.DoubleValue gunGoalSpeedModifierOffsetA() {
        return GUN_GOAL_SPEED_MODIFIER_OFFSET_A;
    }

    /** Cloth：moveTo / gunGoalSpeedModifier 全局 b。 */
    public static ForgeConfigSpec.DoubleValue gunGoalSpeedModifierScaleB() {
        return GUN_GOAL_SPEED_MODIFIER_SCALE_B;
    }

    /** Cloth：strafe 全局 a。 */
    public static ForgeConfigSpec.DoubleValue gunStrafeSpeedModifierOffsetA() {
        return GUN_STRAFE_SPEED_MODIFIER_OFFSET_A;
    }

    /** Cloth：strafe 全局 b。 */
    public static ForgeConfigSpec.DoubleValue gunStrafeSpeedModifierScaleB() {
        return GUN_STRAFE_SPEED_MODIFIER_SCALE_B;
    }

    private NpcConfig() {}

    private static CombatUnitConfig.SpecEntry[] defineSoldiers() {
        BUILDER.comment("Soldier NPC types: refugee through superSpecial.");
        CombatUnitConfig.SpecEntry[] out = new CombatUnitConfig.SpecEntry[SOLDIER_KEYS.length];
        for (int i = 0; i < SOLDIER_KEYS.length; i++) {
            out[i] =
                    CombatUnitConfig.define(
                            BUILDER,
                            SOLDIER_KEYS[i],
                            "Soldier NPC combat tuning.",
                            CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[i],
                            CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[i],
                            CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[i],
                            CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[i],
                            CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[i],
                            CombatUnitConfig.DEFAULT_REMOVE_WHEN_FAR[i],
                            null);
        }
        return out;
    }

    private static CombatUnitConfig.SpecEntry[] defineBodyguards() {
        BUILDER.comment("Bodyguard companion tiers.");
        CombatUnitConfig.SpecEntry[] out = new CombatUnitConfig.SpecEntry[BODYGUARD_KEYS.length];
        for (int i = 0; i < BODYGUARD_KEYS.length; i++) {
            out[i] =
                    CombatUnitConfig.define(
                            BUILDER,
                            BODYGUARD_KEYS[i],
                            "Bodyguard tier combat tuning.",
                            CombatUnitConfig.DEFAULT_BODYGUARD_MAX_HEALTH,
                            CombatUnitConfig.DEFAULT_BODYGUARD_ATTACK_DAMAGE,
                            CombatUnitConfig.DEFAULT_BODYGUARD_GUN_GOAL_SPEED,
                            CombatUnitConfig.DEFAULT_BODYGUARD_ATTRIBUTE_VALUES[i],
                            CombatUnitConfig.DEFAULT_BODYGUARD_COMBAT_GOAL_VALUES[i],
                            null,
                            null);
        }
        return out;
    }

    private static CombatUnitConfig.SpecEntry defineMarshalGuard() {
        double[] attrs = {
            0.46D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.3D, 0.1D, 2.0D, 2.0D
        };
        return CombatUnitConfig.define(
                BUILDER,
                "marshalGuard",
                "Marshal faction guard minion spawned by Lev Marshal.",
                20.0D,
                2.0D,
                1.2D,
                attrs,
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX],
                null,
                0.0D);
    }

    private static CombatUnitConfig.SpecEntry defineLurker() {
        int assaultIndex = CombatUnitConfig.ASSAULT_NPC_DEFAULT_INDEX;
        return CombatUnitConfig.define(
                BUILDER,
                "lurker",
                "Lurker hostile NPC: combat defaults match assault; fixed stealth cloak set; default removeWhenFarAway=0.",
                CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[assaultIndex],
                CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[assaultIndex],
                CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[assaultIndex],
                CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[assaultIndex],
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[assaultIndex],
                0,
                null);
    }

    private static CombatUnitConfig.SpecEntry definePolice() {
        int assaultIndex = CombatUnitConfig.ASSAULT_NPC_DEFAULT_INDEX;
        return CombatUnitConfig.define(
                BUILDER,
                "police",
                "Police neutral NPC: combat defaults match assault; fixed police armor; assault gun pool; alert others on hurt.",
                CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[assaultIndex],
                CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[assaultIndex],
                CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[assaultIndex],
                CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[assaultIndex],
                CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[assaultIndex],
                null,
                null);
    }

    record MysteriousMerchantConfigEntry(
            CombatUnitConfig.SpecEntry unit,
            ForgeConfigSpec.DoubleValue wanderStrollSpeed,
            ForgeConfigSpec.BooleanValue passiveRegenerationEnabled,
            ForgeConfigSpec.IntValue passiveRegenerationAmplifier,
            ForgeConfigSpec.BooleanValue passiveDolphinsGraceEnabled,
            ForgeConfigSpec.IntValue passiveDolphinsGraceAmplifier) {}

    private static MysteriousMerchantConfigEntry defineMysteriousMerchant() {
        int mercenaryIndex = CombatUnitConfig.MERCENARY_NPC_DEFAULT_INDEX;
        ForgeConfigSpec.DoubleValue[] wanderStrollSpeed = new ForgeConfigSpec.DoubleValue[1];
        ForgeConfigSpec.BooleanValue[] passiveRegenerationEnabled = new ForgeConfigSpec.BooleanValue[1];
        ForgeConfigSpec.IntValue[] passiveRegenerationAmplifier = new ForgeConfigSpec.IntValue[1];
        ForgeConfigSpec.BooleanValue[] passiveDolphinsGraceEnabled = new ForgeConfigSpec.BooleanValue[1];
        ForgeConfigSpec.IntValue[] passiveDolphinsGraceAmplifier = new ForgeConfigSpec.IntValue[1];
        CombatUnitConfig.SpecEntry unit =
                CombatUnitConfig.define(
                        BUILDER,
                        "mysteriousMerchant",
                        "Mysterious merchant NPC; defaults match mercenary soldier tuning."
                                + " Jump height is code TIER_HIGH_MID (same as mercenary); not configurable.",
                        20.0D,
                        2.0D,
                        1.08D,
                        CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[mercenaryIndex],
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[mercenaryIndex],
                        null,
                        0.2D,
                        () -> {
                            wanderStrollSpeed[0] =
                                    BUILDER.comment("WaterAvoidingRandomStrollGoal speed when not in combat.")
                                            .defineInRange("wanderStrollSpeed", 0.92, 0.05, 2.0);
                            passiveRegenerationEnabled[0] =
                                    BUILDER.comment("Apply infinite Regeneration while alive.")
                                            .define("passiveRegenerationEnabled", true);
                            passiveRegenerationAmplifier[0] =
                                    BUILDER.comment("Regeneration amplifier (0 = Regen I).")
                                            .defineInRange("passiveRegenerationAmplifier", 0, 0, 255);
                            passiveDolphinsGraceEnabled[0] =
                                    BUILDER.comment("Apply infinite Dolphins Grace while alive.")
                                            .define("passiveDolphinsGraceEnabled", true);
                            passiveDolphinsGraceAmplifier[0] =
                                    BUILDER.comment("Dolphins Grace amplifier (0 = Grace I).")
                                            .defineInRange("passiveDolphinsGraceAmplifier", 1, 0, 255);
                        },
                        null);
        return new MysteriousMerchantConfigEntry(
                unit,
                wanderStrollSpeed[0],
                passiveRegenerationEnabled[0],
                passiveRegenerationAmplifier[0],
                passiveDolphinsGraceEnabled[0],
                passiveDolphinsGraceAmplifier[0]);
    }

    public static void applyLoadedValues() {
        Config.npcGunBurstIntervalOffsetA = GUN_BURST_INTERVAL_OFFSET_A.get();
        Config.npcGunBurstIntervalScaleB = GUN_BURST_INTERVAL_SCALE_B.get();
        Config.npcGunMeleeFracOffsetA = GUN_MELEE_FRAC_OFFSET_A.get();
        Config.npcGunMeleeFracScaleB = GUN_MELEE_FRAC_SCALE_B.get();

        for (int i = 0; i < SOLDIER_ENTRIES.length; i++) {
            CombatUnitConfig.SpecEntry entry = SOLDIER_ENTRIES[i];
            SoldierSpawnArmorProfile profile = SOLDIER_PROFILE_ORDER[i];
            Config.NpcAttributeStats attrs = Config.npcAttributesByProfile[profile.ordinal()];
            Config.CombatGoalStats goals = Config.npcCombatGoalsByProfile[profile.ordinal()];
            CombatUnitConfig.copyIntoAttributeStats(entry, attrs);
            Config.applyNpcGunMeleeFracScale(attrs);
            applyAttributeScales(attrs);
            CombatUnitConfig.copyIntoCombatGoalStats(entry.combatGoals(), goals);
            applyCombatGoalScales(goals);
            if (entry.removeWhenFarAway() != null) {
                Config.removeWhenFarAwayByProfile[profile.ordinal()] = entry.removeWhenFarAway().get() != 0;
            }
        }
        for (int i = 0; i < BODYGUARD_ENTRIES.length; i++) {
            CombatUnitConfig.SpecEntry entry = BODYGUARD_ENTRIES[i];
            BodyguardProfile profile = BODYGUARD_PROFILE_ORDER[i];
            Config.NpcAttributeStats attrs = Config.bodyguardAttributesByProfile[profile.ordinal()];
            Config.CombatGoalStats goals = Config.bodyguardCombatGoalsByProfile[profile.ordinal()];
            CombatUnitConfig.copyIntoAttributeStats(entry, attrs);
            Config.applyNpcGunMeleeFracScale(attrs);
            applyAttributeScales(attrs);
            CombatUnitConfig.copyIntoCombatGoalStats(entry.combatGoals(), goals);
            applyCombatGoalScales(goals);
        }
        Config.bodyguardDeathItemDropChance = BODYGUARD_DEATH_ITEM_DROP_CHANCE.get().floatValue();
        Config.canFightWithotherguard = BODYGUARD_CAN_FIGHT_WITH_OTHERGUARD.get();

        MysteriousMerchantConfigEntry mm = MYSTERIOUS_MERCHANT;
        CombatUnitConfig.SpecEntry mmUnit = mm.unit();
        Config.mysteriousMerchantAttackDamage = mmUnit.attackDamage().get();
        Config.mysteriousMerchantWanderStrollSpeed = mm.wanderStrollSpeed().get();
        CombatUnitConfig.copyIntoAttributeStats(mmUnit, Config.mysteriousMerchantAttributes);
        Config.applyNpcGunMeleeFracScale(Config.mysteriousMerchantAttributes);
        applyAttributeScales(Config.mysteriousMerchantAttributes);
        Config.mysteriousMerchantMaxHealth = Config.mysteriousMerchantAttributes.maxHealth;
        Config.mysteriousMerchantGunGoalSpeedModifier =
                Config.mysteriousMerchantAttributes.gunGoalSpeedModifier;
        CombatUnitConfig.copyIntoCombatGoalStats(mmUnit.combatGoals(), Config.mysteriousMerchantCombatGoals);
        applyCombatGoalScales(Config.mysteriousMerchantCombatGoals);
        Config.mysteriousMerchantPassiveRegenerationEnabled = mm.passiveRegenerationEnabled().get();
        Config.mysteriousMerchantPassiveRegenerationAmplifier = mm.passiveRegenerationAmplifier().get();
        Config.mysteriousMerchantPassiveDolphinsGraceEnabled = mm.passiveDolphinsGraceEnabled().get();
        Config.mysteriousMerchantPassiveDolphinsGraceAmplifier = mm.passiveDolphinsGraceAmplifier().get();
        if (mmUnit.equipmentDropChance() != null) {
            Config.mysteriousMerchantEquipmentDropChance = mmUnit.equipmentDropChance().get().floatValue();
        }

        CombatUnitConfig.SpecEntry mg = MARSHAL_GUARD;
        Config.marshalGuardAttackDamage = mg.attackDamage().get();
        CombatUnitConfig.copyIntoAttributeStats(mg, Config.marshalGuardAttributes);
        Config.applyNpcGunMeleeFracScale(Config.marshalGuardAttributes);
        applyAttributeScales(Config.marshalGuardAttributes);
        Config.marshalGuardMaxHealth = Config.marshalGuardAttributes.maxHealth;
        Config.marshalGuardGunGoalSpeedModifier = Config.marshalGuardAttributes.gunGoalSpeedModifier;
        CombatUnitConfig.copyIntoCombatGoalStats(mg.combatGoals(), Config.marshalGuardCombatGoals);
        applyCombatGoalScales(Config.marshalGuardCombatGoals);
        if (mg.equipmentDropChance() != null) {
            Config.marshalGuardEquipmentDropChance = mg.equipmentDropChance().get().floatValue();
        }

        CombatUnitConfig.SpecEntry lurker = LURKER;
        Config.lurkerAttackDamage = lurker.attackDamage().get();
        CombatUnitConfig.copyIntoAttributeStats(lurker, Config.lurkerAttributes);
        Config.applyNpcGunMeleeFracScale(Config.lurkerAttributes);
        applyAttributeScales(Config.lurkerAttributes);
        Config.lurkerMaxHealth = Config.lurkerAttributes.maxHealth;
        Config.lurkerGunGoalSpeedModifier = Config.lurkerAttributes.gunGoalSpeedModifier;
        CombatUnitConfig.copyIntoCombatGoalStats(lurker.combatGoals(), Config.lurkerCombatGoals);
        applyCombatGoalScales(Config.lurkerCombatGoals);
        if (lurker.removeWhenFarAway() != null) {
            Config.lurkerRemoveWhenFarAway = lurker.removeWhenFarAway().get() != 0;
        }

        CombatUnitConfig.SpecEntry police = POLICE;
        Config.policeAttackDamage = police.attackDamage().get();
        CombatUnitConfig.copyIntoAttributeStats(police, Config.policeAttributes);
        Config.applyNpcGunMeleeFracScale(Config.policeAttributes);
        applyAttributeScales(Config.policeAttributes);
        Config.policeMaxHealth = Config.policeAttributes.maxHealth;
        Config.policeGunGoalSpeedModifier = Config.policeAttributes.gunGoalSpeedModifier;
        CombatUnitConfig.copyIntoCombatGoalStats(police.combatGoals(), Config.policeCombatGoals);
        applyCombatGoalScales(Config.policeCombatGoals);
    }

    private static void applyAttributeScales(Config.NpcAttributeStats stats) {
        if (stats == null) {
            return;
        }
        stats.maxHealth =
                clampDouble(
                        (stats.maxHealth + MAX_HEALTH_OFFSET_A.get()) * MAX_HEALTH_SCALE_B.get(),
                        1.0D,
                        10000.0D);
        stats.movementSpeed = clampDouble((stats.movementSpeed + MOVEMENT_SPEED_OFFSET_A.get()) * MOVEMENT_SPEED_SCALE_B.get(), 0.05D, 2.0D);
        stats.followRange = clampDouble((stats.followRange + FOLLOW_RANGE_OFFSET_A.get()) * FOLLOW_RANGE_SCALE_B.get(), 4.0D, 256.0D);
        stats.meleeSpeed = clampDouble((stats.meleeSpeed + MELEE_SPEED_OFFSET_A.get()) * MELEE_SPEED_SCALE_B.get(), 0.5D, 2.0D);
        stats.gunRadius = clampDouble((stats.gunRadius + GUN_RADIUS_OFFSET_A.get()) * GUN_RADIUS_SCALE_B.get(), 4.0D, 128.0D);
        stats.seeTimeToStopMove = clampFloor((stats.seeTimeToStopMove + SEE_TIME_TO_STOP_MOVE_OFFSET_A.get()) * SEE_TIME_TO_STOP_MOVE_SCALE_B.get(), 0, 100);
        stats.gunStrafeOuterRingFrac = clampDouble((stats.gunStrafeOuterRingFrac + GUN_STRAFE_OUTER_RING_FRAC_OFFSET_A.get()) * GUN_STRAFE_OUTER_RING_FRAC_SCALE_B.get(), 0.0D, 1.0D);
        stats.gunStrafeInnerRingFrac = clampDouble((stats.gunStrafeInnerRingFrac + GUN_STRAFE_INNER_RING_FRAC_OFFSET_A.get()) * GUN_STRAFE_INNER_RING_FRAC_SCALE_B.get(), 0.0D, 1.0D);
        stats.seeTimeToShoot = clampFloor((stats.seeTimeToShoot + SEE_TIME_TO_SHOOT_OFFSET_A.get()) * SEE_TIME_TO_SHOOT_SCALE_B.get(), 0, 100);
        stats.gunGoalSpeedModifier = clampDouble((stats.gunGoalSpeedModifier + GUN_GOAL_SPEED_MODIFIER_OFFSET_A.get()) * GUN_GOAL_SPEED_MODIFIER_SCALE_B.get(), 0.5D, 2.0D);
        stats.gunStrafeSpeedModifier =
                clampDouble(
                        (stats.gunStrafeSpeedModifier + GUN_STRAFE_SPEED_MODIFIER_OFFSET_A.get())
                                * GUN_STRAFE_SPEED_MODIFIER_SCALE_B.get(),
                        0.05D,
                        2.0D);
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
