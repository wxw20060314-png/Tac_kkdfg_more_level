package com.kkdfg.Tacz_morelevel.entity.ai.pathing;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

/**
 * 地面导航：与原版 {@link GroundPathNavigation} 相同，仅换用 {@link SoldierWalkNodeEvaluator}
 * （跳高门槛对齐 {@link com.kkdfg.Tacz_morelevel.api.JumpController} 窗口上沿）。
 * {@code crawlPathfinding} 与 {@link com.kkdfg.Tacz_morelevel.entity.ai.SoldierMoveControl} 同源。
 * <p>
 * {@link net.minecraft.world.entity.ai.navigation.PathNavigation} 在 {@code super} 里就会
 * {@link #createPathFinder}，此时本类字段尚未赋值；用 ThreadLocal 把趴开关传入，
 * 避免 Evaluator 被建成 {@code false}。
 */
public class SoldierGroundPathNavigation extends GroundPathNavigation {

    /** {@code super} → {@link #createPathFinder} 时读取。 */
    private static final ThreadLocal<Boolean> INIT_CRAWL_PATHFINDING = new ThreadLocal<>();

    private final boolean crawlPathfinding;

    public SoldierGroundPathNavigation(Mob mob, Level level) {
        this(mob, level, false);
    }

    public SoldierGroundPathNavigation(Mob mob, Level level, boolean crawlPathfinding) {
        super(pushInitCrawl(mob, crawlPathfinding), level);
        INIT_CRAWL_PATHFINDING.remove();
        this.crawlPathfinding = crawlPathfinding;
    }

    /** 在进入 {@code super} 前写入 ThreadLocal。 */
    private static Mob pushInitCrawl(Mob mob, boolean crawlPathfinding) {
        INIT_CRAWL_PATHFINDING.set(crawlPathfinding);
        return mob;
    }

    public boolean isCrawlPathfinding() {
        return this.crawlPathfinding;
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        boolean crawl = Boolean.TRUE.equals(INIT_CRAWL_PATHFINDING.get());
        this.nodeEvaluator = new SoldierWalkNodeEvaluator(crawl);
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
