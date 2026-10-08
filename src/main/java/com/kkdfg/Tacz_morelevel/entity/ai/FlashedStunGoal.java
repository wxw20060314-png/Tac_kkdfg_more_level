package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import java.util.EnumSet;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 闪光眩晕：有 {@link ModMobEffects#FLASHED} 时占用 MOVE/LOOK/JUMP，拦住枪战与移动 Goal。
 * 只读效果，不改 {@code NoAI}。
 */
public final class FlashedStunGoal extends Goal {

    private final Mob mob;

    public FlashedStunGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return !this.mob.level().isClientSide()
                && this.mob.isAlive()
                && this.mob.hasEffect(ModMobEffects.FLASHED.get());
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setAggressive(false);
    }

    @Override
    public void tick() {
        this.mob.getNavigation().stop();
        var vel = this.mob.getDeltaMovement();
        this.mob.setDeltaMovement(vel.x * 0.2D, vel.y, vel.z * 0.2D);
    }
}
