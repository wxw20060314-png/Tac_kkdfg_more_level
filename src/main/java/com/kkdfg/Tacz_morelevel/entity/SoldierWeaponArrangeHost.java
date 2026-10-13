package com.kkdfg.Tacz_morelevel.entity;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/**
 * 主手武器整理 Goal 宿主（PlayerLike 基类线 / 史莱姆）。Holster、Dual 不实现挂载。
 */
public interface SoldierWeaponArrangeHost {

    Mob asArrangeLiving();

    @Nullable
    ResourceLocation getPreferredMainGunDataId();

    NonNullList<ItemStack> soldierBackpackSlots();

    void compactSoldierInventory();

    boolean tryPutIntoSoldierBackpack(ItemStack stack);

    void swapHandWithSoldierBackpackSlot(EquipmentSlot hand, int index);

    void swapMainAndOffhand();

    /** TaCZ 不在切枪/拉栓/换弹。 */
    boolean canOperateWeaponSwap();

    void afterCombatMainhandArrange(boolean hadMainGunBefore);

    void afterIdleMainhandArrange();

    /** 无目标闲置整理 TTL；默认与士兵基类一致。 */
    default int noTargetWeaponArrangeTtl() {
        return 20;
    }
}
