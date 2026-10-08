package com.kkdfg.Tacz_morelevel.entity.companion.inventory;

import com.tacz.guns.api.item.IAmmo;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

/** {@link CompanionInventory} {@link IItemHandler} */
public final class CompanionInventoryItemHandler implements IItemHandler {

    private final CompanionInventory inventory;

    public CompanionInventoryItemHandler(CompanionInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getSlots() {
        return this.inventory.getContainerSize();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.inventory.getItem(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || stack.isEmpty() || CompanionInventoryBlacklist.isBlocked(stack)) {
            return stack;
        }
        ItemStack existing = this.inventory.getItem(slot);
        if (!existing.isEmpty() && !ItemHandlerHelper.canItemStacksStack(existing, stack)) {
            return stack;
        }
        int max = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
        int space = max - existing.getCount();
        if (space <= 0) {
            return stack;
        }
        int move = Math.min(space, stack.getCount());
        if (!simulate) {
            ItemStack placed = stack.copy();
            placed.setCount(existing.getCount() + move);
            this.inventory.setItem(slot, placed);
        }
        if (move >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(move);
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.inventory.getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (stack.getItem() instanceof IAmmo) {
            int take = Math.min(amount, stack.getCount());
            if (take <= 0) {
                return ItemStack.EMPTY;
            }
            if (simulate) {
                ItemStack preview = stack.copy();
                preview.setCount(take);
                return preview;
            }
            return this.inventory.removeItem(slot, take);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !CompanionInventoryBlacklist.isBlocked(stack);
    }
}
