package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ShockwaveBulletproofArmorGeoModel extends GeoModel<ShockwaveBulletproofArmorItem> {

    private static final String GEO = "shockwave_bulletproof";

    @Override
    public ResourceLocation getModelResource(ShockwaveBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShockwaveBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShockwaveBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            ShockwaveBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<ShockwaveBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
