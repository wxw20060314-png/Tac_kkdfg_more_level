package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class MechanicalHorseRenderer extends GeoEntityRenderer<MechanicalHorseEntity> {

    public MechanicalHorseRenderer(EntityRendererProvider.Context context) {
        super(context, new MechanicalHorseGeoModel());
        this.shadowRadius = 0.75F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
