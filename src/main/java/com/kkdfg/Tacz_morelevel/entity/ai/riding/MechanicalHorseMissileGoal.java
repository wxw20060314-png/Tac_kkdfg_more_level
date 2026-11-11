package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalCowboyEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Fires a tracking volley at the mounted cowboy's current target beyond 10 blocks. */
public class MechanicalHorseMissileGoal extends Goal {

    private static final double MIN_DISTANCE_SQR = 10.0D * 10.0D;

    private final MechanicalCowboyEntity cowboy;

    public MechanicalHorseMissileGoal(MechanicalCowboyEntity cowboy) {
        this.cowboy = cowboy;
    }

    @Override
    public boolean canUse() {
        return this.missileTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        if (!(this.cowboy.getVehicle() instanceof MechanicalHorseEntity horse)) {
            return;
        }
        LivingEntity target = this.missileTarget();
        if (target != null) {
            horse.tryFireMissiles(target);
        }
    }

    private LivingEntity missileTarget() {
        if (!(this.cowboy.getVehicle() instanceof MechanicalHorseEntity)) {
            return null;
        }
        LivingEntity target = this.cowboy.getTarget();
        if (target == null || !target.isAlive() || target.level() != this.cowboy.level()) {
            return null;
        }
        if (this.cowboy.distanceToSqr(target) <= MIN_DISTANCE_SQR) {
            return null;
        }
        return target;
    }
}
