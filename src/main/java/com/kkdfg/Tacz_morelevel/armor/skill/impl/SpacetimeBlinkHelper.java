package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 时空闪现共用：视线落点合规、随机落点、站立判定、传送执行。
 * 供 {@link SpacetimeBlinkSkill} 与 {@link com.kkdfg.Tacz_morelevel.item.custom.SpacetimeStoneItem} 使用。
 */
public final class SpacetimeBlinkHelper {

    public static final double TARGET_RAY_MAX_RANGE = 128.0D;
    public static final int RANDOM_MIN_DISTANCE = 30;
    public static final int RANDOM_MAX_DISTANCE = 60;
    public static final int RANDOM_ATTEMPTS = 16;
    public static final int STAND_SCAN_UP = 16;

    private static final Set<Block> FORBIDDEN_BLINK_BLOCKS = Set.of(Blocks.WATER, Blocks.LAVA);

    private SpacetimeBlinkHelper() {}

    /** 站立视线：射线首个方块正上方两格须为空，且可站立。 */
    public static Optional<Vec3> resolvePlayerLookBlink(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(TARGET_RAY_MAX_RANGE));
        BlockHitResult blockHit =
                level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        if (blockHit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }

        BlockPos hitBlock = blockHit.getBlockPos();
        if (isForbiddenBlinkBlock(level, hitBlock)) {
            return Optional.empty();
        }

        BlockPos feetPos = hitBlock.above();
        BlockPos headPos = feetPos.above();
        if (!isEmptyBlinkSpace(level, feetPos) || !isEmptyBlinkSpace(level, headPos)) {
            return Optional.empty();
        }

        double x = feetPos.getX() + 0.5D;
        double y = feetPos.getY();
        double z = feetPos.getZ() + 0.5D;
        if (!canStandAt(level, player, x, y, z)) {
            return Optional.empty();
        }
        return Optional.of(new Vec3(x, y, z));
    }

    public static Optional<Vec3> resolveRandomBlink(ServerLevel level, Player player) {
        for (int attempt = 0; attempt < RANDOM_ATTEMPTS; attempt++) {
            int distance =
                    RANDOM_MIN_DISTANCE
                            + player.getRandom().nextInt(RANDOM_MAX_DISTANCE - RANDOM_MIN_DISTANCE + 1);
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
            int y = Mth.floor(player.getY());
            Optional<Vec3> dest = findStandPosition(level, player, new BlockPos(x, y, z));
            if (dest.isPresent()) {
                return dest;
            }
            dest = findStandPosition(level, player, new BlockPos(x, y + 8, z));
            if (dest.isPresent()) {
                return dest;
            }
        }
        return Optional.empty();
    }

    /** 当前位置是否适合作为存档点（与落点站立规则一致）。 */
    public static boolean canRecordAt(ServerLevel level, Player player) {
        return canStandAt(level, player, player.getX(), player.getY(), player.getZ());
    }

    /** 存档坐标是否仍可站立落地。 */
    public static boolean canStandAtSaved(ServerLevel level, Player player, double x, double y, double z) {
        BlockPos feet = BlockPos.containing(x, y, z);
        BlockPos head = feet.above();
        if (!isEmptyBlinkSpace(level, feet) || !isEmptyBlinkSpace(level, head)) {
            // 玩家碰撞箱可能与格心不完全对齐：仍用 canStandAt 兜底
            return canStandAt(level, player, x, y, z);
        }
        return canStandAt(level, player, x, y, z);
    }

    public static Optional<Vec3> findStandPosition(ServerLevel level, Player player, BlockPos start) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int i = 0; i < STAND_SCAN_UP; i++) {
            double x = cursor.getX() + 0.5D;
            double y = cursor.getY();
            double z = cursor.getZ() + 0.5D;
            if (canStandAt(level, player, x, y, z)) {
                return Optional.of(new Vec3(x, y, z));
            }
            cursor.move(0, 1, 0);
        }
        cursor.set(start);
        for (int i = 0; i < STAND_SCAN_UP; i++) {
            cursor.move(0, -1, 0);
            if (cursor.getY() < level.getMinBuildHeight()) {
                break;
            }
            double x = cursor.getX() + 0.5D;
            double y = cursor.getY();
            double z = cursor.getZ() + 0.5D;
            if (canStandAt(level, player, x, y, z)) {
                return Optional.of(new Vec3(x, y, z));
            }
        }
        return Optional.empty();
    }

    public static boolean canStandAt(ServerLevel level, Player player, double x, double y, double z) {
        Vec3 dest = new Vec3(x, y, z);
        Vec3 delta = dest.subtract(player.position());
        AABB box = player.getBoundingBox().move(delta);
        if (!level.noCollision(player, box)) {
            return false;
        }
        if (level.containsAnyLiquid(box)) {
            return false;
        }
        BlockPos below = BlockPos.containing(x, y - 0.01D, z);
        return !level.getBlockState(below).isAir();
    }

    public static boolean isForbiddenBlinkBlock(ServerLevel level, BlockPos pos) {
        return isForbiddenBlinkState(level.getBlockState(pos));
    }

    public static boolean isForbiddenBlinkState(BlockState state) {
        if (FORBIDDEN_BLINK_BLOCKS.contains(state.getBlock())) {
            return true;
        }
        return state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.LAVA);
    }

    public static boolean isEmptyBlinkSpace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() && !isForbiddenBlinkState(state);
    }

    /** 同维或跨维传送到存档点（不套用视线 128 格限制）。 */
    public static boolean teleportPlayer(Player player, ServerLevel targetLevel, Vec3 dest) {
        Vec3 from = player.position();
        ServerLevel fromLevel = (ServerLevel) player.level();
        playTeleportParticles(fromLevel, from);

        if (player instanceof ServerPlayer serverPlayer) {
            if (targetLevel == player.level()) {
                serverPlayer.teleportTo(dest.x, dest.y, dest.z);
            } else {
                serverPlayer.teleportTo(
                        targetLevel, dest.x, dest.y, dest.z, serverPlayer.getYRot(), serverPlayer.getXRot());
            }
        } else {
            player.teleportTo(dest.x, dest.y, dest.z);
        }
        player.resetFallDistance();
        playTeleportParticles(targetLevel, dest);
        targetLevel.playSound(
                null,
                dest.x,
                dest.y,
                dest.z,
                SoundEvents.ENDERMAN_TELEPORT,
                player.getSoundSource(),
                1.0F,
                1.0F);
        return true;
    }

    /** 同维短距闪现（时空之石站立/蹲下随机）。 */
    public static boolean blinkInSameDimension(ServerLevel level, Player player, Vec3 dest) {
        if (player.position().distanceTo(dest) > TARGET_RAY_MAX_RANGE) {
            return false;
        }
        Vec3 from = player.position();
        playTeleportParticles(level, from);
        player.teleportTo(dest.x, dest.y, dest.z);
        player.resetFallDistance();
        playTeleportParticles(level, dest);
        level.playSound(
                null,
                dest.x,
                dest.y,
                dest.z,
                SoundEvents.ENDERMAN_TELEPORT,
                player.getSoundSource(),
                1.0F,
                1.0F);
        return true;
    }

    public static void playTeleportParticles(ServerLevel level, Vec3 pos) {
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
