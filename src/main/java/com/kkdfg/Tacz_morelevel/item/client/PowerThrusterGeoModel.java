package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PowerThrusterItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class PowerThrusterGeoModel extends GeoModel<PowerThrusterItem> {

    @Override
    public ResourceLocation getModelResource(PowerThrusterItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/item/" + animatable.getAssetId() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PowerThrusterItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/" + animatable.getAssetId() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(PowerThrusterItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID,
                "animations/item/" + animatable.getAssetId() + ".animation.json");
    }
}
