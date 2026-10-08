package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id303;

import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301.CeilingDuelLateralQuickCrouchPresets;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 303 秒蹲 + 跳跃阀：蹲预设复用 301；蹲结束后若在移动且阀关则开阀，按 (a/x)² 小跳 {@link JumpController#TIER_LOW}。
 */
public final class CeilingDuelLateralQuickCrouchHopGoal extends Goal {

    private enum Stage {
        STAND_WAIT,
        CROUCH
    }

    private final CeilingDuelEntity boss;
    private Stage stage = Stage.STAND_WAIT;
    private int ticksLeft;
    private int pendingCrouchTicks = 1;

    /** 跳跃阀：默认关。 */
    private boolean hopValveOpen;
    private int hopArmedTicks;
    private int hopXTicks = 8;

    public CeilingDuelLateralQuickCrouchHopGoal(CeilingDuelEntity boss) {
        this.boss = boss;
    }

    private boolean hopSkillActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.SKILL
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.LATERAL_YANK_HOP;
    }

    @Override
    public boolean canUse() {
        return this.hopSkillActive()
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
        this.closeHopValve();
        this.beginCycle();
    }

    @Override
    public void stop() {
        this.ticksLeft = 0;
        this.closeHopValve();
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater() && this.boss.getPose() == Pose.CROUCHING) {
            this.boss.setPose(Pose.STANDING);
        }
    }

    @Override
    public void tick() {
        if (!this.hopSkillActive()) {
            return;
        }
        this.tickHopValve();
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
                this.onCrouchFinished();
                this.beginCycle();
            }
        }
    }

    /** 蹲结束：阀关且正在移动 → 开阀并抽独立跳预设 x。 */
    private void onCrouchFinished() {
        if (this.hopValveOpen) {
            return;
        }
        if (this.boss.getMoveDirForward() == 0 && this.boss.getMoveDirStrafe() == 0) {
            return;
        }
        this.hopValveOpen = true;
        this.hopArmedTicks = 0;
        this.hopXTicks = CeilingDuelHopJumpPresets.pickWeighted(this.boss.getRandom()).xTicks();
    }

    private void tickHopValve() {
        if (!this.hopValveOpen) {
            return;
        }
        ++this.hopArmedTicks;
        float ratio = this.hopArmedTicks / (float) Math.max(1, this.hopXTicks);
        float chance = ratio * ratio;
        if (this.hopArmedTicks >= this.hopXTicks) {
            chance = 1.0F;
        } else {
            chance = Mth.clamp(chance, 0.0F, 1.0F);
        }
        if (this.boss.getRandom().nextFloat() >= chance) {
            return;
        }
        if (this.boss.performJumpTier(JumpController.TIER_LOW, false)) {
            this.closeHopValve();
        }
    }

    private void closeHopValve() {
        this.hopValveOpen = false;
        this.hopArmedTicks = 0;
    }

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
