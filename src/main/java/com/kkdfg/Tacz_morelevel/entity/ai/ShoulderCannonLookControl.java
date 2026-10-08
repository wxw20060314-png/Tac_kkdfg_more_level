package com.kkdfg.Tacz_morelevel.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

/**
 * 有目标时不跑原版 {@link LookControl}（会清 {@code xRot}），俯仰由
 * {@link MobGunPlayerLikeAimHelper#applyPlayerLikeView} 写。
 */
public final class ShoulderCannonLookControl extends LookControl {

    public ShoulderCannonLookControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (this.mob.getTarget() != null) {
            return;
        }
        super.tick();
    }
}
