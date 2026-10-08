package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id303;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.skill.id301.CeilingDuelLateralYankPresets;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 技能 {@link CeilingDuelManeuverSkills#LATERAL_YANK_HOP}：仅 {@link Flag#MOVE}；
 * 走位/距离带同 301（&lt;12 退，12–24 中，&gt;24 进），flip 预设复用 301。
 */
public final class CeilingDuelLateralYankHopGoal extends Goal {

    private static final float STRAFE_INNER_BLOCKS = 12.0F;
    private static final float STRAFE_OUTER_BLOCKS = 24.0F;
    private static final float STRAFE_INNER_BLOCKS_SQ = STRAFE_INNER_BLOCKS * STRAFE_INNER_BLOCKS;
    private static final float STRAFE_OUTER_BLOCKS_SQ = STRAFE_OUTER_BLOCKS * STRAFE_OUTER_BLOCKS;

    private static final float BASE_FWD_FLIP_CHANCE = 0.3F;
    private static final float MID_HOLD_CHANCE = 0.50F;
    private static final float MID_FORWARD_CHANCE = 0.35F;

    private final CeilingDuelEntity boss;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final int seeTimeToStopMove;

    private static final int SEE_TIME_MAX = 1000;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private boolean forwardZero;
    private int strafingTime = -1;

    private int flipIntervalTicks = 8;
    private float flipChance = 0.8F;

    public CeilingDuelLateralYankHopGoal(
            CeilingDuelEntity boss,
            double speedModifier,
            float attackRadius,
            int seeTimeToStopMove) {
        this.boss = boss;
        this.speedModifier = speedModifier;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean skillActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.SKILL
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.LATERAL_YANK_HOP;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.boss.getTarget();
        return this.skillActive()
                && target != null
                && target.isAlive()
                && IGun.mainHandHoldGun(this.boss);
    }

    @Override
    public boolean canContinueToUse() {
        return this.skillActive()
                && this.boss.getTarget() != null
                && this.boss.getTarget().isAlive()
                && IGun.mainHandHoldGun(this.boss);
    }

    @Override
    public void start() {
        var preset = CeilingDuelLateralYankPresets.pickWeighted(this.boss.getRandom());
        this.flipIntervalTicks = preset.intervalTicks();
        this.flipChance = preset.chanceFraction();
        this.forwardZero = false;
        this.strafingTime = -1;
        this.seeTime = 0;
    }

    @Override
    public void stop() {
        this.seeTime = 0;
        this.strafingTime = -1;
        this.forwardZero = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.skillActive()) {
            return;
        }
        LivingEntity target = this.boss.getTarget();
        if (target == null) {
            return;
        }
        this.boss.setSprinting(false);
        double distSqr = this.boss.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = MobGunPlayerLikeAimHelper.hasCombatLineOfSight(this.boss, target);
        boolean hadSeeTime = this.seeTime > 0;
        if (hasLineOfSight != hadSeeTime) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }

        if (!(distSqr > (double) this.attackRadiusSqr) && this.seeTime >= this.seeTimeToStopMove) {
            this.boss.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.boss.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= this.flipIntervalTicks) {
            if (this.boss.getRandom().nextFloat() < this.flipChance) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            this.rollForwardAxis(distSqr);
            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distSqr > (double) STRAFE_OUTER_BLOCKS_SQ) {
                this.strafingBackwards = false;
                this.forwardZero = false;
            } else if (distSqr < (double) STRAFE_INNER_BLOCKS_SQ) {
                this.strafingBackwards = true;
                this.forwardZero = false;
            }
            float a = CeilingDuelEntity.GUN_STRAFE_AXIS;
            float fwd = this.forwardZero ? 0.0F : (this.strafingBackwards ? -a : a);
            float side = this.strafingClockwise ? a : -a;
            this.boss.getMoveControl().strafe(fwd, side);
            int fwdDir = this.forwardZero ? 0 : (this.strafingBackwards ? -1 : 1);
            int sideDir = this.strafingClockwise ? 1 : -1;
            this.boss.setMoveDir(fwdDir, sideDir);
        }
    }

    private void rollForwardAxis(double distSqr) {
        if (this.isMidStrafeBand(distSqr)) {
            float r = this.boss.getRandom().nextFloat();
            if (r < MID_HOLD_CHANCE) {
                this.forwardZero = true;
            } else if (r < MID_HOLD_CHANCE + MID_FORWARD_CHANCE) {
                this.forwardZero = false;
                this.strafingBackwards = false;
            } else {
                this.forwardZero = false;
                this.strafingBackwards = true;
            }
            return;
        }
        this.forwardZero = false;
        if (this.boss.getRandom().nextFloat() < BASE_FWD_FLIP_CHANCE) {
            this.strafingBackwards = !this.strafingBackwards;
        }
    }

    private boolean isMidStrafeBand(double distSqr) {
        return distSqr <= (double) STRAFE_OUTER_BLOCKS_SQ && distSqr >= (double) STRAFE_INNER_BLOCKS_SQ;
    }
}
