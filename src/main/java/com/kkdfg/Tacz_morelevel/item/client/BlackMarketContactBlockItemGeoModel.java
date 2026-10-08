package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BlackMarketContactBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class BlackMarketContactBlockItemGeoModel extends GeoModel<BlackMarketContactBlockItem> {
    @Override
    public ResourceLocation getModelResource(BlackMarketContactBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/block/black_market_contact.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackMarketContactBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/block/black_market_contact.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackMarketContactBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/block/black_market_contact.animation.json");
    }
}
