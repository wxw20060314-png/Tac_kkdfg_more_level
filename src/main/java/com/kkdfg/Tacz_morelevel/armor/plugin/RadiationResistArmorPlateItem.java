package com.kkdfg.Tacz_morelevel.armor.plugin;

import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import com.kkdfg.Tacz_morelevel.item.custom.RadiationSeventhBulletproofArmorItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 辐射抗板：抗性点数由构造传入；与辐射等级（amplifier+1）比较见事件。 */
public class RadiationResistArmorPlateItem extends ArmorPluginItem {

    private final float resistance;

    public RadiationResistArmorPlateItem(Properties properties, float resistance) {
        super(properties);
        this.resistance = resistance;
    }

    public float getResistance() {
        return this.resistance;
    }

    /** 已穿防弹甲上安装的辐射抗板之和。 */
    public static float sumResistance(LivingEntity entity) {
        float sum = 0f;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!(armor.getItem() instanceof BulletproofArmor)) {
                continue;
            }
            int slots = BulletproofArmor.pluginSlotCount(armor);
            for (int i = 0; i < slots; i++) {
                ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
                if (plugin.getItem() instanceof RadiationResistArmorPlateItem plate) {
                    sum += plate.getResistance();
                }
            }
        }
        return sum;
    }

    /**
     * 最终抗性 = 抗板 + 辐抗七级每件 0.8 + 综合免疫（amplifier+1，无则 0）。
     */
    public static float totalResistance(LivingEntity entity) {
        float sum =
                sumResistance(entity)
                        + RadiationSeventhBulletproofArmorItem.countEquippedPieces(entity)
                                * RadiationSeventhBulletproofArmorItem.RADIATION_RESISTANCE_PER_PIECE;
        MobEffectInstance immunity = entity.getEffect(ModMobEffects.COMPREHENSIVE_IMMUNITY.get());
        if (immunity != null) {
            sum += Math.max(1, immunity.getAmplifier() + 1);
        }
        return sum;
    }
}
