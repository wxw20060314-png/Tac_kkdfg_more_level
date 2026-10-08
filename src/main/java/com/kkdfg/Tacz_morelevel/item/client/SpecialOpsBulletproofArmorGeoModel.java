package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.SpecialOpsBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class SpecialOpsBulletproofArmorGeoModel extends GeoModel<SpecialOpsBulletproofArmorItem> {

    private static final String GEO = "special_ops_bulletproof";

    @Override
    public ResourceLocation getModelResource(SpecialOpsBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpecialOpsBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpecialOpsBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            SpecialOpsBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<SpecialOpsBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
