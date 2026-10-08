package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthHandler;
import com.kkdfg.Tacz_morelevel.item.custom.StealthCloakArmorItem;
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

/**
 * 关隐身：半透明铁甲（translucent）。
 * 开隐身（真正生效）：整层盔甲不渲染。
 */
public final class StealthCloakArmorRenderer extends GeoArmorRenderer<StealthCloakArmorItem> {

    public StealthCloakArmorRenderer() {
        super(new StealthCloakArmorGeoModel());
    }

    @Override
    public RenderType getRenderType(
            StealthCloakArmorItem animatable,
            ResourceLocation texture,
            @Nullable MultiBufferSource bufferSource,
            float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            StealthCloakArmorItem animatable,
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
                && ArmorStealthHandler.isEffectivelyStealthed(living)) {
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
