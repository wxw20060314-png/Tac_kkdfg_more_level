package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.MonitorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MonitorGeoModel extends GeoModel<MonitorItem> {

    @Override
    public ResourceLocation getModelResource(MonitorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/item/monitor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MonitorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/item/monitor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MonitorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/monitor.animation.json");
    }
}
