package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 黑曜石盔甲：实体渲染使用 {@code textures/models/armor/obsidian_layer_1|2.png}。
 * <p>全套抗火：胸甲 {@link #inventoryTick}。
 */
public class ObsidianArmorItem extends ArmorItem {
    public ObsidianArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        int layer = slot == EquipmentSlot.LEGS ? 2 : 1;
        if ("overlay".equals(type)) {
            return null;
        }
        if (type != null) {
            return null;
        }
        return Tacz_MoreLevel.MOD_ID + ":textures/models/armor/obsidian_layer_" + layer + ".png";
    }

    /** 全套时每 tick 刷新抗火 II（40 tick）。 */
    public static void tickFullSetFireResistance(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (!hasFullObsidianSet(entity)) {
            return;
        }
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 1, false, false));
    }

    public static boolean hasFullObsidianSet(LivingEntity living) {
        return armorSlotsMatchItems(
                List.of(
                        living.getItemBySlot(EquipmentSlot.HEAD),
                        living.getItemBySlot(EquipmentSlot.CHEST),
                        living.getItemBySlot(EquipmentSlot.LEGS),
                        living.getItemBySlot(EquipmentSlot.FEET)),
                List.of(
                        ModItems.OBSIDIAN_HELMET.get(),
                        ModItems.OBSIDIAN_CHESTPLATE.get(),
                        ModItems.OBSIDIAN_LEGGINGS.get(),
                        ModItems.OBSIDIAN_BOOTS.get()));
    }

    private static boolean armorSlotsMatchItems(List<ItemStack> slots, List<Item> expectedItems) {
        if (slots.size() != expectedItems.size()) {
            return false;
        }
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getItem() != expectedItems.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || this.getType() != Type.CHESTPLATE) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            tickFullSetFireResistance(living);
        }
    }

    public boolean isCorrective(List<ItemStack> p_1, List<Item> p_2) {
        return armorSlotsMatchItems(p_1, p_2);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if (pStack.getItem() != ModItems.OBSIDIAN_CHESTPLATE.get()) {
            return;
        }
        pTooltipComponents.add(Component.literal("The obsidian material is able to grant fire resistance to the wearer"));
    }
}
