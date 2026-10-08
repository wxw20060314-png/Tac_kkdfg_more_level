package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301;

import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 与 {@link CeilingDuelLateralYankGoal} 同技能通道 {@link CeilingDuelManeuverSkills#LATERAL_YANK}：
 * 只负责秒蹲姿势；走位/射击由横拉 Goal 负责。301 期间站→蹲→站循环抽预设，技能时长由导演/Timing 结束。
 */
public final class CeilingDuelLateralQuickCrouchGoal extends Goal {

    private enum Stage {
        STAND_WAIT,
        CROUCH
    }

    private final CeilingDuelEntity boss;
    private Stage stage = Stage.STAND_WAIT;
    private int ticksLeft;
    private int pendingCrouchTicks = 1;

    public CeilingDuelLateralQuickCrouchGoal(CeilingDuelEntity boss) {
        this.boss = boss;
    }

    private boolean lateralSkillActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.SKILL
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.LATERAL_YANK;
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
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater() && this.boss.getPose() == Pose.CROUCHING) {
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
            if (!this.boss.isInWater()) {
                this.boss.setPose(Pose.STANDING);
            }
            if (this.ticksLeft <= 0) {
                this.beginCrouch();
            }
            return;
        }
        if (this.stage == Stage.CROUCH) {
            if (!this.boss.isInWater()) {
                this.boss.setPose(Pose.CROUCHING);
            }
            this.boss.setManeuverSneaking(true);
            if (this.ticksLeft <= 0) {
                this.beginCycle();
            }
        }
    }

    /** 抽一轮预设并进入站立等待（或直接蹲）。 */
    private void beginCycle() {
        var preset = CeilingDuelLateralQuickCrouchPresets.pickWeighted(this.boss.getRandom());
        int stand =
                CeilingDuelLateralQuickCrouchPresets.rollTicks(
                        preset.standBaseTicks(), preset.standOffsetPct(), 0, this.boss.getRandom());
        this.pendingCrouchTicks =
                CeilingDuelLateralQuickCrouchPresets.rollTicks(
                        preset.crouchBaseTicks(), preset.crouchOffsetPct(), 1, this.boss.getRandom());
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.STANDING);
        }
        if (stand <= 0) {
            this.beginCrouch();
        } else {
            this.stage = Stage.STAND_WAIT;
            this.ticksLeft = stand;
        }
    }

    private void beginCrouch() {
        this.stage = Stage.CROUCH;
        this.ticksLeft = Math.max(1, this.pendingCrouchTicks);
        this.boss.setManeuverSneaking(true);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.CROUCHING);
        }
    }
}
