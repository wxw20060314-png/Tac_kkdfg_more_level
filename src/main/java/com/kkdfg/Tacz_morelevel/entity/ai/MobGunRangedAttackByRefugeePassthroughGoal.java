package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 难民走位（仅 MOVE）；射击由 {@link MobGunRangedFireGoal}（tap 节奏）负责。
 */
public final class MobGunRangedAttackByRefugeePassthroughGoal<T extends Mob & RangedAttackMob> extends Goal
        implements MobGunSoldierRangedGoal {

    private final T self;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final int seeTimeToStopMove;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;
    private int attackIntervalMin;
    private final GunCombatSight sight;

    private static final int SEE_TIME_MAX = 1000;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public MobGunRangedAttackByRefugeePassthroughGoal(
            T mob,
            double speedModifier,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int attackIntervalMin,
            int seeTimeToShoot) {
        this.self = mob;
        this.speedModifier = speedModifier;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.strafeOuterRingAttackRadiusSqFraction = strafeOuterRingAttackRadiusSqFraction;
        this.strafeInnerRingAttackRadiusSqFraction = strafeInnerRingAttackRadiusSqFraction;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        this.sight = GunCombatSight.COMBAT;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.self.getTarget();
        return target != null && target.isAlive() && IGun.mainHandHoldGun(this.self);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.self.getNavigation().isDone())
                && IGun.mainHandHoldGun(this.self);
    }

    @Override
    public void start() {
        super.start();
        this.self.setAggressive(true);
        this.seeTime = 0;
        this.strafingTime = -1;
    }

    @Override
    public void stop() {
        super.stop();
        this.seeTime = 0;
        this.strafingTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.self.getTarget();
        if (target == null) {
            return;
        }
        this.self.setSprinting(false);
        double distSqr = this.self.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.sight.hasLineOfSight(this.self, target);
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
            this.self.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.self.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }
        if (this.strafingTime >= 20) {
            if ((double) this.self.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if ((double) this.self.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }
        if (this.strafingTime > -1) {
            if (distSqr > (double) (this.attackRadiusSqr * this.strafeOuterRingAttackRadiusSqFraction)) {
                this.strafingBackwards = false;
            } else if (distSqr < (double) (this.attackRadiusSqr * this.strafeInnerRingAttackRadiusSqFraction)) {
                this.strafingBackwards = true;
            }
            this.self.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
        }
    }
}
