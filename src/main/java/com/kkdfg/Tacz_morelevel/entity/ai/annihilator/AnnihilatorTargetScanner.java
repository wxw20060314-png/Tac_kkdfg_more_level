package com.kkdfg.Tacz_morelevel.entity.ai.annihilator;

import com.kkdfg.Tacz_morelevel.entity.ai.SoldierGunPassthroughSight;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AnnihilatorEntity;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** 歼灭者索敌扫描（空间传送主动寻敌）：玩家优先，其次护航/护卫；被打还击不经此白名单。 */
public final class AnnihilatorTargetScanner {

    private AnnihilatorTargetScanner() {}

    public static boolean isAnnihilatorTarget(LivingEntity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isSpectator() && !player.isCreative();
        }
        return entity instanceof AbstractFriendEntity;
    }

    public static boolean canTarget(AnnihilatorEntity mob, LivingEntity candidate) {
        return candidate != mob
                && isAnnihilatorTarget(candidate)
                && mob.canAttack(candidate);
    }

    /** {@link #scanNearest} 专用：蹲下或游泳时不作为扫描目标。 */
    public static boolean isExcludedFromBlinkScan(LivingEntity entity) {
        return entity.isCrouching() || entity.isSwimming();
    }

    public static boolean canScanNearest(AnnihilatorEntity mob, LivingEntity candidate) {
        return canTarget(mob, candidate) && !isExcludedFromBlinkScan(candidate);
    }

    /** 枪械穿透视线：与 {@link MobGunNearestAttackablePassthroughTargetGoal} 一致。 */
    public static boolean canSeeTarget(AnnihilatorEntity mob, LivingEntity target) {
        return target != null
                && target.isAlive()
                && SoldierGunPassthroughSight.hasLineOfSight(mob, target);
    }

    @Nullable
    public static LivingEntity scanNearest(AnnihilatorEntity mob) {
        AABB box =
                mob.getBoundingBox()
                        .inflate(
                                AnnihilatorEntity.SCAN_HALF_EXTENT_PLANAR,
                                AnnihilatorEntity.SCAN_HALF_EXTENT_VERTICAL,
                                AnnihilatorEntity.SCAN_HALF_EXTENT_PLANAR);
        LivingEntity player = scanNearestInBox(mob, Player.class, box);
        if (player != null) {
            return player;
        }
        return scanNearestInBox(mob, AbstractFriendEntity.class, box);
    }

    @Nullable
    private static <T extends LivingEntity> T scanNearestInBox(
            AnnihilatorEntity mob, Class<T> type, AABB box) {
        List<T> candidates =
                mob.level().getEntitiesOfClass(type, box, candidate -> canScanNearest(mob, candidate));
        T best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (T candidate : candidates) {
            double distSq = mob.distanceToSqr(candidate);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = candidate;
            }
        }
        return best;
    }
}
