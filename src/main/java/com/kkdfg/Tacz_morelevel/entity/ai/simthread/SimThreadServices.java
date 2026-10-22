package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;

/** 每 {@link ServerLevel} 一份 {@link SimThreadCpuManager}。 */
public final class SimThreadServices {

    private static final Map<ServerLevel, SimThreadCpuManager> MANAGERS = new ConcurrentHashMap<>();

    private SimThreadServices() {}

    public static SimThreadCpuManager manager(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, ignored -> new SimThreadCpuManager());
    }

    /**
     * 找掩体希望排队抽签概率（见 {@link SimThreadCpuManager#queueSize()}）。
     * level 无效时返回 1。
     */
    public static double queueSize(ServerLevel level) {
        if (level == null || level.isClientSide()) {
            return 1.0D;
        }
        return manager(level).queueSize();
    }

    public static void discard(ServerLevel level) {
        if (level != null) {
            MANAGERS.remove(level);
        }
    }

    /** 清理已卸载的 level 引用，避免泄漏。 */
    public static void purgeInvalid() {
        Iterator<Map.Entry<ServerLevel, SimThreadCpuManager>> it = MANAGERS.entrySet().iterator();
        while (it.hasNext()) {
            ServerLevel level = it.next().getKey();
            if (level == null || level.getServer() == null || !level.getServer().isRunning()) {
                it.remove();
            }
        }
    }
}
