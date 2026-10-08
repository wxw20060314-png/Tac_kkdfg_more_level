package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class BombCartRenderer extends GeoEntityRenderer<BombCartEntity> {

    public BombCartRenderer(EntityRendererProvider.Context context) {
        super(context, new BombCartGeoModel());
        this.shadowRadius = 0.4F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    protected float getDeathMaxRotation(BombCartEntity entityLivingBaseIn) {
        return 0.0F;
    }

    /** 受伤不叠红闪。 */
    @Override
    public int getPackedOverlay(BombCartEntity animatable, float u, float partialTick) {
        return OverlayTexture.NO_OVERLAY;
    }
}
