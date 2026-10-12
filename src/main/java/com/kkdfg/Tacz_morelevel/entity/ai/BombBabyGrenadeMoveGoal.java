package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 炸弹宝宝榴弹走位（仅 MOVE）；射击由 {@link BombBabyGrenadeFireGoal} 负责。
 * 不通视时也允许进环停走侧移。
 */
public final class BombBabyGrenadeMoveGoal extends Goal {

    private final BombBabyEntity mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;

    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public BombBabyGrenadeMoveGoal(BombBabyEntity mob) {
        this.mob = mob;
        this.speedModifier = 1.2D;
        float attackRadius = Config.gunRadiusFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.strafeOuterRingAttackRadiusSqFraction =
                Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
        this.strafeInnerRingAttackRadiusSqFraction =
                Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !this.mob.isHoldingGrenadeLauncher()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
        this.strafingTime = -1;
    }

    @Override
    public void stop() {
        this.strafingTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        this.mob.setSprinting(false);
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (distSqr <= (double) this.attackRadiusSqr) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
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
            this.mob.getMoveControl()
                    .strafe(
                            this.strafingBackwards ? -0.5F : 0.5F,
                            this.strafingClockwise ? 0.5F : -0.5F);
        }
    }
}
