package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunSoldierRangedGoal;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 战争机器空中走位（Ghast 式 setWantedPosition）；射击由 Fire×2 负责。
 */
public final class WarMachineFlyingGunMoveGoal extends Goal implements MobGunSoldierRangedGoal {

    private static final double STRAFE_OFFSET = 4.0D;

    private final WarMachineEntity mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private final int seeTimeToStopMove;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;

    private static final int SEE_TIME_MAX = 1000;
    private int seeTime;
    private int strafingTime = -1;
    private boolean strafingClockwise;
    private boolean strafingBackwards;

    public WarMachineFlyingGunMoveGoal(
            WarMachineEntity mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.strafeOuterRingAttackRadiusSqFraction = strafeOuterRingAttackRadiusSqFraction;
        this.strafeInnerRingAttackRadiusSqFraction = strafeInnerRingAttackRadiusSqFraction;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return this.mob.isPowerMechFlying()
                && target != null
                && target.isAlive()
                && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
        this.seeTime = 0;
        this.strafingTime = -1;
    }

    @Override
    public void stop() {
        this.seeTime = 0;
        this.strafingTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        this.mob.setSprinting(false);
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.mob.hasStrictGunSight() || this.mob.hasRpgGunSight();
        boolean hadSeeTime = this.seeTime > 0;
        if (hasLineOfSight != hadSeeTime) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }

        double wantedX = target.getX();
        double wantedY = target.getEyeY();
        double wantedZ = target.getZ();

        if (!(distSqr > (double) this.attackRadiusSqr) && this.seeTime >= this.seeTimeToStopMove) {
            wantedX = this.mob.getX();
            wantedY = this.mob.getY();
            wantedZ = this.mob.getZ();
            ++this.strafingTime;
        } else {
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
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
            double dx = target.getX() - this.mob.getX();
            double dz = target.getZ() - this.mob.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 1.0E-4D) {
                double perpX = -dz / len;
                double perpZ = dx / len;
                double strafeSign = this.strafingClockwise ? 1.0D : -1.0D;
                if (this.strafingBackwards) {
                    wantedX = this.mob.getX() - dx * 0.5D;
                    wantedZ = this.mob.getZ() - dz * 0.5D;
                } else {
                    wantedX += perpX * STRAFE_OFFSET * strafeSign;
                    wantedZ += perpZ * STRAFE_OFFSET * strafeSign;
                }
            }
        }

        if (target.getEyeY() > this.mob.getEyeY() + 1.0D) {
            wantedY = target.getEyeY();
        }

        this.mob.getMoveControl().setWantedPosition(wantedX, wantedY, wantedZ, this.speedModifier);
    }
}
