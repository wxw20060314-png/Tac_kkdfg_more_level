package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.entity.ai.SoldierGunPassthroughSight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 战争机器双档视线：一次射线同时得出严格看见（加特林）与 RPG 看见（≤ {@link #RPG_MAX_SOLID_BLOCKS} 个实心挡）。
 */
public final class WarMachineGunSight {

    public static final int RPG_MAX_SOLID_BLOCKS = 30;

    private WarMachineGunSight() {}

    public record Result(boolean strictLineOfSight, boolean rpgLineOfSight, int solidBlockCount) {}

    public static Result evaluate(LivingEntity viewer, LivingEntity target) {
        if (viewer == null || target == null || !target.isAlive() || viewer.level() != target.level()) {
            return new Result(false, false, 0);
        }
        return trace(
                viewer.level(),
                viewer.getEyePosition(1.0F),
                target.getEyePosition(1.0F));
    }

    public static boolean hasStrictLineOfSight(LivingEntity viewer, LivingEntity target) {
        return evaluate(viewer, target).strictLineOfSight();
    }

    public static boolean hasRpgLineOfSight(LivingEntity viewer, LivingEntity target) {
        return evaluate(viewer, target).rpgLineOfSight();
    }

    private static Result trace(Level level, Vec3 start, Vec3 end) {
        if (start.distanceToSqr(end) < 1.0e-8D) {
            return new Result(true, true, 0);
        }
        int[] solidCount = {0};
        BlockHitResult hit =
                BlockGetter.traverseBlocks(
                        start,
                        end,
                        level,
                        (lvl, pos) -> {
                            BlockState state = lvl.getBlockState(pos);
                            if (SoldierGunPassthroughSight.isPassthroughForGunSight(state, lvl, pos)) {
                                return null;
                            }
                            BlockHitResult shapeHit =
                                    lvl.clipWithInteractionOverride(
                                            start, end, pos, state.getCollisionShape(lvl, pos), state);
                            if (shapeHit == null) {
                                return null;
                            }
                            solidCount[0]++;
                            if (solidCount[0] > RPG_MAX_SOLID_BLOCKS) {
                                return shapeHit;
                            }
                            return null;
                        },
                        ignored -> null);
        int solids = solidCount[0];
        if (hit != null) {
            return new Result(false, false, solids);
        }
        return new Result(solids == 0, solids <= RPG_MAX_SOLID_BLOCKS, solids);
    }
}
