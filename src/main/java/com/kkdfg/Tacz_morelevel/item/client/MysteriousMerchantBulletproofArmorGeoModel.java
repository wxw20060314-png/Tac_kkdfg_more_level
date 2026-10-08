package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.MysteriousMerchantBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MysteriousMerchantBulletproofArmorGeoModel
        extends GeoModel<MysteriousMerchantBulletproofArmorItem> {

    private static final String GEO = "mysterious_merchant_bulletproof";

    @Override
    public ResourceLocation getModelResource(MysteriousMerchantBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MysteriousMerchantBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(MysteriousMerchantBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            MysteriousMerchantBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<MysteriousMerchantBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
