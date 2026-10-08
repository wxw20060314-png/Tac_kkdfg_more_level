package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.entity.ai.SoldierGunPassthroughSight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 战争机器双档视线：一次射线同时得出严格看见（加特林）与 RPG 看见（≤ {@link #RPG_MAX_SOLID_BLOCKS} 个实心挡）。
 */
public final class WarMachineGunSight {

    public static final int RPG_MAX_SOLID_BLOCKS = 30;

    private static final int MAX_CLIP_ITERATIONS = 96;
    private static final double STEP_EPS = 0.0625D;

    private WarMachineGunSight() {}

    public record Result(boolean strictLineOfSight, boolean rpgLineOfSight, int solidBlockCount) {}

    public static Result evaluate(LivingEntity viewer, LivingEntity target) {
        if (viewer == null || target == null || !target.isAlive() || viewer.level() != target.level()) {
            return new Result(false, false, 0);
        }
        return trace(
                viewer.level(),
                viewer,
                viewer.getEyePosition(1.0F),
                target.getEyePosition(1.0F));
    }

    public static boolean hasStrictLineOfSight(LivingEntity viewer, LivingEntity target) {
        return evaluate(viewer, target).strictLineOfSight();
    }

    public static boolean hasRpgLineOfSight(LivingEntity viewer, LivingEntity target) {
        return evaluate(viewer, target).rpgLineOfSight();
    }

    private static Result trace(Level level, LivingEntity living, Vec3 start, Vec3 end) {
        Vec3 toEnd = end.subtract(start);
        double maxLen = toEnd.length();
        if (maxLen < 1.0e-4D) {
            return new Result(true, true, 0);
        }
        Vec3 dir = toEnd.scale(1.0D / maxLen);
        Vec3 cursor = start;
        int solidCount = 0;
        for (int i = 0; i < MAX_CLIP_ITERATIONS; i++) {
            BlockHitResult hit =
                    level.clip(
                            new ClipContext(
                                    cursor, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, living));
            if (hit.getType() == HitResult.Type.MISS) {
                return new Result(solidCount == 0, solidCount <= RPG_MAX_SOLID_BLOCKS, solidCount);
            }
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (SoldierGunPassthroughSight.isPassthroughForGunSight(state, level, pos)) {
                Vec3 at = hit.getLocation();
                cursor = at.add(dir.scale(STEP_EPS));
            } else {
                solidCount++;
                if (solidCount > RPG_MAX_SOLID_BLOCKS) {
                    return new Result(false, false, solidCount);
                }
                Vec3 at = hit.getLocation();
                cursor = at.add(dir.scale(STEP_EPS));
            }
            if (cursor.distanceToSqr(end) < STEP_EPS * STEP_EPS * 4) {
                return new Result(solidCount == 0, solidCount <= RPG_MAX_SOLID_BLOCKS, solidCount);
            }
            if (cursor.distanceToSqr(start) >= end.distanceToSqr(start) - 1.0e-6D) {
                return new Result(solidCount == 0, solidCount <= RPG_MAX_SOLID_BLOCKS, solidCount);
            }
        }
        return new Result(false, false, solidCount);
    }
}
