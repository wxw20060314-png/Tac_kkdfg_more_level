package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.FlashbangItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class FlashbangGeoModel extends GeoModel<FlashbangItem> {
    @Override
    public ResourceLocation getModelResource(FlashbangItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/item/flashbang.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FlashbangItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/item/flashbang.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FlashbangItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/flashbang.animation.json");
    }
}
