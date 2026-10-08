package com.kkdfg.Tacz_morelevel.network;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.network.packet.ArmorLegendarySkillPacket;
import com.kkdfg.Tacz_morelevel.network.packet.ArmorNightVisionPacket;
import com.kkdfg.Tacz_morelevel.network.packet.PowerMechFlightSyncPacket;
import com.kkdfg.Tacz_morelevel.network.packet.FlashedVisualSyncPacket;
import com.kkdfg.Tacz_morelevel.network.packet.ShockwaveDashPacket;
import com.kkdfg.Tacz_morelevel.network.packet.BlackMarketRefreshPacket;
import com.kkdfg.Tacz_morelevel.network.packet.BlackMarketSyncMenuPacket;
import com.kkdfg.Tacz_morelevel.network.packet.BlackMarketTradePacket;
import com.kkdfg.Tacz_morelevel.network.packet.GrenadeControlPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals);

    private static int nextId = 0;

    public static void init() {
        CHANNEL.registerMessage(
                nextId++,
                GrenadeControlPacket.class,
                GrenadeControlPacket::encode,
                GrenadeControlPacket::decode,
                GrenadeControlPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                BlackMarketRefreshPacket.class,
                BlackMarketRefreshPacket::encode,
                BlackMarketRefreshPacket::decode,
                BlackMarketRefreshPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                BlackMarketTradePacket.class,
                BlackMarketTradePacket::encode,
                BlackMarketTradePacket::decode,
                BlackMarketTradePacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                BlackMarketSyncMenuPacket.class,
                BlackMarketSyncMenuPacket::encode,
                BlackMarketSyncMenuPacket::decode,
                BlackMarketSyncMenuPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                ArmorLegendarySkillPacket.class,
                ArmorLegendarySkillPacket::encode,
                ArmorLegendarySkillPacket::decode,
                ArmorLegendarySkillPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                ArmorNightVisionPacket.class,
                ArmorNightVisionPacket::encode,
                ArmorNightVisionPacket::decode,
                ArmorNightVisionPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                PowerMechFlightSyncPacket.class,
                PowerMechFlightSyncPacket::encode,
                PowerMechFlightSyncPacket::decode,
                PowerMechFlightSyncPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                FlashedVisualSyncPacket.class,
                FlashedVisualSyncPacket::encode,
                FlashedVisualSyncPacket::decode,
                FlashedVisualSyncPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                ShockwaveDashPacket.class,
                ShockwaveDashPacket::encode,
                ShockwaveDashPacket::decode,
                ShockwaveDashPacket::handle);
    }

    private ModNetwork() {}
}
