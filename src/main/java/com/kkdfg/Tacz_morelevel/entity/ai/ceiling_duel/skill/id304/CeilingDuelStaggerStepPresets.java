package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id304;

import net.minecraft.util.RandomSource;

/**
 * 304 颠步预设：x1 走 + x2 停（合计约 15），停下姿势蹲/趴等权分条写入池。
 */
public final class CeilingDuelStaggerStepPresets {

    public enum StopPose {
        CROUCH,
        CRAWL
    }

    /**
     * @param x1 单向移动 tick
     * @param x2 停下 tick
     * @param stopPose 停下姿势
     * @param weight 抽选权重
     */
    public record StaggerPreset(int x1, int x2, StopPose stopPose, int weight) {
        public StaggerPreset {
            if (x1 < 1 || x2 < 1) {
                throw new IllegalArgumentException("x ticks < 1");
            }
            if (weight < 1) {
                throw new IllegalArgumentException("weight < 1");
            }
        }
    }

    /** x1≈x2、合计约 15；蹲/趴各若干等权条。 */
    private static final StaggerPreset[] PRESETS = {
        new StaggerPreset(7, 8, StopPose.CROUCH, 2),
        new StaggerPreset(7, 8, StopPose.CRAWL, 2),
        new StaggerPreset(8, 7, StopPose.CROUCH, 2),
        new StaggerPreset(8, 7, StopPose.CRAWL, 2),
        new StaggerPreset(7, 7, StopPose.CROUCH, 3),
        new StaggerPreset(7, 7, StopPose.CRAWL, 3),
        new StaggerPreset(6, 9, StopPose.CROUCH, 2),
        new StaggerPreset(6, 9, StopPose.CRAWL, 2),
        new StaggerPreset(9, 6, StopPose.CROUCH, 2),
        new StaggerPreset(9, 6, StopPose.CRAWL, 2),
        new StaggerPreset(8, 8, StopPose.CROUCH, 2),
        new StaggerPreset(8, 8, StopPose.CRAWL, 2),
        new StaggerPreset(6, 8, StopPose.CROUCH, 1),
        new StaggerPreset(6, 8, StopPose.CRAWL, 1),
        new StaggerPreset(8, 6, StopPose.CROUCH, 1),
        new StaggerPreset(8, 6, StopPose.CRAWL, 1),
        new StaggerPreset(5, 10, StopPose.CROUCH, 1),
        new StaggerPreset(5, 10, StopPose.CRAWL, 1),
        new StaggerPreset(10, 5, StopPose.CROUCH, 1),
        new StaggerPreset(10, 5, StopPose.CRAWL, 1),
    };

    /** 下一段左右是否反向的概率。 */
    public static final float FLIP_SIDE_CHANCE = 0.5F;

    private CeilingDuelStaggerStepPresets() {}

    public static StaggerPreset pickWeighted(RandomSource random) {
        int total = 0;
        for (StaggerPreset p : PRESETS) {
            total += p.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        for (StaggerPreset p : PRESETS) {
            roll -= p.weight();
            if (roll < 0) {
                return p;
            }
        }
        return PRESETS[PRESETS.length - 1];
    }
}
