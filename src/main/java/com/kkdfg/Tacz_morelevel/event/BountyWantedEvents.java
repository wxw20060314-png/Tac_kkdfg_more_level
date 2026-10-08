package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.bounty.BountyAnnihilatorSummon;
import com.kkdfg.Tacz_morelevel.bounty.BountyWantedHelper;
import com.kkdfg.Tacz_morelevel.entity.custom.MysteriousMerchantEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BountyWantedEvents {

    private BountyWantedEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof MysteriousMerchantEntity)) {
            return;
        }
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity killer = event.getEntity().getKillCredit();
        if (!(killer instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int newLevel = BountyWantedHelper.incrementLevel(serverPlayer);
        if (newLevel >= 3) {
            BountyWantedHelper.setLevel(serverPlayer, 0);
            BountyAnnihilatorSummon.begin(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (BountyAnnihilatorSummon.hasPending(serverPlayer)) {
            BountyAnnihilatorSummon.tickPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BountyAnnihilatorSummon.cancel(serverPlayer);
        }
    }
}
