package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.bridge.id202;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 衔接 {@link CeilingDuelManeuverSkills#INSTANT_CRAWL}（202）：仅 {@link Flag#MOVE}；
 * x1 单向走 → 停下判距离（仅距离，同基础趴）→ 不满足回基础；满足则趴，x2 内持续检距离，
 * 不满足或满 x2 则跳 x3 → 回基础。
 */
public final class CeilingDuelInstantCrawlGoal extends Goal {

    private enum Stage {
        MOVE,
        CRAWL,
        JUMP,
        DONE
    }

    private final CeilingDuelEntity boss;

    private Stage stage = Stage.DONE;
    private int ticksInStage;
    private int x1;
    private int x2;
    private int x3;
    private int forwardAxis;
    private boolean clockwise;

    public CeilingDuelInstantCrawlGoal(CeilingDuelEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean bridgeActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.BRIDGE
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.INSTANT_CRAWL;
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
        var preset = CeilingDuelInstantCrawlPresets.pickWeighted(this.boss.getRandom());
        this.x1 = preset.x1();
        this.x2 = preset.x2();
        this.x3 = preset.x3();
        this.forwardAxis = this.boss.getRandom().nextBoolean() ? 1 : -1;
        this.clockwise = this.boss.getRandom().nextBoolean();
        this.stage = Stage.MOVE;
        this.ticksInStage = 0;
        this.clearCrawl();
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
        this.clearCrawl();
        this.boss.setManeuverSneaking(false);
        this.boss.setMoveDir(0, 0);
        if (!this.boss.isInWater()
                && (this.boss.getPose() == Pose.CROUCHING || this.boss.getPose() == Pose.SWIMMING)) {
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
            case MOVE -> {
                this.holdStanding();
                this.applyMove();
                if (this.ticksInStage >= this.x1) {
                    this.tryEnterCrawlOrAbort();
                }
            }
            case CRAWL -> {
                this.holdCrawl();
                this.boss.setMoveDir(0, 0);
                if (!this.meetsCrawlDistanceOnly()) {
                    this.enterJump();
                } else if (this.ticksInStage >= this.x2) {
                    this.enterJump();
                }
            }
            case JUMP -> {
                this.holdStanding();
                this.boss.setMoveDir(0, 0);
                if (this.ticksInStage >= this.x3) {
                    this.finishToBase();
                }
            }
            default -> {}
        }
    }

    private void tryEnterCrawlOrAbort() {
        this.boss.setMoveDir(0, 0);
        if (!this.meetsCrawlDistanceOnly()) {
            this.finishToBase();
            return;
        }
        this.stage = Stage.CRAWL;
        this.ticksInStage = 0;
        this.holdCrawl();
    }

    private void enterJump() {
        this.clearCrawl();
        this.stage = Stage.JUMP;
        this.ticksInStage = 0;
        this.holdStanding();
        this.boss.performJumpTier(JumpController.TIER_HIGH);
    }

    private void finishToBase() {
        this.stage = Stage.DONE;
        this.clearCrawl();
        this.boss.setManeuverSneaking(false);
        this.holdStanding();
        this.boss.setMoveDir(0, 0);
        this.boss.enterBaseFromBridge();
    }

    /**
     * 仅距离：水平距 &gt; 配置最小平面距，目标眼高于自身，且 planar/dy &gt; 配置比值。
     * 不看血量、视线、TaCZ canCrawl。
     */
    private boolean meetsCrawlDistanceOnly() {
        LivingEntity target = this.boss.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        Config.CombatGoalStats g =
                Config.npcCombatGoalsFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
        double dx = target.getX() - this.boss.getX();
        double dz = target.getZ() - this.boss.getZ();
        double planar = Math.sqrt(dx * dx + dz * dz);
        if (planar <= g.crawlMinPlanarDistance) {
            return false;
        }
        double dy = target.getEyeY() - this.boss.getY();
        if (dy <= 0.0D) {
            return false;
        }
        return planar / dy > g.crawlMinHeightToDistanceRatio;
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

    private void applyMove() {
        this.boss.setMoveDir(this.forwardAxis, this.clockwise ? 1 : -1);
        this.boss.applyStrafeFromMoveDir();
    }
}
