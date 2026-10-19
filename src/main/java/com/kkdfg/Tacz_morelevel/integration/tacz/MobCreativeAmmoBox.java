package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 非护航持枪实体的一格全类型创造弹药箱。 */
public final class MobCreativeAmmoBox {

    public static final int CHECK_INTERVAL_TICKS = 20;

    private static final ResourceLocation AMMO_BOX_ITEM =
            ResourceLocation.fromNamespaceAndPath("tacz", "ammo_box");

    private MobCreativeAmmoBox() {}

    public static NonNullList<ItemStack> createSlot() {
        return NonNullList.withSize(1, ItemStack.EMPTY);
    }

    public static boolean isAllTypeCreative(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IAmmoBox box && box.isAllTypeCreative(stack);
    }

    /**
     * 每 {@link #CHECK_INTERVAL_TICKS} tick 复核。格子空了或不是全类型创造箱时立刻换上。
     * {@code tickCount} 在 {@code super.tick()} 之前读取，生成当拍为 0。
     */
    public static void ensure(NonNullList<ItemStack> slot, int tickCount) {
        ItemStack current = slot.get(0);
        if (tickCount % CHECK_INTERVAL_TICKS != 0 && isAllTypeCreative(current)) {
            return;
        }
        if (!isAllTypeCreative(current)) {
            slot.set(0, createAllTypeCreative());
        }
    }

    public static ItemStack createAllTypeCreative() {
        Item item = BuiltInRegistries.ITEM.get(AMMO_BOX_ITEM);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (stack.getItem() instanceof IAmmoBox box) {
            return box.setCreative(stack, true);
        }
        return ItemStack.EMPTY;
    }
}
