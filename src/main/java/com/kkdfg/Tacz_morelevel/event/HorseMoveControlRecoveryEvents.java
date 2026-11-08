package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.ai.riding.PausedHorseMoveControl;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 骑手异常移除时，每 10 拍为无乘客的马恢复原版移动控制器。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HorseMoveControlRecoveryEvents {

    private static final int CHECK_INTERVAL_TICKS = 10;

    private HorseMoveControlRecoveryEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse horse)
                || horse.level().isClientSide()
                || horse.tickCount % CHECK_INTERVAL_TICKS != 0
                || horse.isVehicle()) {
            return;
        }
        PausedHorseMoveControl.restoreIfPaused(horse);
    }
}
