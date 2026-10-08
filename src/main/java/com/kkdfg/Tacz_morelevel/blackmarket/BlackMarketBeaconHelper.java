package com.kkdfg.Tacz_morelevel.blackmarket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 在黑市联络方块周围 3×3 水平范围内检测已激活信标的最高等级。
 *
 * <p>原版信标为 0–4 级，映射为黑市 1–5 级：无信标→1，信标 N 级→黑市 N+1（上限 5）。
 */
public final class BlackMarketBeaconHelper {

    private BlackMarketBeaconHelper() {}

    /** 信标金字塔等级（0=无/未激活，1–4=原版满级）→ 黑市货架等级（1–5）。 */
    public static int beaconLevelToMarketLevel(int beaconLevel) {
        return Math.min(Math.max(beaconLevel, 0) + 1, BlackMarketDataManager.MAX_MARKET_LEVEL);
    }

    public static int scanMaxActivatedBeaconLevel(Level level, BlockPos center) {
        int max = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                mutable.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                max = Math.max(max, activatedBeaconLevelAt(level, mutable));
            }
        }
        return max;
    }

    private static int activatedBeaconLevelAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BeaconBlockEntity beacon)) {
            return 0;
        }
        if (beacon.getBeamSections().isEmpty()) {
            return 0;
        }
        CompoundTag updateTag = beacon.getUpdateTag();
        int levels = updateTag.getInt("Levels");
        return levels > 0 ? levels : 0;
    }
}
