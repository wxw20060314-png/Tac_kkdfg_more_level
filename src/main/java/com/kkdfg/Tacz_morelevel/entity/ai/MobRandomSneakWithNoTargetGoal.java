package com.kkdfg.Tacz_morelevel.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 间歇蹲下。{@link net.minecraft.client.model.PlayerModel} 用 {@link net.minecraft.world.entity.Entity#isCrouching()}，
 * 即姿势为 {@link Pose#CROUCHING}；仅 {@code setShiftKeyDown} 不会改 {@link Pose}，故模型不会蹲。
 */
public class MobRandomSneakWithNoTargetGoal extends Goal {

    private final Mob mob;
//    private int sneakTicksLeft;
//    protected int timedelay=100;
//    protected int sneaktime=15;
//    protected int sneakrandomtime=50;
    public MobRandomSneakWithNoTargetGoal(Mob mob) {
        this.mob = mob;
    }

    private boolean shouldStandUpFromCombatCrouch() {
        if (this.mob.level().isClientSide()) {
            return false;
        }
        var target = this.mob.getTarget();
        return target == null  && this.mob.getPose() == Pose.CROUCHING;
    }

    @Override
    public boolean canUse() {
        return shouldStandUpFromCombatCrouch();
    }

    @Override
    public boolean canContinueToUse() {
        return shouldStandUpFromCombatCrouch();
    }

    @Override
    public void tick() {
        if (!shouldStandUpFromCombatCrouch()) {
            return;
        }
        if (!this.mob.isInWater()) {
            this.mob.setPose(Pose.STANDING);
        }
    }
}
