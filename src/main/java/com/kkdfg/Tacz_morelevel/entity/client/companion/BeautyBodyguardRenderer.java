package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.BeautyBodyguardEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 美女护航：{@link ModelLayers#PLAYER_SLIM}（细臂）+ {@code textures/entity/beauty_bodyguard/skin_*.png}。
 * 标准 / 高级 / 魔王见 {@link StandardBodyguardRenderer}、{@link EliteBodyguardRenderer}、{@link DemonLordBodyguardRenderer}。
 */
public class BeautyBodyguardRenderer
        extends CompanionPlayerLikeHostileRenderer<BeautyBodyguardEntity, BeautyBodyguardModel> {

    public BeautyBodyguardRenderer(EntityRendererProvider.Context context) {
        super(context, new BeautyBodyguardModel(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5F, true);
    }

    @Override
    public ResourceLocation getTextureLocation(BeautyBodyguardEntity entity) {
        return entity.getBeautySkinTextureLocation();
    }
}
