package com.kkdfg.Tacz_morelevel.config;

import com.kkdfg.Tacz_morelevel.Config;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraftforge.common.ForgeConfigSpec;

/** 战斗单位（士兵/护卫/Boss）公共 TOML 字段定义与运行时缓存拷贝。 */
public final class CombatUnitConfig {

    public static final int COMBAT_GOAL_PARAM_COUNT = 19;

    /**
     * movementSpeed, followRange, meleeSpeed, gunRadius, gunMeleeFrac,
     * seeTimeToStopMove, gunStrafeOuterRingFrac, gunStrafeInnerRingFrac,
     * gunAttackIntervalMin, seeTimeToShoot
     */
    public static final double[][] DEFAULT_NPC_ATTRIBUTE_VALUES = {
            {0.22D, 20.0D, 1.05D, 20.0D, 0.5D, 5.0D, 0.5D, 0.26D, 5.0D, 7.0D},
            {0.3D, 24.0D, 1.15D, 25.0D, 0.5D, 5.0D, 0.5D, 0.25D, 3.0D, 6.0D},
            {0.37D, 42.0D, 1.18D, 32.0D, 0.5D, 5.0D, 0.5D, 0.2D, 2.0D, 5.0D},
            {0.42D, 54.0D, 1.2D, 45.0D, 0.4D, 5.0D, 0.5D, 0.2D, 1.0D, 4.0D},
            {0.46D, 72.0D, 1.22D, 60.0D, 0.4D, 4.0D, 0.4D, 0.15D, 0.0D, 3.0D},
            {0.5D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.3D, 0.1D, 0.0D, 2.0D}
    };

    public static final double[][] DEFAULT_BODYGUARD_ATTRIBUTE_VALUES = {
            {0.33D, 28.0D, 1.05D, 20.0D, 0.5D, 5.0D, 0.5D, 0.26D, 5.0D, 5.0D},
            {0.4D, 35.0D, 1.1D, 28.0D, 0.5D, 5.0D, 0.5D, 0.25D, 3.0D, 4.0D},
            {0.43D, 42.0D, 1.15D, 32.0D, 0.4D, 5.0D, 0.5D, 0.2D, 3.0D, 3.0D},
            {0.48D, 49.0D, 1.2D, 36.0D, 0.4D, 3.0D, 0.3D, 0.1D, 2.0D, 3.0D}
    };

    public static final double[][] DEFAULT_NPC_COMBAT_GOAL_VALUES = {
            {100, 20, 40, 25.0, 0, 20, 3, 12, 15.0, 0, 10, 50, 6.0, 4.0, 1.0, 0, 28, 0, 5.0},
            {100, 20, 40, 25.0, 0, 20, 3, 12, 15.0, 0, 10, 50, 6.0, 4.0, 1.0, 0, 28, 0, 7.0},
            {100, 20, 40, 25.0, 0, 20, 3, 12, 15.0, 1, 10, 50, 6.0, 4.0, 1.0, 0, 28, 1, 10.0},
            {100, 20, 40, 25.0, 0, 20, 3, 12, 15.0, 1, 10, 50, 6.0, 4.0, 1.0, 0, 28, 1, 12.0},
            {100, 20, 40, 25.0, 1, 20, 3, 12, 15.0, 1, 10, 50, 6.0, 4.0, 1.0, 1, 28, 1, 15.0},
            {100, 20, 40, 25.0, 1, 5, 1, 4, 15.0, 1, 10, 50, 6.0, 4.0, 1.0, 1, 28, 1, 18.0}
    };

    public static final double[][] DEFAULT_BODYGUARD_COMBAT_GOAL_VALUES = {
            {100, 20, 40, 25.0, 0, 20, 3, 12, 15.0, 0, 30, 15, 4.5, 1.0, 1.0, 0, 28, 0, 5.0},
            {100, 20, 40, 25.0, 0, 20, 3, 12, 15.0, 0, 30, 15, 4.5, 1.0, 1.0, 0, 28, 1, 7.0},
            {100, 20, 40, 25.0, 1, 20, 3, 12, 15.0, 1, 30, 15, 4.5, 4.0, 1.0, 1, 28, 1, 10.0},
            {100, 20, 40, 22.0, 1, 6, 2, 6, 12.0, 1, 30, 15, 4.5, 4.0, 1.0, 1, 28, 1, 12.0}
    };

    public static final double[] DEFAULT_SOLDIER_MAX_HEALTH = {16.0D, 20.0D, 20.0D, 20.0D, 20.0D, 20.0D};
    public static final double[] DEFAULT_SOLDIER_ATTACK_DAMAGE = {1.5D, 2.0D, 2.0D, 2.0D, 2.0D, 2.0D};
    public static final double[] DEFAULT_SOLDIER_GUN_GOAL_SPEED = {1.0D, 1.05D, 1.08D, 1.1D, 1.15D, 1.2D};
    public static final int[] DEFAULT_REMOVE_WHEN_FAR = {1, 1, 1, 1, 0, 0};

    public static final double DEFAULT_BODYGUARD_MAX_HEALTH = 20.0D;
    public static final double DEFAULT_BODYGUARD_ATTACK_DAMAGE = 2.0D;
    public static final double DEFAULT_BODYGUARD_GUN_GOAL_SPEED = 1.05D;

    public static final int MERCENARY_NPC_DEFAULT_INDEX = 2;
    /** 与 {@code NpcConfig} SOLDIER_KEYS 中 {@code assault} 下标一致。 */
    public static final int ASSAULT_NPC_DEFAULT_INDEX = 3;
    public static final int SPECIAL_FORCES_NPC_DEFAULT_INDEX = 4;
    public static final int SUPER_SPECIAL_NPC_DEFAULT_INDEX = 5;

    /** 未写出的 Goal 分段对应字段为 {@code null}（加载时保留内存默认值）。 */
    public record CombatGoalConfigEntry(
            @Nullable ForgeConfigSpec.IntValue longSneakTimeDelay,
            @Nullable ForgeConfigSpec.IntValue longSneakTime,
            @Nullable ForgeConfigSpec.IntValue longSneakRandomTime,
            @Nullable ForgeConfigSpec.DoubleValue longSneakDistance,
            @Nullable ForgeConfigSpec.IntValue longSneakEnabled,
            @Nullable ForgeConfigSpec.IntValue shortSneakTimeDelay,
            @Nullable ForgeConfigSpec.IntValue shortSneakTime,
            @Nullable ForgeConfigSpec.IntValue shortSneakRandomTime,
            @Nullable ForgeConfigSpec.DoubleValue shortSneakDistance,
            @Nullable ForgeConfigSpec.IntValue shortSneakEnabled,
            @Nullable ForgeConfigSpec.IntValue crawlRandomDenominator,
            @Nullable ForgeConfigSpec.IntValue crawlExitRandomDenominator,
            @Nullable ForgeConfigSpec.DoubleValue crawlMaxHealthExclusive,
            @Nullable ForgeConfigSpec.DoubleValue crawlMinPlanarDistance,
            @Nullable ForgeConfigSpec.DoubleValue crawlMinHeightToDistanceRatio,
            @Nullable ForgeConfigSpec.IntValue crawlEnabled,
            @Nullable ForgeConfigSpec.IntValue jumpChance,
            @Nullable ForgeConfigSpec.IntValue jumpEnabled,
            @Nullable ForgeConfigSpec.DoubleValue jumpMaxDistance) {

        public boolean hasAnySpec() {
            return this.longSneakTimeDelay != null
                    || this.crawlRandomDenominator != null
                    || this.jumpChance != null;
        }

        public static CombatGoalConfigEntry empty() {
            return new CombatGoalConfigEntry(
                    null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null);
        }
    }

    public record SpecEntry(
            ForgeConfigSpec.DoubleValue maxHealth,
            ForgeConfigSpec.DoubleValue movementSpeed,
            ForgeConfigSpec.DoubleValue attackDamage,
            ForgeConfigSpec.DoubleValue followRange,
            ForgeConfigSpec.DoubleValue meleeSpeed,
            ForgeConfigSpec.DoubleValue gunRadius,
            ForgeConfigSpec.DoubleValue gunMeleeFrac,
            ForgeConfigSpec.IntValue seeTimeToStopMove,
            ForgeConfigSpec.DoubleValue gunStrafeOuterRingFrac,
            ForgeConfigSpec.DoubleValue gunStrafeInnerRingFrac,
            ForgeConfigSpec.IntValue gunAttackIntervalMin,
            ForgeConfigSpec.IntValue seeTimeToShoot,
            ForgeConfigSpec.DoubleValue gunGoalSpeedModifier,
            CombatGoalConfigEntry combatGoals,
            ForgeConfigSpec.IntValue removeWhenFarAway,
            ForgeConfigSpec.DoubleValue equipmentDropChance) {}

    private CombatUnitConfig() {}

    /**
     * 定义一节完整战斗单位配置（顶层 push {@code sectionKey}，内含 {@code combatGoals} 子节）。
     *
     * @param attributeDefaults 长度 10，见 {@link #DEFAULT_NPC_ATTRIBUTE_VALUES}
     * @param combatGoalDefaults 长度 19，见 {@link #DEFAULT_NPC_COMBAT_GOAL_VALUES}
     */
    public static SpecEntry define(
            ForgeConfigSpec.Builder builder,
            String sectionKey,
            String comment,
            double maxHealth,
            double attackDamage,
            double gunGoalSpeedModifier,
            double[] attributeDefaults,
            double[] combatGoalDefaults) {
        return define(
                builder,
                sectionKey,
                comment,
                maxHealth,
                attackDamage,
                gunGoalSpeedModifier,
                attributeDefaults,
                combatGoalDefaults,
                null,
                null,
                null,
                null,
                CombatGoalSection.all());
    }

    public static SpecEntry define(
            ForgeConfigSpec.Builder builder,
            String sectionKey,
            String comment,
            double maxHealth,
            double attackDamage,
            double gunGoalSpeedModifier,
            double[] attributeDefaults,
            double[] combatGoalDefaults,
            Integer removeWhenFarAwayDefault,
            Double equipmentDropChanceDefault) {
        return define(
                builder,
                sectionKey,
                comment,
                maxHealth,
                attackDamage,
                gunGoalSpeedModifier,
                attributeDefaults,
                combatGoalDefaults,
                removeWhenFarAwayDefault,
                equipmentDropChanceDefault,
                null,
                null,
                CombatGoalSection.all());
    }

    public static SpecEntry define(
            ForgeConfigSpec.Builder builder,
            String sectionKey,
            String comment,
            double maxHealth,
            double attackDamage,
            double gunGoalSpeedModifier,
            double[] attributeDefaults,
            double[] combatGoalDefaults,
            Integer removeWhenFarAwayDefault,
            Double equipmentDropChanceDefault,
            EnumSet<CombatGoalSection> combatGoalSections) {
        return define(
                builder,
                sectionKey,
                comment,
                maxHealth,
                attackDamage,
                gunGoalSpeedModifier,
                attributeDefaults,
                combatGoalDefaults,
                removeWhenFarAwayDefault,
                equipmentDropChanceDefault,
                null,
                null,
                combatGoalSections);
    }

    public static SpecEntry define(
            ForgeConfigSpec.Builder builder,
            String sectionKey,
            String comment,
            double maxHealth,
            double attackDamage,
            double gunGoalSpeedModifier,
            double[] attributeDefaults,
            double[] combatGoalDefaults,
            Integer removeWhenFarAwayDefault,
            Double equipmentDropChanceDefault,
            Runnable beforeCombatGoals,
            Runnable afterCombatGoals) {
        return define(
                builder,
                sectionKey,
                comment,
                maxHealth,
                attackDamage,
                gunGoalSpeedModifier,
                attributeDefaults,
                combatGoalDefaults,
                removeWhenFarAwayDefault,
                equipmentDropChanceDefault,
                beforeCombatGoals,
                afterCombatGoals,
                CombatGoalSection.all());
    }

    public static SpecEntry define(
            ForgeConfigSpec.Builder builder,
            String sectionKey,
            String comment,
            double maxHealth,
            double attackDamage,
            double gunGoalSpeedModifier,
            double[] attributeDefaults,
            double[] combatGoalDefaults,
            Integer removeWhenFarAwayDefault,
            Double equipmentDropChanceDefault,
            Runnable beforeCombatGoals,
            Runnable afterCombatGoals,
            EnumSet<CombatGoalSection> combatGoalSections) {
        if (attributeDefaults.length != 10) {
            throw new IllegalArgumentException("attributeDefaults must have 10 values");
        }
        if (combatGoalDefaults.length != COMBAT_GOAL_PARAM_COUNT) {
            throw new IllegalArgumentException(
                    "combatGoalDefaults must have " + COMBAT_GOAL_PARAM_COUNT + " values");
        }
        if (combatGoalSections == null) {
            combatGoalSections = CombatGoalSection.all();
        }
        builder.push(sectionKey);
        builder.comment(comment);
        ForgeConfigSpec.DoubleValue maxHealthSpec =
                builder.comment("Attributes.MAX_HEALTH base value.")
                        .defineInRange("maxHealth", maxHealth, 1.0, 1000.0);
        ForgeConfigSpec.DoubleValue movementSpeed =
                builder.comment("Attributes.MOVEMENT_SPEED base value.")
                        .defineInRange("movementSpeed", attributeDefaults[0], 0.05, 2.0);
        ForgeConfigSpec.DoubleValue attackDamageSpec =
                builder.comment("Attributes.ATTACK_DAMAGE base value.")
                        .defineInRange("attackDamage", attackDamage, 0.0, 1000.0);
        ForgeConfigSpec.DoubleValue followRange =
                builder.comment("Attributes.FOLLOW_RANGE base value.")
                        .defineInRange("followRange", attributeDefaults[1], 4.0, 256.0);
        ForgeConfigSpec.DoubleValue meleeSpeed =
                builder.comment("MeleeAttackGoal movement speed multiplier.")
                        .defineInRange("meleeSpeed", attributeDefaults[2], 0.5, 2.0);
        ForgeConfigSpec.DoubleValue gunRadius =
                builder.comment("Gun ranged goal attack radius in blocks.")
                        .defineInRange("gunRadius", attributeDefaults[3], 4.0, 128.0);
        ForgeConfigSpec.DoubleValue gunMeleeFrac =
                builder.comment(
                                "Base fraction of gunRadius treated as melee/hip-fire band (x). "
                                        + "Final frac is clamp((x + gunMeleeFracScale.offsetA) * gunMeleeFracScale.scaleB, 0.05, 1).")
                        .defineInRange("gunMeleeFrac", attributeDefaults[4], 0.05, 1.0);
        ForgeConfigSpec.IntValue seeTimeToStopMove =
                builder.comment("Gun goal: ticks with line-of-sight before stopping navigation to strafe.")
                        .defineInRange("seeTimeToStopMove", (int) attributeDefaults[5], 0, 100);
        ForgeConfigSpec.DoubleValue gunStrafeOuterRingFrac =
                builder.comment("Gun goal strafe: outer ring as fraction of attackRadiusSqr.")
                        .defineInRange("gunStrafeOuterRingFrac", attributeDefaults[6], 0.0, 1.0);
        ForgeConfigSpec.DoubleValue gunStrafeInnerRingFrac =
                builder.comment("Gun goal strafe: inner ring as fraction of attackRadiusSqr.")
                        .defineInRange("gunStrafeInnerRingFrac", attributeDefaults[7], 0.0, 1.0);
        ForgeConfigSpec.IntValue gunAttackIntervalMin =
                builder.comment(
                                "Gun goal: base ticks between mid/long-range shots (x). "
                                        + "Final interval is floor((x + gunBurstIntervalScale.offsetA) * gunBurstIntervalScale.scaleB).")
                        .defineInRange("gunAttackIntervalMin", (int) attributeDefaults[8], 0, 200);
        ForgeConfigSpec.IntValue seeTimeToShoot =
                builder.comment("Gun goal: minimum seeTime ticks before mid/long-range shot.")
                        .defineInRange("seeTimeToShoot", (int) attributeDefaults[9], 0, 100);
        ForgeConfigSpec.DoubleValue gunGoalSpeedModifierSpec =
                builder.comment("MobGunRangedPassthroughAttackGoal movement speed multiplier.")
                        .defineInRange("gunGoalSpeedModifier", gunGoalSpeedModifier, 0.5, 2.0);
        ForgeConfigSpec.IntValue removeWhenFarAway = null;
        if (removeWhenFarAwayDefault != null) {
            removeWhenFarAway =
                    builder.comment("0 = false, 1 = true for removeWhenFarAway (vanilla despawn when far).")
                            .defineInRange("removeWhenFarAway", removeWhenFarAwayDefault, 0, 1);
        }
        ForgeConfigSpec.DoubleValue equipmentDropChance = null;
        if (equipmentDropChanceDefault != null) {
            equipmentDropChance =
                    builder.comment("Death drop chance (0.0-1.0) for main/off hand and each armor piece.")
                            .defineInRange("equipmentDropChance", equipmentDropChanceDefault, 0.0, 1.0);
        }
        if (beforeCombatGoals != null) {
            beforeCombatGoals.run();
        }
        CombatGoalConfigEntry combatGoals;
        if (combatGoalSections.isEmpty()) {
            combatGoals = CombatGoalConfigEntry.empty();
        } else {
            builder.push("combatGoals");
            builder.comment(
                    "Long/short sneak, combat crawl, and jump goal parameters. Enabled fields: 0/1. "
                            + "Only goals this unit registers are listed.");
            combatGoals = defineCombatGoals(builder, combatGoalDefaults, combatGoalSections);
            builder.pop();
        }
        if (afterCombatGoals != null) {
            afterCombatGoals.run();
        }
        builder.pop();
        return new SpecEntry(
                maxHealthSpec,
                movementSpeed,
                attackDamageSpec,
                followRange,
                meleeSpeed,
                gunRadius,
                gunMeleeFrac,
                seeTimeToStopMove,
                gunStrafeOuterRingFrac,
                gunStrafeInnerRingFrac,
                gunAttackIntervalMin,
                seeTimeToShoot,
                gunGoalSpeedModifierSpec,
                combatGoals,
                removeWhenFarAway,
                equipmentDropChance);
    }

    public static CombatGoalConfigEntry defineCombatGoals(
            ForgeConfigSpec.Builder builder, double[] defaults) {
        return defineCombatGoals(builder, defaults, CombatGoalSection.all());
    }

    public static CombatGoalConfigEntry defineCombatGoals(
            ForgeConfigSpec.Builder builder,
            double[] defaults,
            EnumSet<CombatGoalSection> sections) {
        if (defaults.length != COMBAT_GOAL_PARAM_COUNT) {
            throw new IllegalArgumentException(
                    "combat goal defaults must have " + COMBAT_GOAL_PARAM_COUNT + " values");
        }
        ForgeConfigSpec.IntValue longSneakTimeDelay = null;
        ForgeConfigSpec.IntValue longSneakTime = null;
        ForgeConfigSpec.IntValue longSneakRandomTime = null;
        ForgeConfigSpec.DoubleValue longSneakDistance = null;
        ForgeConfigSpec.IntValue longSneakEnabled = null;
        ForgeConfigSpec.IntValue shortSneakTimeDelay = null;
        ForgeConfigSpec.IntValue shortSneakTime = null;
        ForgeConfigSpec.IntValue shortSneakRandomTime = null;
        ForgeConfigSpec.DoubleValue shortSneakDistance = null;
        ForgeConfigSpec.IntValue shortSneakEnabled = null;
        ForgeConfigSpec.IntValue crawlRandomDenominator = null;
        ForgeConfigSpec.IntValue crawlExitRandomDenominator = null;
        ForgeConfigSpec.DoubleValue crawlMaxHealthExclusive = null;
        ForgeConfigSpec.DoubleValue crawlMinPlanarDistance = null;
        ForgeConfigSpec.DoubleValue crawlMinHeightToDistanceRatio = null;
        ForgeConfigSpec.IntValue crawlEnabled = null;
        ForgeConfigSpec.IntValue jumpChance = null;
        ForgeConfigSpec.IntValue jumpEnabled = null;
        ForgeConfigSpec.DoubleValue jumpMaxDistance = null;

        if (sections.contains(CombatGoalSection.SNEAK)) {
            longSneakTimeDelay =
                    builder.comment("Long-distance sneak: ticks between sneak attempts when target is far.")
                            .defineInRange("longSneakTimeDelay", (int) defaults[0], 0, 1000);
            longSneakTime =
                    builder.comment("Long-distance sneak: minimum sneak duration in ticks.")
                            .defineInRange("longSneakTime", (int) defaults[1], 0, 1000);
            longSneakRandomTime =
                    builder.comment("Long-distance sneak: extra random sneak duration in ticks (0..n-1).")
                            .defineInRange("longSneakRandomTime", (int) defaults[2], 0, 1000);
            longSneakDistance =
                    builder.comment("Long-distance sneak: minimum target distance in blocks (not squared).")
                            .defineInRange("longSneakDistance", defaults[3], 0.0, 256.0);
            longSneakEnabled =
                    builder.comment("Long-distance sneak goal enabled (0/1).")
                            .defineInRange("longSneakEnabled", (int) defaults[4], 0, 1);
            shortSneakTimeDelay =
                    builder.comment("Short-distance sneak: ticks between sneak attempts when target is near.")
                            .defineInRange("shortSneakTimeDelay", (int) defaults[5], 0, 1000);
            shortSneakTime =
                    builder.comment("Short-distance sneak: minimum sneak duration in ticks.")
                            .defineInRange("shortSneakTime", (int) defaults[6], 0, 1000);
            shortSneakRandomTime =
                    builder.comment("Short-distance sneak: extra random sneak duration in ticks (0..n-1).")
                            .defineInRange("shortSneakRandomTime", (int) defaults[7], 0, 1000);
            shortSneakDistance =
                    builder.comment("Short-distance sneak: maximum target distance in blocks (not squared).")
                            .defineInRange("shortSneakDistance", defaults[8], 0.0, 256.0);
            shortSneakEnabled =
                    builder.comment("Short-distance sneak goal enabled (0/1).")
                            .defineInRange("shortSneakEnabled", (int) defaults[9], 0, 1);
        }
        if (sections.contains(CombatGoalSection.CRAWL)) {
            crawlRandomDenominator =
                    builder.comment("Combat crawl: 1/N chance per tick to start crawling when not crawling.")
                            .defineInRange("crawlRandomDenominator", (int) defaults[10], 1, 1000);
            crawlExitRandomDenominator =
                    builder.comment("Combat crawl: 1/N chance per tick to stop crawling while crawling.")
                            .defineInRange("crawlExitRandomDenominator", (int) defaults[11], 1, 1000);
            crawlMaxHealthExclusive =
                    builder.comment("Combat crawl: max health in hearts (exclusive) to allow crawling.")
                            .defineInRange("crawlMaxHealthExclusive", defaults[12], 0.0, 1000.0);
            crawlMinPlanarDistance =
                    builder.comment("Combat crawl: minimum planar distance to target in blocks.")
                            .defineInRange("crawlMinPlanarDistance", defaults[13], 0.0, 256.0);
            crawlMinHeightToDistanceRatio =
                    builder.comment("Combat crawl: minimum target height-over-distance ratio.")
                            .defineInRange("crawlMinHeightToDistanceRatio", defaults[14], 0.0, 100.0);
            crawlEnabled =
                    builder.comment("Combat crawl goal enabled (0/1).")
                            .defineInRange("crawlEnabled", (int) defaults[15], 0, 1);
        }
        if (sections.contains(CombatGoalSection.JUMP)) {
            jumpChance =
                    builder.comment("Random jump: 1/N chance per tick when target exists.")
                            .defineInRange("jumpChance", (int) defaults[16], 1, 1000);
            jumpEnabled =
                    builder.comment("Random jump goal enabled (0/1).")
                            .defineInRange("jumpEnabled", (int) defaults[17], 0, 1);
            jumpMaxDistance =
                    builder.comment("Random jump: planar distance to target must be less than this (blocks).")
                            .defineInRange("jumpMaxDistance", defaults[18], 0.0, 256.0);
        }
        return new CombatGoalConfigEntry(
                longSneakTimeDelay,
                longSneakTime,
                longSneakRandomTime,
                longSneakDistance,
                longSneakEnabled,
                shortSneakTimeDelay,
                shortSneakTime,
                shortSneakRandomTime,
                shortSneakDistance,
                shortSneakEnabled,
                crawlRandomDenominator,
                crawlExitRandomDenominator,
                crawlMaxHealthExclusive,
                crawlMinPlanarDistance,
                crawlMinHeightToDistanceRatio,
                crawlEnabled,
                jumpChance,
                jumpEnabled,
                jumpMaxDistance);
    }

    public static Config.NpcAttributeStats defaultAttributeStats(
            double maxHealth,
            double attackDamage,
            double gunGoalSpeedModifier,
            double[] attributeDefaults) {
        return new Config.NpcAttributeStats(
                maxHealth,
                attackDamage,
                gunGoalSpeedModifier,
                attributeDefaults[0],
                attributeDefaults[1],
                attributeDefaults[2],
                attributeDefaults[3],
                attributeDefaults[4],
                (int) attributeDefaults[5],
                attributeDefaults[6],
                attributeDefaults[7],
                (int) attributeDefaults[8],
                (int) attributeDefaults[9]);
    }

    public static Config.CombatGoalStats defaultCombatGoalStats(double[] defaults) {
        return new Config.CombatGoalStats(
                (int) defaults[0],
                (int) defaults[1],
                (int) defaults[2],
                defaults[3],
                defaults[4] != 0,
                (int) defaults[5],
                (int) defaults[6],
                (int) defaults[7],
                defaults[8],
                defaults[9] != 0,
                (int) defaults[10],
                (int) defaults[11],
                (float) defaults[12],
                defaults[13],
                defaults[14],
                defaults[15] != 0,
                (int) defaults[16],
                defaults[17] != 0,
                defaults[18]);
    }

    public static void copyIntoAttributeStats(SpecEntry entry, Config.NpcAttributeStats stats) {
        stats.maxHealth = entry.maxHealth().get();
        stats.attackDamage = entry.attackDamage().get();
        stats.gunGoalSpeedModifier = entry.gunGoalSpeedModifier().get();
        stats.movementSpeed = entry.movementSpeed().get();
        stats.followRange = entry.followRange().get();
        stats.meleeSpeed = entry.meleeSpeed().get();
        stats.gunRadius = entry.gunRadius().get();
        stats.gunMeleeFrac = entry.gunMeleeFrac().get();
        stats.seeTimeToStopMove = entry.seeTimeToStopMove().get();
        stats.gunStrafeOuterRingFrac = entry.gunStrafeOuterRingFrac().get();
        stats.gunStrafeInnerRingFrac = entry.gunStrafeInnerRingFrac().get();
        stats.gunAttackIntervalMin = entry.gunAttackIntervalMin().get();
        stats.seeTimeToShoot = entry.seeTimeToShoot().get();
    }

    public static void copyIntoCombatGoalStats(CombatGoalConfigEntry entry, Config.CombatGoalStats stats) {
        if (entry.longSneakTimeDelay() != null) {
            stats.longSneakTimeDelay = entry.longSneakTimeDelay().get();
        }
        if (entry.longSneakTime() != null) {
            stats.longSneakTime = entry.longSneakTime().get();
        }
        if (entry.longSneakRandomTime() != null) {
            stats.longSneakRandomTime = entry.longSneakRandomTime().get();
        }
        if (entry.longSneakDistance() != null) {
            stats.longSneakDistance = entry.longSneakDistance().get();
        }
        if (entry.longSneakEnabled() != null) {
            stats.longSneakEnabled = entry.longSneakEnabled().get() != 0;
        }
        if (entry.shortSneakTimeDelay() != null) {
            stats.shortSneakTimeDelay = entry.shortSneakTimeDelay().get();
        }
        if (entry.shortSneakTime() != null) {
            stats.shortSneakTime = entry.shortSneakTime().get();
        }
        if (entry.shortSneakRandomTime() != null) {
            stats.shortSneakRandomTime = entry.shortSneakRandomTime().get();
        }
        if (entry.shortSneakDistance() != null) {
            stats.shortSneakDistance = entry.shortSneakDistance().get();
        }
        if (entry.shortSneakEnabled() != null) {
            stats.shortSneakEnabled = entry.shortSneakEnabled().get() != 0;
        }
        if (entry.crawlRandomDenominator() != null) {
            stats.crawlRandomDenominator = entry.crawlRandomDenominator().get();
        }
        if (entry.crawlExitRandomDenominator() != null) {
            stats.crawlExitRandomDenominator = entry.crawlExitRandomDenominator().get();
        }
        if (entry.crawlMaxHealthExclusive() != null) {
            stats.crawlMaxHealthExclusive = entry.crawlMaxHealthExclusive().get().floatValue();
        }
        if (entry.crawlMinPlanarDistance() != null) {
            stats.crawlMinPlanarDistance = entry.crawlMinPlanarDistance().get();
        }
        if (entry.crawlMinHeightToDistanceRatio() != null) {
            stats.crawlMinHeightToDistanceRatio = entry.crawlMinHeightToDistanceRatio().get();
        }
        if (entry.crawlEnabled() != null) {
            stats.crawlEnabled = entry.crawlEnabled().get() != 0;
        }
        if (entry.jumpChance() != null) {
            stats.jumpChance = entry.jumpChance().get();
        }
        if (entry.jumpEnabled() != null) {
            stats.jumpEnabled = entry.jumpEnabled().get() != 0;
        }
        if (entry.jumpMaxDistance() != null) {
            stats.jumpMaxDistance = entry.jumpMaxDistance().get();
        }
    }
}
