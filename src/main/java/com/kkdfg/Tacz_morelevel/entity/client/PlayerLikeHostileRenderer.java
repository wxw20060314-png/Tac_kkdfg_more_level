package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * 必须使用 {@link HumanoidMobRenderer}（而不是 {@link net.minecraft.client.renderer.entity.MobRenderer}），才会注册
 * {@link ItemInHandLayer}。主手 TaCZ 枪依赖该图层；另将默认 {@link ItemInHandLayer} 换为 {@link TaczMobItemInHandLayer}，
 * 以便第三人称抛壳的 {@code isSelf} 门控能通过。
 *
 * <p>原版 {@link HumanoidMobRenderer} 不注册盔甲层（与 {@link net.minecraft.client.renderer.entity.player.PlayerRenderer}
 * 不同），此处按玩家模型补上 {@link HumanoidArmorLayer}，与生成装备一致。
 * {@code slimArms=false} 为 Steve 宽臂；{@code true} 为 Alex 细臂（{@link ModelLayers#PLAYER_SLIM}）。
 *
 * <p>持枪时通过 {@link net.minecraftforge.client.extensions.common.IClientItemExtensions#getArmPose} 设置手臂姿态，
 * 与 {@link net.minecraft.client.renderer.entity.player.PlayerRenderer} 一致，使 TaCZ 枪可随俯仰上抬。
 */
public class PlayerLikeHostileRenderer<T extends net.minecraft.world.entity.Mob & TaczPlayerLikeMob>
        extends HumanoidMobRenderer<T, PlayerLikeHostileModel<T>> {

    /** Steve 宽臂（默认士兵）。 */
    public PlayerLikeHostileRenderer(EntityRendererProvider.Context context) {
        this(context, false);
    }

    /**
     * @param slimArms {@code true} → Alex 细臂 + slim 盔甲层
     */
    public PlayerLikeHostileRenderer(EntityRendererProvider.Context context, boolean slimArms) {
        super(
                context,
                new PlayerLikeHostileModel<>(
                        context.bakeLayer(slimArms ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER),
                        slimArms),
                0.5F);
        List<RenderLayer<T, PlayerLikeHostileModel<T>>> layers =
                Lists.newArrayList(this.layers);
        layers.removeIf(layer -> layer instanceof ItemInHandLayer);
        this.layers.clear();
        this.layers.add(
                new HumanoidArmorLayer<>(
                        this,
                        new HumanoidArmorModel<>(
                                context.bakeLayer(
                                        slimArms
                                                ? ModelLayers.PLAYER_SLIM_INNER_ARMOR
                                                : ModelLayers.PLAYER_INNER_ARMOR)),
                        new HumanoidArmorModel<>(
                                context.bakeLayer(
                                        slimArms
                                                ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR
                                                : ModelLayers.PLAYER_OUTER_ARMOR)),
                        context.getModelManager()));
        this.layers.addAll(layers);
        this.addLayer(new TaczMobItemInHandLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        this.setModelProperties(entity);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void setModelProperties(T entity) {
        PlayerLikeHostileModel model = this.getModel();
        model.applyPlayerSkinOverlayVisibility();
        if (!entity.isTaczCrawling()) {
            model.crouching = entity.isCrouching();
        }
        HumanoidModel.ArmPose mainPose = getArmPose(entity, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offPose = getArmPose(entity, InteractionHand.OFF_HAND);
        if (mainPose.isTwoHanded()) {
            offPose = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }
        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainPose;
            model.leftArmPose = offPose;
        } else {
            model.rightArmPose = offPose;
            model.leftArmPose = mainPose;
        }
    }

    private static HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        if (stack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
            UseAnim useAnim = stack.getUseAnimation();
            if (useAnim == UseAnim.BLOCK) {
                return HumanoidModel.ArmPose.BLOCK;
            }
            if (useAnim == UseAnim.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (useAnim == UseAnim.SPEAR) {
                return HumanoidModel.ArmPose.THROW_SPEAR;
            }
            if (useAnim == UseAnim.CROSSBOW && hand == entity.getUsedItemHand()) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (useAnim == UseAnim.SPYGLASS) {
                return HumanoidModel.ArmPose.SPYGLASS;
            }
            if (useAnim == UseAnim.TOOT_HORN) {
                return HumanoidModel.ArmPose.TOOT_HORN;
            }
            if (useAnim == UseAnim.BRUSH) {
                return HumanoidModel.ArmPose.BRUSH;
            }
        } else if (!entity.swinging && stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        HumanoidModel.ArmPose forgeArmPose =
                net.minecraftforge.client.extensions.common.IClientItemExtensions.of(stack)
                        .getArmPose(entity, hand, stack);
        if (forgeArmPose != null) {
            return forgeArmPose;
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getSoldierSkinTextureLocation();
    }

    @Override
    protected void scale(
            T entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    /** TaCZ 趴下使用 {@link net.minecraft.world.entity.Pose#SWIMMING}；战斗趴下时整体俯仰跟 {@code xRot}。 */
    @Override
    protected void setupRotations(
            T entity,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTicks) {
        float swim = entity.getSwimAmount(partialTicks);
        if (swim > 0.0F) {
            super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
            boolean crawlAim = entity.isTaczCrawling();
            float pitch =
                    crawlAim || entity.isInWater() ? -90.0F - entity.getXRot() : -90.0F;
            float rot = Mth.lerp(swim, 0.0F, pitch);
            poseStack.mulPose(Axis.XP.rotationDegrees(rot));
            if (entity.isVisuallySwimming()) {
                poseStack.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        }
    }
}
