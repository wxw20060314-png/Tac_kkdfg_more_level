package com.kkdfg.Tacz_morelevel.entity.ai.pathing;

import net.minecraft.world.level.pathfinder.Node;

/**
 * 士兵寻路节点：在原版 {@link Node} 上挂斜向四元搭板（是否要求趴）。
 * 下标：0=NW, 1=NE, 2=SW, 3=SE（从邻格斜向进入本格的方向）。
 * <p>寻路扩邻在 {@code isDiagonalValid} 打标；MoveControl 读标进趴后续再接。
 */
public final class SoldierPathNode extends Node {

    /** 西北：dx=-1, dz=-1。 */
    public static final int DIAG_NW = 0;
    /** 东北：dx=+1, dz=-1。 */
    public static final int DIAG_NE = 1;
    /** 西南：dx=-1, dz=+1。 */
    public static final int DIAG_SW = 2;
    /** 东南：dx=+1, dz=+1。 */
    public static final int DIAG_SE = 3;
    public static final int DIAG_COUNT = 4;

    /** 对应斜向进入本格时是否要求趴下。 */
    private final boolean[] diagonalCrawlRequest = new boolean[DIAG_COUNT];

    public SoldierPathNode(int x, int y, int z) {
        super(x, y, z);
    }

    /** {@code (dx,dz)} 为 ±1 斜向时返回下标，否则 -1。 */
    public static int diagonalIndex(int dx, int dz) {
        if (dx == -1 && dz == -1) {
            return DIAG_NW;
        }
        if (dx == 1 && dz == -1) {
            return DIAG_NE;
        }
        if (dx == -1 && dz == 1) {
            return DIAG_SW;
        }
        if (dx == 1 && dz == 1) {
            return DIAG_SE;
        }
        return -1;
    }

    public void clearDiagonalCrawlRequests() {
        for (int i = 0; i < DIAG_COUNT; i++) {
            this.diagonalCrawlRequest[i] = false;
        }
    }

    public void setDiagonalCrawlRequest(int diagIndex, boolean request) {
        if (diagIndex >= 0 && diagIndex < DIAG_COUNT) {
            this.diagonalCrawlRequest[diagIndex] = request;
        }
    }

    public boolean isDiagonalCrawlRequest(int diagIndex) {
        return diagIndex >= 0
                && diagIndex < DIAG_COUNT
                && this.diagonalCrawlRequest[diagIndex];
    }

    public boolean isDiagonalCrawlRequest(int dx, int dz) {
        return this.isDiagonalCrawlRequest(diagonalIndex(dx, dz));
    }

    @Override
    public Node cloneAndMove(int x, int y, int z) {
        SoldierPathNode copy = new SoldierPathNode(x, y, z);
        copy.heapIdx = this.heapIdx;
        copy.g = this.g;
        copy.h = this.h;
        copy.f = this.f;
        copy.cameFrom = this.cameFrom;
        copy.closed = this.closed;
        copy.walkedDistance = this.walkedDistance;
        copy.costMalus = this.costMalus;
        copy.type = this.type;
        System.arraycopy(this.diagonalCrawlRequest, 0, copy.diagonalCrawlRequest, 0, DIAG_COUNT);
        return copy;
    }
}
