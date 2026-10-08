package com.kkdfg.Tacz_morelevel.entity.companion.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** {@link CompanionInventoryBlacklist} */
public final class CompanionStorageSlot extends Slot {

    public CompanionStorageSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !CompanionInventoryBlacklist.isBlocked(stack);
    }
}
