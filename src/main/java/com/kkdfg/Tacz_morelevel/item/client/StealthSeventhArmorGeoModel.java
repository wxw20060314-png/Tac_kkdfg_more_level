package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.StealthSeventhArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 潜行七级：{@code stealth_seventh} geo / 贴图 / 动画（qianxArmor + qianxing）。 */
public final class StealthSeventhArmorGeoModel extends GeoModel<StealthSeventhArmorItem> {

    private static final String GEO = "stealth_seventh";

    @Override
    public ResourceLocation getModelResource(StealthSeventhArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StealthSeventhArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + GEO + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(StealthSeventhArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            StealthSeventhArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<StealthSeventhArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
