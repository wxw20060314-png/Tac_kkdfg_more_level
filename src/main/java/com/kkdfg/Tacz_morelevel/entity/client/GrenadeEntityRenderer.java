package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** 手雷投掷物模型；引信进度条见 {@link com.kkdfg.Tacz_morelevel.client.hud.GrenadeFuseHudOverlay}。 */
public final class GrenadeEntityRenderer extends ThrownItemRenderer<GrenadeEntity> {

    public GrenadeEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
