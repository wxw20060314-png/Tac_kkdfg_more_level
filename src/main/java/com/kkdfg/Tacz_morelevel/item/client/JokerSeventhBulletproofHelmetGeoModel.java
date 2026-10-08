package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.JokerSeventhBulletproofHelmetItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class JokerSeventhBulletproofHelmetGeoModel extends GeoModel<JokerSeventhBulletproofHelmetItem> {

    private static final String GEO = "xiaocou_armor";

    @Override
    public ResourceLocation getModelResource(JokerSeventhBulletproofHelmetItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JokerSeventhBulletproofHelmetItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(JokerSeventhBulletproofHelmetItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            JokerSeventhBulletproofHelmetItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<JokerSeventhBulletproofHelmetItem> renderer) {
        return getModelResource(animatable);
    }
}
