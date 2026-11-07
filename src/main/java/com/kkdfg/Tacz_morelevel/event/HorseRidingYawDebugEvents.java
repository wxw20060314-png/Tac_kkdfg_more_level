package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.ai.riding.HorseRidingNpc;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/** 骑马 NPC：每 20 拍打出马和骑手的朝向。客户端、服务器各打一份。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HorseRidingYawDebugEvents {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LOG_INTERVAL_TICKS = 20;

    private HorseRidingYawDebugEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof HorseRidingNpc) || !(entity.getVehicle() instanceof AbstractHorse horse)) {
            return;
        }
        if (entity.tickCount % LOG_INTERVAL_TICKS != 0) {
            return;
        }
        String side = entity.level().isClientSide() ? "client" : "server";
        float headMinusHorseBody = Mth.wrapDegrees(entity.yHeadRot - horse.yBodyRot);
        LOGGER.info(
                "[horse-yaw {}] npc {} yRot={} yBodyRot={} yHeadRot={} | horse {} yRot={} yBodyRot={} yHeadRot={} | npcHead-horseBody={}",
                side,
                entity.getEncodeId(),
                entity.getYRot(),
                entity.yBodyRot,
                entity.yHeadRot,
                horse.getEncodeId(),
                horse.getYRot(),
                horse.yBodyRot,
                horse.yHeadRot,
                headMinusHorseBody);
    }
}
