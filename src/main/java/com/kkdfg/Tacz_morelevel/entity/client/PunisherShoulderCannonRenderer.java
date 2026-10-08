package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.PunisherShoulderCannonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class PunisherShoulderCannonRenderer extends GeoEntityRenderer<PunisherShoulderCannonEntity> {

    public PunisherShoulderCannonRenderer(EntityRendererProvider.Context context) {
        super(context, new PunisherShoulderCannonGeoModel());
        this.shadowRadius = 0.15F;
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void render(
            PunisherShoulderCannonEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight) {
        poseStack.pushPose();
        ShoulderCannonClientAttach.translateToHost(entity, partialTick, poseStack);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    protected void applyRotations(
            PunisherShoulderCannonEntity animatable,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
        float pitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.0D, -0.15D, 0.0D);
    }

    @Override
    protected float getDeathMaxRotation(PunisherShoulderCannonEntity entityLivingBaseIn) {
        return 0.0F;
    }

    @Override
    public int getPackedOverlay(PunisherShoulderCannonEntity animatable, float u, float partialTick) {
        return OverlayTexture.NO_OVERLAY;
    }
}
