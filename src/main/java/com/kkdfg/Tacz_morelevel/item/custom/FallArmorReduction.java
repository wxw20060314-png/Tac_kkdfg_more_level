package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.armor.plugin.CarbonFiberArmorPlateItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 各防弹套摔落减免按槽位相加，见 {@link com.kkdfg.Tacz_morelevel.event.FallArmorEvents}。 */
public final class FallArmorReduction {

    private FallArmorReduction() {}

    public static float perSlotReduction(ItemStack stack, EquipmentSlot slot) {
        float fromSet = 0f;
        if (stack.getItem() instanceof AbyssSeventhBulletproofArmorItem) {
            fromSet = AbyssSeventhBulletproofArmorItem.FALL_REDUCTION_PER_PIECE;
        } else if (stack.getItem() instanceof ShockwaveBulletproofArmorItem) {
            fromSet = ShockwaveBulletproofArmorItem.FALL_REDUCTION_PER_PIECE;
        } else if (stack.getItem() instanceof PowerMechBulletproofArmorItem
                && slot == EquipmentSlot.FEET) {
            fromSet = PowerMechBulletproofArmorItem.FALL_REDUCTION_BOOTS;
        }
        float fromPlate = CarbonFiberArmorPlateItem.sumFallReduction(stack);
        return fromSet + fromPlate;
    }

    /** 四盔甲槽摔落减免之和，上限 1。 */
    public static float total(LivingEntity entity) {
        float sum = 0f;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            sum += perSlotReduction(entity.getItemBySlot(slot), slot);
        }
        return Math.min(sum, 1f);
    }
}
