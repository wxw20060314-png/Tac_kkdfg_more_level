package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.functional.ShellRender;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CompanionTaczItemInHandLayer<T extends AbstractFriendlyCompanionEntity, M extends PlayerModel<T>>
        extends ItemInHandLayer<T, M> {

    public CompanionTaczItemInHandLayer(HumanoidMobRenderer<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
    }

    @Override
    protected void renderArmWithItem(
            LivingEntity livingEntity,
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        boolean allowShell =
                IGun.mainHandHoldGun(livingEntity) && arm == livingEntity.getMainArm() && !itemStack.isEmpty();
        if (allowShell) {
            ShellRender.isSelf = true;
        }
        try {
            super.renderArmWithItem(livingEntity, itemStack, displayContext, arm, poseStack, buffer, packedLight);
        } finally {
            if (allowShell) {
                ShellRender.isSelf = false;
            }
        }
    }
}
