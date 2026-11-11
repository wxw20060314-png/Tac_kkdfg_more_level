package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class MechanicalHorseMissileLauncher {
    private static final int COOLDOWN_TICKS = 100;
    private static final double MAX_RANGE = 256.0D;
    private static final double MUZZLE_SIDE = 7.38D / 16.0D;
    private static final double MUZZLE_FORWARD = 3.18D / 16.0D;
    private static final double UPPER_MUZZLE_HEIGHT = 20.82D / 16.0D;
    private static final double LOWER_MUZZLE_HEIGHT = 18.48D / 16.0D;
    private static final double CLEARANCE = 0.60D;
    private static final double RECOIL = 0.25D;
    private static final String NEXT_VOLLEY_TICK = "MechanicalHorseNextMissileVolley";

    private MechanicalHorseMissileLauncher() {}

    static boolean tryFire(MechanicalHorseEntity horse, Vec3 requested, @Nullable LivingEntity tracking) {
        if (!(horse.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || !horse.isAlive() || !isFinite(requested)) {
            return false;
        }
        if (tracking != null && !horse.canLockMissileTarget(tracking)) {
            return false;
        }

        CompoundTag state = horse.getPersistentData();
        long now = level.getGameTime();
        long nextVolley = state.getLong(NEXT_VOLLEY_TICK);
        if (now < nextVolley) {
            return false;
        }

        Vec3 origin = horse.position();
        Vec3 target = tracking == null ? limitTarget(level, horse, origin, requested) : requested;
        UUID tracked = tracking == null ? null : tracking.getUUID();
        Vec3 forward = Vec3.directionFromRotation(0.0F, horse.yBodyRot);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        List<MechanicalHorseMissileEntity> volley = new ArrayList<>(4);
        for (int sign : new int[]{-1, 1}) {
            for (boolean upper : new boolean[]{true, false}) {
                double height = upper ? UPPER_MUZZLE_HEIGHT : LOWER_MUZZLE_HEIGHT;
                Vec3 muzzle = origin.add(forward.scale(MUZZLE_FORWARD))
                        .add(side.scale(sign * MUZZLE_SIDE)).add(0.0D, height, 0.0D);
                Vec3 direction = upper ? forward.add(0.0D, 0.48D, 0.0D).normalize() : forward;
                if (!isMuzzleClear(level, horse, muzzle, direction)) {
                    return false;
                }
                MechanicalHorseMissileEntity missile = ModEntities.MECHANICAL_HORSE_MISSILE.get().create(level);
                if (missile == null) {
                    return false;
                }
                missile.launch(horse, muzzle, direction, target, tracked, upper);
                volley.add(missile);
            }
        }

        state.putLong(NEXT_VOLLEY_TICK, now + COOLDOWN_TICKS);
        boolean fired = false;
        try {
            for (MechanicalHorseMissileEntity missile : volley) {
                if (!level.addFreshEntity(missile)) {
                    return false;
                }
            }
            fired = true;
        } finally {
            if (!fired) {
                volley.forEach(Entity::discard);
                state.putLong(NEXT_VOLLEY_TICK, nextVolley);
            }
        }

        horse.push(-forward.x * RECOIL, 0.0D, -forward.z * RECOIL);
        horse.hurtMarked = true;
        level.playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                ModSounds.SOUND_PUNISHER_SHOULDER_CANNON_SHOOT.get(), SoundSource.HOSTILE, 1.2F, 0.75F);
        return true;
    }

    static boolean inRange(Vec3 origin, Vec3 target) {
        return origin.distanceToSqr(target) <= MAX_RANGE * MAX_RANGE;
    }

    static Vec3 limitTarget(ServerLevel level, Entity source, Vec3 origin, Vec3 requested) {
        boolean tooFar = !inRange(origin, requested);
        Vec3 target = tooFar ? origin.add(requested.subtract(origin).normalize().scale(MAX_RANGE)) : requested;
        WorldBorder border = level.getWorldBorder();
        target = new Vec3(Mth.clamp(target.x, border.getMinX(), border.getMaxX()),
                Mth.clamp(target.y, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1),
                Mth.clamp(target.z, border.getMinZ(), border.getMaxZ()));
        if (!tooFar || !level.hasChunkAt(BlockPos.containing(target))) {
            return target;
        }

        Vec3 below = new Vec3(target.x, Math.max(level.getMinBuildHeight(), target.y - 12.0D), target.z);
        BlockHitResult ground = level.clip(new ClipContext(
                target, below, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source));
        return ground.getType() == HitResult.Type.BLOCK ? ground.getLocation() : target;
    }

    private static boolean isMuzzleClear(ServerLevel level, Entity horse, Vec3 muzzle, Vec3 direction) {
        Vec3 exit = muzzle.add(direction.scale(CLEARANCE));
        return level.hasChunkAt(BlockPos.containing(muzzle))
                && level.hasChunkAt(BlockPos.containing(exit))
                && level.getWorldBorder().isWithinBounds(BlockPos.containing(exit))
                && !level.getBlockCollisions(horse, AABB.ofSize(muzzle, 0.14D, 0.14D, 0.14D)).iterator().hasNext()
                && level.clip(new ClipContext(muzzle, exit, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, horse)).getType() == HitResult.Type.MISS;
    }

    private static boolean isFinite(Vec3 p) {
        return Double.isFinite(p.x) && Double.isFinite(p.y) && Double.isFinite(p.z);
    }
}
