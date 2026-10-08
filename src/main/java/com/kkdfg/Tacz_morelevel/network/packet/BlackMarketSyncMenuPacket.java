package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketFeedback;
import com.kkdfg.Tacz_morelevel.block.entity.BlackMarketContactBlockEntity;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketContactMenu;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public final class BlackMarketSyncMenuPacket {

    private final BlockPos blockPos;
    private final List<BlackMarketTradeOffer> offers;
    private final ItemStack refreshCost;
    private final BlackMarketFeedback feedback;

    public BlackMarketSyncMenuPacket(
            BlockPos blockPos,
            List<BlackMarketTradeOffer> offers,
            ItemStack refreshCost,
            BlackMarketFeedback feedback) {
        this.blockPos = blockPos;
        this.offers = offers;
        this.refreshCost = refreshCost;
        this.feedback = feedback;
    }

    public static void sendToPlayer(
            ServerPlayer player,
            BlockPos blockPos,
            List<BlackMarketTradeOffer> offers,
            ItemStack refreshCost,
            BlackMarketFeedback feedback) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new BlackMarketSyncMenuPacket(blockPos, offers, refreshCost, feedback));
    }

    public static void sendToPlayer(
            ServerPlayer player,
            BlackMarketContactBlockEntity blockEntity,
            BlackMarketFeedback feedback) {
        sendToPlayer(
                player,
                blockEntity.getBlockPos(),
                blockEntity.activeOffers(),
                blockEntity.refreshCost(),
                feedback);
    }

    public static void encode(BlackMarketSyncMenuPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.blockPos);
        buf.writeVarInt(packet.feedback.id());
        buf.writeVarInt(packet.offers.size());
        for (BlackMarketTradeOffer offer : packet.offers) {
            buf.writeItem(offer.input());
            buf.writeItem(offer.output());
        }
        buf.writeItem(packet.refreshCost);
    }

    public static BlackMarketSyncMenuPacket decode(FriendlyByteBuf buf) {
        BlockPos blockPos = buf.readBlockPos();
        BlackMarketFeedback feedback = BlackMarketFeedback.fromId(buf.readVarInt());
        int count = buf.readVarInt();
        List<BlackMarketTradeOffer> offers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            offers.add(new BlackMarketTradeOffer(buf.readItem(), buf.readItem()));
        }
        ItemStack refreshCost = buf.readItem();
        return new BlackMarketSyncMenuPacket(blockPos, offers, refreshCost, feedback);
    }

    public static void handle(BlackMarketSyncMenuPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> applyClient(packet));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(BlackMarketSyncMenuPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (!(minecraft.player.containerMenu instanceof BlackMarketContactMenu menu)) {
            return;
        }
        menu.applyClientState(packet.offers, packet.refreshCost);
        showFeedback(packet.feedback);
    }

    @OnlyIn(Dist.CLIENT)
    private static void showFeedback(BlackMarketFeedback feedback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        switch (feedback) {
            case REFRESH_SUCCESS -> {
                minecraft.player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.black_market.refreshed"), true);
                minecraft.getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            case REFRESH_FAIL -> {
                minecraft.player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.black_market.refresh_failed"), true);
                minecraft.getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
            }
            case TRADE_SUCCESS -> {
                minecraft.player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.black_market.traded"), true);
                minecraft.getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
            }
            case TRADE_FAIL -> {
                minecraft.player.displayClientMessage(
                        Component.translatable("message.tacz_morelevel.black_market.trade_failed"), true);
                minecraft.getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
            }
            default -> {}
        }
    }
}
