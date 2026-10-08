package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Pose;

/**
 * 与士兵 {@link com.kkdfg.Tacz_morelevel.entity.client.PlayerLikeHostileModel} 一致：
 * 只同步蹲/趴；{@code riding} 交给渲染器（坐船等乘客姿势）。
 * 陆上「指令停留」仍为站立 + 左手上举（乘客时不套用，见 {@link AbstractFriendlyCompanionEntity#shouldUseOrderedSitVisual()}）。
 */
public class CompanionPlayerLikeHostileModel<T extends AbstractFriendlyCompanionEntity>
        extends PlayerModel<T> {

    public CompanionPlayerLikeHostileModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        boolean crawling = entity.isTaczCrawling();
        if (crawling) {
            this.crouching = false;
        } else if (entity.shouldUseOrderedSitVisual()) {
            this.crouching = false;
        } else {
            this.crouching = entity.isCrouching();
        }
        // 不改 this.riding：与士兵一样，由 LivingEntityRenderer 按 isPassenger 设置
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        if (crawling && entity.getPose() == Pose.SWIMMING) {
            this.swimAmount = 0.3F;
        }
    }

    @Override
    public void setupAnim(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (entity.shouldUseOrderedSitVisual()) {
            this.crouching = false;
            super.setupAnim(entity, 0.0F, 0.0F, ageInTicks, netHeadYaw, headPitch);
            this.leftArm.xRot = CompanionBoatSitPoseHelper.ORDERED_SIT_LEFT_ARM_X_ROT;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.leftSleeve.copyFrom(this.leftArm);
            return;
        }
        float swing = limbSwing;
        float swingAmount = limbSwingAmount;
        if (this.attackTime > 0.0F) {
            swing = 0.0F;
            swingAmount = 0.0F;
        }
        super.setupAnim(entity, swing, swingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity.isTaczCrawling() && IGun.mainHandHoldGun(entity)) {
            this.rightArm.xRot += (float) Math.toRadians(-35.0);
            this.rightArm.yRot += (float) Math.toRadians(25.0);
            this.rightArm.zRot += (float) Math.toRadians(-40.0);
            this.leftArm.xRot += (float) Math.toRadians(-35.0);
            this.leftArm.yRot += (float) Math.toRadians(-25.0);
            this.leftArm.zRot += (float) Math.toRadians(40.0);
            this.rightSleeve.copyFrom(this.rightArm);
            this.leftSleeve.copyFrom(this.leftArm);
        }
    }
}
