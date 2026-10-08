package com.kkdfg.Tacz_morelevel.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 友好同伴驯服交互；默认实现使用绿宝石，子类可覆盖 {@link #isTameItem}。 */
public interface CompanionTameable {

    boolean isTame();

    boolean isTameItem(ItemStack stack);

    /** @return {@code true} 表示驯服成功 */
    boolean tryTame(Player player, InteractionHand hand);

    default void onTameSuccess(Player player) {}
}
