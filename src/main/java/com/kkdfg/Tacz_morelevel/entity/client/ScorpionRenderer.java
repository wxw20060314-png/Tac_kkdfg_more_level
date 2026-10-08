package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.entity.custom.ScorpionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** 毒蝎：Alex 细臂（{@code PLAYER_SLIM}）+ {@code textures/entity/scorpion/skin_*.png}。 */
public final class ScorpionRenderer extends PlayerLikeHostileRenderer<ScorpionEntity> {

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, true);
    }
}
