package com.kkdfg.Tacz_morelevel.entity.ai.bomb_sight;

/**
 * 炸弹视野冲击波预发射：打开即视为技能已用（冷却已写）。
 * 每次 {@link #arm} 都会整表重置。
 */
public final class BombSightDashPrefire {

    private boolean armed;
    private int kineticSnapshot;
    private int ticksArmed;
    private int releaseAfterTicks;

    public boolean isArmed() {
        return this.armed;
    }

    public int kineticSnapshot() {
        return this.kineticSnapshot;
    }

    /** 打开预发射：重置状态并锁定动能快照；调用方负责跳跃与写冷却。 */
    public void arm(int kinetic, int releaseAfterTicks) {
        this.reset();
        this.armed = true;
        this.kineticSnapshot = Math.max(0, kinetic);
        this.ticksArmed = 0;
        this.releaseAfterTicks = BombSightDashPredictor.clampReleaseDelay(releaseAfterTicks);
    }

    public void reset() {
        this.armed = false;
        this.kineticSnapshot = 0;
        this.ticksArmed = 0;
        this.releaseAfterTicks = 0;
    }

    /** @return true 表示本 tick 应施加冲量 */
    public boolean tickShouldRelease() {
        if (!this.armed) {
            return false;
        }
        this.ticksArmed++;
        return this.ticksArmed >= this.releaseAfterTicks;
    }
}
