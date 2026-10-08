package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.LevMarshalEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MarshalGuardEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

/**
 * 护卫有 target、大帅无有效 target 时，把同一目标传给大帅。
 * 未双向连通（名册无槽 / 会话超时 / 大帅未加载）则不传。
 */
public class MarshalGuardShareTargetToOwnerGoal extends TargetGoal {
    private final MarshalGuardEntity guard;

    public MarshalGuardShareTargetToOwnerGoal(MarshalGuardEntity guard) {
        super(guard, false);
        this.guard = guard;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.canShare();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canShare();
    }

    @Override
    public void start() {
        this.share();
        super.start();
    }

    @Override
    public void tick() {
        this.share();
    }

    private boolean canShare() {
        if (this.guard.level().isClientSide()) {
            return false;
        }
        LivingEntity target = this.guard.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        LevMarshalEntity marshal = this.resolveLinkedMarshal();
        if (marshal == null) {
            return false;
        }
        LivingEntity marshalTarget = marshal.getTarget();
        if (marshalTarget != null && marshalTarget.isAlive()) {
            return false;
        }
        return marshal.canAttack(target) && this.guard.wantsToAttack(target, marshal);
    }

    private void share() {
        LivingEntity target = this.guard.getTarget();
        LevMarshalEntity marshal = this.resolveLinkedMarshal();
        if (target == null || !target.isAlive() || marshal == null) {
            return;
        }
        LivingEntity marshalTarget = marshal.getTarget();
        if (marshalTarget != null && marshalTarget.isAlive()) {
            return;
        }
        if (!marshal.canAttack(target) || !this.guard.wantsToAttack(target, marshal)) {
            return;
        }
        marshal.setTarget(target);
    }

    /** 大帅已加载且与本护卫双向会话连通；否则视为未连接。 */
    private LevMarshalEntity resolveLinkedMarshal() {
        LivingEntity owner = this.guard.getMarshalOwner();
        if (!(owner instanceof LevMarshalEntity marshal)) {
            return null;
        }
        if (marshal.level() != this.guard.level()) {
            return null;
        }
        if (!this.guard.isOwnerLinkAlive()) {
            return null;
        }
        if (!marshal.isLinkedGuard(this.guard)) {
            return null;
        }
        return marshal;
    }
}
