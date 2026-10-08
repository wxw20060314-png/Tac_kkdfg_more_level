package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id303;

import net.minecraft.util.RandomSource;

/**
 * 303 跳跃阀预设：与 301 走位/秒蹲池分离；仅 {@code x}（武装后参考 tick）与抽选权重。
 */
public final class CeilingDuelHopJumpPresets {

    /**
     * @param xTicks 武装后满概率参考 tick；第 a tick 跳概率约为 (a/x)²
     * @param weight 抽选权重，须 &gt; 0
     */
    public record HopPreset(int xTicks, int weight) {
        public HopPreset {
            if (xTicks < 1) {
                throw new IllegalArgumentException("xTicks < 1");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }
    }

    private static final HopPreset[] PRESETS = {
        new HopPreset(6, 1),
        new HopPreset(8, 2),
        new HopPreset(10, 2),
        new HopPreset(12, 2),
        new HopPreset(14, 1),
        new HopPreset(16, 1),
    };

    private CeilingDuelHopJumpPresets() {}

    public static HopPreset pickWeighted(RandomSource random) {
        int total = 0;
        for (HopPreset p : PRESETS) {
            total += p.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (HopPreset p : PRESETS) {
            roll -= p.weight();
            if (roll < 0) {
                return p;
            }
        }
        return PRESETS[PRESETS.length - 1];
    }
}
