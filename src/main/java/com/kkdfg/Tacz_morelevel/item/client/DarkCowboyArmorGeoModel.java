package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.DarkCowboyArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class DarkCowboyArmorGeoModel extends GeoModel<DarkCowboyArmorItem> {

    private static final String GEO = "dark_cowboy";

    @Override
    public ResourceLocation getModelResource(DarkCowboyArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkCowboyArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(DarkCowboyArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            DarkCowboyArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<DarkCowboyArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
