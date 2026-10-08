package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/** 炸弹小车：车身 {@code CHE}，摄像头头 {@code HEAD}（随 look 转动）。 */
public final class BombCartGeoModel extends GeoModel<BombCartEntity> {

    private static final String ID = "bomb_cart";

    @Override
    public ResourceLocation getModelResource(BombCartEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/entity/" + ID + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BombCartEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/entity/" + ID + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BombCartEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/entity/" + ID + ".animation.json");
    }

    @Override
    public void setCustomAnimations(
            BombCartEntity animatable, long instanceId, AnimationState<BombCartEntity> animationState) {
        CoreGeoBone head = this.getAnimationProcessor().getBone("HEAD");
        if (head == null) {
            return;
        }
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (data == null) {
            return;
        }
        head.setRotX(data.headPitch() * Mth.DEG_TO_RAD);
        head.setRotY(data.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
