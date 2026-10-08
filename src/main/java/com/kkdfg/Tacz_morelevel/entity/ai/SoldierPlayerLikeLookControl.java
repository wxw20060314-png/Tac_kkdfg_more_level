package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.api.TaczCrawlCapableMob;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

/**
 * 原版 {@link LookControl} 每 tick 会把 {@code xRot} 清零并限制头相对身；
 * 持枪交火时跳过，避免 NPC 无法抬头瞄准。
 */
public final class SoldierPlayerLikeLookControl extends LookControl {

    public SoldierPlayerLikeLookControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (isGunCombatAiming(this.mob)) {
            return;
        }
        super.tick();
    }

    static boolean isGunCombatAiming(Mob mob) {
        return mob instanceof TaczCrawlCapableMob crawl && crawl.isGunCombatAiming();
    }
}
