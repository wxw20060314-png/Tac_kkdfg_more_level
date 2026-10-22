package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import javax.annotation.Nullable;

/**
 * 可接收 Cpu 成功信息的生物侧接口。字段须由实现类自行持有（接口字段只能是静态常量）。
 * <p>UDP：失败无通知；冷却中 {@link #offerSimThreadInfo} 应返回 false 并丢弃。
 */
public interface SimThreadReceiver {

    /** 信息写入后建议存活 tick 数（实现可用 {@link SimThreadMailbox}）。 */
    int INFO_TTL_TICKS = 1;

    /** 成功写入信息后的接收冷却，防止连收/连调。 */
    int RECEIVE_COOLDOWN_TICKS = 5;

    /**
     * 尝试写入一帧信息。冷却中或拒绝时返回 false（Cpu 无义务重试）。
     */
    boolean offerSimThreadInfo(SimThreadInfo info);

    @Nullable
    SimThreadInfo peekSimThreadInfo();

    void clearSimThreadInfo();

    boolean isSimThreadReceiveCooling();

    /**
     * 每 tick 推进 TTL/冷却。未挂实体时可由管理器对实现了本接口的实体代调，
     * 或实体自己在 {@code tick} 里调 {@link SimThreadMailbox#tick(long)}。
     */
    void tickSimThreadReceiver(long gameTime);
}
