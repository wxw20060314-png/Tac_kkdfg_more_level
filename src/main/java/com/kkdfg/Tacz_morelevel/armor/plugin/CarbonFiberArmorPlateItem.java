package com.kkdfg.Tacz_morelevel.armor.plugin;

import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 碳钎维护板：移速 / 跳跃 / 摔落。数值由构造传入。 */
public class CarbonFiberArmorPlateItem extends ArmorPluginItem {

    private final float speedMultiplyBase;
    private final float jumpDeltaVy;
    private final float fallReduction;

    public CarbonFiberArmorPlateItem(
            Properties properties,
            float speedMultiplyBase,
            float jumpDeltaVy,
            float fallReduction) {
        super(properties);
        this.speedMultiplyBase = speedMultiplyBase;
        this.jumpDeltaVy = jumpDeltaVy;
        this.fallReduction = fallReduction;
    }

    public float getSpeedMultiplyBase() {
        return this.speedMultiplyBase;
    }

    public float getJumpDeltaVy() {
        return this.jumpDeltaVy;
    }

    public float getFallReduction() {
        return this.fallReduction;
    }

    public static float sumSpeedMultiplyBase(LivingEntity entity) {
        float sum = 0f;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!(armor.getItem() instanceof BulletproofArmor)) {
                continue;
            }
            int slots = BulletproofArmor.pluginSlotCount(armor);
            for (int i = 0; i < slots; i++) {
                ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
                if (plugin.getItem() instanceof CarbonFiberArmorPlateItem plate) {
                    sum += plate.getSpeedMultiplyBase();
                }
            }
        }
        return sum;
    }

    public static double sumJumpDeltaVy(LivingEntity entity) {
        double sum = 0d;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!(armor.getItem() instanceof BulletproofArmor)) {
                continue;
            }
            int slots = BulletproofArmor.pluginSlotCount(armor);
            for (int i = 0; i < slots; i++) {
                ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
                if (plugin.getItem() instanceof CarbonFiberArmorPlateItem plate) {
                    sum += plate.getJumpDeltaVy();
                }
            }
        }
        return sum;
    }

    public static float sumFallReduction(ItemStack armor) {
        if (!(armor.getItem() instanceof BulletproofArmor)) {
            return 0f;
        }
        float sum = 0f;
        int slots = BulletproofArmor.pluginSlotCount(armor);
        for (int i = 0; i < slots; i++) {
            ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
            if (plugin.getItem() instanceof CarbonFiberArmorPlateItem plate) {
                sum += plate.getFallReduction();
            }
        }
        return sum;
    }
}
