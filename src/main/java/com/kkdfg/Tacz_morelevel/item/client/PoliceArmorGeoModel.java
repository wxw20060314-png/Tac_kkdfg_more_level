package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PoliceArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class PoliceArmorGeoModel extends GeoModel<PoliceArmorItem> {

    private static final String GEO = "police";

    @Override
    public ResourceLocation getModelResource(PoliceArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PoliceArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(PoliceArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            PoliceArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<PoliceArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
