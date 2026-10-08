package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id201;

import net.minecraft.util.RandomSource;

/**
 * 猫步反跳（201）预设。
 * <p>每条含 x1–x4 与三段距离四元组（前进/静止/后退权重，前后翻向概率 0–100）。
 */
public final class CeilingDuelCatStepJumpPresets {

    /** 距离内圈 / 外圈（格），不进池。 */
    public static final float D1 = 8.0F;
    public static final float D2 = 24.0F;

    /**
     * @param fwd 前进权重
     * @param hold 不进不退权重
     * @param back 后退权重
     * @param flipFwdChancePercent 反向走位时前后翻向概率 0–100（0=不变相）
     */
    public record BandQuad(int fwd, int hold, int back, int flipFwdChancePercent) {
        public BandQuad {
            if (fwd < 0 || hold < 0 || back < 0) {
                throw new IllegalArgumentException("band weights < 0");
            }
            if (fwd + hold + back < 1) {
                throw new IllegalArgumentException("band weights sum < 1");
            }
            if (flipFwdChancePercent < 0 || flipFwdChancePercent > 100) {
                throw new IllegalArgumentException("flip chance out of 0–100");
            }
        }
    }

    /**
     * @param x1 拉多久后蹲
     * @param x2 蹲后再多久左右反向
     * @param x3 反向后再多久起身
     * @param x4 起身后再多久跳并结束
     */
    public record CatStepPreset(
            int x1,
            int x2,
            int x3,
            int x4,
            BandQuad near,
            BandQuad mid,
            BandQuad far,
            int weight) {
        public CatStepPreset {
            if (x1 < 0 || x2 < 0 || x3 < 0 || x4 < 0) {
                throw new IllegalArgumentException("x ticks < 0");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }

        public int totalTicks() {
            return this.x1 + this.x2 + this.x3 + this.x4;
        }
    }

    private static final BandQuad NEAR_MIXED = new BandQuad(25, 70, 5, 95);
    private static final BandQuad MID_CERTAIN_HOLD = new BandQuad(5, 75, 5, 50);
    private static final BandQuad FAR_CERTAIN_BACK = new BandQuad(45, 50, 5, 25);

    private static final CatStepPreset[] PRESETS = {
        ofTiming(7, 2, 4, 1),
        ofTiming(6, 2, 4, 1),
        ofTiming(8, 3, 4, 1),
        ofTiming(7, 1, 3, 1),
        ofTiming(8, 2, 4, 1),
        ofTiming(10, 2, 4, 1),
        ofTiming(12, 2, 4, 1),
        ofTiming(12, 3, 4, 1),
        ofTiming(11, 1, 3, 1),
        ofTiming(12, 2, 4, 1),
        ofTiming(10, 2, 6, 1),
        ofTiming(12, 2, 6, 1),
        ofTiming(12, 3, 6, 1),
        ofTiming(11, 1, 5, 1),
        ofTiming(12, 2, 6, 1),
        ofTiming(10, 4, 10, 1),
        ofTiming(12, 4, 10, 1),
        ofTiming(12, 6, 10, 1),
        ofTiming(11, 2, 8, 1),
        ofTiming(12, 4, 10, 1),
    };

    private CeilingDuelCatStepJumpPresets() {}

    private static CatStepPreset ofTiming(int x1, int x2, int x3, int x4) {
        return new CatStepPreset(
                x1, x2, x3, x4, NEAR_MIXED, MID_CERTAIN_HOLD, FAR_CERTAIN_BACK, 1);
    }

    public static CatStepPreset pickWeighted(RandomSource random) {
        int total = 0;
        for (CatStepPreset p : PRESETS) {
            total += p.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (CatStepPreset p : PRESETS) {
            roll -= p.weight();
            if (roll < 0) {
                return p;
            }
        }
        return PRESETS[PRESETS.length - 1];
    }

    public static BandQuad bandForDistance(CatStepPreset preset, double distance) {
        if (distance < D1) {
            return preset.near();
        }
        if (distance <= D2) {
            return preset.mid();
        }
        return preset.far();
    }

    /**
     * 按四元组前三维加权抽前后：+1 前进 / 0 静止 / -1 后退。
     */
    public static int rollForwardAxis(BandQuad band, RandomSource random) {
        int total = band.fwd() + band.hold() + band.back();
        int roll = random.nextInt(Math.max(1, total));
        if (roll < band.fwd()) {
            return 1;
        }
        roll -= band.fwd();
        if (roll < band.hold()) {
            return 0;
        }
        return -1;
    }

    public static boolean rollFlipForward(BandQuad band, RandomSource random) {
        return band.flipFwdChancePercent() > 0
                && random.nextInt(100) < band.flipFwdChancePercent();
    }
}
