package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.api.JumpActingMob;
import com.kkdfg.Tacz_morelevel.api.JumpController;
import com.kkdfg.Tacz_morelevel.api.TaczCrawlCapableMob;
import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierWalkNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 原版 {@link MoveControl} + STRAFE 半砖等站立面放宽；MOVE_TO 起跳改为尝试性选档
 * （软窗口最低够用 → 否则原版拉闩吃默认档）。STRAFE 不起跳。
 * 开启趴寻路时：下一路径点只能趴则进趴；进趴后计时 ≥20 tick 且脚下可站则起身；趴着跨步/试跳软禁。
 */
public final class SoldierMoveControl extends MoveControl {

    private static final int CRAWL_STAND_UP_TICKS = 20;

    private final boolean crawlPathfinding;
    private int crawlTicks;

    public SoldierMoveControl(Mob mob) {
        this(mob, false);
    }

    public SoldierMoveControl(Mob mob, boolean crawlPathfinding) {
        super(mob);
        this.crawlPathfinding = crawlPathfinding;
    }

    public boolean isCrawlPathfinding() {
        return this.crawlPathfinding;
    }

    @Override
    public void tick() {
        if (this.crawlPathfinding) {
            this.tickCrawlPathPose();
        }

        if (this.operation == Operation.STRAFE) {
            float f = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) this.speedModifier * f;
            float f2 = this.strafeForwards;
            float f3 = this.strafeRight;
            float f4 = Mth.sqrt(f2 * f2 + f3 * f3);
            if (f4 < 1.0F) {
                f4 = 1.0F;
            }

            f4 = f1 / f4;
            f2 *= f4;
            f3 *= f4;
            float f5 = Mth.sin(this.mob.getYRot() * ((float) Math.PI / 180F));
            float f6 = Mth.cos(this.mob.getYRot() * ((float) Math.PI / 180F));
            float f7 = f2 * f6 - f3 * f5;
            float f8 = f3 * f6 + f2 * f5;
            if (!this.isWalkable(f7, f8)) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
            }

            this.mob.setSpeed(f1);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedZ - this.mob.getZ();
            double d2 = this.wantedY - this.mob.getY();
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < (double) 2.5000003E-7F) {
                this.mob.setZza(0.0F);
                return;
            }

            float f9 = (float) (Mth.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0F));
            this.mob.setSpeed(
                    (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos blockpos = this.mob.blockPosition();
            BlockState blockstate = this.mob.level().getBlockState(blockpos);
            VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level(), blockpos);
            float stepHeight = this.effectiveStepHeight();
            if (d2 > (double) stepHeight
                            && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getBbWidth())
                    || !voxelshape.isEmpty()
                            && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double) blockpos.getY()
                            && !blockstate.is(BlockTags.DOORS)
                            && !blockstate.is(BlockTags.FENCES)) {
                this.tryPathJump(d2);
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            this.mob.setSpeed(
                    (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround()) {
                this.operation = Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0F);
        }
    }

    /**
     * 未趴：看路径下一节点是否只能趴 → 进趴并清零倒计时。
     * 已趴：累计 tick，≥20 且脚下可站 → 起身。
     */
    private void tickCrawlPathPose() {
        if (!this.isMobCrawling()) {
            this.crawlTicks = 0;
            this.tryEnterCrawlFromNextPathNode();
            return;
        }
        ++this.crawlTicks;
        if (this.crawlTicks < CRAWL_STAND_UP_TICKS) {
            return;
        }
        BlockPos feet = this.mob.blockPosition();
        if (!(this.mob.getNavigation().getNodeEvaluator() instanceof SoldierWalkNodeEvaluator eval)
                || !eval.isStandPoseClearAt(this.mob, feet.getX(), feet.getY(), feet.getZ())) {
            return;
        }
        if (this.mob instanceof TaczCrawlCapableMob crawl) {
            crawl.setTaczCrawling(false);
        }
        this.crawlTicks = 0;
    }

    /** 与 tryPathJump 对称：下一路径点只能趴则进趴。 */
    private void tryEnterCrawlFromNextPathNode() {
        PathNavigation navigation = this.mob.getNavigation();
        if (navigation == null) {
            return;
        }
        Path path = navigation.getPath();
        if (path == null || path.isDone()) {
            return;
        }
        int index = path.getNextNodeIndex();
        if (index < 0 || index >= path.getNodeCount()) {
            return;
        }
        Node next = path.getNode(index);
        if (!(navigation.getNodeEvaluator() instanceof SoldierWalkNodeEvaluator eval)
                || !eval.needsCrawlPoseAt(this.mob, next.x, next.y, next.z)) {
            return;
        }
        if (this.mob instanceof TaczCrawlCapableMob crawl && crawl.setTaczCrawling(true)) {
            this.crawlTicks = 0;
        }
    }

    private boolean isMobCrawling() {
        return this.mob instanceof TaczCrawlCapableMob crawl && crawl.isTaczCrawling();
    }

    /** 趴寻路开启且正在趴时跨步视为 0（只能平地）。 */
    private float effectiveStepHeight() {
        if (this.crawlPathfinding && this.isMobCrawling()) {
            return 0.0F;
        }
        return this.mob.getStepHeight();
    }

    /**
     * 尝试性跳跃：{@link JumpActingMob} 从软窗口最低档起找能跨 {@code neededDeltaY} 的档并设档拉闩；
     * 找不到或非 JumpActingMob 则原版 {@code jump()}（吃默认档）。
     * 趴寻路且正在趴时软禁（天花板技能硬跳不经此路径）。
     */
    private void tryPathJump(double neededDeltaY) {
        if (this.crawlPathfinding && this.isMobCrawling()) {
            return;
        }
        if (this.mob instanceof JumpActingMob jumper) {
            int tier = JumpController.lowestTierForClearance(jumper, neededDeltaY, this.mob);
            if (tier >= 0) {
                jumper.setCurrentJumpTier(tier);
                this.mob.getJumpControl().jump();
                return;
            }
        }
        this.mob.getJumpControl().jump();
    }

    /**
     * 同原版 {@code MoveControl#isWalkable}，额外允许半砖/楼梯/耕地/土径站立面。
     */
    public boolean isWalkable(float relativeX, float relativeZ) {
        PathNavigation pathnavigation = this.mob.getNavigation();
        if (pathnavigation == null) {
            return true;
        }
        NodeEvaluator nodeevaluator = pathnavigation.getNodeEvaluator();
        if (nodeevaluator == null) {
            return true;
        }

        int x = Mth.floor(this.mob.getX() + (double) relativeX);
        int y = this.mob.getBlockY();
        int z = Mth.floor(this.mob.getZ() + (double) relativeZ);
        BlockPathTypes type = nodeevaluator.getBlockPathType(this.mob.level(), x, y, z);
        if (type == BlockPathTypes.WALKABLE) {
            return true;
        }
        Block block = this.mob.level().getBlockState(new BlockPos(x, y, z)).getBlock();
        return isStrafeGroundLike(block);
    }

    private static boolean isStrafeGroundLike(Block block) {
        return block instanceof SlabBlock
                || block instanceof StairBlock
                || block instanceof FarmBlock
                || block instanceof DirtPathBlock;
    }
}
