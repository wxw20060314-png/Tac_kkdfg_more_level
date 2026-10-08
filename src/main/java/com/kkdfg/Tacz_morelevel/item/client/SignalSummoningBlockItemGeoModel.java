package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.SignalSummoningBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class SignalSummoningBlockItemGeoModel extends GeoModel<SignalSummoningBlockItem> {
    @Override
    public ResourceLocation getModelResource(SignalSummoningBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/block/signal_summoning_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SignalSummoningBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/block/signal_summoning_block.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SignalSummoningBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/block/signal_summoning_block.animation.json");
    }
}
