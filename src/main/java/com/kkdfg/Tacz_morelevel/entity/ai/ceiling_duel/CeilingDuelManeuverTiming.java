package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 走位持续时间：小类 → 大类 → 全局回退；偏移分默认 / 非默认（特殊）百分比。
 * <p>不做 Java 继承，仅查表回退。
 */
public final class CeilingDuelManeuverTiming {

    /**
     * @param maxTicks 最大持续 tick；{@code null} 表示继承上一级
     * @param offsetDefaultPct 默认偏移比例（0.5 = ±50%）；{@code null} 继承
     * @param offsetOverridePct 非默认偏移；{@code null} 表示无特殊偏移（用默认）
     */
    public record TimingSpec(
            @Nullable Integer maxTicks,
            @Nullable Float offsetDefaultPct,
            @Nullable Float offsetOverridePct) {

        public static TimingSpec of(int maxTicks, float offsetDefaultPct) {
            return new TimingSpec(maxTicks, offsetDefaultPct, null);
        }

        public static TimingSpec inheritAll() {
            return new TimingSpec(null, null, null);
        }
    }

    /** 大类默认：基础 10±50%、衔接 1（空过渡）、技能 50±20%。 */
    private static final Map<Integer, TimingSpec> CATEGORY = new HashMap<>();
    /** 小类覆盖；{@link CeilingDuelManeuverSkills#BASE} 与大类相同。 */
    private static final Map<Integer, TimingSpec> VARIANT = new HashMap<>();

    private static final TimingSpec FALLBACK = TimingSpec.of(20, 0.0F);

    static {
        CATEGORY.put(CeilingDuelManeuverSkills.CATEGORY_BASE, TimingSpec.of(10, 0.50F));
        CATEGORY.put(CeilingDuelManeuverSkills.CATEGORY_BRIDGE, TimingSpec.of(1, 0.0F));
        CATEGORY.put(CeilingDuelManeuverSkills.CATEGORY_SKILL, TimingSpec.of(50, 0.20F));

        // 基础 100：与大类相同（显式登记，便于以后只改小类）
        VARIANT.put(CeilingDuelManeuverSkills.BASE, TimingSpec.of(10, 0.50F));
        // 衔接 200：空过渡
        VARIANT.put(CeilingDuelManeuverSkills.BRIDGE, TimingSpec.of(1, 0.0F));
    }

    private CeilingDuelManeuverTiming() {}

    public static void putCategory(int category, TimingSpec spec) {
        CATEGORY.put(category, spec);
    }

    public static void putVariant(int maneuverId, TimingSpec spec) {
        VARIANT.put(maneuverId, spec);
    }

    @Nullable
    public static TimingSpec categorySpec(int category) {
        return CATEGORY.get(category);
    }

    @Nullable
    public static TimingSpec variantSpec(int maneuverId) {
        return VARIANT.get(maneuverId);
    }

    public static int resolveMaxTicks(int maneuverId) {
        TimingSpec v = VARIANT.get(maneuverId);
        if (v != null && v.maxTicks() != null) {
            return Math.max(1, v.maxTicks());
        }
        int cat = CeilingDuelManeuverSkills.categoryOf(maneuverId);
        TimingSpec c = CATEGORY.get(cat);
        if (c != null && c.maxTicks() != null) {
            return Math.max(1, c.maxTicks());
        }
        return Math.max(1, FALLBACK.maxTicks());
    }

    /**
     * @param useSpecialOffset {@code true} 时优先用 offsetOverridePct（若有）
     */
    public static float resolveOffsetPct(int maneuverId, boolean useSpecialOffset) {
        TimingSpec v = VARIANT.get(maneuverId);
        TimingSpec c = CATEGORY.get(CeilingDuelManeuverSkills.categoryOf(maneuverId));
        if (useSpecialOffset) {
            Float ov = firstNonNull(v != null ? v.offsetOverridePct() : null, c != null ? c.offsetOverridePct() : null);
            if (ov != null) {
                return Math.max(0.0F, ov);
            }
        }
        Float def = firstNonNull(v != null ? v.offsetDefaultPct() : null, c != null ? c.offsetDefaultPct() : null);
        if (def != null) {
            return Math.max(0.0F, def);
        }
        return 0.0F;
    }

    /**
     * 实际持续 = maxTicks × (1 ± offsetPct 均匀随机)，至少 1，至多 {@link CeilingDuelManeuverSkills#AGE_CAP}。
     */
    public static int rollDurationTicks(int maneuverId, RandomSource random, boolean useSpecialOffset) {
        int max = resolveMaxTicks(maneuverId);
        float pct = resolveOffsetPct(maneuverId, useSpecialOffset);
        if (pct <= 0.0F) {
            return Mth.clamp(max, 1, CeilingDuelManeuverSkills.AGE_CAP);
        }
        float factor = 1.0F + (random.nextFloat() * 2.0F - 1.0F) * pct;
        int rolled = Math.round(max * factor);
        return Mth.clamp(rolled, 1, CeilingDuelManeuverSkills.AGE_CAP);
    }

    @Nullable
    private static Float firstNonNull(@Nullable Float a, @Nullable Float b) {
        return a != null ? a : b;
    }
}
