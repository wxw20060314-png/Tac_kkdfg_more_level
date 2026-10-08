package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.api.TaczGunSoundInvestigatingMob;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

/**
 * 向记忆中枪声位置寻路；无战斗目标时执行，到达或超时后结束。
 */
public final class MobInvestigateGunSoundGoal extends Goal {

    private final PathfinderMob navigatorMob;
    private final TaczGunSoundInvestigatingMob mob;
    /** 走向枪声源的移动速度倍率（叠加基础移速）。 */
    private static final double SPEED_MODIFIER = 1.05D;
    /** 水平距离小于该值（格²）视为到达并清除记忆。 */
    private static final double ARRIVE_HORIZONTAL_DIST_SQ = 3.5 * 3.5;

    public MobInvestigateGunSoundGoal(PathfinderMob mob) {
        if (!(mob instanceof TaczGunSoundInvestigatingMob investigating)) {
            throw new IllegalArgumentException("Mob must implement TaczGunSoundInvestigatingMob");
        }
        this.navigatorMob = mob;
        this.mob = investigating;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !this.navigatorMob.level().isClientSide()
                && this.navigatorMob.getTarget() == null
                && this.mob.hasPendingGunSoundInvestigation()
                && this.mob.getGunSoundInvestigatePos() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && !this.arrivedCloseEnough();
    }

    @Override
    public void start() {
        this.tryMoveTowardSound();
    }

    private void tryMoveTowardSound() {
        BlockPos target = this.mob.getGunSoundInvestigatePos();
        if (target == null) {
            return;
        }
        Path p = this.navigatorMob.getNavigation().createPath(target, 1);
        if (p != null) {
            this.navigatorMob.getNavigation().moveTo(p, SPEED_MODIFIER);
        }
    }

    @Override
    public void tick() {
        if (this.arrivedCloseEnough()) {
            this.mob.clearGunSoundInvestigation();
            this.navigatorMob.getNavigation().stop();
            return;
        }
        if (this.navigatorMob.getNavigation().isDone()) {
            this.tryMoveTowardSound();
        }
    }

    private boolean arrivedCloseEnough() {
        BlockPos target = this.mob.getGunSoundInvestigatePos();
        if (target == null) {
            return true;
        }
        double dx = this.navigatorMob.getX() - (target.getX() + 0.5);
        double dz = this.navigatorMob.getZ() - (target.getZ() + 0.5);
        return dx * dx + dz * dz <= ARRIVE_HORIZONTAL_DIST_SQ;
    }
}
