package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/**
 * 三大基类共用的 SimThread 状态宿主：Mailbox、NORMAL/COVER、COVER 倒计时。
 * 非 NORMAL 时拒绝 {@link #offer}；tick 倒计时归零后切回 NORMAL。
 */
public final class SimThreadCapableSupport {

    private final IntSupplier coverModeDurationTicks;
    private final SimThreadMailbox mailbox = new SimThreadMailbox();
    private SoldierLocomotionMode mode = SoldierLocomotionMode.NORMAL;
    private int coverTicksLeft;
    @Nullable
    private BlockPos pendingCoverPos;
    private boolean pendingCoverCrawl;

    public SimThreadCapableSupport(IntSupplier coverModeDurationTicks) {
        this.coverModeDurationTicks = coverModeDurationTicks != null
                ? coverModeDurationTicks
                : () -> 30;
    }

    public void serverTick(long gameTime) {
        this.mailbox.tick(gameTime);

        if (this.mode == SoldierLocomotionMode.NORMAL) {
            return;
        }
        if (this.coverTicksLeft > 0) {
            this.coverTicksLeft--;
        }
        if (this.coverTicksLeft <= 0) {
            this.exitCover();
        }
    }

    public SoldierLocomotionMode mode() {
        return this.mode;
    }

    public boolean isNormal() {
        return this.mode == SoldierLocomotionMode.NORMAL;
    }

    public int coverTicksLeft() {
        return this.mode == SoldierLocomotionMode.COVER ? Math.max(0, this.coverTicksLeft) : 0;
    }

    @Nullable
    public BlockPos pendingCoverPos() {
        return this.pendingCoverPos;
    }

    public boolean pendingCoverCrawl() {
        return this.pendingCoverCrawl;
    }

    public void enterCover(@Nullable BlockPos coverPos, boolean crawl) {
        this.mode = SoldierLocomotionMode.COVER;
        this.coverTicksLeft = Math.max(0, this.coverModeDurationTicks.getAsInt());
        this.pendingCoverPos = coverPos == null ? null : coverPos.immutable();
        this.pendingCoverCrawl = crawl;
    }

    public void resetToNormal() {
        this.mode = SoldierLocomotionMode.NORMAL;
        this.coverTicksLeft = 0;
        this.pendingCoverPos = null;
        this.pendingCoverCrawl = false;
    }

    /** 清邮箱并切回 NORMAL（到达掩体 / 倒计时结束 / Goal 收尾）。 */
    public void exitCover() {
        this.mailbox.clear();
        this.resetToNormal();
    }

    /** 仅 NORMAL 可收；否则拒绝。 */
    public boolean offer(SimThreadInfo info, long gameTime) {
        if (!this.isNormal()) {
            return false;
        }
        return this.mailbox.offer(info, gameTime);
    }

    @Nullable
    public SimThreadInfo peek() {
        return this.mailbox.peek();
    }

    public void clearMailbox() {
        this.mailbox.clear();
    }

    public boolean isReceiveCooling(long gameTime) {
        return this.mailbox.isCooling(gameTime);
    }

    public void tickMailboxOnly(long gameTime) {
        this.mailbox.tick(gameTime);
    }
}
