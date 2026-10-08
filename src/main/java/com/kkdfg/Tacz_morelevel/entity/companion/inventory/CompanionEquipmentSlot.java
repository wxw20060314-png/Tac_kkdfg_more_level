package com.kkdfg.Tacz_morelevel.entity.companion.inventory;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class CompanionEquipmentSlot extends Slot {

    private static final ResourceLocation[] EMPTY_ARMOR_ICONS = {
        InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
        InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
        InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
        InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
    };

    private final AbstractFriendlyCompanionEntity companion;
    private final EquipmentSlot equipmentSlot;

    public CompanionEquipmentSlot(
            CompanionEquipmentContainer container,
            AbstractFriendlyCompanionEntity companion,
            int containerIndex,
            EquipmentSlot equipmentSlot,
            int x,
            int y) {
        super(container, containerIndex, x, y);
        this.companion = companion;
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (CompanionInventoryBlacklist.isBlocked(stack)) {
            return false;
        }
        return stack.canEquip(this.equipmentSlot, this.companion);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = this.getItem();
        return !stack.isEmpty()
                && !player.isCreative()
                && EnchantmentHelper.hasBindingCurse(stack)
                ? false
                : super.mayPickup(player);
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        super.setByPlayer(stack);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        if (this.equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_ICONS[this.equipmentSlot.getIndex()]);
        }
        if (this.equipmentSlot == EquipmentSlot.OFFHAND) {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
        }
        return super.getNoItemIcon();
    }
}
