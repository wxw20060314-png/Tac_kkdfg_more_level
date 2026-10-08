package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.base;

import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInShortDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import net.minecraft.world.entity.PathfinderMob;

/**
 * BASE/{@link CeilingDuelManeuverSkills#BASE} 专用跳蹲趴：行为同超特对应 Goal，仅多一层基础态门禁。
 */
public final class CeilingDuelBaseManeuverGoals {

    private CeilingDuelBaseManeuverGoals() {}

    private static boolean baseActive(CeilingDuelEntity boss) {
        return boss.getManeuverPhase() == CeilingDuelManeuverPhase.BASE
                && boss.getManeuverSkillId() == CeilingDuelManeuverSkills.BASE;
    }

    public static final class LongSneak extends MobRandomSneakWithTargetInlongDistanceGoal {
        private final CeilingDuelEntity boss;

        public LongSneak(
                CeilingDuelEntity boss,
                int timeDelay,
                int sneakTime,
                int sneakRandomTime,
                double minTargetDistance,
                boolean enabled) {
            super(boss, timeDelay, sneakTime, sneakRandomTime, minTargetDistance, enabled);
            this.boss = boss;
        }

        @Override
        public boolean canUse() {
            return baseActive(this.boss) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return baseActive(this.boss) && super.canContinueToUse();
        }
    }

    public static final class ShortSneak extends MobRandomSneakWithTargetInShortDistanceGoal {
        private final CeilingDuelEntity boss;

        public ShortSneak(
                CeilingDuelEntity boss,
                int timeDelay,
                int sneakTime,
                int sneakRandomTime,
                double maxTargetDistance,
                boolean enabled) {
            super(boss, timeDelay, sneakTime, sneakRandomTime, maxTargetDistance, enabled);
            this.boss = boss;
        }

        @Override
        public boolean canUse() {
            return baseActive(this.boss) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return baseActive(this.boss) && super.canContinueToUse();
        }
    }

    public static final class NoTargetSneakStand extends MobRandomSneakWithNoTargetGoal {
        private final CeilingDuelEntity boss;

        public NoTargetSneakStand(CeilingDuelEntity boss) {
            super(boss);
            this.boss = boss;
        }

        @Override
        public boolean canUse() {
            return baseActive(this.boss) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return baseActive(this.boss) && super.canContinueToUse();
        }
    }

    public static final class Crawl extends MobTaczCombatCrawlGoal {
        private final CeilingDuelEntity boss;

        public Crawl(
                PathfinderMob soldier,
                int crawlRandomDenominator,
                int crawlExitRandomDenominator,
                float maxHealthExclusive,
                double minPlanarDistance,
                double minHeightToDistanceRatio,
                boolean enabled) {
            super(
                    soldier,
                    crawlRandomDenominator,
                    crawlExitRandomDenominator,
                    maxHealthExclusive,
                    minPlanarDistance,
                    minHeightToDistanceRatio,
                    enabled);
            this.boss = (CeilingDuelEntity) soldier;
        }

        @Override
        public boolean canUse() {
            return baseActive(this.boss) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return baseActive(this.boss) && super.canContinueToUse();
        }
    }

    public static final class Jump extends MobRandomJumpGoal {
        private final CeilingDuelEntity boss;

        public Jump(CeilingDuelEntity boss, int jumpChance, boolean enabled, double maxJumpDistance) {
            super(boss, jumpChance, enabled, maxJumpDistance);
            this.boss = boss;
        }

        @Override
        public boolean canUse() {
            return baseActive(this.boss) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return baseActive(this.boss) && super.canContinueToUse();
        }

        @Override
        protected void doJump() {
            this.boss.performJump();
        }
    }
}
