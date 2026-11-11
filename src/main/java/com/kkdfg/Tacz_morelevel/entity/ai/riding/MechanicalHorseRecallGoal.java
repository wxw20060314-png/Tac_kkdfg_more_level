package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalCowboyEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseEntity;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** 把牛仔最后骑过的空机械马末影传到脚下并骑上。看见计时只在这个 Goal 里。 */
public class MechanicalHorseRecallGoal extends Goal {

    private static final int SEE_TICKS = 60;

    private final MechanicalCowboyEntity cowboy;
    private int seeTime;

    public MechanicalHorseRecallGoal(MechanicalCowboyEntity cowboy) {
        this.cowboy = cowboy;
    }

    @Override
    public boolean canUse() {
        this.tickSight();
        MechanicalHorseEntity horse = this.controlledHorse();
        if (horse == null || this.cowboy.isPassenger() || horse.isVehicle()) {
            return false;
        }
        LivingEntity target = this.cowboy.getTarget();
        return target == null || this.seeTime > SEE_TICKS;
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
        MechanicalHorseEntity horse = this.controlledHorse();
        if (!(this.cowboy.level() instanceof ServerLevel level) || horse == null) {
            return;
        }
        Vec3 from = horse.position();
        horse.teleportTo(this.cowboy.getX(), this.cowboy.getY(), this.cowboy.getZ());
        horse.resetFallDistance();
        horse.getNavigation().stop();
        this.playTeleport(level, from);
        this.playTeleport(level, horse.position());
        level.playSound(
                null,
                horse.getX(),
                horse.getY(),
                horse.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                horse.getSoundSource(),
                1.0F,
                1.0F);
        if (!this.cowboy.isPassenger() && !horse.isVehicle()) {
            this.cowboy.startRiding(horse, true);
        }
    }

    private void tickSight() {
        LivingEntity target = this.cowboy.getTarget();
        if (target != null && target.isAlive() && this.cowboy.hasGunCombatSight()) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }
    }

    @Nullable
    private MechanicalHorseEntity controlledHorse() {
        UUID horseId = this.cowboy.controlledMechanicalHorseId();
        if (horseId == null || !(this.cowboy.level() instanceof ServerLevel level)) {
            return null;
        }
        Entity entity = level.getEntity(horseId);
        if (entity instanceof MechanicalHorseEntity horse && horse.isAlive() && horse.level() == level) {
            return horse;
        }
        return null;
    }

    private void playTeleport(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1.0D, pos.z, 32, 0.25D, 0.5D, 0.25D, 0.0D);
    }
}
