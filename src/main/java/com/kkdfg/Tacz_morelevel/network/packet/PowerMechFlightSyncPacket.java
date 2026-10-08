package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import com.kkdfg.Tacz_morelevel.power_mech.PowerMechFlightSync;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public final class PowerMechFlightSyncPacket {

    private final UUID entityId;
    private final boolean flying;

    public PowerMechFlightSyncPacket(UUID entityId, boolean flying) {
        this.entityId = entityId;
        this.flying = flying;
    }

    public static void encode(PowerMechFlightSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityId);
        buf.writeBoolean(msg.flying);
    }

    public static PowerMechFlightSyncPacket decode(FriendlyByteBuf buf) {
        return new PowerMechFlightSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(PowerMechFlightSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> PowerMechFlightSync.setFlying(msg.entityId, msg.flying));
        ctx.setPacketHandled(true);
    }

    /** 向跟踪该实体的所有客户端广播飞行状态。 */
    public static void sendToTracking(Entity entity, boolean flying) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new PowerMechFlightSyncPacket(entity.getUUID(), flying));
    }

    /** 向跟踪该玩家的所有客户端（含本人）广播飞行状态。 */
    public static void sendToTracking(Player player, boolean flying) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new PowerMechFlightSyncPacket(player.getUUID(), flying));
    }

    /** 向单个观察者补发当前状态（如 StartTracking）。 */
    public static void sendToPlayer(ServerPlayer viewer, Entity target, boolean flying) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> viewer),
                new PowerMechFlightSyncPacket(target.getUUID(), flying));
    }
}
