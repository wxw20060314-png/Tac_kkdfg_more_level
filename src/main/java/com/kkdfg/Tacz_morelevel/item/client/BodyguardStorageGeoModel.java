package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BodyguardStorageItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class BodyguardStorageGeoModel extends GeoModel<BodyguardStorageItem> {

    @Override
    public ResourceLocation getModelResource(BodyguardStorageItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/item/bodyguard_storage.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BodyguardStorageItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/bodyguard_storage.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BodyguardStorageItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/bodyguard_storage.animation.json");
    }
}
