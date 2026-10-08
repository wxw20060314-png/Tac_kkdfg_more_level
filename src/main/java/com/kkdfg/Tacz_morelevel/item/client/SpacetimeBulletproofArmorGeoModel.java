package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.SpacetimeBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class SpacetimeBulletproofArmorGeoModel extends GeoModel<SpacetimeBulletproofArmorItem> {

    private static final String GEO = "spacetime_bulletproof";

    @Override
    public ResourceLocation getModelResource(SpacetimeBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpacetimeBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpacetimeBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            SpacetimeBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<SpacetimeBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
