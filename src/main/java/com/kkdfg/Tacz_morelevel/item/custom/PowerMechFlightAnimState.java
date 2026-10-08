package com.kkdfg.Tacz_morelevel.item.custom;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;

/** 动力机甲 fly 翅膀动画的客户端边沿状态（服务端写入无害）。 */
public final class PowerMechFlightAnimState {

    public enum Phase {
        /** 地面收拢。 */
        DEFAULT,
        /** 飞行中，锁 fly 第 0 帧（展开）。 */
        DEPLOYED,
        /** 结束飞行，正播 fly 回收。 */
        RETRACTING
    }

    private static final Map<UUID, Boolean> WAS_FLYING = new ConcurrentHashMap<>();
    private static final Map<UUID, Phase> PHASE = new ConcurrentHashMap<>();

    private PowerMechFlightAnimState() {}

    public static boolean wasFlying(LivingEntity entity) {
        return WAS_FLYING.getOrDefault(entity.getUUID(), false);
    }

    public static Phase getPhase(LivingEntity entity) {
        return PHASE.getOrDefault(entity.getUUID(), Phase.DEFAULT);
    }

    public static void setWasFlying(LivingEntity entity, boolean flying) {
        if (flying) {
            WAS_FLYING.put(entity.getUUID(), true);
        } else {
            WAS_FLYING.remove(entity.getUUID());
        }
    }

    public static void setPhase(LivingEntity entity, Phase phase) {
        if (phase == Phase.DEFAULT) {
            PHASE.remove(entity.getUUID());
        } else {
            PHASE.put(entity.getUUID(), phase);
        }
    }

    public static void clear(LivingEntity entity) {
        WAS_FLYING.remove(entity.getUUID());
        PHASE.remove(entity.getUUID());
    }
}
