package com.kkdfg.Tacz_morelevel.armor.plugin;

import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import net.minecraft.world.item.ItemStack;

/** 防爆钢板：爆炸减伤。数值由构造传入。 */
public class BlastSteelArmorPlateItem extends ArmorPluginItem {

    private final float explosionReduction;

    public BlastSteelArmorPlateItem(Properties properties, float explosionReduction) {
        super(properties);
        this.explosionReduction = explosionReduction;
    }

    public float getExplosionReduction() {
        return this.explosionReduction;
    }

    public static float sumExplosionReduction(ItemStack armor) {
        if (!(armor.getItem() instanceof BulletproofArmor)) {
            return 0f;
        }
        float sum = 0f;
        int slots = BulletproofArmor.pluginSlotCount(armor);
        for (int i = 0; i < slots; i++) {
            ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
            if (plugin.getItem() instanceof BlastSteelArmorPlateItem plate) {
                sum += plate.getExplosionReduction();
            }
        }
        return sum;
    }
}
