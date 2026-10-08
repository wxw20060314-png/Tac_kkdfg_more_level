package com.kkdfg.Tacz_morelevel.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import com.kkdfg.Tacz_morelevel.network.packet.ArmorNightVisionPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ArmorNightVisionClientEvents {

    private ArmorNightVisionClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (ModKeyBindings.ARMOR_NIGHT_VISION == null) {
            return;
        }
        while (ModKeyBindings.ARMOR_NIGHT_VISION.consumeClick()) {
            ModNetwork.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorNightVisionPacket());
        }
    }
}
