package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Pose;

/**
 * 与 {@link net.minecraft.client.renderer.entity.player.PlayerRenderer} 一致：在 {@link #prepareMobModel} 里把
 * {@link net.minecraft.client.model.HumanoidModel#crouching} 设为实体的 {@link AbstractPlayerLikeSoldierEntity#isCrouching()}，
 * 否则 {@link HumanoidModel#setupAnim} 里蹲下肢体偏移永远不会生效（碰撞箱已由 {@code Pose} 同步）。
 */
public class PlayerLikeHostileModel<T extends net.minecraft.world.entity.Mob & TaczPlayerLikeMob> extends PlayerModel<T> {

    public PlayerLikeHostileModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void prepareMobModel(
            T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        boolean crawling = entity.isTaczCrawling();
        if (crawling) {
            this.crouching = false;
        } else {
            this.crouching = entity.isCrouching();
        }
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
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
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
        // 紧贴 renderToBuffer 前恢复全套皮肤层（含手臂内层 + 衣袖外层，即手部 overlay）
        applyPlayerSkinOverlayVisibility();
    }

    /** 与 {@link net.minecraft.client.renderer.entity.player.PlayerRenderer#setModelProperties} 中外层开关一致；NPC 无皮肤层设置，全部显示。 */
    public void applyPlayerSkinOverlayVisibility() {
        this.setAllVisible(true);
        this.hat.visible = true;
        this.jacket.visible = true;
        this.leftSleeve.visible = true;
        this.rightSleeve.visible = true;
        this.leftPants.visible = true;
        this.rightPants.visible = true;
        this.leftArm.visible = true;
        this.rightArm.visible = true;
    }
}
