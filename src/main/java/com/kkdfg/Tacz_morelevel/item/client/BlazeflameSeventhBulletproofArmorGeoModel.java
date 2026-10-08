package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BlazeflameSeventhBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 与 {@link TierBulletproofArmorGeoModel} 相同的几何与动画（七级），仅实体贴图换为爆炎纹理。
 */
public final class BlazeflameSeventhBulletproofArmorGeoModel extends GeoModel<BlazeflameSeventhBulletproofArmorItem> {

    private static final String SHARED_GEO = "seventh_bulletproof";
    private static final String BLAZEFLAME_TEXTURE = "seventh_blazeflame_bulletproof";

    public BlazeflameSeventhBulletproofArmorGeoModel() {}

    @Override
    public ResourceLocation getModelResource(BlazeflameSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + SHARED_GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlazeflameSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + BLAZEFLAME_TEXTURE + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlazeflameSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + SHARED_GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            BlazeflameSeventhBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<BlazeflameSeventhBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
