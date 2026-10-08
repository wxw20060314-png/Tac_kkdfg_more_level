package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.SpacetimeStoneItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class SpacetimeStoneGeoModel extends GeoModel<SpacetimeStoneItem> {

    @Override
    public ResourceLocation getModelResource(SpacetimeStoneItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/item/spacetime_stone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpacetimeStoneItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/spacetime_stone.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpacetimeStoneItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/spacetime_stone.animation.json");
    }
}
