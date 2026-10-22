package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import javax.annotation.Nullable;

/**
 * {@link SimThreadReceiver} 的默认可组合实现：1 tick 邮箱 + 收下后 {@link SimThreadReceiver#RECEIVE_COOLDOWN_TICKS} 冷却。
 * 实体以后可持有本对象并委托接口方法；框架期不挂 NPC。
 */
public final class SimThreadMailbox {

    @Nullable
    private SimThreadInfo pending;
    /** 该 gameTime（含）之后清空 pending；-1 表示无。 */
    private long clearAtGameTime = -1L;
    /** 该 gameTime（含）之前处于接收冷却；-1 表示无。 */
    private long cooldownUntilGameTime = -1L;

    public boolean offer(SimThreadInfo info, long gameTime) {
        if (info == null) {
            return false;
        }
        if (this.isCooling(gameTime)) {
            return false;
        }
        this.pending = info;
        this.clearAtGameTime = gameTime + SimThreadReceiver.INFO_TTL_TICKS;
        this.cooldownUntilGameTime = gameTime + SimThreadReceiver.RECEIVE_COOLDOWN_TICKS;
        return true;
    }

    @Nullable
    public SimThreadInfo peek() {
        return this.pending;
    }

    public void clear() {
        this.pending = null;
        this.clearAtGameTime = -1L;
    }

    public boolean isCooling(long gameTime) {
        return this.cooldownUntilGameTime >= 0L && gameTime < this.cooldownUntilGameTime;
    }

    /** 推进 TTL；冷却只读时间戳，无需递减。 */
    public void tick(long gameTime) {
        if (this.clearAtGameTime >= 0L && gameTime >= this.clearAtGameTime) {
            this.clear();
        }
        if (this.cooldownUntilGameTime >= 0L && gameTime >= this.cooldownUntilGameTime) {
            this.cooldownUntilGameTime = -1L;
        }
    }
}
