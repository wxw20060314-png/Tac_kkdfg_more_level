package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** 这个 NPC 会不会把马类坐骑当成自己的移动方式。 */
public interface HorseRidingNpc {
    boolean adoptsHorseRiding();

    /** 会骑马，并且当前坐在马类身上。这时步行移动 Goal 停用。 */
    static boolean suppressesFootGoals(Mob mob) {
        return mob instanceof HorseRidingNpc riding
                && riding.adoptsHorseRiding()
                && mob.getVehicle() instanceof AbstractHorse;
    }
}
