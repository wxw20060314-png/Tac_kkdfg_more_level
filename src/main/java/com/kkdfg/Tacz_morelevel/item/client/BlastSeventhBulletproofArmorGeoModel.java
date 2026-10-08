package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BlastSeventhBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class BlastSeventhBulletproofArmorGeoModel extends GeoModel<BlastSeventhBulletproofArmorItem> {

    private static final String GEO = "seventh_blast_bulletproof";

    @Override
    public ResourceLocation getModelResource(BlastSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlastSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlastSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            BlastSeventhBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<BlastSeventhBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
