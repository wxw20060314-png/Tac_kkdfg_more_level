package com.kkdfg.Tacz_morelevel.entity.companion.menu;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.CompanionBodyguardAmmoHelper;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionEquipmentContainer;
import com.kkdfg.Tacz_morelevel.menu.ModMenus;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionEquipmentSlot;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionInventory;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionStorageSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public final class CompanionInventoryMenu extends AbstractContainerMenu {

    public static final int SLOT_SIZE = 18;

    
    public static final int COMPANION_PANEL_WIDTH = 176;

    
    public static final int OFFHAND_X = 127;
    public static final int OFFHAND_Y = 20;

    
    public static final int ARMOR_HEAD_X = 54;
    public static final int ARMOR_HEAD_Y = 6;
    public static final int ARMOR_CHEST_X = 54;
    public static final int ARMOR_CHEST_Y = 33;
    public static final int ARMOR_LEGS_X = 108;
    public static final int ARMOR_LEGS_Y = 6;
    public static final int ARMOR_FEET_X = 108;
    public static final int ARMOR_FEET_Y = 33;

    
    public static final int MAINHAND_X = 35;
    public static final int MAINHAND_Y = 20;

    
    public static final int STORAGE_X = 9;
    public static final int STORAGE_Y = 54;
    public static final int HOTBAR_Y = 112;

    public static final int CLOSE_BUTTON_X = 160;
    public static final int CLOSE_BUTTON_Y = 0;
    public static final int CLOSE_BUTTON_SIZE = 18;

    public static final int ENTITY_PREVIEW_X = 88;
    public static final int ENTITY_PREVIEW_Y = 45;

    
    public static final int PLAYER_INV_X = 9;
    public static final int PLAYER_INV_Y =140;
    public static final int PLAYER_HOTBAR_Y = PLAYER_INV_Y + 58;

    public static final int IMAGE_WIDTH = COMPANION_PANEL_WIDTH;
    public static final int IMAGE_HEIGHT = PLAYER_HOTBAR_Y + SLOT_SIZE + 4;

    public static final int EQUIPMENT_COUNT = 6;
    public static final int STORAGE_COUNT = CompanionInventory.STORAGE_SIZE;
    public static final int HOTBAR_COUNT = CompanionInventory.HOTBAR_SIZE;
    public static final int COMPANION_SLOT_COUNT = EQUIPMENT_COUNT + STORAGE_COUNT + HOTBAR_COUNT;
    public static final int PLAYER_INVENTORY_START = COMPANION_SLOT_COUNT;

    private final AbstractFriendlyCompanionEntity companion;
    private final CompanionEquipmentContainer equipmentContainer;

    public CompanionInventoryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(
                containerId,
                playerInventory,
                resolveCompanion(playerInventory, extraData.readVarInt()));
    }

    public CompanionInventoryMenu(
            int containerId, Inventory playerInventory, AbstractFriendlyCompanionEntity companion) {
        super(ModMenus.COMPANION_INVENTORY_MENU.get(), containerId);
        this.companion = companion;
        this.equipmentContainer = new CompanionEquipmentContainer(companion);
        this.addCompanionSlots(companion);
        this.addPlayerSlots(playerInventory);
    }

    private static AbstractFriendlyCompanionEntity resolveCompanion(Inventory playerInventory, int entityId) {
        if (playerInventory.player.level().getEntity(entityId)
                instanceof AbstractFriendlyCompanionEntity companion) {
            return companion;
        }
        throw new IllegalStateException("Unknown companion entity id: " + entityId);
    }

    private void addCompanionSlots(AbstractFriendlyCompanionEntity companion) {
        CompanionInventory inventory = companion.getCompanionInventory();

        this.addSlot(
                new CompanionEquipmentSlot(
                        this.equipmentContainer,
                        companion,
                        0,
                        EquipmentSlot.OFFHAND,
                        OFFHAND_X,
                        OFFHAND_Y));

        this.addSlot(
                new CompanionEquipmentSlot(
                        this.equipmentContainer,
                        companion,
                        1,
                        EquipmentSlot.HEAD,
                        ARMOR_HEAD_X,
                        ARMOR_HEAD_Y));
        this.addSlot(
                new CompanionEquipmentSlot(
                        this.equipmentContainer,
                        companion,
                        2,
                        EquipmentSlot.CHEST,
                        ARMOR_CHEST_X,
                        ARMOR_CHEST_Y));
        this.addSlot(
                new CompanionEquipmentSlot(
                        this.equipmentContainer,
                        companion,
                        3,
                        EquipmentSlot.LEGS,
                        ARMOR_LEGS_X,
                        ARMOR_LEGS_Y));
        this.addSlot(
                new CompanionEquipmentSlot(
                        this.equipmentContainer,
                        companion,
                        4,
                        EquipmentSlot.FEET,
                        ARMOR_FEET_X,
                        ARMOR_FEET_Y));

        this.addSlot(
                new CompanionEquipmentSlot(
                        this.equipmentContainer,
                        companion,
                        5,
                        EquipmentSlot.MAINHAND,
                        MAINHAND_X,
                        MAINHAND_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(
                        new CompanionStorageSlot(
                                inventory,
                                col + row * 9,
                                STORAGE_X + col * SLOT_SIZE,
                                STORAGE_Y + row * SLOT_SIZE));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(
                    new CompanionStorageSlot(
                            inventory,
                            CompanionInventory.HOTBAR_START + col,
                            STORAGE_X + col * SLOT_SIZE,
                            HOTBAR_Y));
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(
                        new Slot(
                                playerInventory,
                                col + row * 9 + 9,
                                PLAYER_INV_X + col * SLOT_SIZE,
                                PLAYER_INV_Y + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(
                    new Slot(playerInventory, col, PLAYER_INV_X + col * SLOT_SIZE, PLAYER_HOTBAR_Y));
        }
    }

    public AbstractFriendlyCompanionEntity companion() {
        return this.companion;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < COMPANION_SLOT_COUNT) {
            CompanionBodyguardAmmoHelper.stripDummyAmmoTag(stack);
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_INVENTORY_START + 27) {
            if (!this.moveToCompanion(stack)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveToCompanion(stack)
                && !this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_START + 27, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    private boolean moveToCompanion(ItemStack stack) {
        if (this.moveItemStackTo(stack, 0, EQUIPMENT_COUNT, false)) {
            return true;
        }
        if (this.moveItemStackTo(stack, EQUIPMENT_COUNT + STORAGE_COUNT, COMPANION_SLOT_COUNT, false)) {
            return true;
        }
        return this.moveItemStackTo(stack, EQUIPMENT_COUNT, EQUIPMENT_COUNT + STORAGE_COUNT, false);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.companion.getCompanionInventory().stillValid(player);
    }
}
