package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.DetectionGrenadeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class DetectionGrenadeGeoModel extends GeoModel<DetectionGrenadeItem> {
    @Override
    public ResourceLocation getModelResource(DetectionGrenadeItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/item/detection_grenade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DetectionGrenadeItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/detection_grenade.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DetectionGrenadeItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/detection_grenade.animation.json");
    }
}
