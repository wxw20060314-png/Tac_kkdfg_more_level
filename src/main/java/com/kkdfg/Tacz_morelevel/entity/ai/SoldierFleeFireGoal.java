package com.kkdfg.Tacz_morelevel.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * 常态逃火：高于躲雷；有 {@link MobEffects#FIRE_RESISTANCE} 时不启用。
 * 算路时临时把 {@link BlockPathTypes#DAMAGE_FIRE} 代价设为 16，圈内随机安全格 {@code moveTo}。
 */
public final class SoldierFleeFireGoal extends Goal {

    private static final int SEARCH_RANGE = 8;
    private static final int SEARCH_VERTICAL = 3;
    private static final int SAMPLE_ATTEMPTS = 12;
    private static final int REPATH_INTERVAL = 5;
    private static final double MOVE_SPEED = 1.25D;
    private static final float FLEE_FIRE_MALUS = 16.0F;

    private final PathfinderMob mob;

    public SoldierFleeFireGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.shouldFlee();
    }

    @Override
    public boolean canContinueToUse() {
        return this.shouldFlee();
    }

    @Override
    public void start() {
        this.tryMoveToSafeCell();
    }

    @Override
    public void tick() {
        if (this.mob.getNavigation().isDone() || this.mob.tickCount % REPATH_INTERVAL == 0) {
            this.tryMoveToSafeCell();
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    private boolean shouldFlee() {
        if (this.mob.level().isClientSide() || !this.mob.isEffectiveAi()) {
            return false;
        }
        if (this.mob.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }
        return this.isStandingOnDamagingFire();
    }

    private boolean isStandingOnDamagingFire() {
        BlockPathTypes type = this.pathTypeAt(this.mob.blockPosition());
        return type == BlockPathTypes.DAMAGE_FIRE || type == BlockPathTypes.LAVA;
    }

    private boolean isSafeDestination(BlockPos pos) {
        BlockPathTypes type = this.pathTypeAt(pos);
        if (type == BlockPathTypes.DAMAGE_FIRE || type == BlockPathTypes.LAVA) {
            return false;
        }
        float malus = this.mob.getPathfindingMalus(type);
        return malus >= 0.0F && malus < FLEE_FIRE_MALUS;
    }

    private BlockPathTypes pathTypeAt(BlockPos pos) {
        return WalkNodeEvaluator.getBlockPathTypeStatic(
                this.mob.level(), pos.mutable());
    }

    private void tryMoveToSafeCell() {
        float old = this.mob.getPathfindingMalus(BlockPathTypes.DAMAGE_FIRE);
        this.mob.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, FLEE_FIRE_MALUS);
        try {
            BlockPos origin = this.mob.blockPosition();
            RandomSource random = this.mob.getRandom();
            for (int i = 0; i < SAMPLE_ATTEMPTS; i++) {
                int dx = random.nextInt(SEARCH_RANGE * 2 + 1) - SEARCH_RANGE;
                int dy = random.nextInt(SEARCH_VERTICAL * 2 + 1) - SEARCH_VERTICAL;
                int dz = random.nextInt(SEARCH_RANGE * 2 + 1) - SEARCH_RANGE;
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos candidate = origin.offset(dx, dy, dz);
                if (!this.isSafeDestination(candidate)) {
                    continue;
                }
                this.mob
                        .getNavigation()
                        .moveTo(
                                candidate.getX() + 0.5D,
                                candidate.getY(),
                                candidate.getZ() + 0.5D,
                                MOVE_SPEED);
                return;
            }
            // 兜底：任意可走向偏移（尽力而为）
            int dx = random.nextInt(SEARCH_RANGE * 2 + 1) - SEARCH_RANGE;
            int dz = random.nextInt(SEARCH_RANGE * 2 + 1) - SEARCH_RANGE;
            if (dx == 0 && dz == 0) {
                dx = 1;
            }
            BlockPos fallback = origin.offset(dx, 0, dz);
            this.mob
                    .getNavigation()
                    .moveTo(
                            fallback.getX() + 0.5D,
                            fallback.getY(),
                            fallback.getZ() + 0.5D,
                            MOVE_SPEED);
        } finally {
            this.mob.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, old);
        }
    }
}
