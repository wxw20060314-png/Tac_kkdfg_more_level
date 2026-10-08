package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/** {@link Pose#CROUCHING} {@link com.kkdfg.Tacz_morelevel.entity.ai.MobRandomSneakWithNoTargetGoal} */
public class CompanionMobRandomSneakWithNoTargetGoal extends Goal {

    private final AbstractFriendEntity companion;

    public CompanionMobRandomSneakWithNoTargetGoal(AbstractFriendEntity companion) {
        this.companion = companion;
    }

    private boolean shouldStandUpFromCombatCrouch() {
        if (this.companion.level().isClientSide()) {
            return false;
        }
        if (this.companion.isTaczCrawling()) {
            return false;
        }
        return this.companion.getTarget() == null && this.companion.getPose() == Pose.CROUCHING;
    }

    @Override
    public boolean canUse() {
        return this.shouldStandUpFromCombatCrouch();
    }

    @Override
    public boolean canContinueToUse() {
        return this.shouldStandUpFromCombatCrouch();
    }

    @Override
    public void tick() {
        if (!this.shouldStandUpFromCombatCrouch()) {
            return;
        }
        if (!this.companion.isInWater()) {
            this.companion.setPose(Pose.STANDING);
        }
    }
}
