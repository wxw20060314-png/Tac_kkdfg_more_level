package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冲击套蓄力：粒子与音效。仅充能期间；玩家与生物共用。
 */
public final class ShockwaveChargeEffects {

    private static final double STOP_SOUND_RANGE = 64.0;

    /**
     * 本地偏移：+X 右，+Y 上，+Z 背。
     * 顺序：左肩、右肩、头后、左背、右背。
     */
    public static float[][] CHARGE_EMITTERS = {
        {-0.35f, 1.35f, 0.50f},
        {0.35f, 1.35f, 0.50f},
        {0.00f, 1.65f, 0.50f},
        {-0.20f, 1.10f, 0.50f},
        {0.20f, 1.10f, 0.50f}
    };

    /** 动能=0 时每发射点粒子份数。 */
    public static int PARTICLE_COUNT_MIN = 1;
    /** 满动能时相对最小值的倍率：max = min * ratio。 */
    public static float PARTICLE_COUNT_MAX_RATIO = 3.0F;

    public static double spreadX = 0.04D;
    public static double spreadY = 0.03D;
    public static double spreadZ = 0.04D;
    public static double flameSpeed = 0.02D;
    public static double smokeSpeed = 0.01D;

    /** 每隔多少 tick 发一次粒子（≥1）；默认 2 降低消耗。 */
    public static int EMIT_INTERVAL_TICKS = 2;

    public static float chargeSoundVolume = 3.0F;
    public static float chargeSoundPitch = 1.0F;
    public static float releaseSoundVolume = 1.0F;
    public static float releaseSoundPitch = 1.0F;

    private ShockwaveChargeEffects() {}

    /** 份数 = lerp(t, min, min×ratio)，t = kinetic / maxKineticFromFullCharge。 */
    public static int particleCountForKinetic(int kinetic) {
        int min = Math.max(0, PARTICLE_COUNT_MIN);
        float ratio = Math.max(1.0F, PARTICLE_COUNT_MAX_RATIO);
        int max = Math.max(min, Math.round(min * ratio));
        int kMax = ShockwaveBulletproofArmorItem.maxKineticFromFullCharge();
        float t = kMax <= 0 ? 0.0F : Mth.clamp(kinetic / (float) kMax, 0.0F, 1.0F);
        return Mth.clamp(Math.round(Mth.lerp(t, (float) min, (float) max)), min, max);
    }

    /** 按下开始蓄力时播放。 */
    public static void startChargeSound(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        entity.level()
                .playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        ModSounds.SOUND_SHOCKWAVE_CHARGE.get(),
                        SoundSource.PLAYERS,
                        chargeSoundVolume,
                        chargeSoundPitch);
    }

    /** 松开 / 释放 / 打断时停止蓄力音。 */
    public static void stopChargeSound(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        var soundLocation = ModSounds.SOUND_SHOCKWAVE_CHARGE.get().getLocation();
        var packet = new ClientboundStopSoundPacket(soundLocation, SoundSource.PLAYERS);
        double rangeSq = STOP_SOUND_RANGE * STOP_SOUND_RANGE;
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(entity) <= rangeSq) {
                viewer.connection.send(packet);
            }
        }
    }

    /** 松开或能量耗尽发射时播放（先停蓄力音再播）。 */
    public static void playReleaseSound(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        stopChargeSound(entity);
        entity.level()
                .playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        ModSounds.SOUND_SHOCKWAVE_RELEASE.get(),
                        SoundSource.PLAYERS,
                        releaseSoundVolume,
                        releaseSoundPitch);
    }

    /** 服务端：胸甲正在蓄力时，五点发火焰+大烟。 */
    public static void tickEmit(LivingEntity entity, ItemStack chest) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (!ShockwaveBulletproofArmorItem.isCharging(chest)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        int interval = Math.max(1, EMIT_INTERVAL_TICKS);
        if (entity.tickCount % interval != 0) {
            return;
        }
        int count = particleCountForKinetic(ShockwaveBulletproofArmorItem.getKinetic(chest));
        if (count <= 0) {
            return;
        }
        for (float[] local : CHARGE_EMITTERS) {
            if (local == null || local.length < 3) {
                continue;
            }
            emitAt(level, entity, local[0], local[1], local[2], count);
        }
    }

    private static void emitAt(
            ServerLevel level,
            LivingEntity entity,
            float localX,
            float localY,
            float localZ,
            int count) {
        double[] world = toWorldPosition(entity, localX, localY, localZ);
        double x = world[0];
        double y = world[1];
        double z = world[2];
        level.sendParticles(
                ParticleTypes.FLAME, x, y, z, count, spreadX, spreadY, spreadZ, flameSpeed);
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE, x, y, z, count, spreadX, spreadY, spreadZ, smokeSpeed);
    }

    /** 实体本地偏移 → 世界坐标。 */
    public static double[] toWorldPosition(
            LivingEntity entity, float localX, float localY, float localZ) {
        float yaw = -entity.getYRot() * Mth.DEG_TO_RAD;
        double cos = Mth.cos(yaw);
        double sin = Mth.sin(yaw);
        double dx = localX * cos - localZ * sin;
        double dz = localX * sin + localZ * cos;
        return new double[] {entity.getX() + dx, entity.getY() + localY, entity.getZ() + dz};
    }
}
