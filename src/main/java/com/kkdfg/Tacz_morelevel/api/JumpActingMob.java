package com.kkdfg.Tacz_morelevel.api;

import net.minecraft.util.RandomSource;

/**
 * {@link JumpController} + 真跳入口：设档后拉原版 {@code JumpControl} 闩，
 * 力度在 {@code getJumpPower}（表档 × block + 药水）消费；深渊等仍走 {@code LivingJumpEvent}。
 */
public interface JumpActingMob extends JumpController {

    /**
     * 设当前档并拉 JumpControl 闩。
     *
     * @return 能否拉闩（地面且非水/岩浆）
     */
    boolean requestJump(int tier);

    /** 默认档起跳。 */
    boolean performJump();

    /**
     * 软窗口内随机一档后起跳（装饰跳用）；寻路仍应走最低够用档。
     */
    default boolean performRandomJump(RandomSource random) {
        return this.requestJump(this.randomJumpTier(random));
    }

    boolean performJumpTier(int tier);

    boolean performJumpTier(int tier, boolean respectMin);

    /** 将 power 映射到最近表档后 {@link #requestJump(int)}。 */
    boolean performJumpPower(float basePower);
}
