package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractDualGunSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;

/**
 * 战争机器 RPG 乱射：数值同加特林（半径/近战比例/间隔），不卡看见，有目标且在射程内即打。
 * 仍要求手里是偏好副手（RPG）；切枪由 {@link WarMachineWeaponSwapGoal} 决定。
 */
public final class WarMachineRpgFireGoal extends MobGunRangedFireGoal<WarMachineEntity> {

    public WarMachineRpgFireGoal(WarMachineEntity mob) {
        super(
                mob,
                (float) Config.warMachineAttributes.gunRadius,
                (float) Config.warMachineAttributes.gunMeleeFrac,
                Config.warMachineAttributes.gunAttackIntervalMin,
                0,
                (m, t) -> true,
                false,
                AbstractDualGunSoldierEntity::isHoldingPreferredOffhandGun,
                MobGunRangedFireGoal.SeeTimeTune.LEGACY);
    }
}
