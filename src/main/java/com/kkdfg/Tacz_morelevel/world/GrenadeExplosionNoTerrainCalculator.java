package com.kkdfg.Tacz_morelevel.world;

import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;

/**
 * 原「不破地形」爆炸计算器已停用；实现与投掷者免伤见 {@link GrenadeEntity#grenadeExplosionDamageCalculator} 及
 * {@link com.kkdfg.Tacz_morelevel.event.GrenadeControlEvents#onLivingHurt}。
 * <p>
 * 旧源码备份（勿取消注释为可编译代码，仅作文档）：
 * <pre>
 * public final class GrenadeExplosionNoTerrainCalculator extends ExplosionDamageCalculator {
 *     public static final GrenadeExplosionNoTerrainCalculator INSTANCE = new GrenadeExplosionNoTerrainCalculator();
 *     private GrenadeExplosionNoTerrainCalculator() {}
 *     {@literal @}Override
 *     public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos,
 *             BlockState state, float fluidExplosionPower) {
 *         return false;
 *     }
 * }
 * </pre>
 */
enum GrenadeExplosionNoTerrainArchive {
    /** 占位，避免空编译单元 */
    MARKER
}
