package com.kkdfg.Tacz_morelevel.entity.companion.inventory;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public final class CompanionEquipmentContainer implements Container {

    public static final int SIZE = 6;

    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.OFFHAND,
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET,
        EquipmentSlot.MAINHAND
    };

    private final AbstractFriendlyCompanionEntity companion;

    public CompanionEquipmentContainer(AbstractFriendlyCompanionEntity companion) {
        this.companion = companion;
    }

    public static EquipmentSlot slotToEquipment(int slot) {
        return slot >= 0 && slot < SIZE ? SLOTS[slot] : EquipmentSlot.MAINHAND;
    }

    public static EquipmentSlot[] allEquipmentSlots() {
        return SLOTS.clone();
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (EquipmentSlot slot : SLOTS) {
            if (!this.companion.getItemBySlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SIZE ? this.companion.getItemBySlot(SLOTS[slot]) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SIZE) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.split(Math.min(amount, stack.getCount()));
        this.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack current = this.getItem(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.companion.setItemSlot(SLOTS[slot], ItemStack.EMPTY);
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SIZE) {
            this.companion.setItemSlot(SLOTS[slot], stack);
        }
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return this.companion.getCompanionInventory().stillValid(player);
    }

    @Override
    public void clearContent() {
        for (EquipmentSlot slot : SLOTS) {
            this.companion.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
