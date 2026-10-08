package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 难民用点射节奏，与 {@link MobGunRangedAttackByRefugeeGoal} 相同；交火视线为头→身→脚
 * （{@link MobGunPlayerLikeAimHelper#hasCombatLineOfSight}）。
 */
public final class MobGunRangedAttackByRefugeePassthroughGoal<T extends Mob & RangedAttackMob> extends Goal
        implements MobGunSoldierRangedGoal {

    private final T self;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final double meleeDistSqThreshold;
    private final int seeTimeToStopMove;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;
    private int attackIntervalMin;
    private final int seeTimeToShoot;

    private static final int SEE_TIME_MAX = 1000;
    private int attackTime;
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
        this.seeTimeToShoot = seeTimeToShoot;
        this.attackRadiusSqr = attackRadius * attackRadius;
        float f = Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        double meleeR = attackRadius * f;
        this.meleeDistSqThreshold = meleeR * meleeR;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
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
        this.attackTime = this.attackIntervalMin / 2;
        this.seeTime = 0;
        this.strafingTime = -1;
        if (!this.self.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.self);
            op.initialData();
            op.draw(this.self::getMainHandItem);
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.self.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = 0;
        this.strafingTime = -1;
        if (!this.self.level().isClientSide()) {
            IGunOperator.fromLivingEntity(this.self).aim(false);
        }
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
        boolean hasLineOfSight = MobGunPlayerLikeAimHelper.hasCombatLineOfSight(this.self, target);
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
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.self, target);
        if (!this.self.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.self);
            boolean inAttackRing = hasLineOfSight && distSqr <= (double) this.attackRadiusSqr;
            boolean meleeBand = inAttackRing && distSqr <= this.meleeDistSqThreshold;
            boolean midLongBand = inAttackRing && distSqr > this.meleeDistSqThreshold;
            if (midLongBand) {
                op.aim(true);
            } else {
                op.aim(false);
            }
            if (this.attackTime > 0) {
                --this.attackTime;
            }
            boolean taczReady = op.getSynDrawCoolDown() == 0L && !op.getSynIsBolting();
            boolean meleeShotOk = meleeBand && this.attackTime == 0 && taczReady && this.seeTime >= 1;
            boolean midLongShotOk =
                    midLongBand && this.attackTime == 0 && taczReady && this.seeTime >= this.seeTimeToShoot;
            if (meleeShotOk || midLongShotOk) {
                this.self.performRangedAttack(target, 1.0F);
                this.attackTime = this.attackIntervalMin;
            }
        }
    }
}
