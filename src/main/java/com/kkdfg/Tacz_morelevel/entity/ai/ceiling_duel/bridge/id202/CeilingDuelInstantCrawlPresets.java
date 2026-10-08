package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id202;

import net.minecraft.util.RandomSource;

/**
 * 衔接秒趴（202）预设：x1 单向走、x2 最大趴持、x3 跳后收尾。
 * 数字可自行增改。
 */
public final class CeilingDuelInstantCrawlPresets {

    /**
     * @param x1 单向移动 tick
     * @param x2 趴下最长 tick（中途距离不满足则提前跳）
     * @param x3 跳跃后维持 tick，再回基础
     */
    public record InstantCrawlPreset(int x1, int x2, int x3, int weight) {
        public InstantCrawlPreset {
            if (x1 < 0 || x2 < 0 || x3 < 0) {
                throw new IllegalArgumentException("x ticks < 0");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }

        public int totalTicks() {
            return this.x1 + this.x2 + this.x3;
        }
    }

    private static final InstantCrawlPreset[] PRESETS = {
        ofTiming(8, 6, 4),
        ofTiming(10, 8, 4),
        ofTiming(6, 4, 3),
        ofTiming(12, 10, 5),
        ofTiming(7, 5, 4),
        ofTiming(8, 4, 4),
        ofTiming(8, 6, 4),
        ofTiming(8, 8, 4),
        ofTiming(10, 5, 4),
        ofTiming(10, 8, 4),
        ofTiming(6, 3, 3),
        ofTiming(6, 6, 3),
        ofTiming(6, 10, 4),
        ofTiming(12, 6, 4),
        ofTiming(12, 10, 5),
        ofTiming(7, 5, 4),
        ofTiming(7, 9, 4),
    };

    private CeilingDuelInstantCrawlPresets() {}

    private static InstantCrawlPreset ofTiming(int x1, int x2, int x3) {
        return new InstantCrawlPreset(x1, x2, x3, 1);
    }

    public static InstantCrawlPreset pickWeighted(RandomSource random) {
        int total = 0;
        for (InstantCrawlPreset p : PRESETS) {
            total += p.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (InstantCrawlPreset p : PRESETS) {
            roll -= p.weight();
            if (roll < 0) {
                return p;
            }
        }
        return PRESETS[PRESETS.length - 1];
    }
}
