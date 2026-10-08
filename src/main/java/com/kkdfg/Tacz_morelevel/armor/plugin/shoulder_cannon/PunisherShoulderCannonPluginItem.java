package com.kkdfg.Tacz_morelevel.armor.plugin.shoulder_cannon;

import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/** 判罚者肩炮插件：胸甲；生成 {@link com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.PunisherShoulderCannonEntity}。物品用原版平面贴图。 */
public class PunisherShoulderCannonPluginItem extends AbstractShoulderCannonPluginItem {

    public PunisherShoulderCannonPluginItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected AbstractShoulderCannonEntity createCannon(LivingEntity host) {
        return ModEntities.PUNISHER_SHOULDER_CANNON.get().create(host.level());
    }
}
