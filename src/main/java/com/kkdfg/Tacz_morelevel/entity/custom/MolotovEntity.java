package com.kkdfg.Tacz_morelevel.entity.custom;

import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class MolotovEntity extends ThrowableItemProjectile {
    private static final int BFS_MAX_PATH = 6;

    public MolotovEntity(EntityType<? extends MolotovEntity> type, Level level) {
        super(type, level);
    }

    public MolotovEntity(Level level, LivingEntity owner) {
        super(ModEntities.MOLOTOV.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOLOTOV.get();
    }

    /** 飞行/碎裂音效以实体为声源，随瓶子位置与距离衰减。 */
    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        Level level = this.level();
        if (level.isClientSide) {
            double x = this.getX();
            double y = this.getY() + 0.5;
            double z = this.getZ();
            double vx = (this.random.nextDouble() - 0.5) * 0.02;
            double vy = this.random.nextDouble() * 0.03;
            double vz = (this.random.nextDouble() - 0.5) * 0.02;
            level.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
        } else {
            this.playSound(
                    ModSounds.SOUND_FIREBOTTLE_FLY.get(),
                    0.2F,
                    0.92F);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            shatterAndSpreadFire(BlockPos.containing(result.getLocation()));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            Level level = this.level();
            BlockPos hitPos = result.getBlockPos();
            BlockState hitState = level.getBlockState(hitPos);
            if (hitState.is(BlockTags.DOORS)) {
                // 门：可破坏，但不走“炸开”表现
                this.playSound(
                        SoundEvents.GLASS_BREAK,
                        1.0F,
                        0.85F + level.random.nextFloat() * 0.25F);
                level.destroyBlock(hitPos, false, this);
                spreadFire(hitPos);
                this.discard();
                return;
            }
            BlockPos impact = hitPos.relative(result.getDirection());
            shatterAndSpreadFire(impact);
        }
    }

    private void shatterAndSpreadFire(BlockPos start) {
        this.playSound(
                SoundEvents.GLASS_BREAK,
                1.0F,
                0.85F + this.random.nextFloat() * 0.25F);
        spreadFire(start);
        this.discard();
    }

    private void spreadFire(BlockPos start) {
        Level level = this.level();
        ArrayDeque<Node> stack = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        stack.push(new Node(start.immutable(), 0));

        while (!stack.isEmpty()) {
            Node cur = stack.pop();
            if (cur.path > BFS_MAX_PATH || !visited.add(cur.pos)) {
                continue;
            }

            BlockState state = level.getBlockState(cur.pos);
            if (isFragilePlant(state)) {
                // 草/花等可一碰即碎：遇火直接清除，并继续 3x3x3
                level.destroyBlock(cur.pos, false, this);
                pushCube(stack, cur.pos, cur.path + 1);
            }

            if (state.is(BlockTags.DOORS)) {
                // 门：可被燃烧瓶破坏，并继续扩散
                level.destroyBlock(cur.pos, false, this);
                pushCube(stack, cur.pos, cur.path + 1);
            }

            // 新 BFS 规则：无论是否空气，当前节点都向六个方向继续搜索
            if (cur.path < BFS_MAX_PATH) {
                for (Direction d : Direction.values()) {
                    stack.push(new Node(cur.pos.relative(d).immutable(), cur.path + 1));
                }
            }

            // 每个节点都检查“该方块上方”是否满足放火条件
            BlockPos firePos = cur.pos.above();
            if (canPlaceMolotovFire(level, firePos)) {
                level.setBlock(firePos, ModBlocks.MOLOTOV_FIRE.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    private static void pushCube(ArrayDeque<Node> stack, BlockPos center, int path) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    stack.push(new Node(center.offset(dx, dy, dz).immutable(), path));
                }
            }
        }
    }

    private static boolean isFragilePlant(BlockState state) {
        return state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.REPLACEABLE_BY_TREES)
                || state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH);
    }

    private static boolean canPlaceMolotovFire(Level level, BlockPos pos) {
        if (!level.isEmptyBlock(pos)) {
            return false; // 禁止重叠
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.isAir()) {
            return false; // 禁止空中悬浮
        }
        return belowState.isFaceSturdy(level, below, Direction.UP);
    }

    private record Node(BlockPos pos, int path) {}
}
