package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.SculkSeventhBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 与七级共用 geo/动画，实体贴图为幽匿 UV。 */
public final class SculkSeventhBulletproofArmorGeoModel extends GeoModel<SculkSeventhBulletproofArmorItem> {

    private static final String SHARED_GEO = "seventh_bulletproof";
    private static final String SCULK_TEXTURE = "seventh_sculk_bulletproof";

    @Override
    public ResourceLocation getModelResource(SculkSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + SHARED_GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SculkSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + SCULK_TEXTURE + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SculkSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + SHARED_GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            SculkSeventhBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<SculkSeventhBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
