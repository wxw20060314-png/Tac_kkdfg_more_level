package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.PunisherShoulderCannonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class PunisherShoulderCannonGeoModel extends GeoModel<PunisherShoulderCannonEntity> {

    private static final String ID = "punisher_shoulder_cannon";

    @Override
    public ResourceLocation getModelResource(PunisherShoulderCannonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/entity/" + ID + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PunisherShoulderCannonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/entity/" + ID + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(PunisherShoulderCannonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/entity/" + ID + ".animation.json");
    }
}
