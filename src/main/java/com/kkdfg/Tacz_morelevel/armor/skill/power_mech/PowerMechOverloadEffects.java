package com.kkdfg.Tacz_morelevel.armor.skill.power_mech;

import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/** 动力机甲过载：服务端音效与周围火焰/烟雾粒子。 */
public final class PowerMechOverloadEffects {

    private static final int FLAME_COUNT = 28;
    private static final int SMOKE_COUNT = 20;
    private static final double SPREAD_XZ = 0.55;
    private static final double SPREAD_Y = 0.45;

    private PowerMechOverloadEffects() {}

    public static void playActivate(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        entity.level()
                .playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        ModSounds.SOUND_POWER_MECH_OVERLOAD.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
        spawnBurst(entity);
    }

    private static void spawnBurst(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.45;
        double z = entity.getZ();
        double spreadX = Math.max(entity.getBbWidth() * 0.5, SPREAD_XZ);
        double spreadY = Math.max(entity.getBbHeight() * 0.35, SPREAD_Y);

        level.sendParticles(
                ParticleTypes.FLAME,
                x,
                y,
                z,
                FLAME_COUNT,
                spreadX,
                spreadY,
                spreadX,
                0.04);
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                x,
                y,
                z,
                SMOKE_COUNT,
                spreadX,
                spreadY,
                spreadX,
                0.02);
    }
}
