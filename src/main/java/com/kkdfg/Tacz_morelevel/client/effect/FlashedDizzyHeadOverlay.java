package com.kkdfg.Tacz_morelevel.client.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.effect.FlashedVisualSync;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 有闪光同步标记时在头顶画晕眩循环贴图。
 *
 * <p>挂在 {@link RenderLevelStageEvent.Stage#AFTER_ENTITIES}；是否闪光见 {@link FlashedVisualSync}
 * （服务端识别效果后经跟踪包同步，不读客户端生物效果表）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FlashedDizzyHeadOverlay {

    private static final int FRAME_COUNT = 5;
    private static final int TICKS_PER_FRAME = 2;
    private static final float HEAD_Y_OFFSET = 0.35F;
    private static final float HALF_SIZE = 0.55F;
    private static final float CAMERA_Z_BIAS = 0.05F;

    private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            FRAMES[i] =
                    ResourceLocation.fromNamespaceAndPath(
                            Tacz_MoreLevel.MOD_ID, "textures/particle/flash_dizzy_" + i + ".png");
        }
    }

    private FlashedDizzyHeadOverlay() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        FlashedVisualSync.pruneExpired(level.getGameTime());

        PoseStack pose = event.getPoseStack();
        float partialTick = event.getPartialTick();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }
            if (!FlashedVisualSync.isFlashed(living)) {
                continue;
            }
            renderAbove(living, pose, buffer, cam, partialTick);
        }

        for (ResourceLocation tex : FRAMES) {
            buffer.endBatch(RenderType.entityCutoutNoCullZOffset(tex));
        }
    }

    @SubscribeEvent
    public static void onClientLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) {
            FlashedVisualSync.clearAll();
        }
    }

    private static void renderAbove(
            LivingEntity living,
            PoseStack pose,
            MultiBufferSource buffer,
            Vec3 cam,
            float partialTick) {
        double x = Mth.lerp(partialTick, living.xo, living.getX());
        double y = Mth.lerp(partialTick, living.yo, living.getY()) + living.getBbHeight() + HEAD_Y_OFFSET;
        double z = Mth.lerp(partialTick, living.zo, living.getZ());

        int frame = (living.tickCount / TICKS_PER_FRAME) % FRAME_COUNT;
        ResourceLocation tex = FRAMES[frame];
        RenderType renderType = RenderType.entityCutoutNoCullZOffset(tex);

        pose.pushPose();
        pose.translate(x - cam.x, y - cam.y, z - cam.z);
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, 0.0F, CAMERA_Z_BIAS);

        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        VertexConsumer consumer = buffer.getBuffer(renderType);
        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;
        float s = HALF_SIZE;

        vertex(consumer, matrix, normal, -s, -s, 0.0F, 0.0F, 1.0F, light, overlay);
        vertex(consumer, matrix, normal, -s, s, 0.0F, 0.0F, 0.0F, light, overlay);
        vertex(consumer, matrix, normal, s, s, 0.0F, 1.0F, 0.0F, light, overlay);
        vertex(consumer, matrix, normal, s, -s, 0.0F, 1.0F, 1.0F, light, overlay);

        pose.popPose();
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light,
            int overlay) {
        consumer
                .vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
