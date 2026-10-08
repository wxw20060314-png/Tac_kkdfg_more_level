package com.kkdfg.Tacz_morelevel.item.custom;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;

/** 夜视头盔 fanz 等动画的客户端边沿状态（服务端写入无害）。 */
public final class ArmorNightVisionAnimState {

    private static final Map<UUID, Boolean> WAS_ENABLED = new ConcurrentHashMap<>();
    private static final Set<UUID> INITIALIZED = ConcurrentHashMap.newKeySet();

    private ArmorNightVisionAnimState() {}

    public static boolean wasEnabled(LivingEntity entity) {
        return WAS_ENABLED.getOrDefault(entity.getUUID(), false);
    }

    public static boolean isInitialized(LivingEntity entity) {
        return INITIALIZED.contains(entity.getUUID());
    }

    public static void setInitialized(LivingEntity entity, boolean initialized) {
        if (initialized) {
            INITIALIZED.add(entity.getUUID());
        } else {
            INITIALIZED.remove(entity.getUUID());
        }
    }

    public static void setWasEnabled(LivingEntity entity, boolean enabled) {
        if (enabled) {
            WAS_ENABLED.put(entity.getUUID(), true);
        } else {
            WAS_ENABLED.remove(entity.getUUID());
        }
    }

    public static void clear(LivingEntity entity) {
        WAS_ENABLED.remove(entity.getUUID());
        INITIALIZED.remove(entity.getUUID());
    }
}
