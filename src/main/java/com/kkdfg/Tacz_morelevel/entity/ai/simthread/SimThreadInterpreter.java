package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * 将成功 {@link SimThreadInfo} 投递到实现了 {@link SimThreadReceiver} 的实体。
 * 未实现接收器则丢弃（UDP）。
 */
public final class SimThreadInterpreter {

    private SimThreadInterpreter() {}

    /**
     * @return 是否成功写入接收器
     */
    public static boolean deliver(ServerLevel level, UUID uuid, SimThreadInfo info) {
        if (info == null || level == null || uuid == null) {
            return false;
        }
        Entity entity = level.getEntity(uuid);
        if (!(entity instanceof SimThreadReceiver receiver)) {
            return false;
        }
        long gameTime = level.getGameTime();
        receiver.tickSimThreadReceiver(gameTime);
        return receiver.offerSimThreadInfo(info);
    }
}
