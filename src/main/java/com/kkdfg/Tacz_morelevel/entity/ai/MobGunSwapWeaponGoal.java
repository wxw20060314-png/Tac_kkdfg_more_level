package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.AbstractDualGunSoldierEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** 主手枪械弹匣过低时与背上（副手）枪械交换。 */
public final class MobGunSwapWeaponGoal extends Goal {

    private static final int SWAP_COOLDOWN_TICKS = 80;

    private final AbstractDualGunSoldierEntity mob;
    private int cooldown;

    public MobGunSwapWeaponGoal(AbstractDualGunSoldierEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || this.cooldown > 0) {
            return false;
        }
        if (!IGun.mainHandHoldGun(this.mob) || !this.mob.hasBackGun()) {
            return false;
        }
        if (this.mob.getTarget() == null) {
            return false;
        }
        IGun gun = IGun.getIGunOrNull(this.mob.getMainHandItem());
        if (gun == null) {
            return false;
        }
        return gun.getCurrentAmmoCount(this.mob.getMainHandItem()) <= 1;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        if (this.mob.swapActiveAndBackGun()) {
            this.cooldown = SWAP_COOLDOWN_TICKS;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void tick() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
