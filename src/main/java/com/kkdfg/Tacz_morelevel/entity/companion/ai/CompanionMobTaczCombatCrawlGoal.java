package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.ai.SoldierGunPassthroughSight;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.integration.tacz.SoldierTaczCrawlHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** {@link com.kkdfg.Tacz_morelevel.entity.ai.MobTaczCombatCrawlGoal} {@link AbstractFriendEntity#mayUseCombatCrouchOrCrawl()} */
public class CompanionMobTaczCombatCrawlGoal extends Goal {

    private final AbstractFriendEntity companion;
    
    private final int crawlRandomDenominator;
    
    private final int crawlExitRandomDenominator;
    
    private final float maxHealthExclusive;
    
    private final double minPlanarDistance;
    
    private final double minHeightToDistanceRatio;
    
    private final boolean enabled;

    public CompanionMobTaczCombatCrawlGoal(AbstractFriendEntity companion) {
        this(companion, 30, 15, 4.5F, 4.0D, 1.0D, true);
    }

    public CompanionMobTaczCombatCrawlGoal(
            AbstractFriendEntity companion,
            int crawlRandomDenominator,
            int crawlExitRandomDenominator,
            float maxHealthExclusive,
            double minPlanarDistance,
            double minHeightToDistanceRatio,
            boolean enabled) {
        this.companion = companion;
        this.crawlRandomDenominator = crawlRandomDenominator;
        this.crawlExitRandomDenominator = crawlExitRandomDenominator;
        this.maxHealthExclusive = maxHealthExclusive;
        this.minPlanarDistance = minPlanarDistance;
        this.minHeightToDistanceRatio = minHeightToDistanceRatio;
        this.enabled = enabled;
    }

    private boolean isRunnable() {
        return this.enabled
                && this.crawlRandomDenominator > 0
                && this.crawlExitRandomDenominator > 0
                && this.maxHealthExclusive > 0.0F
                && this.minPlanarDistance > 0.0D
                && this.minHeightToDistanceRatio > 0.0D;
    }

    private boolean hasSolidCrawlGroundBelow() {
        if (this.companion.isInWater() || this.companion.isInLava()) {
            return false;
        }
        var level = this.companion.level();
        BlockPos below =
                BlockPos.containing(this.companion.getX(), this.companion.getY() - 0.01D, this.companion.getZ())
                        .below();
        BlockState state = level.getBlockState(below);
        if (state.isAir()) {
            return false;
        }
        if (state.getFluidState().is(FluidTags.WATER) || state.getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        return state.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public boolean canUse() {
        return this.isRunnable()
                && !this.companion.level().isClientSide()
                && this.companion.mayUseCombatCrouchOrCrawl()
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
        SoldierTaczCrawlHelper.setCrawlIntent(this.companion, false);
    }

    @Override
    public void tick() {
        this.updateCrawlIntent();
        if (this.companion.isTaczCrawlingSynced()) {
            this.companion.getNavigation().stop();
            this.companion.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void updateCrawlIntent() {
        boolean hold =
                this.companion.isTaczCrawlingSynced()
                        ? this.shouldHoldCrawlIntentContinue()
                        : this.shouldHoldCrawlIntent();
        SoldierTaczCrawlHelper.setCrawlIntent(this.companion, hold);
    }

    
    private boolean shouldCombatCrawl() {
        return true;
    }

    /** {@link #shouldCombatCrawl()} */
    private boolean shouldHoldCrawlIntent() {
        LivingEntity target = this.companion.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!SoldierTaczCrawlHelper.canEnterCrawl(this.companion)) {
            return false;
        }
        if (this.companion.getHealth() >= (this.maxHealthExclusive / 3.0F) * 2.0F) {
            return false;
        }
        if (!SoldierGunPassthroughSight.hasLineOfSightFromFoot(this.companion, target)) {
            return false;
        }
        double dx = target.getX() - this.companion.getX();
        double dz = target.getZ() - this.companion.getZ();
        double planar = Math.sqrt(dx * dx + dz * dz);
        if (planar <= this.minPlanarDistance) {
            return false;
        }
        double dy = target.getEyeY() - this.companion.getY();
        if (dy <= 0.0D) {
            return false;
        }
        return planar / dy > this.minHeightToDistanceRatio;
    }

    
    private boolean shouldHoldCrawlIntentContinue() {
        
        if (this.companion.getRandom().nextInt(this.crawlExitRandomDenominator) == 0) {
            return false;
        }
        if (!this.companion.isTaczCrawlingSynced()
                && this.companion.getRandom().nextInt(this.crawlRandomDenominator) != 0) {
            return false;
        }
        LivingEntity target = this.companion.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!SoldierTaczCrawlHelper.canEnterCrawl(this.companion)) {
            return false;
        }
        if (this.companion.getHealth() >= this.maxHealthExclusive) {
            return false;
        }
        if (!SoldierGunPassthroughSight.hasLineOfSightFromFoot(this.companion, target)) {
            return false;
        }
        double dx = target.getX() - this.companion.getX();
        double dz = target.getZ() - this.companion.getZ();
        double planar = Math.sqrt(dx * dx + dz * dz);
        if (planar <= this.minPlanarDistance) {
            return false;
        }
        double dy = target.getEyeY() - this.companion.getY();
        if (dy <= 0.0D) {
            return (-planar / dy) > (this.minHeightToDistanceRatio / 2.0D);
        }
        return planar / dy > this.minHeightToDistanceRatio;
    }
}
