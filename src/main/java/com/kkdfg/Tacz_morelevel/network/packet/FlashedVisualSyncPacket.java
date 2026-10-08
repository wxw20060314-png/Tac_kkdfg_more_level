package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.effect.FlashedVisualSync;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/** 服务端 → 跟踪客户端：实体闪光头顶特效剩余 tick（0 = 清除）。 */
public final class FlashedVisualSyncPacket {

    private final int entityId;
    private final int remainingTicks;

    public FlashedVisualSyncPacket(int entityId, int remainingTicks) {
        this.entityId = entityId;
        this.remainingTicks = remainingTicks;
    }

    public static void encode(FlashedVisualSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.remainingTicks);
    }

    public static FlashedVisualSyncPacket decode(FriendlyByteBuf buf) {
        return new FlashedVisualSyncPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(FlashedVisualSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> FlashedVisualSync.setFlashed(msg.entityId, msg.remainingTicks));
        ctx.setPacketHandled(true);
    }

    public static void sendToTracking(LivingEntity entity, int remainingTicks) {
        FlashedVisualSyncPacket packet =
                new FlashedVisualSyncPacket(entity.getId(), Math.max(0, remainingTicks));
        if (entity instanceof ServerPlayer player) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
        } else {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
        }
    }

    public static void sendToPlayer(ServerPlayer viewer, LivingEntity target, int remainingTicks) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> viewer),
                new FlashedVisualSyncPacket(target.getId(), Math.max(0, remainingTicks)));
    }
}
