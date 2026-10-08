package com.kkdfg.Tacz_morelevel.power_mech;

import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechFlightState;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** 动力机甲持续飞行：服务端粒子与音效（与 {@link com.kkdfg.Tacz_morelevel.armor.skill.power_mech.PowerMechOverloadEffects} 同套路）。 */
public final class PowerMechFlightEffects {

    private static final double STOP_SOUND_RANGE = 64.0;

    private PowerMechFlightEffects() {}

    public static void tickEmit(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (!PowerMechFlightState.isPowerMechFlying(entity)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        int energy =
                PowerMechBulletproofArmorItem.getFlightEnergy(
                        entity.getItemBySlot(EquipmentSlot.CHEST));
        if (entity.tickCount % PowerMechFlightParticleLayout.getEmitIntervalTicks(energy) != 0) {
            return;
        }

        int flames = PowerMechFlightParticleLayout.getFlameCount(energy);
        int smokes = PowerMechFlightParticleLayout.getSmokeCount(energy);
        for (float[] offset : PowerMechFlightParticleLayout.allEmitters()) {
            emitAt(level, entity, offset, flames, smokes);
        }
    }

    public static void startFlightSound(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        entity.level()
                .playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        ModSounds.SOUND_POWER_MECH_FLIGHT.get(),
                        SoundSource.PLAYERS,
                        PowerMechFlightSoundConstants.volume,
                        PowerMechFlightSoundConstants.pitch);
    }

    public static void stopFlightSound(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        var soundLocation = ModSounds.SOUND_POWER_MECH_FLIGHT.get().getLocation();
        var packet =
                new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                        soundLocation, SoundSource.PLAYERS);
        double rangeSq = STOP_SOUND_RANGE * STOP_SOUND_RANGE;
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(entity) <= rangeSq) {
                viewer.connection.send(packet);
            }
        }
    }

    private static void emitAt(
            ServerLevel level, LivingEntity entity, float[] local, int flames, int smokes) {
        double[] world =
                PowerMechFlightParticleLayout.toWorldPosition(
                        entity, local[0], local[1], local[2]);
        double x = world[0];
        double y = world[1];
        double z = world[2];
        level.sendParticles(
                ParticleTypes.FLAME,
                x,
                y,
                z,
                flames,
                PowerMechFlightParticleLayout.spreadX,
                PowerMechFlightParticleLayout.spreadY,
                PowerMechFlightParticleLayout.spreadZ,
                PowerMechFlightParticleLayout.flameSpeed);
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                x,
                y,
                z,
                smokes,
                PowerMechFlightParticleLayout.spreadX,
                PowerMechFlightParticleLayout.spreadY,
                PowerMechFlightParticleLayout.spreadZ,
                PowerMechFlightParticleLayout.smokeSpeed);
    }
}
