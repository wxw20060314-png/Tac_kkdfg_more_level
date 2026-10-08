package com.kkdfg.Tacz_morelevel.entity.companion.inventory;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public final class CompanionInventory implements Container {

    public static final int STORAGE_SIZE = 27;
    public static final int HOTBAR_SIZE = 9;
    public static final int TOTAL_SIZE = STORAGE_SIZE + HOTBAR_SIZE;

    public static final int HOTBAR_START = STORAGE_SIZE;

    private final AbstractFriendlyCompanionEntity companion;
    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SIZE, ItemStack.EMPTY);

    public CompanionInventory(AbstractFriendlyCompanionEntity companion) {
        this.companion = companion;
    }

    public AbstractFriendlyCompanionEntity companion() {
        return this.companion;
    }

    @Override
    public int getContainerSize() {
        return TOTAL_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < TOTAL_SIZE ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= TOTAL_SIZE) {
            return;
        }
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public void setChanged() {
        
    }

    @Override
    public boolean stillValid(Player player) {
        return this.companion.isAlive()
                && this.companion.isTame()
                && this.companion.isOwnedBy(player)
                && player.distanceToSqr(this.companion) < 64.0D;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("Items", this.saveItemList());
        return tag;
    }

    public void load(CompoundTag tag) {
        for (int i = 0; i < TOTAL_SIZE; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        if (tag.contains("Items", Tag.TAG_LIST)) {
            this.loadItemList(tag.getList("Items", Tag.TAG_COMPOUND));
        }
    }

    public ListTag saveItemList() {
        ListTag list = new ListTag();
        for (int i = 0; i < TOTAL_SIZE; i++) {
            ItemStack stack = this.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                stack.save(itemTag);
                list.add(itemTag);
            }
        }
        return list;
    }

    public void loadItemList(ListTag list) {
        for (int i = 0; i < TOTAL_SIZE; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < TOTAL_SIZE) {
                this.items.set(slot, ItemStack.of(itemTag));
            }
        }
    }
}
