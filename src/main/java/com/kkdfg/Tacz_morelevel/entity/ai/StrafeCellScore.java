package com.kkdfg.Tacz_morelevel.entity.ai;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 横移格打分。只看看见和遮掩，越大越好。
 * 横移一步走不走得到由调用方在下一层丢掉。
 *
 * <p>档位由两个分数是否为 0 决定，先比档，再比 {@code f}，再比遮掩度：
 * 好点（自己看见、对方看不见）优于遮掩点（双 0），略优于中性点（都看见），优于坏点（只被看见）。
 * {@code f = (1 + mySee) / (1 + enemySee)}，双方都是 0 时为 1。
 */
public final class StrafeCellScore {

    private static final float CRAWL_EYE_HEIGHT = 0.4F;
    private static final double FOOT_Y = 0.1D;
    private static final double HEAD_SCORE = 9.0D;
    private static final double BODY_SCORE = 4.0D;
    private static final double FEET_SCORE = 1.0D;

    private StrafeCellScore() {}

    /**
     * @param crawl 该格是否只能趴。为真时对方只检测这一格的头
     */
    @Nullable
    public static Result score(Mob mob, LivingEntity target, int x, int y, int z, boolean crawl) {
        if (mob == null || target == null || !target.isAlive() || mob.level() != target.level()) {
            return null;
        }
        double eye = y + (crawl ? CRAWL_EYE_HEIGHT : mob.getEyeHeight(Pose.STANDING));
        Vec3 head = new Vec3(x + 0.5D, eye, z + 0.5D);
        Vec3 body = new Vec3(x + 0.5D, y + bodyHeight(mob, crawl) * 0.5D, z + 0.5D);
        Vec3 feet = new Vec3(x + 0.5D, y + FOOT_Y, z + 0.5D);

        double mySee = seeScore(mob, head, targetHead(target), targetBody(target), targetFeet(target), false);
        double enemySee = seeScore(target, target.getEyePosition(1.0F), head, body, feet, crawl);
        double seal = sealFraction(mob, x, Mth.floor(eye), z);
        return new Result(mySee, enemySee, seal);
    }

    /** 正数表示 {@code a} 更好。空结果最差。 */
    public static int compare(@Nullable Result a, @Nullable Result b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        int tier = Integer.compare(a.tierRank(), b.tierRank());
        if (tier != 0) {
            return tier;
        }
        int sight = Double.compare(a.f(), b.f());
        if (sight != 0) {
            return sight;
        }
        return Double.compare(a.seal(), b.seal());
    }

    private static double seeScore(
            LivingEntity viewer, Vec3 from, Vec3 head, Vec3 body, Vec3 feet, boolean crawlOnly) {
        double score = 0.0D;
        if (SoldierGunPassthroughSight.hasPassthroughLine(viewer.level(), viewer, from, head)) {
            score += HEAD_SCORE;
        }
        if (crawlOnly) {
            return score;
        }
        if (SoldierGunPassthroughSight.hasPassthroughLine(viewer.level(), viewer, from, body)) {
            score += BODY_SCORE;
        }
        if (SoldierGunPassthroughSight.hasPassthroughLine(viewer.level(), viewer, from, feet)) {
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

    /** 一格的看见与遮掩。{@link #f()} 高更好，{@link #seal()} 高更好。 */
    public record Result(double mySee, double enemySee, double seal) {

        public double f() {
            return (1.0D + this.mySee) / (1.0D + this.enemySee);
        }

        /** 好 3，遮掩 2，中性 1，坏 0。 */
        public int tierRank() {
            boolean iSee = this.mySee > 0.0D;
            boolean theySee = this.enemySee > 0.0D;
            if (iSee && !theySee) {
                return 3;
            }
            if (!iSee && !theySee) {
                return 2;
            }
            if (iSee) {
                return 1;
            }
            return 0;
        }
    }
}
