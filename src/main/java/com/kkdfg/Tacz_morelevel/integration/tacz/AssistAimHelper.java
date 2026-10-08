package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.tacz.guns.api.item.IGun;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * HK-416D 歼灭者辅助瞄准：命中缓存 + 视线射线宽容区；单次步进 yaw/pitch 按距离：
 * ≤{@link #FULL_STEP_DISTANCE} 格为 {@link #FULL_STEP_DEG}°，更远为 {@code 12/d}°。
 */
public final class AssistAimHelper {

    public static final ResourceLocation GUN_ID =
            ResourceLocation.fromNamespaceAndPath("tacz", "hk416d_annihilator");

    /** ≤此距离时，每 tick 水平/垂直最多拉近的度数。 */
    public static final double FULL_STEP_DISTANCE = 12.0;
    public static final float FULL_STEP_DEG = 1.0f;
    /** 命中后记忆时长（client tick），再次命中刷新；{@link com.kkdfg.Tacz_morelevel.client.AssistAimExecutor} 按玩家每 tick -1。 */
    public static final int HIT_MEMORY_TICKS = 40;
    /** 目标与玩家视线射线的最大垂直距离（格），等价于原 3×3 宽容区宽度。 */
    public static final double MAX_AIM_RAY_OFFSET = 1.0;
    /** 玩家与目标的最大直线距离（格），超过则不辅助。 */
    public static final double MAX_ASSIST_RANGE = 200.0;

    private AssistAimHelper() {}

    public static boolean isAnnihilator(@Nullable ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        return gun != null && GUN_ID.equals(gun.getGunId(stack));
    }

    /** 开火时是否仍可对缓存目标辅助（存活、距离、准线宽容区）。 */
    public static boolean canAssistToward(Player player, LivingEntity target) {
        if (!target.isAlive() || target.isSpectator() || target == player) {
            return false;
        }
        if (target.level() != player.level()) {
            return false;
        }
        if (player.distanceTo(target) > MAX_ASSIST_RANGE) {
            return false;
        }
        return distanceToAimRay(player, target.getEyePosition(1.0F)) <= MAX_AIM_RAY_OFFSET;
    }

    /** 实体参考点到「眼位 + 视线方向」直线的垂直距离（格）。 */
    public static double distanceToAimRay(Player player, Vec3 targetPoint) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 toTarget = targetPoint.subtract(eye);
        return toTarget.cross(look).length();
    }

    public static float stepYawToward(Player player, LivingEntity target) {
        float desired = computeYawToward(player, target);
        return stepAngle(player.getYRot(), desired, stepDegForDistance(player.distanceTo(target)));
    }

    public static float stepPitchToward(Player player, LivingEntity target) {
        float desired = computePitchToward(player, target);
        return stepAngle(player.getXRot(), desired, stepDegForDistance(player.distanceTo(target)));
    }

    /** ≤12 格 1°；更远 {@code 12/d}°（d 为与目标的直线距离）。 */
    private static float stepDegForDistance(double distance) {
        if (distance <= FULL_STEP_DISTANCE) {
            return FULL_STEP_DEG;
        }
        return (float) (FULL_STEP_DISTANCE / distance);
    }

    /** 射手眼位指向目标眼位的水平角（度）。 */
    private static float computeYawToward(LivingEntity shooter, LivingEntity target) {
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 aim = target.getEyePosition(1.0F);
        double dx = aim.x - eye.x;
        double dz = aim.z - eye.z;
        if (dx * dx + dz * dz < 1.0E-8D) {
            return shooter.getYRot();
        }
        return Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG - 90.0F));
    }

    /** 射手眼位指向目标眼位的俯仰角（度），clamp 在 ±90°。 */
    private static float computePitchToward(LivingEntity shooter, LivingEntity target) {
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 aim = target.getEyePosition(1.0F);
        double dx = aim.x - eye.x;
        double dy = aim.y - eye.y;
        double dz = aim.z - eye.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0E-4D && Math.abs(dy) < 1.0E-4D) {
            return shooter.getXRot();
        }
        float xRot = (float) (-Mth.atan2(dy, horiz) * Mth.RAD_TO_DEG);
        return Mth.clamp(Mth.wrapDegrees(xRot), -90.0F, 90.0F);
    }

    private static float stepAngle(float current, float desired, float maxStep) {
        float delta = Mth.wrapDegrees(desired - current);
        if (Math.abs(delta) <= maxStep) {
            return desired;
        }
        return current + Math.signum(delta) * maxStep;
    }
}
