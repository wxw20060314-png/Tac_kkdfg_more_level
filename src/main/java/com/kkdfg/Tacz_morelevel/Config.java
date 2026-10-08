package com.kkdfg.Tacz_morelevel;

import com.kkdfg.Tacz_morelevel.config.ArmorConfig;
import com.kkdfg.Tacz_morelevel.config.BossConfig;
import com.kkdfg.Tacz_morelevel.config.CombatUnitConfig;
import com.kkdfg.Tacz_morelevel.config.NpcConfig;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczCompatForgeEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * 模组公共配置（生成 {@code config/tacz_morelevel-common.toml}）。
 * NPC/Boss/盔甲见 {@code npc.toml} / {@code bosses.toml} / {@code armor.toml}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue EQUIPMENT_DROP_CHANCE = BUILDER
            .comment(
                    "Chance (0.0-1.0) that each equipment slot drops on death for player-like soldier mobs:",
                    "main hand (TaCZ gun), offhand, and each armor piece. Same value as vanilla-style Mob#setDropChance.")
            .defineInRange("equipmentDropChance", 0.04, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue MOLOTOV_FIRE_DAMAGE = BUILDER
            .comment("Damage dealt by molotov fire blocks each time they apply in-fire damage (see MolotovFireBlock).")
            .defineInRange("molotovFireDamage", 10.0, 0.0, 1000.0);

    private static final ForgeConfigSpec.DoubleValue LANDMINE_EXPLOSION_POWER = BUILDER
            .comment("Explosion power when a landmine is triggered (Level#explode radius-like parameter).")
            .defineInRange("landmineExplosionPower", 9.0, 0.0, 100.0);

    private static final ForgeConfigSpec.DoubleValue GRENADE_EXPLOSION_POWER = BUILDER
            .comment("Explosion power for thrown grenades (Level#explode).")
            .defineInRange("grenadeExplosionPower", 8.0, 0.0, 100.0);

    private static final ForgeConfigSpec.DoubleValue GRENADE_FUSE_HUD_Y_FRACTION = BUILDER
            .comment(
                    "Client only: grenade priming HUD vertical position, fraction of screen height from top",
                    "(see GrenadeFuseHudOverlay).")
            .defineInRange("grenadeFuseHudYFractionFromTop", 0.10, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue GRENADE_FUSE_HUD_ALPHA = BUILDER
            .comment(
                    "Client only: grenade priming HUD overall opacity (0.0 = invisible, 1.0 = opaque;",
                    "0.5 = 50% transparent).")
            .defineInRange("grenadeFuseHudAlpha", 0.5, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue DETECTION_GRENADE_HORIZONTAL_RADIUS;
    private static final ForgeConfigSpec.DoubleValue DETECTION_GRENADE_VERTICAL_RADIUS;
    private static final ForgeConfigSpec.IntValue DETECTION_GRENADE_GLOWING_DURATION_TICKS;
    private static final ForgeConfigSpec.DoubleValue DETECTION_GRENADE_SLOW_HORIZONTAL_RADIUS;
    private static final ForgeConfigSpec.DoubleValue DETECTION_GRENADE_SLOW_VERTICAL_RADIUS;
    private static final ForgeConfigSpec.IntValue DETECTION_GRENADE_SLOW_DURATION_TICKS;

    private static final ForgeConfigSpec.DoubleValue FLASHBANG_RADIUS;
    private static final ForgeConfigSpec.DoubleValue FLASHBANG_BASE_DURATION_SECONDS;
    private static final ForgeConfigSpec.DoubleValue FLASHBANG_POINT_BLANK_FACTOR;

    static {
        BUILDER.push("detectionGrenade");
        BUILDER.comment("Detection grenade scan and slow effect (DetectionGrenadeEntity).");
        DETECTION_GRENADE_HORIZONTAL_RADIUS =
                BUILDER.comment("Horizontal half-width of glowing scan AABB (blocks).")
                        .defineInRange("horizontalRadius", 20.0, 0.0, 128.0);
        DETECTION_GRENADE_VERTICAL_RADIUS =
                BUILDER.comment("Vertical half-height of glowing scan AABB (blocks).")
                        .defineInRange("verticalRadius", 20.0, 0.0, 128.0);
        DETECTION_GRENADE_GLOWING_DURATION_TICKS =
                BUILDER.comment("Glowing effect duration in ticks (20 ticks = 1 second).")
                        .defineInRange("glowingDurationTicks", 100, 1, 72000);
        DETECTION_GRENADE_SLOW_HORIZONTAL_RADIUS =
                BUILDER.comment("Horizontal half-width of slowness AABB (blocks).")
                        .defineInRange("slowHorizontalRadius", 5.0, 0.0, 128.0);
        DETECTION_GRENADE_SLOW_VERTICAL_RADIUS =
                BUILDER.comment("Vertical half-height of slowness AABB (blocks).")
                        .defineInRange("slowVerticalRadius", 5.0, 0.0, 128.0);
        DETECTION_GRENADE_SLOW_DURATION_TICKS =
                BUILDER.comment("Slowness effect duration in ticks (20 ticks = 1 second).")
                        .defineInRange("slowDurationTicks", 100, 1, 72000);
        BUILDER.pop();

        BUILDER.push("flashbang");
        BUILDER.comment("Flashbang flash radius and full-flash duration (FlashbangEntity).");
        FLASHBANG_RADIUS =
                BUILDER.comment("Flash effect radius in blocks (LOS + facing falloff apply inside).")
                        .defineInRange("radius", 32.0, 0.0, 128.0);
        FLASHBANG_BASE_DURATION_SECONDS =
                BUILDER.comment(
                                "Full-flash (point-blank, facing the blast) effect duration in seconds;",
                                "scaled by facing * distance factor (20 ticks = 1 second).")
                        .defineInRange("baseDurationSeconds", 7.0, 0.05, 3600.0);
        FLASHBANG_POINT_BLANK_FACTOR =
                BUILDER.comment(
                                "Within radius * t, targets are fully flashed (flash=1) if LOS ok;",
                                "facing/distance falloff skipped. t default 0.2.")
                        .defineInRange("pointBlankFactor", 0.2, 0.0, 1.0);
        BUILDER.pop();
    }

    private static final ForgeConfigSpec.DoubleValue PRESENT_TO_DROP_CONFIG = BUILDER
            .comment(
                    "Multiplier for death magazine-ammo percentages (see TaczCompatForgeEvents);",
                    "written to TaczCompatForgeEvents.PRESENT_TO_DROP when this file loads.")
            .defineInRange("presentToDrop", 1.0, 0.0, 10.0);

    private static final ForgeConfigSpec.DoubleValue NPC_OUTGOING_DAMAGE_TO_PLAYER_SCALE = BUILDER
            .comment(
                    "Multiplier for damage from this mod's NPCs to players (LivingHurtEvent,",
                    "source.getEntity() is player-like soldier / neutral gun NPC / slime experiment / companion).",
                    "1.0 = unchanged.")
            .defineInRange("npcOutgoingDamageToPlayerScale", 1.0, 0.01, 100.0);

    private static final SoldierSpawnArmorProfile[] NPC_ATTRIBUTE_PROFILE_ORDER = {
        SoldierSpawnArmorProfile.REFUGEE,
        SoldierSpawnArmorProfile.MILITIA,
        SoldierSpawnArmorProfile.MERCENARY,
        SoldierSpawnArmorProfile.ASSAULT,
        SoldierSpawnArmorProfile.SPECIAL_FORCES,
        SoldierSpawnArmorProfile.SUPER_SPECIAL
    };

    private static final BodyguardProfile[] BODYGUARD_ATTRIBUTE_PROFILE_ORDER = {
        BodyguardProfile.BEAUTY,
        BodyguardProfile.STANDARD,
        BodyguardProfile.ELITE,
        BodyguardProfile.DEMON_LORD
    };

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static final boolean[] removeWhenFarAwayByProfile = new boolean[SoldierSpawnArmorProfile.values().length];

    public static final NpcAttributeStats[] npcAttributesByProfile =
            new NpcAttributeStats[SoldierSpawnArmorProfile.values().length];

    public static final NpcAttributeStats[] bodyguardAttributesByProfile =
            new NpcAttributeStats[BodyguardProfile.values().length];

    public static final CombatGoalStats[] npcCombatGoalsByProfile =
            new CombatGoalStats[SoldierSpawnArmorProfile.values().length];

    public static final CombatGoalStats[] bodyguardCombatGoalsByProfile =
            new CombatGoalStats[BodyguardProfile.values().length];

    public static NpcAttributeStats mysteriousMerchantAttributes;
    public static CombatGoalStats mysteriousMerchantCombatGoals;

    public static double mysteriousMerchantMaxHealth = 20.0D;
    public static double mysteriousMerchantAttackDamage = 2.0D;
    public static double mysteriousMerchantGunGoalSpeedModifier = 1.08D;
    public static double mysteriousMerchantWanderStrollSpeed = 0.92D;
    public static boolean mysteriousMerchantPassiveRegenerationEnabled = true;
    public static int mysteriousMerchantPassiveRegenerationAmplifier = 0;
    public static boolean mysteriousMerchantPassiveDolphinsGraceEnabled = true;
    public static int mysteriousMerchantPassiveDolphinsGraceAmplifier = 1;

    public static NpcAttributeStats annihilatorAttributes;
    public static CombatGoalStats annihilatorCombatGoals;
    public static double annihilatorMaxHealth = 20.0D;
    public static double annihilatorAttackDamage = 2.0D;
    public static double annihilatorGunGoalSpeedModifier = 1.2D;

    /** {@code [punisher]}：判罚者；默认数值同超特。 */
    public static NpcAttributeStats punisherAttributes;
    public static CombatGoalStats punisherCombatGoals;
    public static double punisherMaxHealth = 20.0D;
    public static double punisherAttackDamage = 2.0D;
    public static double punisherGunGoalSpeedModifier = 1.2D;

    /** {@code [scorpion]}：毒蝎（枪套 M107 卓越）；交火 100 / 近战带 20 / 侧移外 80 内 40。 */
    public static NpcAttributeStats scorpionAttributes;
    public static CombatGoalStats scorpionCombatGoals;
    public static double scorpionMaxHealth = 20.0D;
    public static double scorpionAttackDamage = 2.0D;
    public static double scorpionGunGoalSpeedModifier = 1.2D;

    /** {@code [slimeCommander]}：史莱姆指挥官走位/枪 Goal；血量由代码 {@code SPLIT_TABLE} 管，不读配置 maxHealth。 */
    public static NpcAttributeStats slimeCommanderAttributes;
    public static CombatGoalStats slimeCommanderCombatGoals;
    public static double slimeCommanderAttackDamage = 2.0D;
    public static double slimeCommanderGunGoalSpeedModifier = 1.2D;

    public static NpcAttributeStats marshalGuardAttributes;
    public static CombatGoalStats marshalGuardCombatGoals;
    public static double marshalGuardMaxHealth = 20.0D;
    public static double marshalGuardAttackDamage = 2.0D;
    public static double marshalGuardGunGoalSpeedModifier = 1.2D;
    public static float marshalGuardEquipmentDropChance = 0.0f;

    /** {@code [lurker]}：潜伏者；默认数值同突击兵，{@code removeWhenFarAway} 默认 false。 */
    public static NpcAttributeStats lurkerAttributes;
    public static CombatGoalStats lurkerCombatGoals;
    public static double lurkerMaxHealth = 20.0D;
    public static double lurkerAttackDamage = 2.0D;
    public static double lurkerGunGoalSpeedModifier = 1.1D;
    public static boolean lurkerRemoveWhenFarAway = false;

    public static NpcAttributeStats warMachineAttributes;
    public static CombatGoalStats warMachineCombatGoals;
    public static double warMachineMaxHealth = 30.0D;
    public static double warMachineAttackDamage = 2.0D;
    public static double warMachineGunGoalSpeedModifier = 1.0D;

    public static NpcAttributeStats bombBabyAttributes;
    public static CombatGoalStats bombBabyCombatGoals;
    public static double bombBabyMaxHealth = 20.0D;
    public static double bombBabyAttackDamage = 2.0D;
    public static double bombBabyGunGoalSpeedModifier = 1.2D;

    public static NpcAttributeStats ceilingDuelAttributes;
    public static CombatGoalStats ceilingDuelCombatGoals;
    public static double ceilingDuelMaxHealth = 20.0D;
    public static double ceilingDuelAttackDamage = 2.0D;
    public static double ceilingDuelGunGoalSpeedModifier = 1.2D;

    public static NpcAttributeStats levMarshalAttributes;
    public static CombatGoalStats levMarshalCombatGoals;
    public static double levMarshalMaxHealth = 20.0D;
    public static double levMarshalAttackDamage = 2.0D;
    public static double levMarshalGunGoalSpeedModifier = 1.08D;

    public static NpcAttributeStats bombSightAttributes;
    public static CombatGoalStats bombSightCombatGoals;
    public static double bombSightMaxHealth = 25.0D;
    public static double bombSightAttackDamage = 2.0D;
    public static double bombSightGunGoalSpeedModifier = 1.2D;
    /** Passive DAMAGE_RESISTANCE amplifier; default 2 = Resistance III. */
    public static int bombSightResistanceAmplifier = 2;

    /**
     * NPC 全局点射间隔映射：{@code floor((x + offsetA) * scaleB)}，默认 offsetA=0、scaleB=1。
     * 见 {@code npc.toml} {@code [gunBurstIntervalScale]}。
     */
    public static double npcGunBurstIntervalOffsetA = 0.0D;
    public static double npcGunBurstIntervalScaleB = 1.0D;

    /**
     * Boss 全局点射间隔映射（凡在 {@link BossConfig} 注册的单位）。
     * 见 {@code bosses.toml} {@code [gunBurstIntervalScale]}。
     */
    public static double bossGunBurstIntervalOffsetA = 0.0D;
    public static double bossGunBurstIntervalScaleB = 1.0D;

    /**
     * NPC 全局腰射带比例映射：{@code clamp((x + offsetA) * scaleB, 0.05, 1)}。
     * 见 {@code npc.toml} {@code [gunMeleeFracScale]}。
     */
    public static double npcGunMeleeFracOffsetA = 0.0D;
    public static double npcGunMeleeFracScaleB = 1.0D;

    /**
     * Boss 全局腰射带比例映射。
     * 见 {@code bosses.toml} {@code [gunMeleeFracScale]}。
     */
    public static double bossGunMeleeFracOffsetA = 0.0D;
    public static double bossGunMeleeFracScaleB = 1.0D;

    /** {@code [slimeWarrior]}：史莱姆战士走位/枪 Goal；血量由分裂表管。 */
    public static NpcAttributeStats slimeWarriorAttributes;
    public static CombatGoalStats slimeWarriorCombatGoals;
    public static double slimeWarriorAttackDamage = 2.0D;
    public static double slimeWarriorGunGoalSpeedModifier = 1.15D;

    /** NPC 点射间隔：{@code floor((x + a) * b)}，下限 0。 */
    public static int mapNpcGunBurstInterval(int x) {
        return mapGunBurstInterval(x, npcGunBurstIntervalOffsetA, npcGunBurstIntervalScaleB);
    }

    /** Boss 点射间隔：{@code floor((x + a) * b)}，下限 0。 */
    public static int mapBossGunBurstInterval(int x) {
        return mapGunBurstInterval(x, bossGunBurstIntervalOffsetA, bossGunBurstIntervalScaleB);
    }

    private static int mapGunBurstInterval(int x, double offsetA, double scaleB) {
        return Math.max(0, (int) Math.floor((x + offsetA) * scaleB));
    }

    /** NPC 腰射带：{@code clamp((x + a) * b, 0.05, 1)}。 */
    public static double mapNpcGunMeleeFrac(double x) {
        return mapGunMeleeFrac(x, npcGunMeleeFracOffsetA, npcGunMeleeFracScaleB);
    }

    /** Boss 腰射带：{@code clamp((x + a) * b, 0.05, 1)}。 */
    public static double mapBossGunMeleeFrac(double x) {
        return mapGunMeleeFrac(x, bossGunMeleeFracOffsetA, bossGunMeleeFracScaleB);
    }

    private static double mapGunMeleeFrac(double x, double offsetA, double scaleB) {
        double v = (x + offsetA) * scaleB;
        if (v < 0.05D) {
            return 0.05D;
        }
        if (v > 1.0D) {
            return 1.0D;
        }
        return v;
    }

    /** 将单位 {@code gunMeleeFrac} 套上 NPC 全局 a/b（加载配置后调用）。 */
    public static void applyNpcGunMeleeFracScale(NpcAttributeStats stats) {
        if (stats != null) {
            stats.gunMeleeFrac = mapNpcGunMeleeFrac(stats.gunMeleeFrac);
        }
    }

    /** 将单位 {@code gunMeleeFrac} 套上 Boss 全局 a/b（加载配置后调用）。 */
    public static void applyBossGunMeleeFracScale(NpcAttributeStats stats) {
        if (stats != null) {
            stats.gunMeleeFrac = mapBossGunMeleeFrac(stats.gunMeleeFrac);
        }
    }

    public static final class CombatGoalStats {
        public int longSneakTimeDelay;
        public int longSneakTime;
        public int longSneakRandomTime;
        public double longSneakDistance;
        public boolean longSneakEnabled;
        public int shortSneakTimeDelay;
        public int shortSneakTime;
        public int shortSneakRandomTime;
        public double shortSneakDistance;
        public boolean shortSneakEnabled;
        public int crawlRandomDenominator;
        public int crawlExitRandomDenominator;
        public float crawlMaxHealthExclusive;
        public double crawlMinPlanarDistance;
        public double crawlMinHeightToDistanceRatio;
        public boolean crawlEnabled;
        public int jumpChance;
        public boolean jumpEnabled;
        public double jumpMaxDistance;

        public CombatGoalStats(
                int longSneakTimeDelay,
                int longSneakTime,
                int longSneakRandomTime,
                double longSneakDistance,
                boolean longSneakEnabled,
                int shortSneakTimeDelay,
                int shortSneakTime,
                int shortSneakRandomTime,
                double shortSneakDistance,
                boolean shortSneakEnabled,
                int crawlRandomDenominator,
                int crawlExitRandomDenominator,
                float crawlMaxHealthExclusive,
                double crawlMinPlanarDistance,
                double crawlMinHeightToDistanceRatio,
                boolean crawlEnabled,
                int jumpChance,
                boolean jumpEnabled,
                double jumpMaxDistance) {
            this.longSneakTimeDelay = longSneakTimeDelay;
            this.longSneakTime = longSneakTime;
            this.longSneakRandomTime = longSneakRandomTime;
            this.longSneakDistance = longSneakDistance;
            this.longSneakEnabled = longSneakEnabled;
            this.shortSneakTimeDelay = shortSneakTimeDelay;
            this.shortSneakTime = shortSneakTime;
            this.shortSneakRandomTime = shortSneakRandomTime;
            this.shortSneakDistance = shortSneakDistance;
            this.shortSneakEnabled = shortSneakEnabled;
            this.crawlRandomDenominator = crawlRandomDenominator;
            this.crawlExitRandomDenominator = crawlExitRandomDenominator;
            this.crawlMaxHealthExclusive = crawlMaxHealthExclusive;
            this.crawlMinPlanarDistance = crawlMinPlanarDistance;
            this.crawlMinHeightToDistanceRatio = crawlMinHeightToDistanceRatio;
            this.crawlEnabled = crawlEnabled;
            this.jumpChance = jumpChance;
            this.jumpEnabled = jumpEnabled;
            this.jumpMaxDistance = jumpMaxDistance;
        }
    }

    public static final class NpcAttributeStats {
        public double maxHealth;
        public double attackDamage;
        public double gunGoalSpeedModifier;
        public double movementSpeed;
        public double followRange;
        public double meleeSpeed;
        public double gunRadius;
        public double gunMeleeFrac;
        public int seeTimeToStopMove;
        public double gunStrafeOuterRingFrac;
        public double gunStrafeInnerRingFrac;
        public int gunAttackIntervalMin;
        public int seeTimeToShoot;

        public NpcAttributeStats(
                double maxHealth,
                double attackDamage,
                double gunGoalSpeedModifier,
                double movementSpeed,
                double followRange,
                double meleeSpeed,
                double gunRadius,
                double gunMeleeFrac,
                int seeTimeToStopMove,
                double gunStrafeOuterRingFrac,
                double gunStrafeInnerRingFrac,
                int gunAttackIntervalMin,
                int seeTimeToShoot) {
            this.maxHealth = maxHealth;
            this.attackDamage = attackDamage;
            this.gunGoalSpeedModifier = gunGoalSpeedModifier;
            this.movementSpeed = movementSpeed;
            this.followRange = followRange;
            this.meleeSpeed = meleeSpeed;
            this.gunRadius = gunRadius;
            this.gunMeleeFrac = gunMeleeFrac;
            this.seeTimeToStopMove = seeTimeToStopMove;
            this.gunStrafeOuterRingFrac = gunStrafeOuterRingFrac;
            this.gunStrafeInnerRingFrac = gunStrafeInnerRingFrac;
            this.gunAttackIntervalMin = gunAttackIntervalMin;
            this.seeTimeToShoot = seeTimeToShoot;
        }
    }

    static {
        for (int i = 0; i < NPC_ATTRIBUTE_PROFILE_ORDER.length; i++) {
            SoldierSpawnArmorProfile profile = NPC_ATTRIBUTE_PROFILE_ORDER[i];
            npcAttributesByProfile[profile.ordinal()] =
                    CombatUnitConfig.defaultAttributeStats(
                            CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[i],
                            CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[i],
                            CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[i],
                            CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[i]);
            npcCombatGoalsByProfile[profile.ordinal()] =
                    CombatUnitConfig.defaultCombatGoalStats(CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[i]);
            removeWhenFarAwayByProfile[profile.ordinal()] = CombatUnitConfig.DEFAULT_REMOVE_WHEN_FAR[i] != 0;
        }
        for (int i = 0; i < BODYGUARD_ATTRIBUTE_PROFILE_ORDER.length; i++) {
            BodyguardProfile profile = BODYGUARD_ATTRIBUTE_PROFILE_ORDER[i];
            bodyguardAttributesByProfile[profile.ordinal()] =
                    CombatUnitConfig.defaultAttributeStats(
                            CombatUnitConfig.DEFAULT_BODYGUARD_MAX_HEALTH,
                            CombatUnitConfig.DEFAULT_BODYGUARD_ATTACK_DAMAGE,
                            CombatUnitConfig.DEFAULT_BODYGUARD_GUN_GOAL_SPEED,
                            CombatUnitConfig.DEFAULT_BODYGUARD_ATTRIBUTE_VALUES[i]);
            bodyguardCombatGoalsByProfile[profile.ordinal()] =
                    CombatUnitConfig.defaultCombatGoalStats(
                            CombatUnitConfig.DEFAULT_BODYGUARD_COMBAT_GOAL_VALUES[i]);
        }
        int mercenaryIndex = CombatUnitConfig.MERCENARY_NPC_DEFAULT_INDEX;
        int superSpecialIndex = CombatUnitConfig.SUPER_SPECIAL_NPC_DEFAULT_INDEX;
        mysteriousMerchantAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.08D,
                        CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[mercenaryIndex]);
        mysteriousMerchantCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[mercenaryIndex]);
        annihilatorAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.2D,
                        CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex]);
        annihilatorCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        punisherAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.2D,
                        CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex]);
        punisherCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        scorpionAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.2D,
                        new double[] {0.64D, 108.0D, 1.25D, 100.0D, 0.2D, 3.0D, 0.64D, 0.16D, 0.0D, 2.0D});
        scorpionCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        slimeCommanderAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        300.0D,
                        2.0D,
                        1.2D,
                        CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex]);
        slimeCommanderCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        slimeCommanderAttackDamage = 2.0D;
        slimeCommanderGunGoalSpeedModifier = 1.2D;
        int specialForcesIndex = CombatUnitConfig.SPECIAL_FORCES_NPC_DEFAULT_INDEX;
        double[] slimeWarriorAttrs =
                CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[specialForcesIndex].clone();
        // 与当前点射间隔硬编码 0 对齐（配置可改）
        slimeWarriorAttrs[8] = 0.0D;
        slimeWarriorAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[specialForcesIndex],
                        CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[specialForcesIndex],
                        CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[specialForcesIndex],
                        slimeWarriorAttrs);
        slimeWarriorCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[specialForcesIndex]);
        slimeWarriorAttackDamage = CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[specialForcesIndex];
        slimeWarriorGunGoalSpeedModifier =
                CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[specialForcesIndex];
        marshalGuardAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.2D,
                        new double[] {0.64D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.3D, 0.1D, 2.0D, 2.0D});
        marshalGuardCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        int assaultIndex = CombatUnitConfig.ASSAULT_NPC_DEFAULT_INDEX;
        lurkerAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[assaultIndex],
                        CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[assaultIndex],
                        CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[assaultIndex],
                        CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[assaultIndex]);
        lurkerCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[assaultIndex]);
        lurkerMaxHealth = CombatUnitConfig.DEFAULT_SOLDIER_MAX_HEALTH[assaultIndex];
        lurkerAttackDamage = CombatUnitConfig.DEFAULT_SOLDIER_ATTACK_DAMAGE[assaultIndex];
        lurkerGunGoalSpeedModifier = CombatUnitConfig.DEFAULT_SOLDIER_GUN_GOAL_SPEED[assaultIndex];
        lurkerRemoveWhenFarAway = false;
        warMachineAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        30.0D, 2.0D, 1.0D, new double[] {0.8D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.3D, 0.1D, 0.0D, 2.0D});
        warMachineCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        bombBabyAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.2D,
                        CombatUnitConfig.DEFAULT_NPC_ATTRIBUTE_VALUES[superSpecialIndex]);
        bombBabyCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        ceilingDuelAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.2D,
                        new double[] {0.64D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.3D, 0.1D, 0.0D, 2.0D});
        ceilingDuelCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
        levMarshalAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        20.0D,
                        2.0D,
                        1.08D,
                        new double[] {0.4D, 108.0D, 1.18D, 80.0D, 0.5D, 5.0D, 0.5D, 0.2D, 3.0D, 5.0D});
        levMarshalCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[mercenaryIndex]);
        bombSightAttributes =
                CombatUnitConfig.defaultAttributeStats(
                        25.0D,
                        2.0D,
                        1.2D,
                        new double[] {0.64D, 108.0D, 1.25D, 80.0D, 0.4D, 3.0D, 0.022D, 0.0056D, 0.0D, 2.0D});
        bombSightCombatGoals =
                CombatUnitConfig.defaultCombatGoalStats(
                        CombatUnitConfig.DEFAULT_NPC_COMBAT_GOAL_VALUES[superSpecialIndex]);
    }

    public static boolean removeWhenFarAwayFor(SoldierSpawnArmorProfile profile) {
        return removeWhenFarAwayByProfile[profile.ordinal()];
    }

    public static NpcAttributeStats npcAttributesFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesByProfile[profile.ordinal()];
    }

    public static double maxHealthFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).maxHealth;
    }

    public static double attackDamageFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).attackDamage;
    }

    public static double gunGoalSpeedModifierFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).gunGoalSpeedModifier;
    }

    public static double movementSpeedFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).movementSpeed;
    }

    public static double followRangeFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).followRange;
    }

    public static double meleeSpeedFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).meleeSpeed;
    }

    public static float gunRadiusFor(SoldierSpawnArmorProfile profile) {
        return (float) npcAttributesFor(profile).gunRadius;
    }

    public static float gunMeleeFracFor(SoldierSpawnArmorProfile profile) {
        return (float) npcAttributesFor(profile).gunMeleeFrac;
    }

    public static int seeTimeToStopMoveFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).seeTimeToStopMove;
    }

    public static float gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile profile) {
        return (float) npcAttributesFor(profile).gunStrafeOuterRingFrac;
    }

    public static float gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile profile) {
        return (float) npcAttributesFor(profile).gunStrafeInnerRingFrac;
    }

    public static int gunAttackIntervalMinFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).gunAttackIntervalMin;
    }

    public static int seeTimeToShootFor(SoldierSpawnArmorProfile profile) {
        return npcAttributesFor(profile).seeTimeToShoot;
    }

    public static NpcAttributeStats bodyguardAttributesFor(BodyguardProfile profile) {
        return bodyguardAttributesByProfile[profile.ordinal()];
    }

    public static double bodyguardMaxHealthFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).maxHealth;
    }

    public static double bodyguardAttackDamageFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).attackDamage;
    }

    public static double bodyguardGunGoalSpeedModifierFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).gunGoalSpeedModifier;
    }

    public static double bodyguardMovementSpeedFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).movementSpeed;
    }

    public static double bodyguardFollowRangeFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).followRange;
    }

    public static double bodyguardMeleeSpeedFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).meleeSpeed;
    }

    public static float bodyguardGunRadiusFor(BodyguardProfile profile) {
        return (float) bodyguardAttributesFor(profile).gunRadius;
    }

    public static float bodyguardGunMeleeFracFor(BodyguardProfile profile) {
        return (float) bodyguardAttributesFor(profile).gunMeleeFrac;
    }

    public static int bodyguardSeeTimeToStopMoveFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).seeTimeToStopMove;
    }

    public static float bodyguardGunStrafeOuterRingFracFor(BodyguardProfile profile) {
        return (float) bodyguardAttributesFor(profile).gunStrafeOuterRingFrac;
    }

    public static float bodyguardGunStrafeInnerRingFracFor(BodyguardProfile profile) {
        return (float) bodyguardAttributesFor(profile).gunStrafeInnerRingFrac;
    }

    public static int bodyguardGunAttackIntervalMinFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).gunAttackIntervalMin;
    }

    public static int bodyguardSeeTimeToShootFor(BodyguardProfile profile) {
        return bodyguardAttributesFor(profile).seeTimeToShoot;
    }

    public static CombatGoalStats npcCombatGoalsFor(SoldierSpawnArmorProfile profile) {
        return npcCombatGoalsByProfile[profile.ordinal()];
    }

    public static CombatGoalStats bodyguardCombatGoalsFor(BodyguardProfile profile) {
        return bodyguardCombatGoalsByProfile[profile.ordinal()];
    }

    public static float bodyguardDeathItemDropChance() {
        return bodyguardDeathItemDropChance;
    }

    public static boolean canFightWithotherguard() {
        return canFightWithotherguard;
    }

    public static float equipmentDropChance = 0.04f;
    public static float warMachineEquipmentDropChance = 0.2f;
    public static float annihilatorEquipmentDropChance = 0.2f;
    public static float punisherEquipmentDropChance = 0.2f;
    public static float scorpionEquipmentDropChance = 0.2f;
    public static float bombBabyEquipmentDropChance = 0.2f;
    public static float levMarshalEquipmentDropChance = 0.2f;
    public static float bombSightEquipmentDropChance = 0.2f;
    public static float mysteriousMerchantEquipmentDropChance = 0.2f;
    public static float ceilingDuelEquipmentDropChance = 0.2f;
    public static float ceilingDuelHelmetDropChance = 1.0f;

    public static float molotovFireDamage = 5.0f;
    public static float landmineExplosionPower = 9.0f;
    public static float grenadeExplosionPower = 8.0f;
    public static double detectionGrenadeHorizontalRadius = 20.0;
    public static double detectionGrenadeVerticalRadius = 20.0;
    public static int detectionGrenadeGlowingDurationTicks = 100;
    public static double detectionGrenadeSlowHorizontalRadius = 5.0;
    public static double detectionGrenadeSlowVerticalRadius = 5.0;
    public static int detectionGrenadeSlowDurationTicks = 100;
    public static float grenadeFuseHudYFractionFromTop = 0.10f;
    public static float grenadeFuseHudAlpha = 0.5f;
    /** 闪光弹作用半径（格）；对应 common.toml {@code flashbang.radius}。 */
    public static double flashbangRadius = 32.0;
    /** 闪光弹满闪基础秒数；对应 {@code flashbang.baseDurationSeconds}。 */
    public static double flashbangBaseDurationSeconds = 7.0;
    /** 近点满闪比例 t：距离 ≤ radius*t 时满闪；对应 {@code flashbang.pointBlankFactor}。 */
    public static double flashbangPointBlankFactor = 0.2;
    /** NPC 对玩家伤害倍率；对应 common.toml {@code npcOutgoingDamageToPlayerScale}（0.01–100，默认 1）。 */
    public static float npcOutgoingDamageToPlayerScale = 1.0f;
    public static float bodyguardDeathItemDropChance = 1.0f;
    public static boolean canFightWithotherguard = true;

    /**
     * 缓存 {@code armor.toml} 中 {@code bulletproofArmorTiers.level*}；下标 0 = level 1。
     * 由 {@link ArmorConfig} 填充。
     */
    public static final BulletproofTierStats[] bulletproofTierByLevel =
            new BulletproofTierStats[ArmorConfig.BULLETPROOF_TIER_COUNT];

    /** 缓存 {@code armor.toml} {@code explosionReductionPerPiece.*}（每件爆炸减伤）。 */
    public static float blastExplosionReductionPerPiece = 0.1f;
    public static float blazeflameExplosionReductionPerPiece = 0.2f;
    public static float powerMechExplosionReductionPerPiece = 0.08f;
    public static float specialOpsExplosionReductionPerPiece = 0.08f;
    public static float shockwaveExplosionReductionPerPiece = 0.08f;
    /** 缓存 {@code armor.toml} {@code bulletproof.maxReduction}。 */
    public static float maxBulletproofReduction = 0.98f;
    /** 缓存 {@code armor.toml} {@code superbWarfare.t}：SBW 防弹减伤倍率。 */
    public static float superbWarfareBulletproofScale = 1.0f;

    public static final class BulletproofTierStats {
        public float totalBulletproof;
        public float totalPenReduction;

        public BulletproofTierStats(float totalBulletproof, float totalPenReduction) {
            this.totalBulletproof = totalBulletproof;
            this.totalPenReduction = totalPenReduction;
        }
    }

    static {
        ArmorConfig.initDefaultCaches();
    }

    /** @param level 1–8（与 {@link #bulletproofTierByLevel} 下标 +1 对应） */
    public static BulletproofTierStats bulletproofTierForLevel(int level) {
        int index = Mth.clamp(level, 1, ArmorConfig.BULLETPROOF_TIER_COUNT) - 1;
        return bulletproofTierByLevel[index];
    }

    private Config() {}

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            loadCommonConfig();
        } else if (event.getConfig().getSpec() == NpcConfig.SPEC) {
            NpcConfig.applyLoadedValues();
        } else if (event.getConfig().getSpec() == BossConfig.SPEC) {
            BossConfig.applyLoadedValues();
        } else if (event.getConfig().getSpec() == ArmorConfig.SPEC) {
            ArmorConfig.applyLoadedValues();
        }
    }

    private static void loadCommonConfig() {
        equipmentDropChance = EQUIPMENT_DROP_CHANCE.get().floatValue();
        molotovFireDamage = MOLOTOV_FIRE_DAMAGE.get().floatValue();
        landmineExplosionPower = LANDMINE_EXPLOSION_POWER.get().floatValue();
        grenadeExplosionPower = GRENADE_EXPLOSION_POWER.get().floatValue();
        detectionGrenadeHorizontalRadius = DETECTION_GRENADE_HORIZONTAL_RADIUS.get();
        detectionGrenadeVerticalRadius = DETECTION_GRENADE_VERTICAL_RADIUS.get();
        detectionGrenadeGlowingDurationTicks = DETECTION_GRENADE_GLOWING_DURATION_TICKS.get();
        detectionGrenadeSlowHorizontalRadius = DETECTION_GRENADE_SLOW_HORIZONTAL_RADIUS.get();
        detectionGrenadeSlowVerticalRadius = DETECTION_GRENADE_SLOW_VERTICAL_RADIUS.get();
        detectionGrenadeSlowDurationTicks = DETECTION_GRENADE_SLOW_DURATION_TICKS.get();
        grenadeFuseHudYFractionFromTop = GRENADE_FUSE_HUD_Y_FRACTION.get().floatValue();
        grenadeFuseHudAlpha = GRENADE_FUSE_HUD_ALPHA.get().floatValue();
        flashbangRadius = FLASHBANG_RADIUS.get();
        flashbangBaseDurationSeconds = FLASHBANG_BASE_DURATION_SECONDS.get();
        flashbangPointBlankFactor = FLASHBANG_POINT_BLANK_FACTOR.get();
        TaczCompatForgeEvents.PRESENT_TO_DROP = PRESENT_TO_DROP_CONFIG.get();
        npcOutgoingDamageToPlayerScale = NPC_OUTGOING_DAMAGE_TO_PLAYER_SCALE.get().floatValue();
    }
}
