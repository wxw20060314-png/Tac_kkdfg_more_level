package com.kkdfg.Tacz_morelevel.armor.plugin;

import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 防弹钢板：子弹减伤 / 穿甲减免。数值由构造传入。 */
public class BallisticSteelArmorPlateItem extends ArmorPluginItem {

    private final float bulletproofBonus;
    private final float penetrationReductionBonus;

    public BallisticSteelArmorPlateItem(
            Properties properties, float bulletproofBonus, float penetrationReductionBonus) {
        super(properties);
        this.bulletproofBonus = bulletproofBonus;
        this.penetrationReductionBonus = penetrationReductionBonus;
    }

    public float getBulletproofBonus() {
        return this.bulletproofBonus;
    }

    public float getPenetrationReductionBonus() {
        return this.penetrationReductionBonus;
    }

    public static float sumBulletproofBonus(LivingEntity entity) {
        float sum = 0f;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!(armor.getItem() instanceof BulletproofArmor)) {
                continue;
            }
            int slots = BulletproofArmor.pluginSlotCount(armor);
            for (int i = 0; i < slots; i++) {
                ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
                if (plugin.getItem() instanceof BallisticSteelArmorPlateItem plate) {
                    sum += plate.getBulletproofBonus();
                }
            }
        }
        return sum;
    }

    public static float sumPenetrationReductionBonus(LivingEntity entity) {
        float sum = 0f;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!(armor.getItem() instanceof BulletproofArmor)) {
                continue;
            }
            int slots = BulletproofArmor.pluginSlotCount(armor);
            for (int i = 0; i < slots; i++) {
                ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
                if (plugin.getItem() instanceof BallisticSteelArmorPlateItem plate) {
                    sum += plate.getPenetrationReductionBonus();
                }
            }
        }
        return sum;
    }
}
