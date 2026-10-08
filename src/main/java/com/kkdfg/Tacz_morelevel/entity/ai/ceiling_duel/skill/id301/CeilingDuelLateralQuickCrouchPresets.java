package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 横拉技能（301）配套秒蹲预设：与 {@link CeilingDuelLateralYankGoal} 同通道。
 * {@code a[0]} 站立等待，{@code a[1]} 等待偏移，{@code a[2]} 蹲持续，{@code a[3]} 蹲偏移。
 */
public final class CeilingDuelLateralQuickCrouchPresets {

    public record CrouchPreset(
            float standBaseTicks,
            float standOffsetPct,
            float crouchBaseTicks,
            float crouchOffsetPct,
            int weight) {
        public CrouchPreset {
            if (standBaseTicks < 0.0F || crouchBaseTicks < 0.0F) {
                throw new IllegalArgumentException("base ticks < 0");
            }
            if (standOffsetPct < 0.0F || crouchOffsetPct < 0.0F) {
                throw new IllegalArgumentException("offset < 0");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }
    }

    private static final CrouchPreset[] PRESETS = {
        new CrouchPreset(2.0F, 0.0F, 1.0F, 0.0F, 1),
        new CrouchPreset(3.0F, 0.5F, 2.0F, 0.5F, 1),
        new CrouchPreset(5.0F, 0.3F, 4.0F, 0.3F, 1),
        new CrouchPreset(8.0F, 0.2F, 5.0F, 0.2F, 1),
        // 站立仍短，蹲稍长
        new CrouchPreset(3.0F, 0.3F, 8.0F, 0.2F, 1),
        new CrouchPreset(4.0F, 0.25F, 10.0F, 0.2F, 1),
        new CrouchPreset(5.0F, 0.2F, 12.0F, 0.15F, 1),
        new CrouchPreset(2.0F, 0.2F, 9.0F, 0.25F, 1),
    };

    private CeilingDuelLateralQuickCrouchPresets() {}

    public static CrouchPreset pickWeighted(RandomSource random) {
        int total = 0;
        for (CrouchPreset p : PRESETS) {
            total += p.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (CrouchPreset p : PRESETS) {
            roll -= p.weight();
            if (roll < 0) {
                return p;
            }
        }
        return PRESETS[PRESETS.length - 1];
    }

    public static int rollTicks(float base, float offsetPct, int minTicks, RandomSource random) {
        int rolled;
        if (offsetPct <= 0.0F) {
            rolled = Math.round(base);
        } else {
            float factor = 1.0F + (random.nextFloat() * 2.0F - 1.0F) * offsetPct;
            rolled = Math.round(base * factor);
        }
        return Mth.clamp(rolled, minTicks, 1_000_000);
    }
}
