package com.kkdfg.Tacz_morelevel.entity.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * 只躲避：读 {@link SoldierThrowableThreatSense} 戳，校验实体后按种类距离跑开。
 */
public final class SoldierAvoidThrowableGoal<T extends PathfinderMob & SoldierThrowableThreatSense>
        extends Goal {

    private static final double FLEE_GRENADE = 10.0D;
    private static final double FLEE_FLASHBANG = 6.0D;
    private static final double FLEE_MOLOTOV = 6.0D;

    private final T mob;
    @Nullable private Vec3 threatPos;
    private double fleeDistance = FLEE_GRENADE;

    public SoldierAvoidThrowableGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.refreshFromStamp();
    }

    @Override
    public boolean canContinueToUse() {
        return this.refreshFromStamp();
    }

    @Override
    public void start() {
        this.tryMoveAway();
    }

    @Override
    public void tick() {
        if (this.mob.getNavigation().isDone() || this.mob.tickCount % 15 == 0) {
            if (this.refreshFromStamp()) {
                this.tryMoveAway();
            }
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.threatPos = null;
    }

    private boolean refreshFromStamp() {
        if (this.mob.level().isClientSide() || !this.mob.isEffectiveAi()) {
            this.threatPos = null;
            return false;
        }
        Entity threat = SoldierThrowableThreatScan.resolveValidThreat(this.mob, this.mob);
        if (threat == null) {
            this.threatPos = null;
            return false;
        }
        this.threatPos = threat.position();
        this.fleeDistance = Math.max(1.0D, fleeDistanceFor(this.mob.getThrowableThreatKind()));
        return true;
    }

    private static double fleeDistanceFor(@Nullable ThrowableThreatKind kind) {
        if (kind == null) {
            return 0.0D;
        }
        return switch (kind) {
            case GRENADE -> FLEE_GRENADE;
            case FLASHBANG -> FLEE_FLASHBANG;
            case MOLOTOV -> FLEE_MOLOTOV;
            case NONE -> 0.0D;
        };
    }

    private void tryMoveAway() {
        if (this.threatPos == null) {
            return;
        }
        int horiz = Mth.ceil(this.fleeDistance);
        Vec3 dest = DefaultRandomPos.getPosAway(this.mob, horiz, 7, this.threatPos);
        if (dest != null) {
            this.mob.getNavigation().moveTo(dest.x, dest.y, dest.z, 1.25D);
            return;
        }
        Vec3 away = this.mob.position().subtract(this.threatPos);
        if (away.lengthSqr() < 1.0E-6D) {
            away = new Vec3(0.0D, 0.0D, 1.0D);
        }
        away = away.normalize().scale(this.fleeDistance);
        Vec3 manual = this.mob.position().add(away);
        this.mob.getNavigation().moveTo(manual.x, manual.y, manual.z, 1.25D);
    }
}
