package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/**
 * 可参与模拟线程掩体流程的实体能力（Requester + Receiver + 走位模式）。
 * 真正发请求 / 读包走位由 Goal 完成；本接口只提供状态与邮箱。
 */
public interface SimThreadCapableMob extends SimThreadRequester, SimThreadReceiver {

    SoldierLocomotionMode getLocomotionMode();

    /**
     * 进入 COVER 并按 {@link #getCoverModeDurationTicks()} 赋值倒计时。
     * 可附带掩体坐标供 Move Goal 使用。
     */
    void enterCoverMode(@Nullable BlockPos coverPos, boolean crawl);

    /**
     * 退出 COVER：清接收器邮箱并切回 {@link SoldierLocomotionMode#NORMAL}。
     * 到达掩体点或倒计时结束时由 Goal / Support 调用。
     */
    void exitCoverMode();

    /** COVER 剩余 tick；非 COVER 时为 0。 */
    int getCoverModeTicksLeft();

    @Nullable
    BlockPos getPendingCoverPos();

    boolean isPendingCoverCrawl();

    /**
     * COVER 模式时长（进入时写入倒计时）。子类可 override。
     */
    int getCoverModeDurationTicks();
}
