package com.kkdfg.Tacz_morelevel.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 【TEST】客户端：同步字段 head/body + 渲染头相对身 + 相对载具 → actionbar {@code [TEST-CL]}。
 * 服务端见 {@code System.out} 的 {@code [TEST-SV]}。测完删本类即可。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HeadBodyYawDiffClientTestEvents {

    private static final double SEND_RANGE_SQ = 48.0D * 48.0D;

    private HeadBodyYawDiffClientTestEvents() {}

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof PlayerLikeHeadBodyYawMob)) {
            return;
        }
        if (!(event.getRenderer().getModel() instanceof HumanoidModel<?> model)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.distanceToSqr(entity) > SEND_RANGE_SQ) {
            return;
        }

        float head = entity.yHeadRot;
        float body = entity.yBodyRot;
        float headMinusBody = Math.abs(Mth.wrapDegrees(head - body));
        float renderAbsDeg = Math.abs(model.head.yRot * Mth.RAD_TO_DEG);

        String mountPart = "mount=- |b-m|=-";
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            float mountYaw =
                    vehicle instanceof LivingEntity living
                            ? living.yBodyRot
                            : vehicle.getYRot();
            float bodyMinusMount = Math.abs(Mth.wrapDegrees(body - mountYaw));
            mountPart =
                    String.format("mount=%.1f |b-m|=%.1f", mountYaw, bodyMinusMount);
        }

        player.displayClientMessage(
                Component.literal(
                        String.format(
                                "[TEST-CL] head=%.1f body=%.1f |h-b|=%.1f render|Δ|=%.1f %s ride=%s id=%d",
                                head,
                                body,
                                headMinusBody,
                                renderAbsDeg,
                                mountPart,
                                entity.isPassenger(),
                                entity.getId())),
                true);
    }
}
