package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierWalkNodeEvaluator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 掩体格打分（与 {@link StrafeCellScore} 分离，公式对齐但不复用）。
 * <p><b>类型</b>（好 / 遮掩 / 中性 / 坏）：视线与 {@link StrafeCellScore} 同式；{@code crawl}
 * 由调用方按节点 {@link SoldierWalkNodeEvaluator#needsCrawlPoseAt} 传入（与 CoverStrafe 对齐）。
 * 窗外不做射线，mySee=enemySee=0；整表后 {@link #degradeConcealedWithoutPeek} 用表内八邻窥视退化遮掩。
 * <p><b>掩护度</b>{@code seal}：与类型无关，按 <strong>当前碰撞箱</strong>
 * {@code floor(bb+1)} 整格体积扫挡枪比例（脚底层除外），见 {@link #coverSealFraction}。
 * <p>同档再比乘积（越小越好）：{@code (1+distRatio)×(1+(1-seal))×(1+nodeCost)}；
 * {@code f=(1+mySee)/(1+enemySee)} 仅同档细比（越大越好）。
 */
public final class CoverCellScore {

    /** 找点选用：有效档低于此值丢弃（坏=0）。 */
    public static final int MIN_SELECT_TIER = 1;

    private static final float CRAWL_EYE_HEIGHT = 0.4F;
    private static final double FOOT_Y = 0.1D;
    private static final double HEAD_SCORE = 9.0D;
    private static final double BODY_SCORE = 4.0D;
    private static final double FEET_SCORE = 1.0D;

    private CoverCellScore() {}

    /**
     * @param crawl 该格是否按趴姿态评（对方只测头）；应对齐 Strafe 的 needsCrawlPoseAt
     * @param meleeRadius 期望交战距离；≤0 时距离项为 0
     * @param nodeCost 寻路节点代价（如 {@code Node#costMalus}）
     */
    @Nullable
    public static Result score(
            Mob mob,
            LivingEntity target,
            int x,
            int y,
            int z,
            boolean crawl,
            float meleeRadius,
            float nodeCost) {
        if (mob == null || target == null || !target.isAlive() || mob.level() != target.level()) {
            return null;
        }
        BlockPos origin = mob.blockPosition();
        boolean inWindow = SoldierWalkNodeEvaluator.isInChebyshevWindow(
                x, z, origin.getX(), origin.getZ(), SoldierWalkNodeEvaluator.REACH_WINDOW_7X7_CHEBYSHEV);

        // —— 类型：视线（窗外不扫，双 0）；crawl 同 Strafe ——
        double mySee;
        double enemySee;
        double eye = y + (crawl ? CRAWL_EYE_HEIGHT : mob.getEyeHeight(Pose.STANDING));
        Vec3 feet = new Vec3(x + 0.5D, y + FOOT_Y, z + 0.5D);
        if (!inWindow) {
            mySee = 0.0D;
            enemySee = 0.0D;
        } else {
            Vec3 head = new Vec3(x + 0.5D, eye, z + 0.5D);
            Vec3 body = new Vec3(x + 0.5D, y + bodyHeight(mob, crawl) * 0.5D, z + 0.5D);
            mySee = seeScore(mob, head, targetHead(target), targetBody(target), targetFeet(target), false);
            enemySee = seeScore(target, target.getEyePosition(1.0F), head, body, feet, crawl);
        }

        // —— 掩护度：当前碰撞箱整格，与类型 / 窗无关 ——
        double seal = coverSealFraction(mob, x, y, z);

        double dist = feet.distanceTo(target.position());
        double distRatio = meleeRadius > 0.0F ? Math.abs(dist - meleeRadius) / meleeRadius : 0.0D;
        double product =
                (1.0D + distRatio)
                        * (1.0D + (1.0D - seal))
                        * (1.0D + Math.max(0.0F, nodeCost));
        return new Result(mySee, enemySee, seal, distRatio, product, inWindow, false);
    }

    /**
     * 掩护度：以脚格为中心，按 mob <strong>当前</strong> BB 的
     * {@code floor(bb+1)} 整格（与 NodeEvaluator 同式）扫挡枪比例；不扫脚底层 {@code footY}。
     */
    public static double coverSealFraction(Mob mob, int footX, int footY, int footZ) {
        if (mob == null || mob.level() == null) {
            return 0.0D;
        }
        Level level = mob.level();
        int cellW = Math.max(1, Mth.floor(mob.getBbWidth() + 1.0F));
        int cellH = Math.max(1, Mth.floor(mob.getBbHeight() + 1.0F));
        int cellD = cellW;
        int total = cellW * cellH * cellD;
        if (total <= 0) {
            return 0.0D;
        }
        int originDx = -(cellW - 1) / 2;
        int originDz = -(cellD - 1) / 2;
        int blocked = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int iy = 1; iy <= cellH; iy++) {
            for (int ix = 0; ix < cellW; ix++) {
                for (int iz = 0; iz < cellD; iz++) {
                    pos.set(footX + originDx + ix, footY + iy, footZ + originDz + iz);
                    BlockState state = level.getBlockState(pos);
                    if (!SoldierGunPassthroughSight.isPassthroughForGunSight(state, level, pos)) {
                        blocked++;
                    }
                }
            }
        }
        return blocked / (double) total;
    }

    /**
     * 就地处理已打分表：遮掩点若同 y 八邻均无窥视（表内且 {@code mySee>0}），退化成坏。
     * 未入表邻格视为看不见。只读邻格已有分，不补射线。与掩护度无关。
     *
     * @param scoredByPos key = {@link BlockPos#asLong(int, int, int)}
     */
    public static void degradeConcealedWithoutPeek(Map<Long, Result> scoredByPos) {
        if (scoredByPos == null || scoredByPos.isEmpty()) {
            return;
        }
        List<Long> toDegrade = new ArrayList<>();
        for (Map.Entry<Long, Result> entry : scoredByPos.entrySet()) {
            Result result = entry.getValue();
            if (result == null || !result.isRawConcealed() || result.concealedDegraded()) {
                continue;
            }
            if (!hasPeekNeighbor(entry.getKey(), scoredByPos)) {
                toDegrade.add(entry.getKey());
            }
        }
        for (Long key : toDegrade) {
            Result result = scoredByPos.get(key);
            if (result != null) {
                scoredByPos.put(key, result.degradeConcealed());
            }
        }
    }

    /** 同 y 八邻是否有表内且能看见目标的格。 */
    private static boolean hasPeekNeighbor(long selfKey, Map<Long, Result> scoredByPos) {
        int x = BlockPos.getX(selfKey);
        int y = BlockPos.getY(selfKey);
        int z = BlockPos.getZ(selfKey);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                Result neighbor = scoredByPos.get(BlockPos.asLong(x + dx, y, z + dz));
                if (neighbor != null && neighbor.mySee() > 0.0D) {
                    return true;
                }
            }
        }
        return false;
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
        int product = Double.compare(b.product(), a.product());
        if (product != 0) {
            return product;
        }
        return Double.compare(a.f(), b.f());
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

    /**
     * @param seal 掩护度（当前 BB 整格），与类型无关
     * @param product 距离×掩护度×节点代价乘积，越小越好
     * @param inWindow 是否在 mob 的 7×7 内（窗外类型为双 0）
     * @param concealedDegraded 遮掩无八邻窥视后标真，{@link #tierRank()} 当 0
     */
    public record Result(
            double mySee,
            double enemySee,
            double seal,
            double distRatio,
            double product,
            boolean inWindow,
            boolean concealedDegraded) {

        /** 与 Strafe 同式：{@code (1+mySee)/(1+enemySee)}。 */
        public double f() {
            return (1.0D + this.mySee) / (1.0D + this.enemySee);
        }

        /** 原始 2×2 是否为遮掩（未计退化）。 */
        public boolean isRawConcealed() {
            return this.mySee <= 0.0D && this.enemySee <= 0.0D;
        }

        public Result degradeConcealed() {
            return new Result(
                    this.mySee,
                    this.enemySee,
                    this.seal,
                    this.distRatio,
                    this.product,
                    this.inWindow,
                    true);
        }

        /** 好 3，遮掩 2，中性 1，坏 0；遮掩无窥视退化后为 0。只看视线类型，不看 seal。 */
        public int tierRank() {
            boolean iSee = this.mySee > 0.0D;
            boolean theySee = this.enemySee > 0.0D;
            if (iSee && !theySee) {
                return 3;
            }
            if (!iSee && !theySee) {
                return this.concealedDegraded ? 0 : 2;
            }
            if (iSee) {
                return 1;
            }
            return 0;
        }
    }
}
