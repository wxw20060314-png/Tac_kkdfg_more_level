package com.kkdfg.Tacz_morelevel.block.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.effect.MolotovBurnEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MolotovFireBlock extends Block {
    private static final int PERIOD_TICKS = 5;
    private static final int BURN_EFFECT_PERIOD_TICKS = 20;
    /** 燃烧 effect 时长累加上限。 */
    private static final int BURN_EFFECT_MAX_TICKS = 1000;
    private static final int LIFETIME_TICKS = 200; // 10 seconds
    /** Horizontal faces: fraction 1/N per random tick attempt (feel of ground/wall crawl). */
    private static final int SPREAD_CHANCE_HORIZONTAL = 3;
    /** Vertical neighbors (up/down): thinner so fire reads as creeping along surfaces. */
    private static final int SPREAD_CHANCE_VERTICAL = 5;

    public MolotovFireBlock(BlockBehaviour.Properties properties) {
        super(properties.noCollission().instabreak().lightLevel(s -> 10).replaceable().randomTicks());
    }

    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (level.isClientSide) {
            spawnAroundParticles(level, living);
            return;
        }
        if (living.tickCount % PERIOD_TICKS == 0) {
            living.hurt(level.damageSources().inFire(), Config.molotovFireDamage);
            living.setSecondsOnFire(3);
        }
        if (living.tickCount % BURN_EFFECT_PERIOD_TICKS == 0) {
            var burn = ModMobEffects.MOLOTOV_BURN.get();
            MobEffectInstance cur = living.getEffect(burn);
            int next =
                    Math.min(
                            BURN_EFFECT_MAX_TICKS,
                            (cur != null ? cur.getDuration() : 0)
                                    + MolotovBurnEffect.APPLY_DURATION_TICKS);
            int amp =
                    Math.max(
                            MolotovBurnEffect.APPLY_AMPLIFIER,
                            cur != null ? cur.getAmplifier() : 0);
            living.addEffect(new MobEffectInstance(burn, next, amp, false, false, true));
        }
    }

    private static void spawnAroundParticles(Level level, LivingEntity living) {
        if (living.tickCount % 2 != 0) {
            return;
        }
        double x = living.getX() + (living.getRandom().nextDouble() - 0.5D) * living.getBbWidth();
        double y = living.getY() + living.getRandom().nextDouble() * living.getBbHeight();
        double z = living.getZ() + (living.getRandom().nextDouble() - 0.5D) * living.getBbWidth();
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.02D, 0.0D);
        if (living.getRandom().nextBoolean()) {
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, LIFETIME_TICKS);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (Direction d : Direction.values()) {
            int n = d.getAxis().isHorizontal()
                    ? SPREAD_CHANCE_HORIZONTAL
                    : SPREAD_CHANCE_VERTICAL;
            if (random.nextInt(n) != 0) {
                continue;
            }
            trySpreadTowardNeighbor(level, pos, d);
        }

        int extraNeighbors = random.nextInt(3);
        for (int i = 0; i < extraNeighbors; i++) {
            trySpreadTowardNeighbor(level, pos, Direction.getRandom(random));
        }
    }

    /**
     * Fire creeps toward an adjacent cell: clears thin plants/doors like the impact splash,
     * then places fire on flat tops or one step above solid sides (vanilla-style wall lip).
     */
    private void trySpreadTowardNeighbor(ServerLevel level, BlockPos origin, Direction d) {
        BlockPos candidate = origin.relative(d);
        if (!tryClaimFireCell(level, candidate) && d.getAxis().isHorizontal()) {
            BlockPos lip = candidate.above();
            if (!tryClaimFireCell(level, lip)) {
                tryClaimFireCell(level, origin.above().relative(d));
            }
        }
    }

    private boolean tryClaimFireCell(ServerLevel level, BlockPos pos) {
        clearSoftObstacle(level, pos);
        if (canPlaceOnSurface(level, pos)) {
            level.setBlock(pos, this.defaultBlockState(), 3);
            return true;
        }
        return false;
    }

    private void clearSoftObstacle(ServerLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (!isSpreadConsumable(s)) {
            return;
        }
        level.destroyBlock(pos, false);
    }

    private static boolean isSpreadConsumable(BlockState state) {
        if (state.is(BlockTags.DOORS)) {
            return true;
        }
        return state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.REPLACEABLE_BY_TREES)
                || state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    1.0F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.3F,
                    false);
        }

        for (int i = 0; i < 3; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }

    private boolean canPlaceOnSurface(Level level, BlockPos pos) {
        if (!level.isEmptyBlock(pos) || level.getBlockState(pos).is(this) || level.getBlockState(pos).is(Blocks.FIRE)) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return !belowState.isAir() && belowState.isFaceSturdy(level, below, Direction.UP);
    }
}
