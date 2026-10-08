package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/** {@link Pose#CROUCHING} {@link com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithTargetInlongDistanceGoal} {@link CompanionMobSneakGuard} */
public class CompanionMobRandomSneakWithTargetInLongDistanceGoal extends Goal {

    private final AbstractFriendEntity companion;
    private int sneakTicksLeft;
    
    private final int timeDelay;
    
    private final int sneakTime;
    /** {@link #sneakTime} */
    private final int sneakRandomTime;
    /** {@link net.minecraft.world.entity.Entity#distanceToSqr} */
    private final double minTargetDistanceSqr;
    
    private final boolean enabled;

    public CompanionMobRandomSneakWithTargetInLongDistanceGoal(AbstractFriendEntity companion) {
        this(companion, 100, 20, 40, 25.0D, true);
    }

    
    public CompanionMobRandomSneakWithTargetInLongDistanceGoal(
            AbstractFriendEntity companion,
            int timeDelay,
            int sneakTime,
            int sneakRandomTime,
            double minTargetDistance,
            boolean enabled) {
        this.companion = companion;
        this.timeDelay = timeDelay;
        this.sneakTime = sneakTime;
        this.sneakRandomTime = sneakRandomTime;
        this.minTargetDistanceSqr = minTargetDistance * minTargetDistance;
        this.enabled = enabled;
    }

    private boolean isRunnable() {
        return this.enabled
                && this.timeDelay > 0
                && this.sneakTime > 0
                && this.sneakRandomTime > 0
                && this.minTargetDistanceSqr > 0.0D;
    }

    @Override
    public boolean canUse() {
        return CompanionMobSneakGuard.maySneak(this.companion)
                && this.isRunnable()
                && this.targetValidAndInLongSneakDistance();
    }

    @Override
    public boolean canContinueToUse() {
        return CompanionMobSneakGuard.maySneak(this.companion)
                && this.isRunnable()
                && this.targetValidAndInLongSneakDistance();
    }

    private boolean targetValidAndInLongSneakDistance() {
        LivingEntity target = this.companion.getTarget();
        return target != null
                && target.isAlive()
                && this.companion.distanceToSqr(target) >= this.minTargetDistanceSqr;
    }

    @Override
    public void tick() {
        if (!CompanionMobSneakGuard.maySneak(this.companion)) {
            CompanionMobSneakGuard.clearCrouchIfBlocked(this.companion);
            return;
        }
        if (this.sneakTicksLeft > 0) {
            this.sneakTicksLeft--;
            if (this.sneakTicksLeft == 0) {
                this.companion.setPose(Pose.STANDING);
            } else if (!this.companion.isInWater()) {
                this.companion.setPose(Pose.CROUCHING);
            }
        } else if (this.companion.getRandom().nextInt(this.timeDelay) == 0) {
            this.sneakTicksLeft = this.sneakTime + this.companion.getRandom().nextInt(this.sneakRandomTime);
        }
    }
}
