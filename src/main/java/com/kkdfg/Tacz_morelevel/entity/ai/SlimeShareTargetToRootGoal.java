package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.AbstractSlimeExperimentEntity;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

/**
 * 对齐 {@link MarshalGuardShareTargetToOwnerGoal}：非根史莱姆有 target、树根无有效 target 时，
 * 把同一目标传给 root（root {@code setTarget} 再下发给其他成员）。
 */
public class SlimeShareTargetToRootGoal extends TargetGoal {
    private final AbstractSlimeExperimentEntity slime;

    public SlimeShareTargetToRootGoal(AbstractSlimeExperimentEntity slime) {
        super(slime, false);
        this.slime = slime;
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
        if (this.slime.level().isClientSide() || this.slime.isTreeRoot()) {
            return false;
        }
        LivingEntity target = this.slime.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        AbstractSlimeExperimentEntity root = this.resolveLinkedRoot();
        if (root == null) {
            return false;
        }
        LivingEntity rootTarget = root.getTarget();
        if (rootTarget != null && rootTarget.isAlive()) {
            return false;
        }
        return root.canAttack(target);
    }

    private void share() {
        LivingEntity target = this.slime.getTarget();
        AbstractSlimeExperimentEntity root = this.resolveLinkedRoot();
        if (target == null || !target.isAlive() || root == null) {
            return;
        }
        LivingEntity rootTarget = root.getTarget();
        if (rootTarget != null && rootTarget.isAlive()) {
            return;
        }
        if (!root.canAttack(target)) {
            return;
        }
        root.setTarget(target);
    }

    /** 树根已加载且与本实体同树；自己是根则无需上送。 */
    @Nullable
    private AbstractSlimeExperimentEntity resolveLinkedRoot() {
        if (this.slime.isTreeRoot()) {
            return null;
        }
        AbstractSlimeExperimentEntity root = this.slime.findRootEntity();
        if (root == null || root == this.slime || !root.isAlive()) {
            return null;
        }
        if (root.level() != this.slime.level()) {
            return null;
        }
        if (!this.slime.isSameTree(root)) {
            return null;
        }
        return root;
    }
}
