package com.kkdfg.Tacz_morelevel.power_mech.war_machine;

import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechFlightState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

/** 战争机器飞行持续粒子：固定间隔与数量，不读胸甲能量。 */
public final class WarMachineFlightEffects {

    private WarMachineFlightEffects() {}

    public static void tickEmit(WarMachineEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (!PowerMechFlightState.isPowerMechFlying(entity)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        int interval = Math.max(1, WarMachineFlightParticleLayout.emitIntervalTicks);
        if (entity.tickCount % interval != 0) {
            return;
        }

        int flames = Math.max(0, WarMachineFlightParticleLayout.flameCount);
        int smokes = Math.max(0, WarMachineFlightParticleLayout.smokeCount);
        for (float[] offset : WarMachineFlightParticleLayout.emitters()) {
            emitAt(level, entity, offset, flames, smokes);
        }
    }

    private static void emitAt(
            ServerLevel level, WarMachineEntity entity, float[] local, int flames, int smokes) {
        double[] world =
                WarMachineFlightParticleLayout.toWorldPosition(
                        entity, local[0], local[1], local[2]);
        double x = world[0];
        double y = world[1];
        double z = world[2];
        if (flames > 0) {
            level.sendParticles(
                    ParticleTypes.FLAME,
                    x,
                    y,
                    z,
                    flames,
                    WarMachineFlightParticleLayout.spreadX,
                    WarMachineFlightParticleLayout.spreadY,
                    WarMachineFlightParticleLayout.spreadZ,
                    WarMachineFlightParticleLayout.flameSpeed);
        }
        if (smokes > 0) {
            level.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    x,
                    y,
                    z,
                    smokes,
                    WarMachineFlightParticleLayout.spreadX,
                    WarMachineFlightParticleLayout.spreadY,
                    WarMachineFlightParticleLayout.spreadZ,
                    WarMachineFlightParticleLayout.smokeSpeed);
        }
    }
}
