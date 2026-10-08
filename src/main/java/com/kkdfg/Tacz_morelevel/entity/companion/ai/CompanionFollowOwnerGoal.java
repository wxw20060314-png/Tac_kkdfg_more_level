package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.api.CompanionMob;
import net.minecraft.world.entity.Entity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.core.BlockPos;

public class CompanionFollowOwnerGoal extends Goal {
    private static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private final CompanionMob companion;
    private LivingEntity owner;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public CompanionFollowOwnerGoal(
            CompanionMob companion,
            double speedModifier,
            float startDistance,
            float stopDistance,
            boolean canFly) {
        this.companion = companion;
        this.level = ((net.minecraft.world.entity.Mob) companion).level();
        this.speedModifier = speedModifier;
        this.navigation = ((net.minecraft.world.entity.Mob) companion).getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.canFly = canFly;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(this.navigation instanceof GroundPathNavigation) && !(this.navigation instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for CompanionFollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        if (!this.companion.isTame()) {
            return false;
        }
        LivingEntity livingentity = this.companion.getOwner();
        if (livingentity == null || livingentity.isSpectator() || this.unableToMove()) {
            return false;
        }
        if (((Entity) this.companion).distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        }
        this.owner = livingentity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.companion.isTame() || this.navigation.isDone() || this.unableToMove()) {
            return false;
        }
        return ((Entity) this.companion).distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    private boolean unableToMove() {
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.companion;
        return this.companion.isOrderedToSit() || mob.isPassenger() || mob.isLeashed();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.companion;
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
        mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        ((net.minecraft.world.entity.Mob) this.companion).setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.companion;
        mob.getLookControl().setLookAt(this.owner, 10.0F, (float) mob.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (((Entity) this.companion).distanceToSqr(this.owner) >= 144.0D) {
                this.teleportToOwner();
            } else {
                this.navigation.moveTo(this.owner, this.speedModifier);
            }
        }
    }

    private void teleportToOwner() {
        BlockPos blockpos = this.owner.blockPosition();
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.companion;
        for (int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            if (this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l)) {
                return;
            }
        }
    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
            return false;
        }
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        }
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.companion;
        mob.moveTo((double) x + 0.5D, (double) y, (double) z + 0.5D, mob.getYRot(), mob.getXRot());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos) {
        BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pos.mutable());
        if (blockpathtypes != BlockPathTypes.WALKABLE) {
            return false;
        }
        BlockState blockstate = this.level.getBlockState(pos.below());
        if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
            return false;
        }
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.companion;
        BlockPos blockpos = pos.subtract(mob.blockPosition());
        return this.level.noCollision(mob, mob.getBoundingBox().move(blockpos));
    }

    private int randomIntInclusive(int min, int max) {
        return ((net.minecraft.world.entity.Mob) this.companion).getRandom().nextInt(max - min + 1) + min;
    }
}
