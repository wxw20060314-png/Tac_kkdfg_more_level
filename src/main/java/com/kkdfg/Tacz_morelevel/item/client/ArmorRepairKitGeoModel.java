package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.ArmorRepairKitItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ArmorRepairKitGeoModel extends GeoModel<ArmorRepairKitItem> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/item/armor_repair_kit.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/item/armor_repair_kit.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "animations/item/armor_repair_kit.animation.json");

    @Override
    public ResourceLocation getModelResource(ArmorRepairKitItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ArmorRepairKitItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ArmorRepairKitItem animatable) {
        return ANIMATION;
    }
}
