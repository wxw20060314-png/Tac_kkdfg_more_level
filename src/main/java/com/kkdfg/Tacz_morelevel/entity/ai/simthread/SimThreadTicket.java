package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.UUID;
import javax.annotation.Nullable;

/** 队列中的一张请求票：uuid + 类型 + 紧急 + 可选找掩体距离闸。 */
final class SimThreadTicket {

    enum Seat {
        WAIT,
        READY,
        RUNNING
    }

    /** {@link #coverNear}/{@link #coverFar} 为该值时表示不限距。 */
    static final float COVER_RANGE_UNSET = -1.0F;

    final UUID uuid;
    @Nullable
    SimThreadEventType type;
    /** 由 NPC/调用方传入，越大越优先；合并取 max，等待 tick 不变。 */
    int urgency;
    Seat seat;
    /** 进入 Ready 后的等待 tick；Wait/刚入队为 0。合并时不变。 */
    int waitTicks;
    /** 在 RUNNING 时又被 submit 合并：本进程结束后重新进等待区。 */
    boolean requeueAfterRun;
    /** 找掩体近界；{@link #COVER_RANGE_UNSET} 不限。 */
    float coverNear = COVER_RANGE_UNSET;
    /** 找掩体远界；{@link #COVER_RANGE_UNSET} 不限。 */
    float coverFar = COVER_RANGE_UNSET;

    SimThreadTicket(UUID uuid, SimThreadEventType type, int urgency) {
        this(uuid, type, urgency, COVER_RANGE_UNSET, COVER_RANGE_UNSET);
    }

    SimThreadTicket(
            UUID uuid, SimThreadEventType type, int urgency, float coverNear, float coverFar) {
        this.uuid = uuid;
        this.type = type;
        this.urgency = Math.max(0, urgency);
        this.seat = Seat.WAIT;
        this.waitTicks = 0;
        this.coverNear = coverNear;
        this.coverFar = coverFar;
    }

    boolean isRunnable() {
        return SimThreadEventType.isRunnable(this.type);
    }

    boolean hasCoverRangeGate() {
        return this.coverFar >= 0.0F || this.coverNear >= 0.0F;
    }

    /** 同 uuid 合并：等待不变，紧急取 max，类型后写覆盖；距离闸取后写。 */
    void merge(SimThreadEventType type, int urgency, float coverNear, float coverFar) {
        this.type = type;
        this.urgency = Math.max(this.urgency, Math.max(0, urgency));
        this.coverNear = coverNear;
        this.coverFar = coverFar;
        if (this.seat == Seat.RUNNING) {
            this.requeueAfterRun = true;
        }
    }

    void merge(SimThreadEventType type, int urgency) {
        this.merge(type, urgency, this.coverNear, this.coverFar);
    }

    void resetWait() {
        this.waitTicks = 0;
    }

    long scheduleScore(int ageCap) {
        int aged = Math.min(Math.max(0, this.waitTicks), ageCap);
        return (long) this.urgency + aged;
    }
}
