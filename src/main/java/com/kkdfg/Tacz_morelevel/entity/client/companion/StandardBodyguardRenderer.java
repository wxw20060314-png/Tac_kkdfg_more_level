package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.StandardBodyguardEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** 标准护航：{@link ModelLayers#PLAYER}（宽臂）+ {@code textures/entity/standard_bodyguard/skin_*.png}。 */
public class StandardBodyguardRenderer
        extends CompanionPlayerLikeHostileRenderer<StandardBodyguardEntity, StandardBodyguardModel> {

    public StandardBodyguardRenderer(EntityRendererProvider.Context context) {
        super(context, new StandardBodyguardModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F, false);
    }

    @Override
    public ResourceLocation getTextureLocation(StandardBodyguardEntity entity) {
        return entity.getStandardSkinTextureLocation();
    }
}
