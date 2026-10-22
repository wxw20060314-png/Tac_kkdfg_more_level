package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

/**
 * 走位模式：默认一般；掩体信息触发后进入 COVER，tick 倒计时结束回 NORMAL。
 */
public enum SoldierLocomotionMode {
    /** 追击 / 横移等原逻辑。 */
    NORMAL,
    /** 朝掩体点 moveTo。 */
    COVER
}
