package com.kkdfg.Tacz_morelevel.power_mech;

import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * 动力机甲飞行粒子发射点（相对玩家本地坐标）。
 *
 * <p>localX：右为正；localY：上为正；localZ：背为正（玩家面朝前方时）。
 */
public final class PowerMechFlightParticleLayout {

    public static float backLeftX = -0.35f;
    public static float backLeftY = 1.0f;
    public static float backLeftZ = 0.4f;

    public static float backRightX = 0.35f;
    public static float backRightY = 1.0f;
    public static float backRightZ = 0.4f;

    public static float leftHeelX = -0.12f;
    public static float leftHeelY = 0.05f;
    public static float leftHeelZ = 0.08f;

    public static float rightHeelX = 0.12f;
    public static float rightHeelY = 0.05f;
    public static float rightHeelZ = 0.08f;

    public static double spreadX = 0.03;
    public static double spreadY = 0.02;
    public static double spreadZ = 0.03;

    public static double flameSpeed = 0.012;

    public static double smokeSpeed = 0.006;

    public static int emitIntervalTicks = 5;

    public static int emitIntervalMin = 1;

    public static float flameCountScale = 1f;

    public static float smokeCountScale = 1f;

    public static int flameCountMin = 1;
    public static int flameCountMax = 12;
    public static int smokeCountMin = 1;
    public static int smokeCountMax = 12;

    private PowerMechFlightParticleLayout() {}

    public static int getEmitIntervalTicks(int energy) {
        float multiplier = PowerMechBulletproofArmorItem.getFlightSpeedMultiplier(energy);
        int base = Math.max(1, emitIntervalTicks);
        int min = Mth.clamp(emitIntervalMin, 1, base);
        return Mth.clamp(Mth.floor(base / multiplier), min, base);
    }

    public static int getFlameCount(int energy) {
        float multiplier = PowerMechBulletproofArmorItem.getFlightSpeedMultiplier(energy);
        int interval = getEmitIntervalTicks(energy);
        float raw = multiplier * flameCountScale / interval;
        return Mth.clamp(Mth.floor(raw), flameCountMin, flameCountMax);
    }

    public static int getSmokeCount(int energy) {
        float multiplier = PowerMechBulletproofArmorItem.getFlightSpeedMultiplier(energy);
        int interval = getEmitIntervalTicks(energy);
        float raw = multiplier * smokeCountScale * 0.75f / interval;
        return Mth.clamp(Mth.floor(raw), smokeCountMin, smokeCountMax);
    }

    public static float[][] allEmitters() {
        return new float[][] {
            {backLeftX, backLeftY, backLeftZ},
            {backRightX, backRightY, backRightZ},
            {leftHeelX, leftHeelY, leftHeelZ},
            {rightHeelX, rightHeelY, rightHeelZ}
        };
    }

    /** 实体本地偏移 → 世界坐标。 */
    public static double[] toWorldPosition(LivingEntity entity, float localX, float localY, float localZ) {
        float yaw = -entity.getYRot() * Mth.DEG_TO_RAD;
        double cos = Mth.cos(yaw);
        double sin = Mth.sin(yaw);
        double dx = localX * cos - localZ * sin;
        double dz = localX * sin + localZ * cos;
        return new double[] {entity.getX() + dx, entity.getY() + localY, entity.getZ() + dz};
    }
}
