package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id305;

import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 技能 {@link CeilingDuelManeuverSkills#SLIDE_STEP}（305）：仅 {@link Flag#MOVE}；
 * 极快秒蹲（蹲时须贴地才推进）→ 进趴加冲量 → 起身中跳 → 循环；距 &lt;16 进衔接。
 */
public final class CeilingDuelSlideStepGoal extends Goal {

    private static final float RANGE_MIN = 16.0F;
    private static final float RANGE_MID = 40.0F;
    /** 进趴时一次水平冲量（不每 tick 覆盖速度，避免抢起跳/退出）。 */
    private static final double CRAWL_SLIDE_IMPULSE = 0.65D;

    private enum Stage {
        STAND,
        CROUCH,
        CRAWL,
        DONE
    }

    private final CeilingDuelEntity boss;

    private Stage stage = Stage.DONE;
    private int ticksInStage;
    private int x1 = 2;
    private int x2 = 2;
    private int x3 = 8;
    private float crawlFlipChance = 0.35F;
    private int squatCount;
    private int forwardAxis = 1;
    private boolean clockwise = true;

    public CeilingDuelSlideStepGoal(CeilingDuelEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean skillActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.SKILL
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.SLIDE_STEP;
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
        this.squatCount = 0;
        this.clockwise = this.boss.getRandom().nextBoolean();
        this.rollForwardForDistance();
        this.pickCrouchTiming();
        this.stage = Stage.STAND;
        this.ticksInStage = 0;
        this.clearCrawlAndCrouch();
        this.applyMoveNormal();
    }

    @Override
    public void stop() {
        this.stage = Stage.DONE;
        this.ticksInStage = 0;
        this.squatCount = 0;
        this.clearCrawlAndCrouch();
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
        double d = this.boss.distanceToTarget();
        if (d < RANGE_MIN) {
            this.finishToBridge();
            return;
        }
        this.boss.getNavigation().stop();
        ++this.ticksInStage;
        switch (this.stage) {
            case STAND -> {
                this.clearCrawlAndCrouch();
                if (!this.boss.isInWater()) {
                    this.boss.setPose(Pose.STANDING);
                }
                this.applyMoveNormal();
                if (this.ticksInStage >= this.x1) {
                    this.enterCrouch();
                }
            }
            case CROUCH -> {
                this.clearCrawlOnly();
                this.boss.setManeuverSneaking(true);
                if (!this.boss.isInWater()) {
                    this.boss.setPose(Pose.CROUCHING);
                }
                this.applyMoveNormal();
                // 蹲：脚不在地上不计时、不结束
                if (!this.boss.onGround()) {
                    --this.ticksInStage;
                    break;
                }
                if (this.ticksInStage >= this.x2) {
                    this.onCrouchFinished();
                }
            }
            case CRAWL -> {
                this.holdCrawl();
                if (this.boss.getRandom().nextFloat() < this.crawlFlipChance) {
                    this.clockwise = !this.clockwise;
                }
                int side = this.clockwise ? 1 : -1;
                this.boss.setMoveDir(this.forwardAxis, side);
                this.boss.getMoveControl().strafe(0.0F, 0.0F);
                if (this.ticksInStage >= this.x3) {
                    this.finishCrawlWithJump();
                }
            }
            default -> {}
        }
    }

    private void pickCrouchTiming() {
        var c = CeilingDuelSlideStepPresets.pickCrouch(this.boss.getRandom());
        this.x1 = c.x1();
        this.x2 = c.x2();
    }

    private void pickCrawlTiming() {
        var c = CeilingDuelSlideStepPresets.pickCrawl(this.boss.getRandom());
        this.x3 = c.x3();
        this.crawlFlipChance = c.flipSideChance();
    }

    private void enterCrouch() {
        this.stage = Stage.CROUCH;
        this.ticksInStage = 0;
        this.boss.setManeuverSneaking(true);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.CROUCHING);
        }
    }

    private void onCrouchFinished() {
        ++this.squatCount;
        this.boss.setManeuverSneaking(false);
        if (!this.boss.isInWater()) {
            this.boss.setPose(Pose.STANDING);
        }
        float p =
                CeilingDuelSlideStepPresets.crawlEnterChance(this.squatCount, this.boss.getRandom());
        if (this.boss.getRandom().nextFloat() < p) {
            this.enterCrawl();
            return;
        }
        this.pickCrouchTiming();
        this.rollForwardForDistance();
        this.stage = Stage.STAND;
        this.ticksInStage = 0;
    }

    private void enterCrawl() {
        this.pickCrawlTiming();
        this.rollForwardForDistance();
        this.stage = Stage.CRAWL;
        this.ticksInStage = 0;
        this.holdCrawl();
        this.applyCrawlSlideImpulse();
    }

    private void finishCrawlWithJump() {
        this.clearCrawlAndCrouch();
        this.squatCount = 0;
        this.boss.performJumpTier(JumpController.TIER_MID);
        this.pickCrouchTiming();
        this.rollForwardForDistance();
        this.stage = Stage.STAND;
        this.ticksInStage = 0;
        this.applyMoveNormal();
    }

    private void rollForwardForDistance() {
        double d = this.boss.distanceToTarget();
        float r = this.boss.getRandom().nextFloat();
        // 16–40：中进中退；&gt;40：大概率前进（同颠步外带，但不因此强制退出）
        if (d >= RANGE_MIN && d <= RANGE_MID) {
            if (r < 0.45F) {
                this.forwardAxis = 1;
            } else if (r < 0.90F) {
                this.forwardAxis = -1;
            } else {
                this.forwardAxis = 0;
            }
            return;
        }
        if (d > RANGE_MID) {
            this.forwardAxis = r < 0.75F ? 1 : (r < 0.90F ? -1 : 0);
            return;
        }
        this.forwardAxis = r < 0.85F ? -1 : 1;
    }

    private void applyMoveNormal() {
        float a = CeilingDuelEntity.GUN_STRAFE_AXIS;
        this.boss.setMoveDir(this.forwardAxis, this.clockwise ? 1 : -1);
        this.boss.getMoveControl()
                .strafe(this.forwardAxis * a, (this.clockwise ? 1 : -1) * a);
    }

    /** 仅进趴调用：在选定前后/侧向上叠加水平冲量。 */
    private void applyCrawlSlideImpulse() {
        int side = this.clockwise ? 1 : -1;
        this.boss.setMoveDir(this.forwardAxis, side);
        this.boss.getMoveControl().strafe(0.0F, 0.0F);

        float yaw = this.boss.getYRot() * ((float) Math.PI / 180.0F);
        float sin = Mth.sin(yaw);
        float cos = Mth.cos(yaw);
        double dx = (double) side * cos - (double) this.forwardAxis * sin;
        double dz = (double) this.forwardAxis * cos + (double) side * sin;
        double lenSq = dx * dx + dz * dz;
        if (lenSq < 1.0E-7D) {
            return;
        }
        double inv = CRAWL_SLIDE_IMPULSE / Math.sqrt(lenSq);
        this.boss.setDeltaMovement(this.boss.getDeltaMovement().add(dx * inv, 0.0D, dz * inv));
        this.boss.hasImpulse = true;
    }

    private void holdCrawl() {
        this.boss.setManeuverSneaking(false);
        this.boss.setManeuverCrawling(true);
        this.boss.setTaczCrawling(true);
    }

    private void clearCrawlOnly() {
        this.boss.setManeuverCrawling(false);
        if (this.boss.isTaczCrawlingSynced()) {
            this.boss.setTaczCrawling(false);
        }
    }

    private void clearCrawlAndCrouch() {
        this.boss.setManeuverSneaking(false);
        this.clearCrawlOnly();
        if (!this.boss.isInWater()
                && (this.boss.getPose() == Pose.CROUCHING || this.boss.getPose() == Pose.SWIMMING)) {
            this.boss.setPose(Pose.STANDING);
        }
    }

    private void finishToBridge() {
        this.stage = Stage.DONE;
        this.clearCrawlAndCrouch();
        this.boss.setMoveDir(0, 0);
        this.boss.getMoveControl().strafe(0.0F, 0.0F);
        this.boss.enterBridge();
    }
}
