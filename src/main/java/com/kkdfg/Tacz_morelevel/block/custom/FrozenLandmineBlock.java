package com.kkdfg.Tacz_morelevel.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 冰冻地雷：与 {@link LandmineBlock} 放置与碰撞相同；触发后在地面留下龙息式区域云（雪花粒子），进入者获得缓慢 IV 2 秒。
 */
public final class FrozenLandmineBlock extends AbstractLandmineBlock {
    /** 区域存在时间（刻），略长于单次缓慢持续时间，便于走过地块仍吃到效果。 */
    private static final int CLOUD_DURATION_TICKS = 400;
    /** 水平覆盖半径（方块），约一格半直径的圆斑。 */
    private static final float CLOUD_RADIUS = 6.0F;
    /** 缓慢 IV：amplifier 3，持续 2 秒。 */
    private static final int SLOWNESS_DURATION_TICKS = 120;

    public FrozenLandmineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel sl) || level.isClientSide) {
            return;
        }
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        sl.removeBlock(pos, false);
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.2D;
        double cz = pos.getZ() + 0.5D;
        AreaEffectCloud cloud = new AreaEffectCloud(sl, cx, cy, cz);
        cloud.setOwner(living);
        cloud.setParticle(ParticleTypes.SNOWFLAKE);
        cloud.setRadius(CLOUD_RADIUS);
        cloud.setDuration(CLOUD_DURATION_TICKS);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(0.0F);
        cloud.setFixedColor(0xB3E5FC);
        cloud.addEffect(
                new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        SLOWNESS_DURATION_TICKS,
                        5,
                        false,
                        true,
                        true));
        sl.addFreshEntity(cloud);
    }
}
