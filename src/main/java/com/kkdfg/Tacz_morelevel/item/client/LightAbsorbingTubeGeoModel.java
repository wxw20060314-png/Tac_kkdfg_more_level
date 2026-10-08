package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.LightAbsorbingTubeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class LightAbsorbingTubeGeoModel extends GeoModel<LightAbsorbingTubeItem> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "geo/item/light_absorbing_tube.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "textures/item/light_absorbing_tube.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "animations/item/light_absorbing_tube.animation.json");

    @Override
    public ResourceLocation getModelResource(LightAbsorbingTubeItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LightAbsorbingTubeItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LightAbsorbingTubeItem animatable) {
        return ANIMATION;
    }
}
