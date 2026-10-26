package com.kkdfg.Tacz_morelevel.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 【TEST】乘骑时原版用 ±85° 限制头相对「假身子」；临时改成 ±45° 验证假身子路径。
 * 测完只删本类，并从 {@code tacz_morelevel.mixins.json} 的 {@code client} 去掉本类名；
 * Mixin 通道（插件 / json / mods.toml）保留。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererRidingYawTestMixin {

    /** 原版 {@code f3 >= 85.0F} / 赋值为 85。 */
    @ModifyConstant(method = "render", constant = @Constant(floatValue = 85.0F))
    private float taczmorelevel$ridingHeadBodyClampPos(float original) {
        return 45.0F;
    }

    /** 原版 {@code f3 < -85.0F} / 赋值为 -85。 */
    @ModifyConstant(method = "render", constant = @Constant(floatValue = -85.0F))
    private float taczmorelevel$ridingHeadBodyClampNeg(float original) {
        return -45.0F;
    }

    /**
     * 原版软跟阈值 {@code f3*f3 > 2500}（|f3|>50）。上限改 45 后改为 45²，
     * 否则夹到 45 时软跟永远不触发。
     */
    @ModifyConstant(method = "render", constant = @Constant(floatValue = 2500.0F))
    private float taczmorelevel$ridingSoftFollowThreshold(float original) {
        return 45.0F * 45.0F;
    }
}
