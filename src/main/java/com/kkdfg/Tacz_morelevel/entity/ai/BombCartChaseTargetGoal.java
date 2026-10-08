package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

/** 有 target 时冲过去；近距引爆由实体 {@link BombCartEntity#aiStep()} 处理。无跟随主人。 */
public class BombCartChaseTargetGoal extends Goal {

    private final BombCartEntity cart;
    private final double speedModifier;
    private Path path;
    private int recalcTimer;

    public BombCartChaseTargetGoal(BombCartEntity cart, double speedModifier) {
        this.cart = cart;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cart.getOwnerUUID() == null) {
            return false;
        }
        LivingEntity target = this.cart.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        this.path = this.cart.getNavigation().createPath(target, 0);
        return this.path != null || this.cart.distanceToSqr(target) <= BombCartEntity.DETONATE_RANGE * BombCartEntity.DETONATE_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.cart.getOwnerUUID() == null) {
            return false;
        }
        LivingEntity target = this.cart.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        PathNavigation navigation = this.cart.getNavigation();
        if (this.path != null) {
            navigation.moveTo(this.path, this.speedModifier);
        }
        this.recalcTimer = 0;
    }

    @Override
    public void stop() {
        this.cart.getNavigation().stop();
        this.path = null;
    }

    @Override
    public void tick() {
        LivingEntity target = this.cart.getTarget();
        if (target == null) {
            return;
        }
        this.cart.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--this.recalcTimer <= 0) {
            this.recalcTimer = 5;
            this.cart.getNavigation().moveTo(target, this.speedModifier);
        }
    }
}
