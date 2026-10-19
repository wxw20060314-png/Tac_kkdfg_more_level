package com.kkdfg.Tacz_morelevel.integration.tacz;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/** 一格创造弹药箱。TaCZ 用 {@link #getStackInSlot} 就地扣弹，不抽出箱子。 */
public final class MobCreativeAmmoBoxHandler implements IItemHandler {

    private final NonNullList<ItemStack> slot;

    public MobCreativeAmmoBoxHandler(NonNullList<ItemStack> slot) {
        this.slot = slot;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        return this.slot.get(0);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }
}
