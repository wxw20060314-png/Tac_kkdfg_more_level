package com.kkdfg.Tacz_morelevel.effect;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

/**
 * 闪光头顶特效的客户端可见状态。
 *
 * <p>原版不把非玩家生物的效果表同步给旁观者，故由服务端在效果变更时经跟踪包写入此缓存。
 */
public final class FlashedVisualSync {

    /** entity network id → 客户端世界结束 gameTime（超过则视为结束） */
    private static final Map<Integer, Long> CLIENT_FLASHED_UNTIL = new ConcurrentHashMap<>();

    private FlashedVisualSync() {}

    public static void setFlashed(int entityId, int remainingTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        if (remainingTicks <= 0) {
            CLIENT_FLASHED_UNTIL.remove(entityId);
            return;
        }
        CLIENT_FLASHED_UNTIL.put(entityId, mc.level.getGameTime() + (long) remainingTicks);
    }

    public static boolean isFlashed(LivingEntity living) {
        Long until = CLIENT_FLASHED_UNTIL.get(living.getId());
        if (until == null) {
            return false;
        }
        if (living.level().getGameTime() > until) {
            CLIENT_FLASHED_UNTIL.remove(living.getId());
            return false;
        }
        return true;
    }

    public static void clearAll() {
        CLIENT_FLASHED_UNTIL.clear();
    }

    public static void pruneExpired(long gameTime) {
        Iterator<Map.Entry<Integer, Long>> it = CLIENT_FLASHED_UNTIL.entrySet().iterator();
        while (it.hasNext()) {
            if (gameTime > it.next().getValue()) {
                it.remove();
            }
        }
    }
}
