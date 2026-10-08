package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** 客户端按键 → 服务端切换盔甲夜视开关。 */
public final class ArmorNightVisionPacket {

    public ArmorNightVisionPacket() {}

    public static void encode(ArmorNightVisionPacket msg, FriendlyByteBuf buf) {}

    public static ArmorNightVisionPacket decode(FriendlyByteBuf buf) {
        return new ArmorNightVisionPacket();
    }

    public static void handle(ArmorNightVisionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(
                () -> {
                    ServerPlayer sender = ctx.getSender();
                    if (sender != null) {
                        ArmorNightVisionHandler.toggle(sender);
                    }
                });
        ctx.setPacketHandled(true);
    }
}
