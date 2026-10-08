package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.DemonLordBodyguardEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** 魔王护航：宽臂 + {@code textures/entity/demon_lord_bodyguard/skin_*.png}。 */
public class DemonLordBodyguardRenderer
        extends CompanionPlayerLikeHostileRenderer<DemonLordBodyguardEntity, DemonLordBodyguardModel> {

    public DemonLordBodyguardRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonLordBodyguardModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F, false);
    }

    @Override
    public ResourceLocation getTextureLocation(DemonLordBodyguardEntity entity) {
        return entity.getDemonLordSkinTextureLocation();
    }
}
