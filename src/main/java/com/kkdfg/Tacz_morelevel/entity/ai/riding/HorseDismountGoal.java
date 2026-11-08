package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** 骑着马且目标还在时，连续 200 tick 看不见就下马。 */
public class HorseDismountGoal extends Goal {

    private static final int UNSEEN_LIMIT = 100;

    private final Mob mob;
    private int unseen;

    public HorseDismountGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return this.ridingWithTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ridingWithTarget() != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        this.unseen = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.ridingWithTarget();
        if (target == null) {
            this.unseen = 0;
            return;
        }
        if (GunCombatSight.COMBAT.hasLineOfSight(this.mob, target)) {
            this.unseen = 0;
            return;
        }
        if (++this.unseen >= UNSEEN_LIMIT) {
            this.unseen = 0;
            this.mob.stopRiding();
        }
    }

    private LivingEntity ridingWithTarget() {
        if (!(this.mob.getVehicle() instanceof AbstractHorse) || !(this.mob instanceof HorseRidingNpc npc) || !npc.adoptsHorseRiding()) {
            return null;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return null;
        }
        return target;
    }
}
