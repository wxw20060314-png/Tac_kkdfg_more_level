package com.kkdfg.Tacz_morelevel.entity.ai.annihilator;

import com.kkdfg.Tacz_morelevel.entity.custom.AnnihilatorEntity;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 歼灭者空间传送：落点解析、执行传送；成功时写入 {@link AnnihilatorBlinkCooldown}。 */
public final class AnnihilatorBlink {

    private static final Set<Block> FORBIDDEN_BLINK_BLOCKS = Set.of(Blocks.WATER, Blocks.LAVA);
    private static final int STAND_SCAN_TICKS = 8;

    private AnnihilatorBlink() {}

    public static boolean tryBlinkTo(
            AnnihilatorEntity mob, Vec3 dest, AnnihilatorBlinkCooldown cooldown) {
        return tryBlinkTo(mob, dest, cooldown, true);
    }

    public static boolean tryBlinkTo(
            AnnihilatorEntity mob,
            Vec3 dest,
            AnnihilatorBlinkCooldown cooldown,
            boolean requireCooldownReady) {
        if (requireCooldownReady && cooldown.isOnCooldown()) {
            return false;
        }
        if (!canBlinkFromCurrentPosition(mob)) {
            return false;
        }
        if (!executeTeleport(mob, dest)) {
            return false;
        }
        cooldown.trigger();
        return true;
    }

    public static boolean tryBlinkNearTarget(
            AnnihilatorEntity mob, LivingEntity target, AnnihilatorBlinkCooldown cooldown) {
        return resolveNearTarget(mob, target)
                .map(dest -> tryBlinkTo(mob, dest, cooldown))
                .orElse(false);
    }

    /** 受伤闪避：自身周围随机落点；不等待冷却，须有合法站立点（非当前位置、脚下有方块）。 */
    public static boolean tryRandomNearbyOnDamage(
            AnnihilatorEntity mob, AnnihilatorBlinkCooldown cooldown) {
        return resolveRandomNearby(mob)
                .map(dest -> tryBlinkTo(mob, dest, cooldown, false))
                .orElse(false);
    }

    public static boolean tryRandomNearby(AnnihilatorEntity mob, AnnihilatorBlinkCooldown cooldown) {
        return resolveRandomNearby(mob)
                .map(dest -> tryBlinkTo(mob, dest, cooldown))
                .orElse(false);
    }

    public static Optional<Vec3> resolveRandomNearby(AnnihilatorEntity mob) {
        if (!(mob.level() instanceof ServerLevel)) {
            return Optional.empty();
        }
        BlockPos currentFeet = mob.blockPosition();
        for (int attempt = 0; attempt < AnnihilatorEntity.DAMAGE_BLINK_RANDOM_ATTEMPTS; attempt++) {
            double offsetX =
                    (mob.getRandom().nextDouble() * 2.0D - 1.0D)
                            * AnnihilatorEntity.DAMAGE_BLINK_HALF_PLANAR;
            double offsetZ =
                    (mob.getRandom().nextDouble() * 2.0D - 1.0D)
                            * AnnihilatorEntity.DAMAGE_BLINK_HALF_PLANAR;
            double offsetY =
                    (mob.getRandom().nextDouble() * 2.0D - 1.0D)
                            * AnnihilatorEntity.DAMAGE_BLINK_HALF_VERTICAL;
            BlockPos start =
                    BlockPos.containing(
                            mob.getX() + offsetX, mob.getY() + offsetY, mob.getZ() + offsetZ);
            Optional<Vec3> dest = findStandNear(mob, start, currentFeet);
            if (dest.isPresent()) {
                return dest;
            }
        }
        return Optional.empty();
    }

    public static Optional<Vec3> resolveNearTarget(AnnihilatorEntity mob, LivingEntity target) {
        if (!(mob.level() instanceof ServerLevel)) {
            return Optional.empty();
        }
        Vec3 toTarget = target.position().subtract(mob.position());
        double planarLen = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        Vec3 behindDir;
        if (planarLen > 1.0E-4D) {
            behindDir = new Vec3(-toTarget.x / planarLen, 0.0D, -toTarget.z / planarLen);
        } else {
            double yaw = mob.getYRot() * (Math.PI / 180.0D);
            behindDir = new Vec3(-Mth.sin((float) yaw), 0.0D, Mth.cos((float) yaw));
        }

        double dist = AnnihilatorEntity.BLINK_NEAR_TARGET_DISTANCE;
        Optional<Vec3> dest =
                findStandNear(
                        mob,
                        BlockPos.containing(
                                target.getX() + behindDir.x * dist,
                                target.getY(),
                                target.getZ() + behindDir.z * dist));
        if (dest.isPresent()) {
            return dest;
        }

        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0D * i) / 8.0D;
            double ox = Math.cos(angle) * dist;
            double oz = Math.sin(angle) * dist;
            dest =
                    findStandNear(
                            mob, BlockPos.containing(target.getX() + ox, target.getY(), target.getZ() + oz));
            if (dest.isPresent()) {
                return dest;
            }
        }
        return Optional.empty();
    }

    private static Optional<Vec3> findStandNear(AnnihilatorEntity mob, BlockPos start) {
        return findStandNear(mob, start, null);
    }

    private static Optional<Vec3> findStandNear(
            AnnihilatorEntity mob, BlockPos start, @javax.annotation.Nullable BlockPos excludeFeet) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int i = 0; i <= STAND_SCAN_TICKS; i++) {
            Optional<Vec3> dest =
                    tryStandAtBlock(mob, level, cursor.getX(), cursor.getY(), cursor.getZ(), excludeFeet);
            if (dest.isPresent()) {
                return dest;
            }
            cursor.move(0, 1, 0);
        }
        cursor.set(start);
        for (int i = 1; i <= STAND_SCAN_TICKS; i++) {
            cursor.move(0, -1, 0);
            if (cursor.getY() < level.getMinBuildHeight()) {
                break;
            }
            Optional<Vec3> dest =
                    tryStandAtBlock(mob, level, cursor.getX(), cursor.getY(), cursor.getZ(), excludeFeet);
            if (dest.isPresent()) {
                return dest;
            }
        }
        return Optional.empty();
    }

    private static Optional<Vec3> tryStandAtBlock(
            AnnihilatorEntity mob, ServerLevel level, int x, int y, int z) {
        return tryStandAtBlock(mob, level, x, y, z, null);
    }

    private static Optional<Vec3> tryStandAtBlock(
            AnnihilatorEntity mob,
            ServerLevel level,
            int x,
            int y,
            int z,
            @javax.annotation.Nullable BlockPos excludeFeet) {
        double fx = x + 0.5D;
        double fy = y;
        double fz = z + 0.5D;
        if (!canStandAt(mob, level, fx, fy, fz, excludeFeet)) {
            return Optional.empty();
        }
        return Optional.of(new Vec3(fx, fy, fz));
    }

    private static boolean canStandAt(AnnihilatorEntity mob, ServerLevel level, double x, double y, double z) {
        return canStandAt(mob, level, x, y, z, null);
    }

    private static boolean canStandAt(
            AnnihilatorEntity mob,
            ServerLevel level,
            double x,
            double y,
            double z,
            @javax.annotation.Nullable BlockPos excludeFeet) {
        BlockPos feet = BlockPos.containing(x, y, z);
        if (excludeFeet != null && feet.equals(excludeFeet)) {
            return false;
        }
        Vec3 dest = new Vec3(x, y, z);
        AABB box = mob.getBoundingBox().move(dest.subtract(mob.position()));
        if (!level.noCollision(mob, box)) {
            return false;
        }
        if (level.containsAnyLiquid(box)) {
            return false;
        }
        if (!hasSolidFloorBelow(level, x, y, z)) {
            return false;
        }
        BlockPos head = feet.above();
        return isEmptyBlinkSpace(level, feet) && isEmptyBlinkSpace(level, head);
    }

    private static boolean canBlinkFromCurrentPosition(AnnihilatorEntity mob) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return false;
        }
        return hasSolidFloorBelow(level, mob.getX(), mob.getY(), mob.getZ());
    }

    private static boolean hasSolidFloorBelow(ServerLevel level, double x, double y, double z) {
        BlockState belowState = level.getBlockState(BlockPos.containing(x, y - 0.01D, z));
        return !belowState.isAir() && !isForbiddenBlinkState(belowState);
    }

    private static boolean isForbiddenBlinkState(BlockState state) {
        if (FORBIDDEN_BLINK_BLOCKS.contains(state.getBlock())) {
            return true;
        }
        return state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.LAVA);
    }

    private static boolean isEmptyBlinkSpace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() && !isForbiddenBlinkState(state);
    }

    private static boolean executeTeleport(AnnihilatorEntity mob, Vec3 dest) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return false;
        }
        Vec3 from = mob.position();
        playTeleportParticles(level, from);
        mob.teleportTo(dest.x, dest.y, dest.z);
        mob.resetFallDistance();
        mob.getNavigation().stop();
        playTeleportParticles(level, dest);
        level.playSound(
                null,
                dest.x,
                dest.y,
                dest.z,
                SoundEvents.ENDERMAN_TELEPORT,
                mob.getSoundSource(),
                1.0F,
                1.0F);
        return true;
    }

    private static void playTeleportParticles(ServerLevel level, Vec3 pos) {
        level.sendParticles(
                ParticleTypes.PORTAL,
                pos.x,
                pos.y + 1.0D,
                pos.z,
                32,
                0.25D,
                0.5D,
                0.25D,
                0.0D);
    }
}
