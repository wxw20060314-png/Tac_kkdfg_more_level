package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id302;

import net.minecraft.util.RandomSource;

/**
 * 302 横拉左右切换预设（与 301 同结构，可独立调）。
 */
public final class CeilingDuelLateralYankCrawlPresets {

    public record FlipPreset(int intervalTicks, int chancePercent, int weight) {
        public FlipPreset {
            if (intervalTicks < 1) {
                throw new IllegalArgumentException("intervalTicks < 1");
            }
            if (chancePercent < 0 || chancePercent > 100) {
                throw new IllegalArgumentException("chancePercent out of 0–100");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }

        public float chanceFraction() {
            return this.chancePercent / 100.0F;
        }
    }

    private static final FlipPreset[] PRESETS = {
        new FlipPreset(4, 100, 1),
        new FlipPreset(4, 80, 1),
        new FlipPreset(6, 100, 1),
        new FlipPreset(6, 80, 1),
        new FlipPreset(8, 100, 1),
        new FlipPreset(8, 80, 1),
        new FlipPreset(8, 65, 1),
        new FlipPreset(10, 100, 1),
        new FlipPreset(10, 80, 1),
        new FlipPreset(12, 100, 1),
        new FlipPreset(12, 80, 1),
        new FlipPreset(14, 80, 1),
    };

    private CeilingDuelLateralYankCrawlPresets() {}

    public static FlipPreset pickWeighted(RandomSource random) {
        int total = 0;
        for (FlipPreset p : PRESETS) {
            total += p.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (FlipPreset p : PRESETS) {
            roll -= p.weight();
            if (roll < 0) {
                return p;
            }
        }
        return PRESETS[PRESETS.length - 1];
    }
}
