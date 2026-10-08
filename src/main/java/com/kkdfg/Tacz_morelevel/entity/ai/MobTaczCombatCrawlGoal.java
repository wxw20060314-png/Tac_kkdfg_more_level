package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.api.TaczCombatCrawlMob;
import com.kkdfg.Tacz_morelevel.integration.tacz.SoldierTaczCrawlHelper;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 战斗趴下：{@link #shouldCombatCrawl()} 控制 Goal 是否运行；
 * 实际趴下意图由 {@link #shouldHoldCrawlIntent()} 写入同步字段，维持逻辑在 {@link SoldierTaczCrawlHelper#maintainCrawlFromSync}。
 * 单次趴下最多维持 {@link #MAX_CRAWL_TICKS} tick；结束后 {@link #CRAWL_COOLDOWN_TICKS} tick 内不可再趴。
 */
public class MobTaczCombatCrawlGoal extends Goal {

    /** 单次趴下最长 tick（含随机提前退出）。 */
    private static final int MAX_CRAWL_TICKS = 12;
    /** 起身后再次可趴的间隔。 */
    private static final int CRAWL_COOLDOWN_TICKS = 40;

    private final PathfinderMob soldier;
    /** 未趴下时，每 tick 以 1/{@code crawlRandomDenominator} 概率写入趴下意图。 */
    private final int crawlRandomDenominator;
    /** 已趴下时，每 tick 以 1/{@code crawlExitRandomDenominator} 概率结束维持。 */
    private final int crawlExitRandomDenominator;
    /** 血量须严格小于此值才趴下（心）。 */
    private final float maxHealthExclusive;
    /** 与目标水平距离须严格大于此值（格）。 */
    private final double minPlanarDistance;
    /** 目标高于自身的高度差与水平距离之比须严格大于此值。 */
    private final double minHeightToDistanceRatio;
    /** 为 {@code false} 时本 Goal 不启用。 */
    private final boolean enabled;
    /** 当前这次已同步趴下累计 tick。 */
    private int crawlHoldTicks;
    /** {@link PathfinderMob#tickCount} 达到该值前不可再进趴。 */
    private int crawlReadyAtTick;

    public MobTaczCombatCrawlGoal(PathfinderMob soldier) {
        this(soldier, 10, 50, 6.0F, 4.0D, 1.0D, true);
    }

    public MobTaczCombatCrawlGoal(
            PathfinderMob soldier,
            int crawlRandomDenominator,
            int crawlExitRandomDenominator,
            float maxHealthExclusive,
            double minPlanarDistance,
            double minHeightToDistanceRatio,
            boolean enabled) {
        if (!(soldier instanceof TaczCombatCrawlMob)) {
            throw new IllegalArgumentException("Mob must implement TaczCombatCrawlMob");
        }
        this.soldier = soldier;
        this.crawlRandomDenominator = crawlRandomDenominator;
        this.crawlExitRandomDenominator = crawlExitRandomDenominator;
        this.maxHealthExclusive = maxHealthExclusive;
        this.minPlanarDistance = minPlanarDistance;
        this.minHeightToDistanceRatio = minHeightToDistanceRatio;
        this.enabled = enabled;
    }

    private TaczCombatCrawlMob crawlHost() {
        return (TaczCombatCrawlMob) this.soldier;
    }

    private boolean hasSolidCrawlGroundBelow() {
        if (this.soldier.isInWater() || this.soldier.isInLava()) {
            return false;
        }
        var level = this.soldier.level();
        BlockPos below =
                BlockPos.containing(this.soldier.getX(), this.soldier.getY() - 0.01D, this.soldier.getZ())
                        .below();
        BlockState state = level.getBlockState(below);
        if (state.isAir()) {
            return false;
        }
        // 下方方块本身是流体（水/岩浆源或流动）
        if (state.getFluidState().is(FluidTags.WATER) || state.getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        // 与 MolotovEntity / AbstractLandmineBlock 一致：上表面可承重
        return state.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public boolean canUse() {
        return this.enabled
                && !this.soldier.level().isClientSide()
                && this.shouldCombatCrawl()
                && this.hasSolidCrawlGroundBelow();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.updateCrawlIntent();
    }

    @Override
    public void stop() {
        if (this.crawlHost().isTaczCrawlingSynced()) {
            this.beginCrawlCooldown();
        }
        this.crawlHoldTicks = 0;
        SoldierTaczCrawlHelper.setCrawlIntent(this.crawlHost(), false);
    }

    @Override
    public void tick() {
        this.updateCrawlIntent();
        if (this.crawlHost().isTaczCrawlingSynced()) {
            this.soldier.getNavigation().stop();
            this.soldier.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void updateCrawlIntent() {
        boolean crawling = this.crawlHost().isTaczCrawlingSynced();
        if (!crawling) {
            this.crawlHoldTicks = 0;
        }
        boolean hold =
                crawling ? this.shouldHoldCrawlIntentContinue() : this.shouldHoldCrawlIntent();
        if (crawling && !hold) {
            this.beginCrawlCooldown();
        }
        if (!hold) {
            this.crawlHoldTicks = 0;
        }
        SoldierTaczCrawlHelper.setCrawlIntent(this.crawlHost(), hold);
    }

    private void beginCrawlCooldown() {
        this.crawlReadyAtTick = this.soldier.tickCount + CRAWL_COOLDOWN_TICKS;
    }

    private boolean isCrawlOnCooldown() {
        return this.soldier.tickCount < this.crawlReadyAtTick;
    }

    /** Goal 是否启用（调试时可长期 {@code true}）。 */
    private boolean shouldCombatCrawl() {
        return true;
    }

    /** 是否写入趴下意图；可在此实现自定义规则，与 {@link #shouldCombatCrawl()} 分离。 */
    private boolean shouldHoldCrawlIntent() {
        if (this.isCrawlOnCooldown()) {
            return false;
        }
        LivingEntity target = this.soldier.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!SoldierTaczCrawlHelper.canEnterCrawl(this.soldier)) {
            return false;
        }
        if (this.soldier.getHealth() >= (this.maxHealthExclusive / 3.0F) * 2.0F) {
            return false;
        }
        if (!SoldierGunPassthroughSight.hasLineOfSightFromFoot(this.soldier, target)) {
            return false;
        }
        double dx = target.getX() - this.soldier.getX();
        double dz = target.getZ() - this.soldier.getZ();
        double planar = Math.sqrt(dx * dx + dz * dz);
        if (planar <= this.minPlanarDistance) {
            return false;
        }
        double dy = target.getEyeY() - this.soldier.getY();
        if (dy <= 0.0D) {
            return false;
        }
        return planar / dy > this.minHeightToDistanceRatio;
    }

    /** 已趴下时是否继续维持趴下意图（血量、视线、距离等条件与进入逻辑略有不同）。 */
    private boolean shouldHoldCrawlIntentContinue() {
        if (this.crawlHoldTicks >= MAX_CRAWL_TICKS) {
            return false;
        }
        ++this.crawlHoldTicks;
        // 已趴下：约 1/crawlExitRandomDenominator 概率本 tick 结束（不再维持）
        if (this.soldier.getRandom().nextInt(this.crawlExitRandomDenominator) == 0) {
            return false;
        }
        // 下面仍是血量、视线、距离等条件…
        if (!this.crawlHost().isTaczCrawlingSynced()
                && this.soldier.getRandom().nextInt(this.crawlRandomDenominator) != 0) {
            return false;
        }
        LivingEntity target = this.soldier.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!SoldierTaczCrawlHelper.canEnterCrawl(this.soldier)) {
            return false;
        }
        if (this.soldier.getHealth() >= this.maxHealthExclusive) {
            return false;
        }
        if (!SoldierGunPassthroughSight.hasLineOfSightFromFoot(this.soldier, target)) {
            return false;
        }
        double dx = target.getX() - this.soldier.getX();
        double dz = target.getZ() - this.soldier.getZ();
        double planar = Math.sqrt(dx * dx + dz * dz);
        if (planar <= this.minPlanarDistance) {
            return false;
        }
        double dy = target.getEyeY() - this.soldier.getY();
        if (dy <= 0.0D) {
            return (-planar / dy) > (this.minHeightToDistanceRatio / 2.0D);
        }
        return planar / dy > this.minHeightToDistanceRatio;
    }
}
