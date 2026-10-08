package com.kkdfg.Tacz_morelevel.block.client;

import com.kkdfg.Tacz_morelevel.block.entity.LimitedSpawnerBlockEntity;
import com.kkdfg.Tacz_morelevel.spawner.LimitedBaseSpawner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LimitedSpawnerRenderer implements BlockEntityRenderer<LimitedSpawnerBlockEntity> {

    private final EntityRenderDispatcher entityRenderer;

    public LimitedSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(
            LimitedSpawnerBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        LimitedBaseSpawner spawner = blockEntity.getSpawner();
        Entity entity =
                spawner.getOrCreateDisplayEntity(
                        blockEntity.getLevel(),
                        blockEntity.getLevel().getRandom(),
                        blockEntity.getBlockPos());
        if (entity != null) {
            float scale = 0.53125F;
            float size = Math.max(entity.getBbWidth(), entity.getBbHeight());
            if ((double) size > 1.0D) {
                scale /= size;
            }
            poseStack.translate(0.0F, 0.4F, 0.0F);
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            (float)
                                    Mth.lerp(
                                                    (double) partialTick,
                                                    spawner.getoSpin(),
                                                    spawner.getSpin())
                                            * 10.0F));
            poseStack.translate(0.0F, -0.2F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
            poseStack.scale(scale, scale, scale);
            this.entityRenderer.render(
                    entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, packedLight);
        }
        poseStack.popPose();
    }
}
