package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.base.classic;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunRangedPassthroughAttackGoal;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverPhase;
import com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelManeuverSkills;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 经典枪走位：仅 {@link Flag#MOVE}；射击由 {@link com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel.CeilingDuelGunFireGoal} 负责。
 * 仅在 {@link CeilingDuelManeuverPhase#BASE} 且编号为 {@link CeilingDuelManeuverSkills#BASE} 时启用。
 */
public final class CeilingDuelClassicGunStrafeGoal extends MobGunRangedPassthroughAttackGoal<CeilingDuelEntity> {

    private final CeilingDuelEntity boss;

    public CeilingDuelClassicGunStrafeGoal(
            CeilingDuelEntity boss,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int seeTimeToShoot) {
        super(
                boss,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                meleeLinearFracOfRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                seeTimeToShoot);
        this.boss = boss;
    }

    @Override
    protected EnumSet<Flag> goalFlags() {
        return EnumSet.of(Goal.Flag.MOVE);
    }

    @Override
    protected boolean performsRangedAttack() {
        return false;
    }

    @Override
    public void start() {
        this.boss.enterBaseFresh();
        super.start();
    }

    private boolean skillActive() {
        return this.boss.getManeuverPhase() == CeilingDuelManeuverPhase.BASE
                && this.boss.getManeuverSkillId() == CeilingDuelManeuverSkills.BASE;
    }

    @Override
    public boolean canUse() {
        return this.skillActive() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.skillActive() && super.canContinueToUse();
    }

    @Override
    protected float strafeAxis() {
        return CeilingDuelEntity.GUN_STRAFE_AXIS;
    }
}
