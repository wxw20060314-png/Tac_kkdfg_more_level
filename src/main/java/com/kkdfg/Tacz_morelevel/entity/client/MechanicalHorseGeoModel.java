package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 机械马的几何、贴图和动画资源。 */
public final class MechanicalHorseGeoModel extends GeoModel<MechanicalHorseEntity> {

    private static final String ID = "mechanical_horse";

    @Override
    public ResourceLocation getModelResource(MechanicalHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/entity/" + ID + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MechanicalHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/entity/" + ID + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(MechanicalHorseEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/entity/" + ID + ".animation.json");
    }
}
