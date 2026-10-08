package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 炸弹宝宝榴弹弹道：发射点为原点，目标高度线 {@code y = H}。
 * <ul>
 *   <li>{@code H > 0}：仰角 0–90°，远交点 / 近交点各自收集区间，各取更近
 *       {@link #PREFERRED_AIM_PITCH_DEG} 的一档，再两者比谁更近该常量。</li>
 *   <li>{@code H < 0}：仅一交点，仰角 0～-90° 扫描，合法区间进集合后取最接近 0° 的。</li>
 * </ul>
 * 无交点的仰角跳过；x≈0 的退化首交点丢弃后按单交点正常流程。
 */
public final class BombBabyBallistics {

    /** 0–±90° 分割份数（步长 1.5°）。 */
    public static final int PITCH_SAMPLES = 60;

    /** 目标水平距低于该值时直瞄目标（不跑采样）。 */
    public static final double DIRECT_AIM_RANGE = 10.0D;

    /**
     * 正仰角优选参考（度）。改这个数即可切换「靠近多少度」；非配置项。
     */
    public static final float PREFERRED_AIM_PITCH_DEG = 45.0F;

    private static final float MAX_PITCH_DEG = 90.0F;
    private static final double MIN_HIT_X = 0.05D;
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
        // 与 TaCZ shootOnce：processedSpeed = (speed / 20) * globalModifier
        return new BulletPhysics(
                Mth.clamp(speed / 20.0F, 0.0F, Float.MAX_VALUE), gravity, friction, lifeTicks);
    }

    /**
     * 计算朝向目标的 yaw 与仰角；无法到达返回 empty。
     *
     * @param preferHighArc 已废弃：筛选改由 {@link #PREFERRED_AIM_PITCH_DEG} 决定，参数保留兼容调用方
     */
    public static Optional<AimAngles> solveAim(
            LivingEntity shooter,
            LivingEntity target,
            ResourceLocation gunDataId,
            @Nullable Boolean preferHighArc,
            RandomSource random) {
        if (shooter == null || target == null || !target.isAlive()) {
            return Optional.empty();
        }
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 aimPoint = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        double dx = aimPoint.x - eye.x;
        double dz = aimPoint.z - eye.z;
        double range = Math.sqrt(dx * dx + dz * dz);
        double height = aimPoint.y - eye.y;
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        if (range < 0.05D) {
            float elev = height >= 0.0D ? 85.0F : -30.0F;
            return Optional.of(new AimAngles(yaw, elev));
        }

        if (range <= DIRECT_AIM_RANGE) {
            float elev =
                    (float) (Mth.atan2(height, range) * (180.0D / Math.PI));
            elev = Mth.clamp(elev, -90.0F, 85.0F);
            return Optional.of(new AimAngles(yaw, elev));
        }

        BulletPhysics physics = readPhysics(gunDataId);
        Optional<PitchBracket> chosen = pickBracket(range, height, physics);
        if (chosen.isEmpty()) {
            return Optional.empty();
        }
        PitchBracket bracket = chosen.get();
        float pitch = Mth.lerp(random.nextFloat(), bracket.loDeg(), bracket.hiDeg());
        return Optional.of(new AimAngles(yaw, pitch));
    }

    public static Optional<AimAngles> solveAim(
            LivingEntity shooter, LivingEntity target, ResourceLocation gunDataId, RandomSource random) {
        return solveAim(shooter, target, gunDataId, null, random);
    }

    private record PitchBracket(float loDeg, float hiDeg, float midDeg) {}

    private record PitchSample(float pitchDeg, double rangeAtHeight) {}

    /**
     * H≥0：远交点、近交点各自进集合并取近 {@link #PREFERRED_AIM_PITCH_DEG}，再比两者。
     * H&lt;0：0～-90 单交点集合，取最接近 0°。
     */
    private static Optional<PitchBracket> pickBracket(
            double targetRange, double targetHeight, BulletPhysics physics) {
        if (targetHeight < 0.0D) {
            List<PitchBracket> below =
                    bracketsAlongPitchSweep(
                            0.0F, -MAX_PITCH_DEG, false, targetRange, targetHeight, physics);
            return pickClosestTo(below, 0.0F);
        }

        List<PitchBracket> far =
                bracketsAlongPitchSweep(0.0F, MAX_PITCH_DEG, true, targetRange, targetHeight, physics);
        List<PitchBracket> near =
                bracketsAlongPitchSweep(0.0F, MAX_PITCH_DEG, false, targetRange, targetHeight, physics);

        Optional<PitchBracket> farBest = pickClosestTo(far, PREFERRED_AIM_PITCH_DEG);
        Optional<PitchBracket> nearBest = pickClosestTo(near, PREFERRED_AIM_PITCH_DEG);

        List<PitchBracket> finalists = new ArrayList<>(2);
        farBest.ifPresent(finalists::add);
        nearBest.ifPresent(finalists::add);
        return pickClosestTo(finalists, PREFERRED_AIM_PITCH_DEG);
    }

    private static Optional<PitchBracket> pickClosestTo(List<PitchBracket> brackets, float preferredDeg) {
        if (brackets.isEmpty()) {
            return Optional.empty();
        }
        PitchBracket best = brackets.get(0);
        float bestDist = Math.abs(best.midDeg() - preferredDeg);
        for (int i = 1; i < brackets.size(); i++) {
            PitchBracket b = brackets.get(i);
            float d = Math.abs(b.midDeg() - preferredDeg);
            if (d < bestDist) {
                best = b;
                bestDist = d;
            }
        }
        return Optional.of(best);
    }

    /**
     * 从 {@code pitchFrom} 扫到 {@code pitchTo}（含端点）；{@code useFarHit} 为远交点否则近交点。
     * 无交点的仰角不进样本列表，再在有效样本间夹目标距离。
     */
    private static List<PitchBracket> bracketsAlongPitchSweep(
            float pitchFrom,
            float pitchTo,
            boolean useFarHit,
            double targetRange,
            double targetHeight,
            BulletPhysics physics) {
        int n = PITCH_SAMPLES;
        float span = pitchTo - pitchFrom;
        List<PitchSample> valid = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            float pitchDeg = pitchFrom + span * (i / (float) n);
            double[] hits = intersectRangesAtHeight(pitchDeg, targetHeight, physics);
            double dist = useFarHit ? hits[1] : hits[0];
            if (!Double.isNaN(dist)) {
                valid.add(new PitchSample(pitchDeg, dist));
            }
        }
        return bracketsFromValidSamples(valid, targetRange);
    }

    /** 仅在「有交点」的仰角样本之间夹距离；跳过中间无交点的空洞。 */
    private static List<PitchBracket> bracketsFromValidSamples(List<PitchSample> valid, double targetRange) {
        List<PitchBracket> out = new ArrayList<>();
        for (int i = 0; i < valid.size(); i++) {
            PitchSample s = valid.get(i);
            if (Math.abs(s.rangeAtHeight() - targetRange) <= 1.0e-3D) {
                out.add(new PitchBracket(s.pitchDeg(), s.pitchDeg(), s.pitchDeg()));
            }
        }
        for (int i = 0; i < valid.size() - 1; i++) {
            PitchSample a = valid.get(i);
            PitchSample b = valid.get(i + 1);
            double lo = Math.min(a.rangeAtHeight(), b.rangeAtHeight());
            double hi = Math.max(a.rangeAtHeight(), b.rangeAtHeight());
            if (targetRange >= lo && targetRange <= hi) {
                float p0 = a.pitchDeg();
                float p1 = b.pitchDeg();
                out.add(new PitchBracket(Math.min(p0, p1), Math.max(p0, p1), (p0 + p1) * 0.5F));
            }
        }
        return out;
    }

    /**
     * @return {@code [nearOrNaN, farOrNaN]} 与高度线交点的水平距离；丢弃 x≈0 的退化首点后按出现顺序记入。
     */
    private static double[] intersectRangesAtHeight(float pitchElevDeg, double heightLine, BulletPhysics physics) {
        double[] hits = new double[] {Double.NaN, Double.NaN};
        int hitCount = 0;
        double pitchRad = Math.toRadians(pitchElevDeg);
        double vx = physics.speedPerTick() * Math.cos(pitchRad);
        double vy = physics.speedPerTick() * Math.sin(pitchRad);
        double x = 0.0D;
        double y = 0.0D;
        double prevX = 0.0D;
        double prevY = 0.0D;
        double drag = 1.0D - physics.friction();

        for (int tick = 0; tick < physics.maxLifeTicks(); tick++) {
            prevX = x;
            prevY = y;
            x += vx;
            y += vy;
            vx *= drag;
            vy *= drag;
            vy -= physics.gravity();

            if ((prevY - heightLine) * (y - heightLine) <= 0.0D && Math.abs(y - prevY) > 1.0e-8D) {
                double t = (heightLine - prevY) / (y - prevY);
                double hitX = prevX + t * (x - prevX);
                // 出发点 / 平行贴线时的 x≈0 假首点丢掉，其余交点正常记（可只剩 1 个）
                if (hitX > MIN_HIT_X) {
                    if (hitCount == 0) {
                        hits[0] = hitX;
                        hitCount = 1;
                    } else {
                        hits[1] = hitX;
                        if (hits[0] > hits[1]) {
                            double tmp = hits[0];
                            hits[0] = hits[1];
                            hits[1] = tmp;
                        }
                        return hits;
                    }
                }
            }
            if (vx * vx + vy * vy < 1.0e-6D) {
                break;
            }
        }
        return hits;
    }

    /** 水平距离（眼睛到目标中心）。 */
    public static double horizontalRange(LivingEntity shooter, LivingEntity target) {
        Vec3 eye = shooter.getEyePosition(1.0F);
        Vec3 aim = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        double dx = aim.x - eye.x;
        double dz = aim.z - eye.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
