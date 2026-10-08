package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 炸弹宝宝射击 Goal：仅在握着主武器（MK14）时启用；有 target 时用 {@link BombBabySight}。
 * 选目标仍走基类士兵的 {@link SoldierGunPassthroughSight}。
 */
public final class MobGunRangedBombBabyAttackGoal extends MobGunRangedPassthroughAttackGoal<BombBabyEntity> {

    public MobGunRangedBombBabyAttackGoal(
            BombBabyEntity mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int seeTimeToShoot) {
        super(
                mob,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                meleeLinearFracOfRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                seeTimeToShoot);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.mob().isHoldingPreferredPrimaryGun();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.mob().isHoldingPreferredPrimaryGun();
    }

    @Override
    protected boolean hasGunLineOfSight(LivingEntity target) {
        return BombBabySight.hasLineOfSight(this.mob(), target);
    }
}
