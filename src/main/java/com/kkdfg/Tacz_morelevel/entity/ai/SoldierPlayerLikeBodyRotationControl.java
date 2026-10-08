package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

/**
 * 原版 {@link BodyRotationControl} 在客户端会把 {@code yBodyRot} 锁到 {@code yRot}，
 * 与 {@link MobGunPlayerLikeAimHelper#tickHeadTurnBodyFollow} 冲突；对 {@link PlayerLikeHeadBodyYawMob} 始终跳过。
 */
public final class SoldierPlayerLikeBodyRotationControl extends BodyRotationControl {

    private final Mob entity;

    public SoldierPlayerLikeBodyRotationControl(Mob mob) {
        super(mob);
        this.entity = mob;
    }

    @Override
    public void clientTick() {
        if (this.entity instanceof PlayerLikeHeadBodyYawMob) {
            return;
        }
        super.clientTick();
    }
}
