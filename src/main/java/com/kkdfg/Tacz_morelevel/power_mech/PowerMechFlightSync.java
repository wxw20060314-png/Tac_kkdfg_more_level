package com.kkdfg.Tacz_morelevel.power_mech;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;

/** 动力机甲飞行状态：服务端权威，经 {@link com.kkdfg.Tacz_morelevel.network.packet.PowerMechFlightSyncPacket} 同步到各客户端（供动画等）。 */
public final class PowerMechFlightSync {

    private static final Map<UUID, Boolean> CLIENT_FLYING = new ConcurrentHashMap<>();

    private PowerMechFlightSync() {}

    public static void setFlying(UUID entityId, boolean flying) {
        if (flying) {
            CLIENT_FLYING.put(entityId, true);
        } else {
            CLIENT_FLYING.remove(entityId);
        }
    }

    public static boolean isFlying(UUID entityId) {
        return CLIENT_FLYING.getOrDefault(entityId, false);
    }

    public static boolean isFlying(Player player) {
        return isFlying(player.getUUID());
    }

    public static void clear(UUID playerId) {
        CLIENT_FLYING.remove(playerId);
    }
}
