package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;

/**
 * 炸弹宝宝走位：仅握主武器时启用；视线 {@link GunCombatSight#BLAST}。
 * 射击由 {@link MobGunRangedFireGoal}（同 BLAST）负责。
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
                seeTimeToShoot,
                GunCombatSight.BLAST);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.mob().isHoldingPreferredPrimaryGun();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.mob().isHoldingPreferredPrimaryGun();
    }
}
