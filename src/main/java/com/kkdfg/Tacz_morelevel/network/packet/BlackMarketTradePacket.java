package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketFeedback;
import com.kkdfg.Tacz_morelevel.block.entity.BlackMarketContactBlockEntity;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketContactMenu;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public final class BlackMarketTradePacket {

    private final BlockPos blockPos;
    private final int offerIndex;

    public BlackMarketTradePacket(BlockPos blockPos, int offerIndex) {
        this.blockPos = blockPos;
        this.offerIndex = offerIndex;
    }

    public static void encode(BlackMarketTradePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.blockPos);
        buf.writeVarInt(packet.offerIndex);
    }

    public static BlackMarketTradePacket decode(FriendlyByteBuf buf) {
        return new BlackMarketTradePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(BlackMarketTradePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(
                () -> {
                    ServerPlayer player = context.getSender();
                    if (player == null) {
                        return;
                    }
                    if (!(player.level() instanceof ServerLevel serverLevel)) {
                        return;
                    }
                    if (!(player.containerMenu instanceof BlackMarketContactMenu menu)
                            || !menu.blockPos().equals(packet.blockPos)) {
                        return;
                    }
                    if (!(serverLevel.getBlockEntity(packet.blockPos)
                            instanceof BlackMarketContactBlockEntity blockEntity)) {
                        return;
                    }
                    BlackMarketTradeOffer offer = blockEntity.activeOffer(packet.offerIndex);
                    if (offer == null || offer.isEmpty()) {
                        BlackMarketSyncMenuPacket.sendToPlayer(
                                player, blockEntity, BlackMarketFeedback.TRADE_FAIL);
                        return;
                    }
                    if (!offer.executeTrade(player)) {
                        BlackMarketSyncMenuPacket.sendToPlayer(
                                player, blockEntity, BlackMarketFeedback.TRADE_FAIL);
                        return;
                    }
                    menu.syncFromBlockEntity(blockEntity);
                    BlackMarketSyncMenuPacket.sendToPlayer(
                            player, blockEntity, BlackMarketFeedback.TRADE_SUCCESS);
                });
        context.setPacketHandled(true);
    }
}
