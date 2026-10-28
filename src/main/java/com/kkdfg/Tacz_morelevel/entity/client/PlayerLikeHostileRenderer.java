package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.api.TaczPlayerLikeMob;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

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

    /** CV {@link net.minecraft.client.renderer.entity.LivingEntityRenderer#render}；仅 85→45、2500→2025。 */
    @Override
    public void render(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        this.setModelProperties(entity);
        if (MinecraftForge.EVENT_BUS.post(
                new RenderLivingEvent.Pre<T, PlayerLikeHostileModel<T>>(
                        entity, this, partialTicks, poseStack, buffer, packedLight))) {
            return;
        }
        poseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(entity, partialTicks);

        boolean shouldSit =
                entity.isPassenger()
                        && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        this.model.riding = shouldSit;
        this.model.young = entity.isBaby();
        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float f2 = f1 - f;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) entity.getVehicle();
            f = Mth.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
            f2 = f1 - f;
            float f3 = Mth.wrapDegrees(f2);
            if (f3 < -56.25F) {
                f3 = -56.25F;
            }

            if (f3 >= 56.25F) {
                f3 = 56.25F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2025.0F) {
                f += f3 * 0.2F;
            }

            f2 = f1 - f;
        }

        float f6 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (isEntityUpsideDown(entity)) {
            f6 *= -1.0F;
            f2 *= -1.0F;
        }

        if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate(
                        (float) (-direction.getStepX()) * f4,
                        0.0F,
                        (float) (-direction.getStepZ()) * f4);
            }
        }

        float f7 = this.getBob(entity, partialTicks);
        this.setupRotations(entity, poseStack, f7, f, partialTicks);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (!shouldSit && entity.isAlive()) {
            f8 = entity.walkAnimation.speed(partialTicks);
            f5 = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) {
                f5 *= 3.0F;
            }

            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        this.model.prepareMobModel(entity, f5, f8, partialTicks);
        this.model.setupAnim(entity, f5, f8, f7, f2, f6);
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = this.isBodyVisible(entity);
        boolean flag1 = !flag && !entity.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(entity);
        RenderType rendertype = this.getRenderType(entity, flag, flag1, flag2);
        if (rendertype != null) {
            VertexConsumer vertexconsumer = buffer.getBuffer(rendertype);
            int i = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            this.model.renderToBuffer(
                    poseStack, vertexconsumer, packedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }

        if (!entity.isSpectator()) {
            for (RenderLayer<T, PlayerLikeHostileModel<T>> renderlayer : this.layers) {
                renderlayer.render(
                        poseStack, buffer, packedLight, entity, f5, f8, partialTicks, f7, f2, f6);
            }
        }

        poseStack.popPose();
        // 原版此处 super.render → EntityRenderer 名牌；这里不能 super（会回到 LivingEntityRenderer ±85）
        var renderNameTagEvent =
                new RenderNameTagEvent(
                        entity,
                        entity.getDisplayName(),
                        this,
                        poseStack,
                        buffer,
                        packedLight,
                        partialTicks);
        MinecraftForge.EVENT_BUS.post(renderNameTagEvent);
        if (renderNameTagEvent.getResult() != Event.Result.DENY
                && (renderNameTagEvent.getResult() == Event.Result.ALLOW
                        || this.shouldShowName(entity))) {
            this.renderNameTag(
                    entity, renderNameTagEvent.getContent(), poseStack, buffer, packedLight);
        }
        MinecraftForge.EVENT_BUS.post(
                new RenderLivingEvent.Post<T, PlayerLikeHostileModel<T>>(
                        entity, this, partialTicks, poseStack, buffer, packedLight));
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
