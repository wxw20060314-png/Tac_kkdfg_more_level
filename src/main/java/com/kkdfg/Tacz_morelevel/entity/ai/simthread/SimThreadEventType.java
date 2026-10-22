package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import javax.annotation.Nullable;

/**
 * 模拟线程事件小类。{@code null} 表示非法/取消：管理器与 Cpu 均可拒绝并清除进程。
 */
public enum SimThreadEventType {
    /** 找掩体（求解逻辑稍后实现；框架期 Solver 可空跑）。 */
    FIND_COVER(SimThreadEventKind.COVER);

    private final SimThreadEventKind kind;

    SimThreadEventType(SimThreadEventKind kind) {
        this.kind = kind;
    }

    public SimThreadEventKind kind() {
        return this.kind;
    }

    /** 非空且已注册的类型才可入队/上机。 */
    public static boolean isRunnable(@Nullable SimThreadEventType type) {
        return type != null;
    }
}
