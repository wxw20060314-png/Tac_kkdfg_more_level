package com.kkdfg.Tacz_morelevel.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 掩体格打分。{@link #partial} 只算看见、距离、密封。
 * {@link #applyCost} 再乘上节点代价。返回值越小越好。
 */
public final class CoverCellScore {

    private static final float CRAWL_EYE_HEIGHT = 0.4F;
    private static final double FOOT_Y = 0.1D;
    private static final double HEAD_SCORE = 9.0D;
    private static final double BODY_SCORE = 4.0D;
    private static final double FEET_SCORE = 1.0D;
    private static final String MOD_PACKAGE = "com.kkdfg.Tacz_morelevel.";

    private CoverCellScore() {}

    /** (1+f) × (1+distRatio) × (1+(1-seal))。 */
    public static double partial(
            Mob mob,
            LivingEntity target,
            int x,
            int y,
            int z,
            boolean crawl,
            float meleeRadius) {
        if (mob == null || target == null || !target.isAlive() || mob.level() != target.level()) {
            return Double.POSITIVE_INFINITY;
        }
        double eye = y + (crawl ? CRAWL_EYE_HEIGHT : mob.getEyeHeight(Pose.STANDING));
        Vec3 head = new Vec3(x + 0.5D, eye, z + 0.5D);
        Vec3 body = new Vec3(x + 0.5D, y + bodyHeight(mob, crawl) * 0.5D, z + 0.5D);
        Vec3 feet = new Vec3(x + 0.5D, y + FOOT_Y, z + 0.5D);

        double mySee = seeScore(mob, head, targetHead(target), targetBody(target), targetFeet(target), false);
        double enemySee = modNpc(target)
                ? seeScore(mob, target.getEyePosition(1.0F), head, body, feet, crawl)
                : FEET_SCORE;

        double f = enemySee / (mySee + 1.0D);
        double dist = feet.distanceTo(target.position());
        double distRatio = meleeRadius > 0.0F ? Math.abs(dist - meleeRadius) / meleeRadius : 0.0D;
        double seal = sealFraction(mob, x, Mth.floor(eye), z);
        return (1.0D + f) * (1.0D + distRatio) * (1.0D + (1.0D - seal));
    }

    /** 接上路线后，第二个参数改为该格节点的代价。 */
    public static double applyCost(double partial, float nodeCost) {
        if (partial == Double.POSITIVE_INFINITY) {
            return partial;
        }
        return (1.0D + Math.max(0.0F, nodeCost)) * partial;
    }

    private static double seeScore(Mob mob, Vec3 from, Vec3 head, Vec3 body, Vec3 feet, boolean crawlOnly) {
        double score = 0.0D;
        if (SoldierGunPassthroughSight.hasPassthroughLine(mob.level(), mob, from, head)) {
            score += HEAD_SCORE;
        }
        if (crawlOnly) {
            return score;
        }
        if (SoldierGunPassthroughSight.hasPassthroughLine(mob.level(), mob, from, body)) {
            score += BODY_SCORE;
        }
        if (SoldierGunPassthroughSight.hasPassthroughLine(mob.level(), mob, from, feet)) {
            score += FEET_SCORE;
        }
        return score;
    }

    private static double sealFraction(Mob mob, int x, int headY, int z) {
        int blocked = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                pos.set(x + dx, headY, z + dz);
                BlockState state = mob.level().getBlockState(pos);
                if (!SoldierGunPassthroughSight.isPassthroughForGunSight(state, mob.level(), pos)) {
                    blocked++;
                }
            }
        }
        return blocked / 9.0D;
    }

    private static boolean modNpc(LivingEntity entity) {
        return entity.getClass().getName().startsWith(MOD_PACKAGE);
    }

    private static float bodyHeight(Mob mob, boolean crawl) {
        if (crawl) {
            return Player.SWIMMING_BB_HEIGHT;
        }
        return mob.getDimensions(Pose.STANDING).height;
    }

    private static Vec3 targetHead(LivingEntity target) {
        return target.getEyePosition(1.0F);
    }

    private static Vec3 targetBody(LivingEntity target) {
        return target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
    }

    private static Vec3 targetFeet(LivingEntity target) {
        return new Vec3(target.getX(), target.getBoundingBox().minY + FOOT_Y, target.getZ());
    }
}
