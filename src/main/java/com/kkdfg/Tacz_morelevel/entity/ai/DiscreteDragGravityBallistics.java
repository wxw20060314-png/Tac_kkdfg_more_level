package com.kkdfg.Tacz_morelevel.entity.ai;

import java.util.Optional;
import net.minecraft.util.Mth;

/**
 * 离散阻力+重力抛体：每 tick 先按当前速度位移，再速度乘 {@code a}，竖直减 {@code b}。
 * <p>
 * 固定飞行拍数 {@code n} 时，所有仰角落点构成圆：圆心 {@code (0, B_n)}，半径 {@code V·A_n}。
 * 本类只在 {@code n = 1..nMax} 上找<strong>第一个</strong>使目标落在圆盘内（含边界）的 {@code n}，
 * 再与该 {@code n} 绑定求仰角；超距（皆在圆外）返回 empty。不含引信/弹跳/挡墙。
 */
public final class DiscreteDragGravityBallistics {

    private DiscreteDragGravityBallistics() {}

    /**
     * @param ticks 飞行拍数 {@code n ≥ 1}
     * @param pitchElevationDeg 相对水平面仰角（度，向上为正）
     * @param coverage {@code r² - d²}；≥ 0 表示目标在圆盘内
     */
    public record Solution(int ticks, float pitchElevationDeg, double coverage) {}

    /**
     * @param x 目标相对发射点水平距（向右为正；通常用水平距离的绝对值或带符号的平面 x）
     * @param y 目标相对发射点高度差（向上为正）
     * @param speed 初速 {@code V}（格/tick）
     * @param drag 阻力乘子 {@code a}，{@code (0,1)}；{@code a → 1} 走无阻力极限
     * @param gravity 每 tick 竖直速度减量 {@code b > 0}
     * @param maxTicks 搜索上限（含），至少为 1
     */
    public static Optional<Solution> solveFirstCovering(
            double x,
            double y,
            double speed,
            double drag,
            double gravity,
            int maxTicks) {
        if (!(speed > 0.0D) || !(gravity >= 0.0D) || maxTicks < 1) {
            return Optional.empty();
        }
        if (!(drag > 0.0D) || drag > 1.0D) {
            return Optional.empty();
        }

        final boolean noDrag = Math.abs(1.0D - drag) < 1.0e-12D;
        for (int n = 1; n <= maxTicks; n++) {
            double aN;
            double bN;
            if (noDrag) {
                aN = n;
                bN = -gravity * n * (n - 1) * 0.5D;
            } else {
                double oneMinusAn = 1.0D - Math.pow(drag, n);
                double oneMinusA = 1.0D - drag;
                aN = oneMinusAn / oneMinusA;
                bN = gravity * oneMinusAn / (oneMinusA * oneMinusA) - n * gravity / oneMinusA;
            }
            double radius = speed * aN;
            double dy = y - bN;
            double distSq = x * x + dy * dy;
            double coverage = radius * radius - distSq;
            if (coverage >= 0.0D) {
                float pitch = (float) (Mth.atan2(dy, x) * (180.0D / Math.PI));
                return Optional.of(new Solution(n, pitch, coverage));
            }
        }
        return Optional.empty();
    }
}
