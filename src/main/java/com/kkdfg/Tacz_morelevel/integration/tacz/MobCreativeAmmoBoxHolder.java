package com.kkdfg.Tacz_morelevel.integration.tacz;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** 一格全类型创造弹药箱，交给 TaCZ {@code ITEM_HANDLER} 扫描。 */
public interface MobCreativeAmmoBoxHolder {

    NonNullList<ItemStack> mobCreativeAmmoBox();
}
