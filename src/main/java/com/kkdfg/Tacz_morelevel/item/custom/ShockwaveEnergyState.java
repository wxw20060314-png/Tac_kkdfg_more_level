package com.kkdfg.Tacz_morelevel.item.custom;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 冲击波能量只读门面（HUD / 查询）；数据在胸甲 NBT。 */
public final class ShockwaveEnergyState {

    private ShockwaveEnergyState() {}

    public static boolean hasFullSet(LivingEntity entity) {
        return ShockwaveBulletproofArmorItem.hasFullSet(entity);
    }

    public static ItemStack chest(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST);
    }

    public static int getEnergy(LivingEntity entity) {
        if (!hasFullSet(entity)) {
            return 0;
        }
        return ShockwaveBulletproofArmorItem.getEnergy(chest(entity));
    }

    public static float getEnergyRatio(LivingEntity entity) {
        return getEnergy(entity) / (float) ShockwaveBulletproofArmorItem.ENERGY_MAX;
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        if (!hasFullSet(entity)) {
            return false;
        }
        return ShockwaveBulletproofArmorItem.isDashOnCooldown(
                chest(entity), entity.level().getGameTime());
    }
}
