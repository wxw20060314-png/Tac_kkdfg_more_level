package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.EnergyCoreItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class EnergyCoreGeoModel extends GeoModel<EnergyCoreItem> {

    @Override
    public ResourceLocation getModelResource(EnergyCoreItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/item/" + animatable.getAssetId() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EnergyCoreItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/" + animatable.getAssetId() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(EnergyCoreItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/" + animatable.getAssetId() + ".animation.json");
    }
}
