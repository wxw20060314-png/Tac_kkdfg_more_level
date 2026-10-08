package com.kkdfg.Tacz_morelevel.entity.ai.annihilator;

import com.kkdfg.Tacz_morelevel.entity.custom.AnnihilatorEntity;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** 无目标时周期性扫描玩家/护航；看不见目标时才空间传送。 */
public final class AnnihilatorBlinkSkillGoal extends Goal {

    private final AnnihilatorEntity mob;

    public AnnihilatorBlinkSkillGoal(AnnihilatorEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (needsTargetScan(target)) {
            return true;
        }
        return !AnnihilatorTargetScanner.canSeeTarget(this.mob, target);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.mob.level().isClientSide()) {
            return;
        }
        if (this.mob.tickCount % AnnihilatorEntity.TARGET_SCAN_INTERVAL_TICKS != 0) {
            return;
        }
        LivingEntity target = this.mob.getTarget();
        if (needsTargetScan(target)) {
            LivingEntity found = AnnihilatorTargetScanner.scanNearest(this.mob);
            if (found == null) {
                return;
            }
            this.mob.setTarget(found);
            tryBlinkIfCannotSee(found);
            return;
        }
        tryBlinkIfCannotSee(target);
    }

    private void tryBlinkIfCannotSee(LivingEntity target) {
        if (AnnihilatorTargetScanner.canSeeTarget(this.mob, target)) {
            return;
        }
        AnnihilatorBlink.tryBlinkNearTarget(this.mob, target, this.mob.getBlinkCooldown());
    }

    private static boolean needsTargetScan(@Nullable LivingEntity target) {
        return target == null || !target.isAlive();
    }
}
