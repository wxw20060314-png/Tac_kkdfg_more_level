package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id304;

import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 技能 {@link CeilingDuelManeuverSkills#STAGGER_STEP}（304）：仅 {@link Flag#MOVE}；
 * 走 x1 → 停 x2（蹲/趴）→ 再走；距 &lt;16 立刻进衔接；射击由 GunFire 负责。
 */
public final class CeilingDuelStaggerStepGoal extends Goal {

    private static final float ABORT_DISTANCE = 16.0F;
    private static final float MID_BAND = 40.0F;

    private enum Stage {
        MOVE,
        STOP,
        DONE
    }

    private final CeilingDuelEntity boss;

    private Stage stage = Stage.DONE;
    private int ticksInStage;
    private int x1;
    private int x2;
    private CeilingDuelStaggerStepPresets.StopPose stopPose =
            CeilingDuelStaggerStepPresets.StopPose.CROUCH;
    private int forwardAxis = 1;
    private boolean clockwise = true;

    public CeilingDuelStaggerStepGoal(CeilingDuelEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean skillActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.SKILL
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.STAGGER_STEP;
    }

    @Override
    public boolean canUse() {
        return this.skillActive()
                && this.boss.getTarget() != null
                && this.boss.getTarget().isAlive()
                && IGun.mainHandHoldGun(this.boss);
    }

    @Override
    public boolean canContinueToUse() {
        return this.skillActive()
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
        this.pickSegment();
        this.rollForwardForDistance();
        this.clockwise = this.boss.getRandom().nextBoolean();
        this.stage = Stage.MOVE;
        this.ticksInStage = 0;
        this.clearStopPose();
        this.applyMove();
    }

    @Override
    public void stop() {
        this.stage = Stage.DONE;
        this.ticksInStage = 0;
        this.clearStopPose();
        this.boss.setMoveDir(0, 0);
    }

    @Override
    public void tick() {
        if (!this.skillActive() || this.stage == Stage.DONE) {
            return;
        }
        if (this.boss.getTarget() == null) {
            return;
        }
        if (this.boss.distanceToTarget() < ABORT_DISTANCE) {
            this.finishToBridge();
            return;
        }
        this.boss.getNavigation().stop();
        ++this.ticksInStage;
        switch (this.stage) {
            case MOVE -> {
                this.clearStopPose();
                this.applyMove();
                if (this.ticksInStage >= this.x1) {
                    this.enterStop();
                }
            }
            case STOP -> {
                this.holdStopPose();
                this.hardStopHorizontal();
                if (this.ticksInStage >= this.x2) {
                    this.enterNextMove();
                }
            }
            default -> {}
        }
    }

    private void pickSegment() {
        var preset = CeilingDuelStaggerStepPresets.pickWeighted(this.boss.getRandom());
        this.x1 = preset.x1();
        this.x2 = preset.x2();
        this.stopPose = preset.stopPose();
    }

    private void enterStop() {
        this.stage = Stage.STOP;
        this.ticksInStage = 0;
        this.hardStopHorizontal();
        this.holdStopPose();
    }

    private void enterNextMove() {
        this.pickSegment();
        this.clearStopPose();
        if (this.boss.getRandom().nextFloat() < CeilingDuelStaggerStepPresets.FLIP_SIDE_CHANCE) {
            this.clockwise = !this.clockwise;
        }
        this.rollForwardForDistance();
        this.stage = Stage.MOVE;
        this.ticksInStage = 0;
        this.applyMove();
    }

    private void rollForwardForDistance() {
        double d = this.boss.distanceToTarget();
        float r = this.boss.getRandom().nextFloat();
        if (d >= MID_BAND) {
            // 大概率前进
            if (r < 0.75F) {
                this.forwardAxis = 1;
            } else if (r < 0.90F) {
                this.forwardAxis = -1;
            } else {
                this.forwardAxis = 0;
            }
            return;
        }
        if (d >= ABORT_DISTANCE) {
            // 16–40：中进中退
            if (r < 0.45F) {
                this.forwardAxis = 1;
            } else if (r < 0.90F) {
                this.forwardAxis = -1;
            } else {
                this.forwardAxis = 0;
            }
            return;
        }
        // <16 大概率退（通常已 abort）
        this.forwardAxis = r < 0.85F ? -1 : 1;
    }

    private void holdStopPose() {
        if (this.stopPose == CeilingDuelStaggerStepPresets.StopPose.CRAWL) {
            this.boss.setManeuverSneaking(false);
            this.boss.setManeuverCrawling(true);
            this.boss.setTaczCrawling(true);
            return;
        }
        this.clearCrawlOnly();
        this.boss.setManeuverSneaking(true);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.CROUCHING);
        }
    }

    private void clearStopPose() {
        this.boss.setManeuverSneaking(false);
        this.clearCrawlOnly();
        if (!this.boss.isInWater()
                && (this.boss.getPose() == Pose.CROUCHING || this.boss.getPose() == Pose.SWIMMING)) {
            this.boss.setPose(Pose.STANDING);
        }
    }

    private void clearCrawlOnly() {
        this.boss.setManeuverCrawling(false);
        if (this.boss.isTaczCrawlingSynced()) {
            this.boss.setTaczCrawling(false);
        }
    }

    private void applyMove() {
        this.boss.setMoveDir(this.forwardAxis, this.clockwise ? 1 : -1);
        this.boss.applyStrafeFromMoveDir();
    }

    /** 急停：清 moveDir、侧移指令与水平速度，保留竖直分量。 */
    private void hardStopHorizontal() {
        this.boss.setMoveDir(0, 0);
        this.boss.getMoveControl().strafe(0.0F, 0.0F);
        var v = this.boss.getDeltaMovement();
        this.boss.setDeltaMovement(0.0D, v.y, 0.0D);
    }

    private void finishToBridge() {
        this.stage = Stage.DONE;
        this.clearStopPose();
        this.hardStopHorizontal();
        this.boss.enterBridge();
    }
}
