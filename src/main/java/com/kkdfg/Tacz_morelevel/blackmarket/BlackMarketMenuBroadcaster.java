package com.kkdfg.Tacz_morelevel.blackmarket;

import com.kkdfg.Tacz_morelevel.block.entity.BlackMarketContactBlockEntity;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketContactMenu;
import com.kkdfg.Tacz_morelevel.network.packet.BlackMarketSyncMenuPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class BlackMarketMenuBroadcaster {

    private BlackMarketMenuBroadcaster() {}

    /** 同步所有已打开黑市界面的玩家（各玩家按其打开的联络方块等级取货）。 */
    public static void syncAllViewers(ServerLevel level, BlackMarketFeedback feedback) {
        for (ServerPlayer player : level.players()) {
            if (!(player.containerMenu instanceof BlackMarketContactMenu menu)) {
                continue;
            }
            if (!(level.getBlockEntity(menu.blockPos()) instanceof BlackMarketContactBlockEntity blockEntity)) {
                continue;
            }
            syncPlayerMenu(player, menu, blockEntity, feedback);
        }
    }

    /** @deprecated 使用 {@link #syncAllViewers}；保留参数以兼容旧调用。 */
    @Deprecated
    public static void syncViewers(
            ServerLevel level, BlackMarketContactBlockEntity blockEntity, BlackMarketFeedback feedback) {
        syncAllViewers(level, feedback);
    }

    static void syncPlayerMenu(
            ServerPlayer player,
            BlackMarketContactMenu menu,
            BlackMarketContactBlockEntity blockEntity,
            BlackMarketFeedback feedback) {
        menu.applyClientState(blockEntity.activeOffers(), blockEntity.refreshCost());
        BlackMarketSyncMenuPacket.sendToPlayer(player, blockEntity, feedback);
    }
}
