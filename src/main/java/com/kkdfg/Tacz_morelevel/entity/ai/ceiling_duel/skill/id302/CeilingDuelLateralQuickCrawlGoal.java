package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id302;

import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 与 {@link CeilingDuelLateralYankCrawlGoal} 同技能通道 {@link CeilingDuelManeuverSkills#LATERAL_YANK_CRAWL}：
 * 只负责低频秒趴；走位由横拉 Goal、射击由 GunFire 负责。
 */
public final class CeilingDuelLateralQuickCrawlGoal extends Goal {

    private enum Stage {
        STAND_WAIT,
        CRAWL
    }

    private final CeilingDuelEntity boss;
    private Stage stage = Stage.STAND_WAIT;
    private int ticksLeft;
    private int pendingCrawlTicks = 1;

    public CeilingDuelLateralQuickCrawlGoal(CeilingDuelEntity boss) {
        this.boss = boss;
    }

    private boolean lateralSkillActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.SKILL
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.LATERAL_YANK_CRAWL;
    }

    @Override
    public boolean canUse() {
        return this.lateralSkillActive()
                && this.boss.getTarget() != null
                && this.boss.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.beginCycle();
    }

    @Override
    public void stop() {
        this.ticksLeft = 0;
        this.clearCrawl();
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater()
                && (this.boss.getPose() == Pose.CROUCHING || this.boss.getPose() == Pose.SWIMMING)) {
            this.boss.setPose(Pose.STANDING);
        }
    }

    @Override
    public void tick() {
        if (!this.lateralSkillActive()) {
            return;
        }
        if (this.ticksLeft > 0) {
            --this.ticksLeft;
        }
        if (this.stage == Stage.STAND_WAIT) {
            this.holdStanding();
            if (this.ticksLeft <= 0) {
                this.beginCrawl();
            }
            return;
        }
        if (this.stage == Stage.CRAWL) {
            this.holdCrawl();
            if (this.ticksLeft <= 0) {
                this.beginCycle();
            }
        }
    }

    private void beginCycle() {
        var preset = CeilingDuelLateralQuickCrawlPresets.pickWeighted(this.boss.getRandom());
        int stand =
                CeilingDuelLateralQuickCrawlPresets.rollTicks(
                        preset.standBaseTicks(), preset.standOffsetPct(), 0, this.boss.getRandom());
        this.pendingCrawlTicks =
                CeilingDuelLateralQuickCrawlPresets.rollTicks(
                        preset.crawlBaseTicks(), preset.crawlOffsetPct(), 1, this.boss.getRandom());
        this.holdStanding();
        if (stand <= 0) {
            this.beginCrawl();
        } else {
            this.stage = Stage.STAND_WAIT;
            this.ticksLeft = stand;
        }
    }

    private void beginCrawl() {
        this.stage = Stage.CRAWL;
        this.ticksLeft = Math.max(1, this.pendingCrawlTicks);
        this.holdCrawl();
    }

    private void holdStanding() {
        this.clearCrawl();
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.STANDING);
        }
    }

    private void holdCrawl() {
        this.boss.setManeuverSneaking(false);
        this.boss.setManeuverCrawling(true);
        this.boss.setTaczCrawling(true);
    }

    private void clearCrawl() {
        this.boss.setManeuverCrawling(false);
        if (this.boss.isTaczCrawlingSynced()) {
            this.boss.setTaczCrawling(false);
        }
    }
}
