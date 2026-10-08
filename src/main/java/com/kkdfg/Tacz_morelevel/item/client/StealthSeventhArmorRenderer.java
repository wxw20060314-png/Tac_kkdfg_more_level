package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthSeventhHandler;
import com.kkdfg.Tacz_morelevel.item.custom.StealthSeventhArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** 潜行七级：{@code stealth_seventh} + glowmask；真正隐身时不渲染盔甲层。 */
public final class StealthSeventhArmorRenderer extends GeoArmorRenderer<StealthSeventhArmorItem> {

    public StealthSeventhArmorRenderer() {
        super(new StealthSeventhArmorGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(
            StealthSeventhArmorItem animatable,
            ResourceLocation texture,
            @Nullable MultiBufferSource bufferSource,
            float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            StealthSeventhArmorItem animatable,
            BakedGeoModel model,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha) {
        Entity entity = getCurrentEntity();
        if (entity instanceof LivingEntity living
                && ArmorStealthSeventhHandler.isEffectivelyStealthed(living)) {
            return;
        }
        super.actuallyRender(
                poseStack,
                animatable,
                model,
                renderType,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha);
    }
}
