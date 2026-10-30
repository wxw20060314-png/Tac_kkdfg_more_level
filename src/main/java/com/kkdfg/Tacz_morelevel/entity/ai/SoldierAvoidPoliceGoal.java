package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.PoliceEntity;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 无仇恨时躲附近警察：每 {@link #SCAN_INTERVAL_TICKS} tick 扫一次；
 * 扫到则 {@code getPosAway} + {@code moveTo}，找不到点或 moveTo 失败则不动。
 */
public final class SoldierAvoidPoliceGoal extends Goal {

    private static final double RADIUS = 8.0D;
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final double MOVE_SPEED = 1.15D;

    private final PathfinderMob mob;
    private int cooldown;

    public SoldierAvoidPoliceGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.idleEligible()) {
            return false;
        }
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.cooldown = SCAN_INTERVAL_TICKS;
        PoliceEntity police = this.findNearestPolice();
        return police != null && this.tryMoveAway(police.position());
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.idleEligible()) {
            return false;
        }
        if (this.cooldown > 0) {
            this.cooldown--;
            return !this.mob.getNavigation().isDone();
        }
        this.cooldown = SCAN_INTERVAL_TICKS;
        PoliceEntity police = this.findNearestPolice();
        if (police == null) {
            return false;
        }
        return this.tryMoveAway(police.position()) || !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    private boolean idleEligible() {
        return !this.mob.level().isClientSide()
                && this.mob.isEffectiveAi()
                && this.mob.getTarget() == null;
    }

    @Nullable
    private PoliceEntity findNearestPolice() {
        AABB box = this.mob.getBoundingBox().inflate(RADIUS);
        PoliceEntity best = null;
        double bestSq = RADIUS * RADIUS;
        for (PoliceEntity p :
                this.mob
                        .level()
                        .getEntitiesOfClass(
                                PoliceEntity.class, box, e -> e.isAlive() && !e.isRemoved())) {
            double d2 = this.mob.distanceToSqr(p);
            if (d2 < bestSq) {
                bestSq = d2;
                best = p;
            }
        }
        return best;
    }

    /** @return {@code moveTo} 是否成功接上路径 */
    private boolean tryMoveAway(Vec3 policePos) {
        int horiz = Mth.ceil(RADIUS);
        Vec3 dest = DefaultRandomPos.getPosAway(this.mob, horiz, 7, policePos);
        if (dest == null) {
            return false;
        }
        return this.mob.getNavigation().moveTo(dest.x, dest.y, dest.z, MOVE_SPEED);
    }
}
