package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/**
 * {@link SimThreadEventType#FIND_COVER} 成功载荷：最优掩体格与是否建议趴。
 * 由 {@link FindCoverSolver} 经 7×7 可达表 + {@link com.kkdfg.Tacz_morelevel.entity.ai.CoverCellScore} 选出。
 */
public final class FindCoverInfo extends SimThreadInfo {

    @Nullable
    private final BlockPos coverPos;
    private final boolean crawl;

    public FindCoverInfo(@Nullable BlockPos coverPos, boolean crawl) {
        super(SimThreadEventType.FIND_COVER);
        this.coverPos = coverPos == null ? null : coverPos.immutable();
        this.crawl = crawl;
    }

    /** 尚无选点结果时的占位成功包（框架/测试用）。 */
    public static FindCoverInfo emptyPlaceholder() {
        return new FindCoverInfo(null, false);
    }

    @Nullable
    public BlockPos coverPos() {
        return this.coverPos;
    }

    public boolean crawl() {
        return this.crawl;
    }
}
