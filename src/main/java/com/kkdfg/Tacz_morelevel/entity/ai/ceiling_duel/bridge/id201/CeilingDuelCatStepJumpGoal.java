package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id201;

import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 衔接 {@link CeilingDuelManeuverSkills#CAT_STEP_JUMP}：仅 {@link Flag#MOVE}；
 * 站立左右拉 → x1 蹲 → x2 左右必反 → x3 起 → x4 跳 → 立刻回基础。
 * 射击由 {@link com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelGunFireGoal} 负责。
 */
public final class CeilingDuelCatStepJumpGoal extends Goal {

    private enum Stage {
        PULL,
        CROUCH,
        REVERSED,
        STAND,
        DONE
    }

    private final CeilingDuelEntity boss;

    private Stage stage = Stage.DONE;
    private int ticksInStage;
    private int x1;
    private int x2;
    private int x3;
    private int x4;
    private int forwardAxis;
    private boolean clockwise;
    private boolean flipForwardOnReverse;
    private CeilingDuelCatStepJumpPresets.BandQuad activeBand;

    public CeilingDuelCatStepJumpGoal(CeilingDuelEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean bridgeActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.BRIDGE
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.CAT_STEP_JUMP;
    }

    @Override
    public boolean canUse() {
        return this.bridgeActive()
                && this.boss.getTarget() != null
                && this.boss.getTarget().isAlive()
                && IGun.mainHandHoldGun(this.boss);
    }

    @Override
    public boolean canContinueToUse() {
        return this.bridgeActive()
                && this.stage != Stage.DONE
                && this.boss.getTarget() != null
                && this.boss.getTarget().isAlive()
                && IGun.mainHandHoldGun(this.boss);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        var preset = CeilingDuelCatStepJumpPresets.pickWeighted(this.boss.getRandom());
        this.x1 = preset.x1();
        this.x2 = preset.x2();
        this.x3 = preset.x3();
        this.x4 = preset.x4();
        double d = this.boss.distanceToTarget();
        this.activeBand = CeilingDuelCatStepJumpPresets.bandForDistance(preset, d);
        this.forwardAxis =
                CeilingDuelCatStepJumpPresets.rollForwardAxis(this.activeBand, this.boss.getRandom());
        this.flipForwardOnReverse =
                CeilingDuelCatStepJumpPresets.rollFlipForward(this.activeBand, this.boss.getRandom());
        this.clockwise = this.boss.getRandom().nextBoolean();
        this.stage = Stage.PULL;
        this.ticksInStage = 0;
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.STANDING);
        }
        this.boss.setPhaseDurationMax(Math.max(1, preset.totalTicks() + 2));
        this.applyMove();
    }

    @Override
    public void stop() {
        this.stage = Stage.DONE;
        this.ticksInStage = 0;
        this.boss.setManeuverSneaking(false);
        this.boss.setMoveDir(0, 0);
        if (!this.boss.isInWater() && this.boss.getPose() == Pose.CROUCHING) {
            this.boss.setPose(Pose.STANDING);
        }
    }

    @Override
    public void tick() {
        if (!this.bridgeActive() || this.stage == Stage.DONE) {
            return;
        }
        if (this.boss.getTarget() == null) {
            return;
        }
        this.boss.getNavigation().stop();
        ++this.ticksInStage;
        switch (this.stage) {
            case PULL -> {
                this.holdStanding();
                this.applyMove();
                if (this.ticksInStage >= this.x1) {
                    this.enterCrouch();
                }
            }
            case CROUCH -> {
                this.holdCrouch();
                this.applyMove();
                if (this.ticksInStage >= this.x2) {
                    this.enterReversed();
                }
            }
            case REVERSED -> {
                this.holdCrouch();
                this.applyMove();
                if (this.ticksInStage >= this.x3) {
                    this.enterStand();
                }
            }
            case STAND -> {
                this.holdStanding();
                this.applyMove();
                if (this.ticksInStage >= this.x4) {
                    this.doJumpAndFinish();
                }
            }
            default -> {}
        }
    }

    private void enterCrouch() {
        this.stage = Stage.CROUCH;
        this.ticksInStage = 0;
        this.holdCrouch();
    }

    private void enterReversed() {
        this.stage = Stage.REVERSED;
        this.ticksInStage = 0;
        this.clockwise = !this.clockwise;
        if (this.flipForwardOnReverse) {
            this.forwardAxis = -this.forwardAxis;
        }
        this.holdCrouch();
        this.applyMove();
    }

    private void enterStand() {
        this.stage = Stage.STAND;
        this.ticksInStage = 0;
        this.holdStanding();
    }

    private void doJumpAndFinish() {
        this.stage = Stage.DONE;
        this.boss.performJumpTier(JumpController.TIER_HIGH_MID);
        this.boss.setManeuverSneaking(false);
        this.holdStanding();
        this.boss.setMoveDir(0, 0);
        this.boss.enterBaseFromBridge();
    }

    private void holdStanding() {
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.STANDING);
        }
    }

    private void holdCrouch() {
        this.boss.setManeuverSneaking(true);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.CROUCHING);
        }
    }

    private void applyMove() {
        this.boss.setMoveDir(this.forwardAxis, this.clockwise ? 1 : -1);
        this.boss.applyStrafeFromMoveDir();
    }
}
