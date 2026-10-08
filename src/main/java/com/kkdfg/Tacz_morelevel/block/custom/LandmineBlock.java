package com.kkdfg.Tacz_morelevel.block.custom;

import com.kkdfg.Tacz_morelevel.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 地雷：与 {@link LivingEntity} 碰撞后移除自身并在原地引发破坏地形的爆炸。
 */
public final class LandmineBlock extends AbstractLandmineBlock {

    public LandmineBlock(BlockBehaviour.Properties properties) {
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
        double cy = pos.getY() + 5.0D / 32.0D;
        double cz = pos.getZ() + 0.5D;
        /*
         * 勿把踩雷者作为 explode 的 source：原版会对爆炸源实体跳过或减免伤害，导致玩家踩雷不伤。
         * indirectEntity 仍可设为踩雷者便于死亡消息等。
         */
        DamageSource blast = sl.damageSources().explosion(null, living);
        ExplosionDamageCalculator calc = new ExplosionDamageCalculator();
        sl.explode(
                null,
                blast,
                calc,
                cx,
                cy,
                cz,
                Config.landmineExplosionPower,
                false,
                Level.ExplosionInteraction.TNT);
    }
}
