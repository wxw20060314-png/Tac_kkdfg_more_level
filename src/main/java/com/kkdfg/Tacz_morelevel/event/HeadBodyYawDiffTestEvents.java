package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 【TEST】服务端 NPC head/body/mount → {@link System#out}。测完删本类即可。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HeadBodyYawDiffTestEvents {

    private HeadBodyYawDiffTestEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (!(entity instanceof PlayerLikeHeadBodyYawMob)) {
            return;
        }

        float head = entity.yHeadRot;
        float body = entity.yBodyRot;
        float headMinusBody = Math.abs(Mth.wrapDegrees(head - body));

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

        System.out.printf(
                "[TEST-SV] head=%.1f body=%.1f |h-b|=%.1f %s ride=%s id=%d%n",
                head,
                body,
                headMinusBody,
                mountPart,
                entity.isPassenger(),
                entity.getId());
    }
}
