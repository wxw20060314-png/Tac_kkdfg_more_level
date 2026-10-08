package com.kkdfg.Tacz_morelevel.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 间歇蹲下。{@link net.minecraft.client.model.PlayerModel} 用 {@link net.minecraft.world.entity.Entity#isCrouching()}，
 * 即姿势为 {@link Pose#CROUCHING}；仅 {@code setShiftKeyDown} 不会改 {@link Pose}，故模型不会蹲。
 */
public class MobRandomSneakWithTargetInShortDistanceGoal extends Goal {

    private final Mob mob;
    private int sneakTicksLeft;
    /** 无蹲下剩余 tick 时，每 tick 以 1/{@code timeDelay} 概率开始新一轮蹲下。 */
    private final int timeDelay;
    /** 单次蹲下持续的基础 tick 数。 */
    private final int sneakTime;
    /** 在 {@link #sneakTime} 之上附加的随机 tick 上限（不含）。 */
    private final int sneakRandomTime;
    /** 与当前目标距离的平方上限，与 {@link net.minecraft.world.entity.Entity#distanceToSqr} 比较（含 Y 差）；仅在此范围内才间歇蹲下。 */
    private final double maxTargetDistanceSqr;
    /** 为 {@code false} 时本 Goal 不启用。 */
    private final boolean enabled;

    public MobRandomSneakWithTargetInShortDistanceGoal(Mob mob) {
        this(mob, 45, 15, 50, 12.0D, true);
    }

    public MobRandomSneakWithTargetInShortDistanceGoal(
            Mob mob,
            int timeDelay,
            int sneakTime,
            int sneakRandomTime,
            double maxTargetDistance,
            boolean enabled) {
        this.mob = mob;
        this.timeDelay = timeDelay;
        this.sneakTime = sneakTime;
        this.sneakRandomTime = sneakRandomTime;
        this.maxTargetDistanceSqr = maxTargetDistance * maxTargetDistance;
        this.enabled = enabled;
    }

    @Override
    public boolean canUse() {
        return this.enabled && this.targetValidAndWithinSneakDistance();
    }

    @Override
    public boolean canContinueToUse() {
        return this.enabled && this.targetValidAndWithinSneakDistance();
    }

    private boolean targetValidAndWithinSneakDistance() {
        LivingEntity target = this.mob.getTarget();
        return target != null
                && target.isAlive()
                && this.mob.distanceToSqr(target) <= this.maxTargetDistanceSqr;
    }

    @Override
    public void tick() {
        if (this.sneakTicksLeft > 0) {
            this.sneakTicksLeft--;
            if (this.sneakTicksLeft == 0) {
                this.mob.setPose(Pose.STANDING);
            } else if (!this.mob.isInWater()) {
                this.mob.setPose(Pose.CROUCHING);
            }
        } else if (this.mob.getRandom().nextInt(this.timeDelay) == 0) {
            this.sneakTicksLeft = this.sneakTime + this.mob.getRandom().nextInt(this.sneakRandomTime);
        }
    }
}
