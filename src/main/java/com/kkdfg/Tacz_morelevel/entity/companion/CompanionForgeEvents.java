package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CompanionForgeEvents {

    private CompanionForgeEvents() {}

    
    @SubscribeEvent
    public static void onBodyguardLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof AbstractFriendlyCompanionEntity companion)) {
            return;
        }
        if (companion.level().isClientSide()) {
            return;
        }
        CompanionBodyguardDeathDropHelper.spawnDeathDrops(companion, event.getDrops());
    }
}
