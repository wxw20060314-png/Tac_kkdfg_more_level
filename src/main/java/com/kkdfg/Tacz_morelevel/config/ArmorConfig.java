package com.kkdfg.Tacz_morelevel.config;

import com.kkdfg.Tacz_morelevel.Config;
import net.minecraftforge.common.ForgeConfigSpec;

/** 盔甲配置（生成 {@code config/tacz_morelevel/armor.toml}）。 */
public final class ArmorConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final int BULLETPROOF_TIER_COUNT = 8;

    private static final float[][] DEFAULT_BULLETPROOF_TIER_TOTALS = {
        {0.32f, 0.1f},
        {0.4f, 0.2f},
        {0.48f, 0.3f},
        {0.56f, 0.4f},
        {0.64f, 0.5f},
        {0.74f, 0.6f},
        {0.84f, 0.72f},
        {0.87f, 0.8f}
    };

    /** 爆破 / 爆炎 / 动力机甲 / 特种 / 冲击波：每件爆炸减伤默认。 */
    public static final double DEFAULT_BLAST_EXPLOSION_REDUCTION = 0.1D;
    public static final double DEFAULT_BLAZEFLAME_EXPLOSION_REDUCTION = 0.2D;
    public static final double DEFAULT_POWER_MECH_EXPLOSION_REDUCTION = 0.08D;
    public static final double DEFAULT_SPECIAL_OPS_EXPLOSION_REDUCTION = 0.08D;
    public static final double DEFAULT_SHOCKWAVE_EXPLOSION_REDUCTION = 0.08D;
    /** 防弹减伤比例上限默认（{@code armor.toml} {@code bulletproof.maxReduction}）。 */
    public static final double DEFAULT_MAX_BULLETPROOF_REDUCTION = 0.98D;
    /** 卓越前线防弹减伤倍率默认（{@code armor.toml} {@code superbWarfare.t}）。 */
    public static final double DEFAULT_SUPERB_WARFARE_BULLETPROOF_SCALE = 1.0D;

    private record BulletproofTierConfigEntry(
            ForgeConfigSpec.DoubleValue totalBulletproof, ForgeConfigSpec.DoubleValue totalPenReduction) {}

    private static final BulletproofTierConfigEntry[] BULLETPROOF_ARMOR_TIERS = defineBulletproofArmorTiers();

    private static final ForgeConfigSpec.DoubleValue BLAST_EXPLOSION_REDUCTION_PER_PIECE;
    private static final ForgeConfigSpec.DoubleValue BLAZEFLAME_EXPLOSION_REDUCTION_PER_PIECE;
    private static final ForgeConfigSpec.DoubleValue POWER_MECH_EXPLOSION_REDUCTION_PER_PIECE;
    private static final ForgeConfigSpec.DoubleValue SPECIAL_OPS_EXPLOSION_REDUCTION_PER_PIECE;
    private static final ForgeConfigSpec.DoubleValue SHOCKWAVE_EXPLOSION_REDUCTION_PER_PIECE;
    private static final ForgeConfigSpec.DoubleValue MAX_BULLETPROOF_REDUCTION;
    private static final ForgeConfigSpec.DoubleValue SUPERB_WARFARE_BULLETPROOF_SCALE;

    static {
        BUILDER.push("bulletproof");
        BUILDER.comment(
                "Combat clamp for BulletproofArmor damage reduction (TaCZ and Superb Warfare).",
                "Effective reduction = clamp(sum [* superbWarfare.t], 0, maxReduction).");
        MAX_BULLETPROOF_REDUCTION =
                BUILDER.comment("Max bulletproof reduction ratio (default 0.98).")
                        .defineInRange("maxReduction", DEFAULT_MAX_BULLETPROOF_REDUCTION, 0.0, 0.999);
        BUILDER.pop();

        BUILDER.push("explosionReductionPerPiece");
        BUILDER.comment(
                "Explosion damage reduction per equipped armor piece (IS_EXPLOSION).",
                "Pieces stack; total clamped to 1.0 in combat.");
        BLAST_EXPLOSION_REDUCTION_PER_PIECE =
                BUILDER.comment("Blast seventh set (blast): default 0.10 → 40% full set.")
                        .defineInRange("blast", DEFAULT_BLAST_EXPLOSION_REDUCTION, 0.0, 1.0);
        BLAZEFLAME_EXPLOSION_REDUCTION_PER_PIECE =
                BUILDER.comment("Blazeflame seventh set: default 0.20 → 80% full set.")
                        .defineInRange("blazeflame", DEFAULT_BLAZEFLAME_EXPLOSION_REDUCTION, 0.0, 1.0);
        POWER_MECH_EXPLOSION_REDUCTION_PER_PIECE =
                BUILDER.comment("Power Mech set: default 0.08 → 32% full set.")
                        .defineInRange("powerMech", DEFAULT_POWER_MECH_EXPLOSION_REDUCTION, 0.0, 1.0);
        SPECIAL_OPS_EXPLOSION_REDUCTION_PER_PIECE =
                BUILDER.comment("Special Ops set: default 0.08 → 32% full set.")
                        .defineInRange("specialOps", DEFAULT_SPECIAL_OPS_EXPLOSION_REDUCTION, 0.0, 1.0);
        SHOCKWAVE_EXPLOSION_REDUCTION_PER_PIECE =
                BUILDER.comment("Shockwave set: default 0.08 → 32% full set.")
                        .defineInRange("shockwave", DEFAULT_SHOCKWAVE_EXPLOSION_REDUCTION, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("superbWarfare");
        BUILDER.comment(
                "Superb Warfare soft-compat: scales bulletproof reduction against SBW damage only.",
                "Effective reduction = clamp(sumBulletproof * t, 0, bulletproof.maxReduction).",
                "Smaller t = weaker reduction; larger t = stronger (still capped by maxReduction).");
        SUPERB_WARFARE_BULLETPROOF_SCALE =
                BUILDER.comment("Multiplier t for bulletproof reduction vs Superb Warfare (default 1.0).")
                        .defineInRange("t", DEFAULT_SUPERB_WARFARE_BULLETPROOF_SCALE, 0.0, 10.0);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ArmorConfig() {}

    private static BulletproofTierConfigEntry[] defineBulletproofArmorTiers() {
        BUILDER.push("bulletproofArmorTiers");
        BUILDER.comment(
                "Full-set totals for BulletproofArmor#setLevel (1-8); each value is divided by 4 per piece.",
                "Both fields must be in (0, 1): full-set bulletproof reduction sum cap ~0.9 in combat.");
        BulletproofTierConfigEntry[] out = new BulletproofTierConfigEntry[BULLETPROOF_TIER_COUNT];
        for (int i = 0; i < BULLETPROOF_TIER_COUNT; i++) {
            int level = i + 1;
            BUILDER.push("level" + level);
            float[] defaults = DEFAULT_BULLETPROOF_TIER_TOTALS[i];
            ForgeConfigSpec.DoubleValue bulletproof =
                    BUILDER.comment("Full-set bulletproof damage reduction total for tier " + level + ".")
                            .defineInRange("totalBulletproof", defaults[0], 0.001, 0.999);
            ForgeConfigSpec.DoubleValue penReduction =
                    BUILDER.comment("Full-set armorIgnore reduction total for tier " + level + ".")
                            .defineInRange("totalPenReduction", defaults[1], 0.001, 0.999);
            BUILDER.pop();
            out[i] = new BulletproofTierConfigEntry(bulletproof, penReduction);
        }
        BUILDER.pop();
        return out;
    }

    /** 用默认表填充 {@link Config} 盔甲缓存（配置加载前兜底）。 */
    public static void initDefaultCaches() {
        for (int i = 0; i < BULLETPROOF_TIER_COUNT; i++) {
            float[] d = DEFAULT_BULLETPROOF_TIER_TOTALS[i];
            Config.bulletproofTierByLevel[i] = new Config.BulletproofTierStats(d[0], d[1]);
        }
        Config.blastExplosionReductionPerPiece = (float) DEFAULT_BLAST_EXPLOSION_REDUCTION;
        Config.blazeflameExplosionReductionPerPiece = (float) DEFAULT_BLAZEFLAME_EXPLOSION_REDUCTION;
        Config.powerMechExplosionReductionPerPiece = (float) DEFAULT_POWER_MECH_EXPLOSION_REDUCTION;
        Config.specialOpsExplosionReductionPerPiece = (float) DEFAULT_SPECIAL_OPS_EXPLOSION_REDUCTION;
        Config.shockwaveExplosionReductionPerPiece = (float) DEFAULT_SHOCKWAVE_EXPLOSION_REDUCTION;
        Config.maxBulletproofReduction = (float) DEFAULT_MAX_BULLETPROOF_REDUCTION;
        Config.superbWarfareBulletproofScale = (float) DEFAULT_SUPERB_WARFARE_BULLETPROOF_SCALE;
    }

    public static void applyLoadedValues() {
        for (int i = 0; i < BULLETPROOF_ARMOR_TIERS.length; i++) {
            Config.BulletproofTierStats stats = Config.bulletproofTierByLevel[i];
            BulletproofTierConfigEntry entry = BULLETPROOF_ARMOR_TIERS[i];
            stats.totalBulletproof = entry.totalBulletproof().get().floatValue();
            stats.totalPenReduction = entry.totalPenReduction().get().floatValue();
        }
        Config.blastExplosionReductionPerPiece = BLAST_EXPLOSION_REDUCTION_PER_PIECE.get().floatValue();
        Config.blazeflameExplosionReductionPerPiece =
                BLAZEFLAME_EXPLOSION_REDUCTION_PER_PIECE.get().floatValue();
        Config.powerMechExplosionReductionPerPiece =
                POWER_MECH_EXPLOSION_REDUCTION_PER_PIECE.get().floatValue();
        Config.specialOpsExplosionReductionPerPiece =
                SPECIAL_OPS_EXPLOSION_REDUCTION_PER_PIECE.get().floatValue();
        Config.shockwaveExplosionReductionPerPiece =
                SHOCKWAVE_EXPLOSION_REDUCTION_PER_PIECE.get().floatValue();
        Config.maxBulletproofReduction = MAX_BULLETPROOF_REDUCTION.get().floatValue();
        Config.superbWarfareBulletproofScale = SUPERB_WARFARE_BULLETPROOF_SCALE.get().floatValue();
    }
}
