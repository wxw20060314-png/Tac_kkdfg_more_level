package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** 挂在骑手身上。马身在水里时让马跳跃上浮。不占旗，避免被骑马移动关掉。 */
public class HorseWaterFloatGoal extends Goal {

    private final Mob mob;

    public HorseWaterFloatGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return this.horseInWater() != null;
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
    public void tick() {
        AbstractHorse horse = this.horseInWater();
        if (horse != null) {
            horse.getJumpControl().jump();
        }
    }

    private AbstractHorse horseInWater() {
        if (!(this.mob.getVehicle() instanceof AbstractHorse horse)
                || !(this.mob instanceof HorseRidingNpc npc)
                || !npc.adoptsHorseRiding()) {
            return null;
        }
        if (horse.isInWater() && horse.getFluidHeight(FluidTags.WATER) > horse.getFluidJumpThreshold()) {
            return horse;
        }
        return null;
    }
}
