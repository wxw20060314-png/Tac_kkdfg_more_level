package com.kkdfg.Tacz_morelevel.power_mech.war_machine;

import com.kkdfg.Tacz_morelevel.power_mech.PowerMechFlightParticleLayout;
import net.minecraft.world.entity.LivingEntity;

/** 战争机器飞行粒子：固定密度，暂不按胸甲能量缩放。 */
public final class WarMachineFlightParticleLayout {

    public static int emitIntervalTicks = 2;

    public static int flameCount = 2;

    public static int smokeCount = 1;

    public static double spreadX = PowerMechFlightParticleLayout.spreadX;

    public static double spreadY = PowerMechFlightParticleLayout.spreadY;

    public static double spreadZ = PowerMechFlightParticleLayout.spreadZ;

    public static double flameSpeed = PowerMechFlightParticleLayout.flameSpeed;

    public static double smokeSpeed = PowerMechFlightParticleLayout.smokeSpeed;

    private WarMachineFlightParticleLayout() {}

    /** 仅背部双推进点，比玩家四发射点更稀疏。 */
    public static float[][] emitters() {
        return new float[][] {
            {
                PowerMechFlightParticleLayout.backLeftX,
                PowerMechFlightParticleLayout.backLeftY,
                PowerMechFlightParticleLayout.backLeftZ
            },
            {
                PowerMechFlightParticleLayout.backRightX,
                PowerMechFlightParticleLayout.backRightY,
                PowerMechFlightParticleLayout.backRightZ
            }
        };
    }

    public static double[] toWorldPosition(LivingEntity entity, float localX, float localY, float localZ) {
        return PowerMechFlightParticleLayout.toWorldPosition(entity, localX, localY, localZ);
    }
}
