package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel;

import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 走位导演：推进监视计时、阶段时长；驱动 BASE→技能 / 技能→衔接 / 衔接→BASE；
 * 丢目标时 {@link CeilingDuelEntity#onTargetLost()}。
 */
public final class CeilingDuelManeuverDirectorGoal extends Goal {

    private final CeilingDuelEntity boss;

    public CeilingDuelManeuverDirectorGoal(CeilingDuelEntity boss) {
        this.boss = boss;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.boss.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        if (this.boss.getManeuverPhase() != CeilingDuelManeuverPhase.BASE) {
            this.boss.enterBaseFresh();
        } else {
            this.boss.ensureBaseSkillId();
        }
    }

    @Override
    public void stop() {
        this.boss.onTargetLost();
    }

    @Override
    public void tick() {
        this.boss.tickManeuverTelemetry();
        switch (this.boss.getManeuverPhase()) {
            case BASE -> this.tickBase();
            case SKILL -> this.tickSkill();
            case BRIDGE -> this.tickBridge();
        }
    }

    private void tickBase() {
        if (!this.boss.isPhaseTimedOut()) {
            return;
        }
        int picked = this.boss.pickSkillIdOrEmpty();
        if (picked < 0 || !this.boss.tryEnterSkill(picked)) {
            // 空或失败：不转，刷新基础时长继续 0 走位
            this.boss.refreshBasePhaseDuration();
        }
    }

    private void tickSkill() {
        if (this.boss.isPhaseTimedOut()) {
            // 时长到：正常结束进衔接；衔接失败则特殊通道回基础
            if (!this.boss.enterBridge()) {
                this.boss.failToBase();
            }
        }
    }

    private void tickBridge() {
        if (this.boss.isPhaseTimedOut()) {
            this.boss.enterBaseFromBridge();
        }
    }
}
