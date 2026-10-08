package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthHandler;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorStealthSeventhHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.functional.ShellRender;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * TaCZ 的 {@link ShellRender} 在模型里用 {@code isSelf} 门控，原版只在
 * {@link net.minecraft.client.player.LocalPlayer} 的 {@code ItemInHandLayer} 上置 true，其它生物第三人称不画抛壳。
 * 本图层在渲染主手持枪臂时临时打开该门控。
 * <p>隐身套真正生效时不渲染手持物（含枪）。
 */
public class TaczMobItemInHandLayer<T extends net.minecraft.world.entity.Mob & TaczPlayerLikeMob>
        extends ItemInHandLayer<T, PlayerLikeHostileModel<T>> {

    public TaczMobItemInHandLayer(PlayerLikeHostileRenderer<T> renderer, ItemInHandRenderer itemInHandRenderer) {
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
        if (ArmorStealthHandler.isEffectivelyStealthed(livingEntity)
                || ArmorStealthSeventhHandler.isEffectivelyStealthed(livingEntity)) {
            return;
        }
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
