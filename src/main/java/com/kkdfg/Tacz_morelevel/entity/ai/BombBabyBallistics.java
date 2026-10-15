package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 炸弹宝宝榴弹瞄准：近距直瞄；否则 {@link DiscreteDragGravityBallistics} 最早包住解。
 * 瞄点为 {@link #aimPoint}（目标脚下块上表面中心）。仰角解出后再加抖动。
 * <p>旧「仰角扫描积分」见仓库 {@code old_code_reference/BombBabyBallistics_PitchSweepReference.java.txt}。
 */
public final class BombBabyBallistics {

    /** 目标水平距低于该值时直瞄（不跑圆族）。 */
    public static final double DIRECT_AIM_RANGE = 10.0D;

    /** 解出仰角后的随机抖动半幅（度）：实际为 {@code [-JITTER, +JITTER]}。 */
    public static final float AIM_PITCH_JITTER_DEG = 2.0F;

    /** 圆族搜索拍数硬顶（再与弹寿命取 min）。 */
    public static final int MAX_SOLVE_TICKS_CAP = 100;

    private static final float FALLBACK_SPEED = 60.0F;
    private static final float FALLBACK_GRAVITY = 0.06F;
    private static final float FALLBACK_FRICTION = 0.05F;
    private static final float FALLBACK_LIFE_SEC = 5.0F;

    private BombBabyBallistics() {}

    public record BulletPhysics(float speedPerTick, float gravity, float friction, int maxLifeTicks) {}

    /** 仰角（相对水平面，度，向上为正）与水平 yaw。 */
    public record AimAngles(float yawDeg, float pitchElevationDeg) {
        /** Minecraft {@code xRot}：抬头为负。 */
        public float minecraftXRot() {
            return -this.pitchElevationDeg;
        }
    }

    public static BulletPhysics readPhysics(ResourceLocation gunDataId) {
        return TimelessAPI.getCommonGunIndex(gunDataId)
                .map(idx -> fromGunData(idx.getGunData()))
                .orElseGet(BombBabyBallistics::fallbackPhysics);
    }

    private static BulletPhysics fallbackPhysics() {
        return new BulletPhysics(
                FALLBACK_SPEED / 20.0F,
                FALLBACK_GRAVITY,
                FALLBACK_FRICTION,
                Math.max(1, Mth.ceil(FALLBACK_LIFE_SEC * 20.0F)));
    }

    private static BulletPhysics fromGunData(GunData gunData) {
        if (gunData == null) {
            return fallbackPhysics();
        }
        BulletData bullet = gunData.getBulletData();
        if (bullet == null) {
            return fallbackPhysics();
        }
        float speed = Math.max(0.01F, bullet.getSpeed());
        speed *= AmmoConfig.GLOBAL_BULLET_SPEED_MODIFIER.get();
        float gravity = Math.max(0.0F, bullet.getGravity());
        float friction = Mth.clamp(bullet.getFriction(), 0.0F, 0.99F);
        int lifeTicks = Math.max(1, Mth.ceil(bullet.getLifeSecond() * 20.0F));
        return new BulletPhysics(
                Mth.clamp(speed / 20.0F, 0.0F, Float.MAX_VALUE), gravity, friction, lifeTicks);
    }

    /**
     * 计算朝向目标的 yaw 与仰角；无法到达返回 empty。
     */
    public static Optional<AimAngles> solveAim(
            LivingEntity shooter,
            LivingEntity target,
            ResourceLocation gunDataId,
            RandomSource random) {
        if (shooter == null || target == null || !target.isAlive() || random == null) {
            return Optional.empty();
        }
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 aimPoint = aimPoint(target);
        double dx = aimPoint.x - eye.x;
        double dz = aimPoint.z - eye.z;
        double range = Math.sqrt(dx * dx + dz * dz);
        double height = aimPoint.y - eye.y;
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        float pitch;
        if (range < 0.05D) {
            pitch = height >= 0.0D ? 85.0F : -30.0F;
        } else if (range <= DIRECT_AIM_RANGE) {
            pitch = (float) (Mth.atan2(height, range) * (180.0D / Math.PI));
            pitch = Mth.clamp(pitch, -90.0F, 85.0F);
        } else {
            BulletPhysics physics = readPhysics(gunDataId);
            double drag = 1.0D - physics.friction();
            int nMax = Mth.clamp(physics.maxLifeTicks(), 1, MAX_SOLVE_TICKS_CAP);
            Optional<DiscreteDragGravityBallistics.Solution> sol =
                    DiscreteDragGravityBallistics.solveFirstCovering(
                            range,
                            height,
                            physics.speedPerTick(),
                            drag,
                            physics.gravity(),
                            nMax);
            if (sol.isEmpty()) {
                return Optional.empty();
            }
            pitch = sol.get().pitchElevationDeg();
        }
        pitch = applyPitchJitter(pitch, random);
        return Optional.of(new AimAngles(yaw, pitch));
    }

    private static float applyPitchJitter(float pitchElevationDeg, RandomSource random) {
        float jitter =
                Mth.lerp(random.nextFloat(), -AIM_PITCH_JITTER_DEG, AIM_PITCH_JITTER_DEG);
        return Mth.clamp(pitchElevationDeg + jitter, -90.0F, 85.0F);
    }

    /**
     * 榴弹落点：目标脚下那一格（{@code blockPosition().below()}）的上表面中心。
     */
    public static Vec3 aimPoint(LivingEntity target) {
        BlockPos under = target.blockPosition().below();
        return Vec3.atBottomCenterOf(under).add(0.0D, 1.0D, 0.0D);
    }

    /** 水平距离（眼睛到脚下瞄点）。 */
    public static double horizontalRange(LivingEntity shooter, LivingEntity target) {
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 aim = aimPoint(target);
        double dx = aim.x - eye.x;
        double dz = aim.z - eye.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
