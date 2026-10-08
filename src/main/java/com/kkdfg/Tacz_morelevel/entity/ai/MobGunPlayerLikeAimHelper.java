package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.api.TaczCrawlCapableMob;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 持枪战斗时的视角与 {@link net.minecraft.world.entity.player.Player} 对齐：
 * 头/手 instant 跟目标；有位移时躯干朝移动方向缓跟（受 {@link #MAX_HEAD_BODY_YAW_DEG}° 限制）。
 * 非交火时由实现 {@link com.kkdfg.Tacz_morelevel.api.PlayerLikeHeadBodyYawMob} 的实体每 tick 调用
 * {@link #tickHeadTurnBodyFollow} 保持同一限制。
 *
 * <p>交火瞄点：头（眼）→ 身中 → 脚，用 {@link SoldierGunPassthroughSight} 射线判定；
 * 索敌仍用眼对眼 {@link SoldierGunPassthroughSight#hasLineOfSight}。
 */
public final class MobGunPlayerLikeAimHelper {

    /** 头相对躯干的最大水平偏角（度）；躯干相对头同理。 */
    public static final float MAX_HEAD_BODY_YAW_DEG = 45.0F;
    /** 与原版 {@link net.minecraft.world.entity.LivingEntity#tickHeadTurn} 一致。 */
    private static final float BODY_FOLLOW_MOVE_FACTOR = 0.3F;
    /** 与原版 {@link net.minecraft.world.entity.LivingEntity#tickHeadTurn} 大角度跟进一致。 */
    private static final float BODY_CATCHUP_DEG_PER_TICK = 50.0F;
    private static final double MIN_MOVE_DELTA_SQ = 0.0025000002D;
    private static final double FOOT_AIM_Y_OFFSET = 0.1D;

    private MobGunPlayerLikeAimHelper() {}

    /**
     * 交火可见点：优先眼，其次身中，再次脚；均不通则 empty。
     * 射击 Goal 的 LOS 应使用 {@link #hasCombatLineOfSight}，与瞄准同源。
     */
    public static Optional<Vec3> findAimPoint(LivingEntity shooter, LivingEntity target) {
        if (shooter == null || target == null || !target.isAlive()) {
            return Optional.empty();
        }
        if (shooter.level() != target.level()) {
            return Optional.empty();
        }
        Vec3 head = target.getEyePosition(1.0F);
        if (SoldierGunPassthroughSight.hasPassthroughLineFromEye(shooter, head)) {
            return Optional.of(head);
        }
        Vec3 body = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        if (SoldierGunPassthroughSight.hasPassthroughLineFromEye(shooter, body)) {
            return Optional.of(body);
        }
        Vec3 foot =
                new Vec3(
                        target.getX(),
                        target.getBoundingBox().minY + FOOT_AIM_Y_OFFSET,
                        target.getZ());
        if (SoldierGunPassthroughSight.hasPassthroughLineFromEye(shooter, foot)) {
            return Optional.of(foot);
        }
        return Optional.empty();
    }

    /** 交火是否看见（头/身/脚任一点通）。 */
    public static boolean hasCombatLineOfSight(LivingEntity shooter, LivingEntity target) {
        return findAimPoint(shooter, target).isPresent();
    }

    /**
     * 每 tick 调用：头/朝向对准目标可打点（无通点时仍朝眼睛转头）；躯干在
     * {@link #tickHeadTurnBodyFollow} 中按位移更新（避免早于 moveControl）。
     */
    public static void applyPlayerLikeView(LivingEntity shooter, LivingEntity target) {
        Vec3 aim = findAimPoint(shooter, target).orElseGet(() -> target.getEyePosition(1.0F));
        float aimYaw = computeYawToward(shooter, aim);
        float aimPitch = computePitchToward(shooter, aim);
        shooter.setYRot(aimYaw);
        shooter.setYHeadRot(aimYaw);
        shooter.setXRot(aimPitch);
        if (shooter instanceof TaczCrawlCapableMob crawl && crawl.isTaczCrawling()) {
            shooter.yBodyRot = aimYaw;
        }
    }

    /**
     * 在 {@link net.minecraft.world.entity.LivingEntity} 位移与 moveControl 之后调用：
     * 横移/追击时躯干朝移动方向缓跟，并 clamp 在 {@code headYaw ± MAX} 内。
     */
    public static void tickHeadTurnBodyFollow(LivingEntity entity, float headYaw) {
        if (entity instanceof TaczCrawlCapableMob crawl && crawl.isTaczCrawling()) {
            entity.yBodyRot = headYaw;
            return;
        }
        float moveYaw = computeMovementYaw(entity);
        if (!Float.isNaN(moveYaw)) {
            syncBodyYawTowardMovement(entity, headYaw, moveYaw);
        } else {
            syncBodyYawToHead(entity, headYaw);
        }
    }

    /**
     * 保证 {@code yBodyRot} 落在 {@code headYaw ± MAX} 内；已超出时每 tick 最多跟进
     * {@link #BODY_CATCHUP_DEG_PER_TICK}°，直到回到允许范围。
     */
    public static void syncBodyYawToHead(LivingEntity entity, float headYaw) {
        float offset = Mth.wrapDegrees(headYaw - entity.yBodyRot);
        if (offset > MAX_HEAD_BODY_YAW_DEG) {
            float targetBody = Mth.wrapDegrees(headYaw - MAX_HEAD_BODY_YAW_DEG);
            entity.yBodyRot = stepBodyYawToward(entity.yBodyRot, targetBody);
        } else if (offset < -MAX_HEAD_BODY_YAW_DEG) {
            float targetBody = Mth.wrapDegrees(headYaw + MAX_HEAD_BODY_YAW_DEG);
            entity.yBodyRot = stepBodyYawToward(entity.yBodyRot, targetBody);
        }
    }

    /**
     * 躯干朝 {@code moveYaw} 缓跟，但相对 {@code headYaw} 不超过 {@link #MAX_HEAD_BODY_YAW_DEG}。
     */
    public static void syncBodyYawTowardMovement(LivingEntity entity, float headYaw, float moveYaw) {
        float offset = Mth.clamp(Mth.wrapDegrees(moveYaw - headYaw), -MAX_HEAD_BODY_YAW_DEG, MAX_HEAD_BODY_YAW_DEG);
        float desiredBody = Mth.wrapDegrees(headYaw + offset);
        float delta = Mth.wrapDegrees(desiredBody - entity.yBodyRot);
        entity.yBodyRot = Mth.wrapDegrees(entity.yBodyRot + delta * BODY_FOLLOW_MOVE_FACTOR);
        syncBodyYawToHead(entity, headYaw);
    }

    /**
     * @return 本 tick 水平位移方向（度）；无显著位移时 {@link Float#NaN}
     */
    public static float computeMovementYaw(LivingEntity entity) {
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        if (dx * dx + dz * dz < MIN_MOVE_DELTA_SQ) {
            return Float.NaN;
        }
        float moveYaw = Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG - 90.0F));
        float diff = Math.abs(Mth.wrapDegrees(entity.getYRot() - moveYaw));
        if (diff > 95.0F && diff < 265.0F) {
            moveYaw = Mth.wrapDegrees(moveYaw - 180.0F);
        }
        return moveYaw;
    }

    private static float stepBodyYawToward(float current, float target) {
        float delta = Mth.wrapDegrees(target - current);
        if (Math.abs(delta) <= BODY_CATCHUP_DEG_PER_TICK) {
            return target;
        }
        return Mth.wrapDegrees(current + Math.signum(delta) * BODY_CATCHUP_DEG_PER_TICK);
    }

    public static float computeYawToward(LivingEntity shooter, LivingEntity target) {
        return computeYawToward(
                shooter, findAimPoint(shooter, target).orElseGet(() -> target.getEyePosition(1.0F)));
    }

    public static float computePitchToward(LivingEntity shooter, LivingEntity target) {
        return computePitchToward(
                shooter, findAimPoint(shooter, target).orElseGet(() -> target.getEyePosition(1.0F)));
    }

    public static float computeYawToward(LivingEntity shooter, Vec3 aim) {
        Vec3 eye = shooter.getEyePosition(1.0F);
        double dx = aim.x - eye.x;
        double dz = aim.z - eye.z;
        if (dx * dx + dz * dz < 1.0E-8D) {
            return shooter.getYRot();
        }
        return Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG - 90.0F));
    }

    public static float computePitchToward(LivingEntity shooter, Vec3 aim) {
        Vec3 eye = shooter.getEyePosition(1.0F);
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
}
