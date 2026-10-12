package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel;

import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedFireGoal;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;

/**
 * 对掏常驻射击：继承通用 {@link MobGunRangedFireGoal}，便于以后加对掏专用逻辑。
 * 走位由各阶段 MOVE Goal 负责。
 */
public final class CeilingDuelGunFireGoal extends MobGunRangedFireGoal<CeilingDuelEntity> {

    public CeilingDuelGunFireGoal(
            CeilingDuelEntity boss,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int attackIntervalMin,
            int seeTimeToShoot) {
        super(
                boss,
                attackRadius,
                meleeLinearFracOfRadius,
                attackIntervalMin,
                seeTimeToShoot,
                GunCombatSight.COMBAT);
    }
}
