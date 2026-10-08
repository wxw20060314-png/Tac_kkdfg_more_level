package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

/**
 * 默认玩家皮肤预览；蹲下 / 趴下与 {@link CompanionPlayerLikeHostileRenderer} 一致。
 */
public class VanillaSkinCompanionRenderer
        extends CompanionPlayerLikeHostileRenderer<
                AbstractFriendlyCompanionEntity, VanillaSkinCompanionModel> {

    public VanillaSkinCompanionRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new VanillaSkinCompanionModel(context.bakeLayer(ModelLayers.PLAYER), false),
                0.5F,
                false);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractFriendlyCompanionEntity entity) {
        GameProfile profile = new GameProfile(entity.getUUID(), "Companion");
        return DefaultPlayerSkin.getDefaultSkin(profile.getId());
    }
}
