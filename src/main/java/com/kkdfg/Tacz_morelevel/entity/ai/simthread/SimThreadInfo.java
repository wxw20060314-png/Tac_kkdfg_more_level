package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

/**
 * Cpu 成功时投递给 {@link SimThreadReceiver} 的信息基类。
 * 约定：写入后仅保证约 1 tick 可读，随后应被清空；Goal 需在当 tick 自行拷贝所需数据。
 */
public abstract class SimThreadInfo {

    private final SimThreadEventType type;

    protected SimThreadInfo(SimThreadEventType type) {
        if (!SimThreadEventType.isRunnable(type)) {
            throw new IllegalArgumentException("SimThreadInfo requires a runnable event type");
        }
        this.type = type;
    }

    public final SimThreadEventType type() {
        return this.type;
    }

    public final SimThreadEventKind kind() {
        return this.type.kind();
    }
}
