package com.kkdfg.Tacz_morelevel.armor.plugin;

import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** 防闪光护目镜：仅头盔；每档削减闪光剩余 tick 数由构造传入。 */
public class FlashResistGogglesItem extends ArmorPluginItem {

    private final int reduceTicks;

    public FlashResistGogglesItem(Properties properties, int reduceTicks) {
        super(properties, ArmorItem.Type.HELMET);
        this.reduceTicks = Math.max(0, reduceTicks);
    }

    public int getReduceTicks() {
        return this.reduceTicks;
    }

    /** 已穿头盔上安装的护目镜削减量之和。 */
    public static int sumReduceTicks(LivingEntity entity) {
        int sum = 0;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!(armor.getItem() instanceof BulletproofArmor bulletproof)) {
                continue;
            }
            if (bulletproof.getType() != ArmorItem.Type.HELMET) {
                continue;
            }
            int slots = BulletproofArmor.pluginSlotCount(armor);
            for (int i = 0; i < slots; i++) {
                ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
                if (plugin.getItem() instanceof FlashResistGogglesItem goggles) {
                    sum += goggles.getReduceTicks();
                }
            }
        }
        return sum;
    }
}
