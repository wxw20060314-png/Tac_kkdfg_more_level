package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * ??????????????????????{@code < 1} ??????{@code ITEM_HANDLER} ????????
 * TaCZ {@link IGunOperator#reload()}????{@link Goal.Flag#MOVE}/{@link Goal.Flag#LOOK}?? */
public class CompanionMobGunReloadGoal extends Goal {
    private final AbstractFriendEntity companion;

    public CompanionMobGunReloadGoal(AbstractFriendEntity companion) {
        this.companion = companion;
    }

    @Override
    public boolean canUse() {
        if (this.companion.level().isClientSide()
                || !this.companion.isTame()
                || !IGun.mainHandHoldGun(this.companion)) {
            return false;
        }
        ItemStack stack = this.companion.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null || gun.useInventoryAmmo(stack)) {
            return false;
        }
        Optional<com.tacz.guns.resource.index.CommonGunIndex> indexOpt =
                TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOpt.isEmpty()) {
            return false;
        }
        GunData gunData = indexOpt.get().getGunData();
        if (gunData.getReloadData().isInfinite()) {
            return false;
        }
        int maxMag = Math.max(1, AttachmentDataUtils.getAmmoCountWithAttachment(stack, gunData));
        int cur = gun.getCurrentAmmoCount(stack);
        if (cur >= maxMag) {
            return false;
        }
        LivingEntity target = this.companion.getTarget();
        if (target != null && cur >= 1) {
            return false;
        }
        if (!(this.companion instanceof com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity)) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this.companion);
        if (op.getSynReloadState().getStateType().isReloading()) {
            return false;
        }
        if (op.getSynDrawCoolDown() != 0L || op.getSynIsBolting()) {
            return false;
        }
        return target == null || cur < 1 || op.getSynShootCoolDown() == 0L;
    }

    @Override
    public boolean canContinueToUse() {
        return IGun.mainHandHoldGun(this.companion)
                && IGunOperator.fromLivingEntity(this.companion)
                        .getSynReloadState()
                        .getStateType()
                        .isReloading();
    }

    @Override
    public void start() {
        IGunOperator.fromLivingEntity(this.companion).reload();
    }
}
