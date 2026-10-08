package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.AbyssSeventhBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 与七级共用 geo/动画，实体贴图为深渊 UV。 */
public final class AbyssSeventhBulletproofArmorGeoModel extends GeoModel<AbyssSeventhBulletproofArmorItem> {

    private static final String SHARED_GEO = "seventh_bulletproof";
    private static final String ABYSS_TEXTURE = "seventh_abyss_bulletproof";

    public AbyssSeventhBulletproofArmorGeoModel() {}

    @Override
    public ResourceLocation getModelResource(AbyssSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + SHARED_GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + ABYSS_TEXTURE + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + SHARED_GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            AbyssSeventhBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<AbyssSeventhBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
