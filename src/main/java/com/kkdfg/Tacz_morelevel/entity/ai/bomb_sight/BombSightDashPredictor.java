package com.kkdfg.Tacz_morelevel.entity.ai.bomb_sight;

import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 炸弹视野冲刺估距：平面坐标自己 (0,0)、目标头 (x0, y0)；
 * 朝头方向加冲量后做带重力抛物线仿真，取水平距达到 x0 时的 y1，
 * 仅当 {@code y0 - H ≤ y1 ≤ y0}（H 默认身高）判定可行。
 */
public final class BombSightDashPredictor {

    /** 空中水平阻尼（每 tick 乘子）。 */
    public static final double AIR_FRICTION = 0.91D;
    /** 竖直速度阻尼（接近原版活体空中）。 */
    public static final double VERTICAL_DRAG = 0.98D;
    /** 每 tick 重力加速度（格/tick²）。 */
    public static final double GRAVITY = 0.08D;
    public static final double STOP_SPEED = 0.02D;
    public static final int MAX_SIM_TICKS = 100;
    /** 目标身高带高度（头以下），默认玩家身高。 */
    public static final double DEFAULT_BODY_HEIGHT = 1.8D;

    private BombSightDashPredictor() {}

    /**
     * 释放点取眼高；目标点取头部。平面：x0=水平距，y0=头相对眼高的升高。
     *
     * @return 抛物线在 x=x0 处的 y1 落在 [y0-H, y0] 内则为可行
     */
    public static boolean isTrajectoryFeasible(
            LivingEntity self, LivingEntity target, int kinetic) {
        return isTrajectoryFeasible(self, target, kinetic, DEFAULT_BODY_HEIGHT);
    }

    public static boolean isTrajectoryFeasible(
            LivingEntity self, LivingEntity target, int kinetic, double bodyHeight) {
        double impulse = ShockwaveBulletproofArmorItem.impulseFromKinetic(kinetic);
        if (impulse <= 1.0E-6D) {
            return false;
        }
        double launchX = self.getX();
        double launchY = self.getEyeY();
        double launchZ = self.getZ();
        double headX = target.getX();
        double headY = target.getEyeY();
        double headZ = target.getZ();

        double x0 = horizontalDistance(headX - launchX, headZ - launchZ);
        if (x0 < 1.0E-3D) {
            return false;
        }
        double y0 = headY - launchY;
        double h = Math.max(0.1D, bodyHeight);

        Vec3 toHead = new Vec3(headX - launchX, headY - launchY, headZ - launchZ);
        if (toHead.lengthSqr() < 1.0E-8D) {
            return false;
        }
        Vec3 look = toHead.normalize();
        double vx = look.x * impulse;
        double vy = look.y * impulse;
        double vz = look.z * impulse;

        double px = 0.0D;
        double py = 0.0D;
        double pz = 0.0D;
        double prevHoriz = 0.0D;
        double prevY = 0.0D;

        for (int i = 0; i < MAX_SIM_TICKS; i++) {
            px += vx;
            py += vy;
            pz += vz;
            double horiz = Math.sqrt(px * px + pz * pz);

            if (horiz >= x0 && prevHoriz < x0) {
                double t = (x0 - prevHoriz) / Math.max(1.0E-6D, horiz - prevHoriz);
                double y1 = Mth.lerp(t, prevY, py);
                return y1 >= y0 - h && y1 <= y0;
            }

            prevHoriz = horiz;
            prevY = py;

            vy -= GRAVITY;
            vx *= AIR_FRICTION;
            vz *= AIR_FRICTION;
            vy *= VERTICAL_DRAG;

            if (vx * vx + vy * vy + vz * vz < STOP_SPEED * STOP_SPEED) {
                break;
            }
            // 掉到目标身高带以下很多且水平仍未到 x0：后续更低，不可行
            if (horiz < x0 && py < y0 - h - 2.0D && vy <= 0.0D) {
                break;
            }
        }
        return false;
    }

    /** 朝目标头部的瞄准方向（未归一化前的位移也可）。 */
    public static Vec3 lookAtHead(LivingEntity self, LivingEntity target) {
        return new Vec3(
                target.getX() - self.getX(),
                target.getEyeY() - self.getEyeY(),
                target.getZ() - self.getZ());
    }

    public static double horizontalDistance(double dx, double dz) {
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double horizontalDistanceTo(double ax, double az, double bx, double bz) {
        return horizontalDistance(bx - ax, bz - az);
    }

    public static int clampReleaseDelay(int ticks) {
        return Mth.clamp(ticks, 1, 3);
    }
}
