package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.EliteBodyguardEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** 高级护航：宽臂 + {@code textures/entity/elite_bodyguard/skin_*.png}。 */
public class EliteBodyguardRenderer
        extends CompanionPlayerLikeHostileRenderer<EliteBodyguardEntity, EliteBodyguardModel> {

    public EliteBodyguardRenderer(EntityRendererProvider.Context context) {
        super(context, new EliteBodyguardModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F, false);
    }

    @Override
    public ResourceLocation getTextureLocation(EliteBodyguardEntity entity) {
        return entity.getEliteSkinTextureLocation();
    }
}
