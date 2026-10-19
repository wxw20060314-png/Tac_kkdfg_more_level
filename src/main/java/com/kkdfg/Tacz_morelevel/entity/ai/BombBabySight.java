package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 炸弹宝宝视线：眼到眼射线；普通枪械可穿方块直接穿过；
 * 实心遮挡若可被「枪爆炸半径 × {@link #BLAST_STRENGTH_FACTOR}」在贴脸强度下炸开，则视为可射击清障。
 * 爆炸半径从 TaCZ 枪数据动态读取（默认 {@link #DEFAULT_BLAST_GUN_ID}）。
 * <p>Goal 接线未挂；仅提供判定 API。
 */
public final class BombBabySight {

    /** 默认参照爆破 MK14。 */
    public static final ResourceLocation DEFAULT_BLAST_GUN_ID =
            ResourceLocation.fromNamespaceAndPath("tacz", "mk14_blast");

    /**
     * 原版爆炸射线倍率约 0.7～1.3；AI 用 1.2（略低于最大破坏 1.3）。
     */
    public static final float BLAST_STRENGTH_FACTOR = 1.2F;

    private static final float FALLBACK_RADIUS = 1.0F;

    private BombBabySight() {}

    public static boolean hasLineOfSight(LivingEntity viewer, LivingEntity target) {
        return hasLineOfSight(viewer, target, resolveGunId(viewer));
    }

    public static boolean hasLineOfSight(
            LivingEntity viewer, LivingEntity target, ResourceLocation gunDataId) {
        if (viewer == null || target == null || !target.isAlive() || viewer.level() != target.level()) {
            return false;
        }
        BlastParams params = readBlastParams(gunDataId);
        if (!params.destroyBlock() || params.radius() <= 0.0F) {
            return SoldierGunPassthroughSight.hasLineOfSight(viewer, target);
        }
        float strength = params.radius() * BLAST_STRENGTH_FACTOR;
        return trace(
                viewer.level(),
                viewer.getEyePosition(1.0F),
                target.getEyePosition(1.0F),
                strength);
    }

    /** 主手是枪则用其 gunId，否则默认爆破 MK14。 */
    public static ResourceLocation resolveGunId(LivingEntity viewer) {
        if (viewer == null) {
            return DEFAULT_BLAST_GUN_ID;
        }
        ItemStack main = viewer.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(main);
        if (gun == null) {
            return DEFAULT_BLAST_GUN_ID;
        }
        return gun.getGunId(main);
    }

    public static BlastParams readBlastParams(ResourceLocation gunDataId) {
        if (gunDataId == null) {
            return new BlastParams(FALLBACK_RADIUS, true);
        }
        return TimelessAPI.getCommonGunIndex(gunDataId)
                .map(idx -> explosionFromGunData(idx.getGunData()))
                .orElse(new BlastParams(FALLBACK_RADIUS, true));
    }

    private static BlastParams explosionFromGunData(GunData gunData) {
        if (gunData == null) {
            return new BlastParams(FALLBACK_RADIUS, true);
        }
        BulletData bullet = gunData.getBulletData();
        if (bullet == null) {
            return new BlastParams(FALLBACK_RADIUS, true);
        }
        ExplosionData explosion = bullet.getExplosionData();
        if (explosion == null || !explosion.isExplode()) {
            return new BlastParams(0.0F, false);
        }
        return new BlastParams(Math.max(0.0F, explosion.getRadius()), explosion.isDestroyBlock());
    }

    /**
     * 贴脸爆炸强度下，方块吸收量 {@code (res+0.3)*0.3} 是否小于强度（可破）。
     */
    public static boolean isDestroyableByBlastStrength(
            BlockState state, BlockGetter level, BlockPos pos, float strength) {
        if (state.isAir()) {
            return true;
        }
        if (SoldierGunPassthroughSight.isPassthroughForGunSight(state, level, pos)) {
            return true;
        }
        float resistance = state.getBlock().getExplosionResistance();
        float absorbed = (resistance + 0.3F) * 0.3F;
        return absorbed < strength;
    }

    private static boolean trace(Level level, Vec3 start, Vec3 end, float strength) {
        if (start.distanceToSqr(end) < 1.0e-8D) {
            return true;
        }
        BlockHitResult hit =
                BlockGetter.traverseBlocks(
                        start,
                        end,
                        level,
                        (lvl, pos) -> {
                            BlockState state = lvl.getBlockState(pos);
                            if (isDestroyableByBlastStrength(state, lvl, pos, strength)) {
                                return null;
                            }
                            return lvl.clipWithInteractionOverride(
                                    start, end, pos, state.getCollisionShape(lvl, pos), state);
                        },
                        ignored -> null);
        return hit == null;
    }

    public record BlastParams(float radius, boolean destroyBlock) {}
}
