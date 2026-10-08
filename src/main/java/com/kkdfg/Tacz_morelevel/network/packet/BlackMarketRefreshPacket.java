package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketFeedback;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketInventoryHelper;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketSavedData;
import com.kkdfg.Tacz_morelevel.block.entity.BlackMarketContactBlockEntity;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketContactMenu;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public final class BlackMarketRefreshPacket {

    private final BlockPos blockPos;

    public BlackMarketRefreshPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static void encode(BlackMarketRefreshPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.blockPos);
    }

    public static BlackMarketRefreshPacket decode(FriendlyByteBuf buf) {
        return new BlackMarketRefreshPacket(buf.readBlockPos());
    }

    public static void handle(BlackMarketRefreshPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
                    BlackMarketSavedData state = BlackMarketSavedData.get(serverLevel);
                    ItemStack cost = state.refreshCost();
                    if (!cost.isEmpty()
                            && !BlackMarketInventoryHelper.consumeItems(player.getInventory(), cost)) {
                        BlackMarketSyncMenuPacket.sendToPlayer(
                                player, blockEntity, BlackMarketFeedback.REFRESH_FAIL);
                        return;
                    }
                    blockEntity.performManualRefresh(serverLevel);
                });
        context.setPacketHandled(true);
    }
}
