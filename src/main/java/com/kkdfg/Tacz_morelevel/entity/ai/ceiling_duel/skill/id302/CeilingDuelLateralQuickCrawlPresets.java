package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id302;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 302 配套秒趴预设：站立等待更长（频率低于 301 秒蹲），再短趴。
 */
public final class CeilingDuelLateralQuickCrawlPresets {

    public record CrawlPreset(
            float standBaseTicks,
            float standOffsetPct,
            float crawlBaseTicks,
            float crawlOffsetPct,
            int weight) {
        public CrawlPreset {
            if (standBaseTicks < 0.0F || crawlBaseTicks < 0.0F) {
                throw new IllegalArgumentException("base ticks < 0");
            }
            if (standOffsetPct < 0.0F || crawlOffsetPct < 0.0F) {
                throw new IllegalArgumentException("offset < 0");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }
    }

    /** 站立基数明显长于 301，趴持续仍短。 */
    private static final CrawlPreset[] PRESETS = {
        new CrawlPreset(12.0F, 0.25F, 2.0F, 0.3F, 1),
        new CrawlPreset(16.0F, 0.2F, 3.0F, 0.3F, 1),
        new CrawlPreset(20.0F, 0.2F, 2.0F, 0.25F, 1),
        new CrawlPreset(24.0F, 0.15F, 4.0F, 0.25F, 1),
        new CrawlPreset(30.0F, 0.15F, 3.0F, 0.2F, 1),
        new CrawlPreset(40.0F, 0.1F, 4.0F, 0.2F, 1),
    };

    private CeilingDuelLateralQuickCrawlPresets() {}

    public static CrawlPreset pickWeighted(RandomSource random) {
        int total = 0;
        for (CrawlPreset p : PRESETS) {
            total += p.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (CrawlPreset p : PRESETS) {
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
