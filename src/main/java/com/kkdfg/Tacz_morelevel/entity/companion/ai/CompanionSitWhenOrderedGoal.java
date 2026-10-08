package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.api.CompanionMob;
import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class CompanionSitWhenOrderedGoal extends Goal {
    private final CompanionMob companion;

    public CompanionSitWhenOrderedGoal(CompanionMob companion) {
        this.companion = companion;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        return this.companion.isOrderedToSit();
    }

    @Override
    public boolean canUse() {
        if (!this.companion.isTame()) {
            return false;
        }
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.companion;
        if (mob.isInWaterOrBubble() || !mob.onGround()) {
            return false;
        }
        LivingEntity owner = this.companion.getOwner();
        if (owner != null
                && ((Entity) this.companion).distanceToSqr(owner) < 144.0D
                && owner.getLastHurtByMob() != null) {
            return false;
        }
        return this.companion.isOrderedToSit();
    }

    @Override
    public void start() {
        ((net.minecraft.world.entity.Mob) this.companion).getNavigation().stop();
    }

    @Override
    public void stop() {}
}
