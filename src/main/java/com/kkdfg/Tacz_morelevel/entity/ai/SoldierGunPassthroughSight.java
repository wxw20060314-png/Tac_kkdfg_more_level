package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 枪械 NPC 用「弹道向」视线（改写原版 Sensing）：沿起点到终点只扫一遍方块，
 * 穿过与 TaCZ 子弹一致的方块（{@link ModBlocks#BULLET_IGNORE_BLOCKS}、配置
 * {@link AmmoConfig#PASS_THROUGH_BLOCKS}，以及 {@link AmmoConfig#DESTROY_GLASS} 开启时
 * 与 TaCZ {@code DestroyGlassBlock} 相同的玻璃/玻璃板），其余视为遮挡。
 *
 * <p>{@link #hasLineOfSight} 固定眼→眼，供索敌等使用。交火瞄点（头→身→脚）见
 * {@link MobGunPlayerLikeAimHelper#findAimPoint}，射线复用 {@link #hasPassthroughLine}。
 */
public final class SoldierGunPassthroughSight {

    private SoldierGunPassthroughSight() {}

    /** 眼→眼；索敌等「严格看见」用。 */
    public static boolean hasLineOfSight(Entity viewer, Entity target) {
        if (!(viewer instanceof LivingEntity living) || target == null || !target.isAlive()) {
            return false;
        }
        return hasPassthroughLine(
                living.level(),
                living,
                living.getEyePosition(1.0F),
                target.getEyePosition(1.0F));
    }

    /** 脚底略抬高，用于战斗趴下的「脚→脚」穿透视线。 */
    public static boolean hasLineOfSightFromFoot(Entity viewer, Entity target) {
        if (!(viewer instanceof LivingEntity living) || target == null || !target.isAlive()) {
            return false;
        }
        if (living.level() != target.level()) {
            return false;
        }
        Vec3 start =
                new Vec3(living.getX(), living.getBoundingBox().minY + 0.1D, living.getZ());
        Vec3 end = new Vec3(target.getX(), target.getBoundingBox().minY + 0.1D, target.getZ());
        return hasPassthroughLine(living.level(), living, start, end);
    }

    /**
     * 从射击者眼睛到 {@code end} 的穿透射线是否通畅（供瞄准选点复用）。
     */
    public static boolean hasPassthroughLineFromEye(LivingEntity living, Vec3 end) {
        if (living == null || end == null) {
            return false;
        }
        return hasPassthroughLine(living.level(), living, living.getEyePosition(1.0F), end);
    }

    public static boolean hasPassthroughLine(Level level, LivingEntity living, Vec3 start, Vec3 end) {
        if (level != living.level()) {
            return false;
        }
        if (start.distanceToSqr(end) < 1.0e-8D) {
            return true;
        }
        BlockHitResult hit =
                BlockGetter.traverseBlocks(
                        start,
                        end,
                        level,
                        (lvl, pos) -> {
                            BlockState state = lvl.getBlockState(pos);
                            if (isPassthroughForGunSight(state, lvl, pos)) {
                                return null;
                            }
                            return lvl.clipWithInteractionOverride(
                                    start, end, pos, state.getCollisionShape(lvl, pos), state);
                        },
                        ignored -> null);
        return hit == null;
    }

    /**
     * 与 TaCZ 弹道遮挡一致：{@code tacz:bullet_ignore}（默认铁栏杆、栅栏、栅栏门、树叶）、
     * 配置 {@code PassThroughBlocks}；当 {@code DestroyGlass} 开启时，玻璃/玻璃板与 TaCZ
     * 可打碎范围一致，视线可穿过（墙、冰等仍遮挡）。
     */
    public static boolean isPassthroughForGunSight(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.isAir()) {
            return true;
        }
        if (state.is(ModBlocks.BULLET_IGNORE_BLOCKS)) {
            return true;
        }
        if (isDestroyableGlassForGunSight(state)) {
            return true;
        }
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return blockId != null && AmmoConfig.PASS_THROUGH_BLOCKS.get().contains(blockId.toString());
    }

    /**
     * 与 TaCZ {@code DestroyGlassBlock} 同一批方块；仅当 {@link AmmoConfig#DESTROY_GLASS} 为 true。
     */
    private static boolean isDestroyableGlassForGunSight(BlockState state) {
        if (!AmmoConfig.DESTROY_GLASS.get()) {
            return false;
        }
        Block block = state.getBlock();
        if (block instanceof AbstractGlassBlock || block instanceof StainedGlassPaneBlock) {
            return true;
        }
        // 原版玻璃板：IronBarsBlock + instrument HAT（与 TaCZ DestroyGlassBlock 一致）
        return block instanceof IronBarsBlock && state.instrument() == NoteBlockInstrument.HAT;
    }
}
