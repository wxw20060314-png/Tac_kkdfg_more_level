package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id305;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 305 滑步预设：秒蹲 (x1,x2) 与趴 (x3, 左右变相概率) 分池。
 */
public final class CeilingDuelSlideStepPresets {

    /** @param x1 站→蹲；{@code x2} 蹲→起；约 2–3 tick。 */
    public record CrouchPreset(int x1, int x2, int weight) {
        public CrouchPreset {
            if (x1 < 1 || x2 < 1) {
                throw new IllegalArgumentException("crouch x < 1");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }
    }

    /**
     * @param x3 趴持续 tick
     * @param flipSideChancePercent 趴中每 tick 左右反向概率 0–100
     */
    public record CrawlPreset(int x3, int flipSideChancePercent, int weight) {
        public CrawlPreset {
            if (x3 < 1) {
                throw new IllegalArgumentException("x3 < 1");
            }
            if (flipSideChancePercent < 0 || flipSideChancePercent > 100) {
                throw new IllegalArgumentException("flip % out of 0–100");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }

        public float flipSideChance() {
            return this.flipSideChancePercent / 100.0F;
        }
    }

    private static final CrouchPreset[] CROUCH_PRESETS = {
        new CrouchPreset(2, 2, 3),
        new CrouchPreset(2, 3, 3),
        new CrouchPreset(3, 2, 3),
        new CrouchPreset(3, 3, 2),
        new CrouchPreset(2, 2, 2),
        new CrouchPreset(3, 2, 1),
    };

    private static final CrawlPreset[] CRAWL_PRESETS = {
        new CrawlPreset(6, 30, 2),
        new CrawlPreset(8, 35, 3),
        new CrawlPreset(10, 40, 2),
        new CrawlPreset(7, 25, 2),
        new CrawlPreset(9, 45, 1),
    };

    private CeilingDuelSlideStepPresets() {}

    public static CrouchPreset pickCrouch(RandomSource random) {
        return pick(CROUCH_PRESETS, random);
    }

    public static CrawlPreset pickCrawl(RandomSource random) {
        return pick(CRAWL_PRESETS, random);
    }

    /**
     * 第 n 次蹲起后进趴概率：2≈40%、3≈80%、≥4=100%；对 2/3 加 ±0.08 波动。
     */
    public static float crawlEnterChance(int squatCount, RandomSource random) {
        if (squatCount < 2) {
            return 0.0F;
        }
        if (squatCount >= 4) {
            return 1.0F;
        }
        float base = squatCount == 2 ? 0.40F : 0.80F;
        float delta = (random.nextFloat() * 2.0F - 1.0F) * 0.08F;
        return Mth.clamp(base + delta, 0.0F, 1.0F);
    }

    private static <T> T pick(T[] presets, RandomSource random) {
        int total = 0;
        for (T p : presets) {
            if (p instanceof CrouchPreset c) {
                total += c.weight();
            } else if (p instanceof CrawlPreset c) {
                total += c.weight();
            }
        }
        int roll = random.nextInt(Math.max(1, total));
        for (T p : presets) {
            int w = p instanceof CrouchPreset c ? c.weight() : ((CrawlPreset) p).weight();
            roll -= w;
            if (roll < 0) {
                return p;
            }
        }
        return presets[presets.length - 1];
    }
}
