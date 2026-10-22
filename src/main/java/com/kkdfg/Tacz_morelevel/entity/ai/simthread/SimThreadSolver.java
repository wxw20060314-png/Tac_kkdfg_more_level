package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/** 按事件小类求解；失败返回 empty（UDP，不通知请求方）。 */
public interface SimThreadSolver {

    boolean supports(SimThreadEventType type);

    /**
     * 执行瞬间动态读世界。实体已死、类型不适用等均返回 empty。
     */
    Optional<SimThreadInfo> solve(ServerLevel level, UUID uuid, SimThreadEventType type);
}
