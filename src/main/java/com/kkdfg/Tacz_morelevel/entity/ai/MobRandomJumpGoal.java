package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.api.JumpActingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/** 有存活攻击目标且当前看得见时，在地面且非液体中低概率跳跃（装饰用 AI）；走 {@link JumpActingMob#performRandomJump}（软窗口内随机档）。 */
public class MobRandomJumpGoal extends Goal {

    /** {@code < 0} 表示不限制与目标的距离。 */
    private static final double NO_MAX_DISTANCE = -1.0D;

    private final Mob mob;
    /** 每 tick 以 1/{@code jumpChance} 概率跳跃；{@code <= 0} 表示禁用跳跃。 */
    private final int jumpChance;
    /** 为 {@code false} 时本 Goal 不启用。 */
    private final boolean enabled;
    /** 与 target 平面距离须小于该值才跳；{@link #NO_MAX_DISTANCE} 为不限。 */
    private final double maxJumpDistance;

    public MobRandomJumpGoal(Mob mob) {
        this(mob, 28, true);
    }

    public MobRandomJumpGoal(Mob mob, int jumpChance, boolean enabled) {
        this(mob, jumpChance, enabled, NO_MAX_DISTANCE);
    }

    public MobRandomJumpGoal(Mob mob, int jumpChance, boolean enabled, double maxJumpDistance) {
        this.mob = mob;
        this.jumpChance = jumpChance;
        this.enabled = enabled;
        this.maxJumpDistance = maxJumpDistance;
    }

    @Override
    public boolean canUse() {
        return this.enabled
                && this.hasLivingTarget()
                && this.canSeeTarget()
                && this.isWithinJumpDistance();
    }

    @Override
    public boolean canContinueToUse() {
        return this.enabled
                && this.hasLivingTarget()
                && this.canSeeTarget()
                && this.isWithinJumpDistance();
    }

    private boolean hasLivingTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    private boolean canSeeTarget() {
        LivingEntity target = this.mob.getTarget();
        return target != null && this.mob.getSensing().hasLineOfSight(target);
    }

    private boolean isWithinJumpDistance() {
        if (this.maxJumpDistance < 0.0D) {
            return true;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return false;
        }
        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        return dx * dx + dz * dz < this.maxJumpDistance * this.maxJumpDistance;
    }

    @Override
    public void tick() {
        if (this.jumpChance <= 0) {
            return;
        }
        if (this.mob.onGround()
                && !this.mob.isInWater()
                && !this.mob.isInLava()
                && this.mob.getRandom().nextInt(this.jumpChance) == 0) {
            this.doJump();
        }
    }

    /** 子类可覆盖以自定义跳跃；默认 {@link JumpActingMob#performRandomJump}（[min, max] 随机档）。 */
    protected void doJump() {
        if (this.mob instanceof JumpActingMob jumper) {
            jumper.performRandomJump(this.mob.getRandom());
        } else {
            this.mob.getJumpControl().jump();
        }
    }
}
