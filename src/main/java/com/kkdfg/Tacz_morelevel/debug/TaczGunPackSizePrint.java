package com.kkdfg.Tacz_morelevel.debug;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.tacz.guns.network.message.ServerMessageSyncGunPack;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.network.DataType;
import io.netty.buffer.Unpooled;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 进世界 / datapack 同步时打印 TaCZ {@link ServerMessageSyncGunPack} 载荷大小（未压缩）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TaczGunPackSizePrint {

    private TaczGunPackSizePrint() {}

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        CommonAssetsManager assets = CommonAssetsManager.getInstance();
        if (assets == null) {
            System.out.println("[TACZ sync] CommonAssetsManager null");
            return;
        }
        Map<DataType, Map<ResourceLocation, String>> cache = assets.getNetworkCache();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ServerMessageSyncGunPack.encode(new ServerMessageSyncGunPack(cache), buf);
            int bytes = buf.readableBytes();
            System.out.println(
                    "[TACZ sync] bytes="
                            + bytes
                            + " KB="
                            + String.format("%.1f", bytes / 1024.0)
                            + " MB="
                            + String.format("%.3f", bytes / (1024.0 * 1024.0)));
        } finally {
            buf.release();
        }
    }
}
