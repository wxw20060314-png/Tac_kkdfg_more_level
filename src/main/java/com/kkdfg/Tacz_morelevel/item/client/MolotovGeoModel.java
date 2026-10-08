package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.MolotovItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MolotovGeoModel extends GeoModel<MolotovItem> {
    @Override
    public ResourceLocation getModelResource(MolotovItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/item/molotov.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MolotovItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/item/molotov.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MolotovItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/molotov.animation.json");
    }
}
