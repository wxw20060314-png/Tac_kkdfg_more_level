package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * 近战 Goal 常驻包装：仅允许徒手近战的时机可启用（有枪走远程 Goal，靠 {@code canUse} 互斥）。
 */
public final class SoldierUnarmedMeleeGoal extends Goal {

    private final PathfinderMob mob;
    private final MeleeAttackGoal melee;

    public SoldierUnarmedMeleeGoal(PathfinderMob mob, MeleeAttackGoal melee) {
        this.mob = mob;
        this.melee = melee;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.allowMelee() && this.melee.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.allowMelee() && this.melee.canContinueToUse();
    }

    private boolean allowMelee() {
        if (this.mob instanceof AbstractPlayerLikeSoldierEntity soldier) {
            return soldier.canUseUnarmedMeleeGoal();
        }
        if (this.mob instanceof AbstractFriendEntity friend) {
            return friend.canUseUnarmedMeleeGoal();
        }
        return !IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public void start() {
        this.melee.start();
    }

    @Override
    public void stop() {
        this.melee.stop();
    }

    @Override
    public void tick() {
        this.melee.tick();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.melee.requiresUpdateEveryTick();
    }
}
