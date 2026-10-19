package com.kkdfg.Tacz_morelevel.entity.ai.bomb_sight;

import com.kkdfg.Tacz_morelevel.entity.custom.BombSightEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 第一次进入趴下时打开 60 tick 计时。
 * 计时走完或脚下有承重方块就尝试站立，失败则每 tick 再试，不重新开计时。
 */
public final class BombSightReleaseCrawlGoal extends Goal {

    private static final int CRAWL_TICKS = 60;

    private final BombSightEntity mob;
    /** 上一 tick 是否趴着，用来认第一次进入。 */
    private boolean wasCrawling;
    private int crawlTicks;

    public BombSightReleaseCrawlGoal(BombSightEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        return !this.mob.level().isClientSide();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean now = this.mob.isTaczCrawling();
        if (!this.wasCrawling && now && this.crawlTicks <= 0) {
            this.crawlTicks = CRAWL_TICKS;
        }
        if (this.crawlTicks > 0) {
            this.crawlTicks--;
        }
        if (now && (this.crawlTicks <= 0 || this.hasBlockUnderFeet())) {
            this.crawlTicks = 0;
            this.mob.setTaczCrawling(false);
        }
        this.wasCrawling = this.mob.isTaczCrawling();
    }

    /** 脚所在格的正下方能承重。 */
    private boolean hasBlockUnderFeet() {
        if (this.mob.isInWater() || this.mob.isInLava()) {
            return false;
        }
        BlockPos below = this.mob.blockPosition().below();
        BlockState state = this.mob.level().getBlockState(below);
        if (state.isAir()
                || state.getFluidState().is(FluidTags.WATER)
                || state.getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        return state.isFaceSturdy(this.mob.level(), below, Direction.UP);
    }
}
