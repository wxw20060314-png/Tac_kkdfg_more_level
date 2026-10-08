package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;


public final class CompanionTamedGunGoal extends Goal {

    private final AbstractFriendEntity mob;
    private final Goal gun;

    public CompanionTamedGunGoal(AbstractFriendEntity mob, Goal gun) {
        this.mob = mob;
        this.gun = gun;
        EnumSet<Goal.Flag> flags = gun.getFlags();
        this.setFlags(flags.isEmpty() ? EnumSet.noneOf(Goal.Flag.class) : EnumSet.copyOf(flags));
    }

    @Override
    public boolean canUse() {
        return this.mob.canUseCompanionGunCombatGoal() && this.gun.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.canUseCompanionGunCombatGoal() && this.gun.canContinueToUse();
    }

    @Override
    public void start() {
        this.gun.start();
    }

    @Override
    public void stop() {
        this.gun.stop();
    }

    @Override
    public void tick() {
        this.gun.tick();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return this.gun.requiresUpdateEveryTick();
    }
}
