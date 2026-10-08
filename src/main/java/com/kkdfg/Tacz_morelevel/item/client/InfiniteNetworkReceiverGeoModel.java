package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.InfiniteNetworkReceiverItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class InfiniteNetworkReceiverGeoModel extends GeoModel<InfiniteNetworkReceiverItem> {

    @Override
    public ResourceLocation getModelResource(InfiniteNetworkReceiverItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/item/infinite_network_receiver.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(InfiniteNetworkReceiverItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/infinite_network_receiver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(InfiniteNetworkReceiverItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/infinite_network_receiver.animation.json");
    }
}
