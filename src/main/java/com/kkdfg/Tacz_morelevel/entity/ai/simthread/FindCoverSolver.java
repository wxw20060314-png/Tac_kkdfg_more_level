package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import com.kkdfg.Tacz_morelevel.entity.ai.CoverCellScore;
import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierWalkNodeEvaluator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;

/**
 * {@link SimThreadEventType#FIND_COVER}：7×7 可达表（与导航同趴开关）打分 → 遮掩无窥视退化 →
 * 只选档 {@link CoverCellScore#MIN_SELECT_TIER} 及以上最优，打包 {@link FindCoverInfo}。
 * 每格 {@code crawl} 对齐 Strafe：{@link SoldierWalkNodeEvaluator#needsCrawlPoseAt}。
 * 失败返回 empty（UDP，不通知）。
 */
public final class FindCoverSolver implements SimThreadSolver {

    /** 无 FOLLOW_RANGE 时的期望交战距离兜底。 */
    private static final float DEFAULT_MELEE_RADIUS = 12.0F;

    @Override
    public boolean supports(SimThreadEventType type) {
        return type == SimThreadEventType.FIND_COVER;
    }

    @Override
    public Optional<SimThreadInfo> solve(ServerLevel level, UUID uuid, SimThreadEventType type) {
        if (type != SimThreadEventType.FIND_COVER) {
            return Optional.empty();
        }
        Entity entity = level.getEntity(uuid);
        if (!(entity instanceof Mob mob) || !mob.isAlive()) {
            return Optional.empty();
        }
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || target.level() != mob.level()) {
            return Optional.empty();
        }

        List<Node> nodes = SoldierWalkNodeEvaluator.expandReachable7x7(mob);
        if (nodes.isEmpty()) {
            return Optional.empty();
        }

        float meleeRadius = preferredMeleeRadius(mob);
        SoldierWalkNodeEvaluator poseEval = poseEvaluator(mob);
        Map<Long, CoverCellScore.Result> scored = new HashMap<>();
        Map<Long, Node> nodeByKey = new HashMap<>();
        Map<Long, Boolean> crawlByKey = new HashMap<>();

        for (Node node : nodes) {
            if (node == null || node.costMalus < 0.0F) {
                continue;
            }
            boolean crawl = poseEval != null && poseEval.needsCrawlPoseAt(mob, node.x, node.y, node.z);
            CoverCellScore.Result score = CoverCellScore.score(
                    mob, target, node.x, node.y, node.z, crawl, meleeRadius, node.costMalus);
            if (score == null) {
                continue;
            }
            long key = BlockPos.asLong(node.x, node.y, node.z);
            scored.put(key, score);
            nodeByKey.put(key, node);
            crawlByKey.put(key, crawl);
        }

        CoverCellScore.degradeConcealedWithoutPeek(scored);

        CoverCellScore.Result bestScore = null;
        Node bestNode = null;
        long bestKey = 0L;
        for (Map.Entry<Long, CoverCellScore.Result> entry : scored.entrySet()) {
            CoverCellScore.Result score = entry.getValue();
            if (score == null || score.tierRank() < CoverCellScore.MIN_SELECT_TIER) {
                continue;
            }
            if (CoverCellScore.compare(score, bestScore) > 0) {
                bestScore = score;
                bestKey = entry.getKey();
                bestNode = nodeByKey.get(bestKey);
            }
        }

        if (bestNode == null) {
            return Optional.empty();
        }
        boolean crawl = Boolean.TRUE.equals(crawlByKey.get(bestKey));
        BlockPos pos = new BlockPos(bestNode.x, bestNode.y, bestNode.z);
        return Optional.of(new FindCoverInfo(pos, crawl));
    }

    @Nullable
    private static SoldierWalkNodeEvaluator poseEvaluator(Mob mob) {
        PathNavigation navigation = mob.getNavigation();
        if (navigation == null) {
            return null;
        }
        NodeEvaluator eval = navigation.getNodeEvaluator();
        return eval instanceof SoldierWalkNodeEvaluator soldier ? soldier : null;
    }

    private static float preferredMeleeRadius(Mob mob) {
        if (mob.getAttributes().hasAttribute(Attributes.FOLLOW_RANGE)) {
            double follow = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
            if (follow > 0.0D) {
                return (float) (follow * 0.35D);
            }
        }
        return DEFAULT_MELEE_RADIUS;
    }
}
