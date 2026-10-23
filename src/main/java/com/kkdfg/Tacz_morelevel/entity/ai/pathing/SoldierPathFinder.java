package com.kkdfg.Tacz_morelevel.entity.ai.pathing;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;

/**
 * 原版 A* 壳：仅在算边步长 {@link #distance} 时，若斜向进入且目标
 * {@link SoldierPathNode} 对应搭板要求趴，则额外 +{@link SoldierWalkNodeEvaluator#CRAWL_ONLY_MALUS}（写入 g）。
 */
public final class SoldierPathFinder extends PathFinder {

    public SoldierPathFinder(NodeEvaluator evaluator, int maxVisitedNodes) {
        super(evaluator, maxVisitedNodes);
    }

    @Override
    protected float distance(Node from, Node to) {
        float d = super.distance(from, to);
        if (!(to instanceof SoldierPathNode soldierTo)) {
            return d;
        }
        int dx = Integer.signum(to.x - from.x);
        int dz = Integer.signum(to.z - from.z);
        if (Math.abs(dx) == 1
                && Math.abs(dz) == 1
                && soldierTo.isDiagonalCrawlRequest(dx, dz)) {
            d += SoldierWalkNodeEvaluator.CRAWL_ONLY_MALUS;
        }
        return d;
    }
}
