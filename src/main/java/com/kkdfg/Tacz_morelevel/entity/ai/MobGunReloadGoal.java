package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.util.AttachmentDataUtils;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * 士兵换弹：无目标且弹匣未满，或有目标且弹匣 {@code < 1} 时调用 {@link IGunOperator#reload()}；依赖
 * {@link com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper} 的虚拟备弹 / 背包直读补弹，使换弹能完成。
 */
public class MobGunReloadGoal extends Goal {
    private final Mob mob;

    public MobGunReloadGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !IGun.mainHandHoldGun(this.mob)) {
            return false;
        }
        ItemStack stack = this.mob.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null || gun.useInventoryAmmo(stack)) {
            return false;
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOpt.isEmpty()) {
            return false;
        }
        int max = Math.max(1, AttachmentDataUtils.getAmmoCountWithAttachment(stack, indexOpt.get().getGunData()));
        int cur = gun.getCurrentAmmoCount(stack);
        if (cur >= max) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target != null && cur >= 1) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
        ReloadState.StateType reloadType = op.getSynReloadState().getStateType();
        if (reloadType.isReloading()) {
            return false;
        }
        if (op.getSynDrawCoolDown() != 0L || op.getSynIsBolting()) {
            return false;
        }
        return target == null || cur < 1 || op.getSynShootCoolDown() == 0L;
    }

    @Override
    public boolean canContinueToUse() {
        return IGun.mainHandHoldGun(this.mob)
                && IGunOperator.fromLivingEntity(this.mob).getSynReloadState().getStateType().isReloading();
    }

    @Override
    public void start() {
        IGunOperator.fromLivingEntity(this.mob).reload();
    }
}
