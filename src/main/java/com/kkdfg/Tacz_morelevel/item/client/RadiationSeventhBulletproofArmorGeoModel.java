package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.RadiationSeventhBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 与七级共用 geo/动画，实体贴图为辐抗 UV。 */
public final class RadiationSeventhBulletproofArmorGeoModel
        extends GeoModel<RadiationSeventhBulletproofArmorItem> {

    private static final String SHARED_GEO = "seventh_bulletproof";
    private static final String RADIATION_TEXTURE = "seventh_radiation_bulletproof";

    @Override
    public ResourceLocation getModelResource(RadiationSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + SHARED_GEO + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RadiationSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + RADIATION_TEXTURE + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RadiationSeventhBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + SHARED_GEO + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            RadiationSeventhBulletproofArmorItem animatable,
            @Nullable
                    software.bernie.geckolib.renderer.GeoRenderer<RadiationSeventhBulletproofArmorItem>
                            renderer) {
        return getModelResource(animatable);
    }
}
