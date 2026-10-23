package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** {@link com.kkdfg.Tacz_morelevel.entity.ai.MobRandomJumpGoal} */
public class CompanionMobRandomJumpGoal extends Goal {

    
    private static final double NO_MAX_DISTANCE = -1.0D;

    private final AbstractFriendEntity companion;
    
    private final int jumpChance;
    
    private final boolean enabled;
    /** {@link #NO_MAX_DISTANCE} */
    private final double maxJumpDistance;

    public CompanionMobRandomJumpGoal(AbstractFriendEntity companion) {
        this(companion, 28, true);
    }

    public CompanionMobRandomJumpGoal(AbstractFriendEntity companion, int jumpChance, boolean enabled) {
        this(companion, jumpChance, enabled, NO_MAX_DISTANCE);
    }

    public CompanionMobRandomJumpGoal(
            AbstractFriendEntity companion, int jumpChance, boolean enabled, double maxJumpDistance) {
        this.companion = companion;
        this.jumpChance = jumpChance;
        this.enabled = enabled;
        this.maxJumpDistance = maxJumpDistance;
    }

    private boolean isRunnable() {
        return this.enabled
                && this.jumpChance > 0
                && this.companion.mayUseCombatCrouchOrCrawl()
                && !this.companion.isTaczCrawling()
                && !this.companion.isOrderedToSit()
                && IGun.mainHandHoldGun(this.companion)
                && !this.companion.hasEffect(ModMobEffects.FLASHED.get());
    }

    @Override
    public boolean canUse() {
        return this.isRunnable()
                && this.hasLivingTarget()
                && this.canSeeTarget()
                && this.isWithinJumpDistance();
    }

    @Override
    public boolean canContinueToUse() {
        return this.isRunnable()
                && this.hasLivingTarget()
                && this.canSeeTarget()
                && this.isWithinJumpDistance();
    }

    private boolean hasLivingTarget() {
        return this.companion.getTarget() != null && this.companion.getTarget().isAlive();
    }

    private boolean canSeeTarget() {
        LivingEntity target = this.companion.getTarget();
        return target != null && this.companion.getSensing().hasLineOfSight(target);
    }

    private boolean isWithinJumpDistance() {
        if (this.maxJumpDistance < 0.0D) {
            return true;
        }
        LivingEntity target = this.companion.getTarget();
        if (target == null) {
            return false;
        }
        double dx = target.getX() - this.companion.getX();
        double dz = target.getZ() - this.companion.getZ();
        return dx * dx + dz * dz < this.maxJumpDistance * this.maxJumpDistance;
    }

    @Override
    public void tick() {
        if (!this.isRunnable()) {
            return;
        }
        if (this.companion.onGround()
                && !this.companion.isInWater()
                && !this.companion.isInLava()
                && this.companion.getRandom().nextInt(this.jumpChance) == 0) {
            this.companion.performRandomJump(this.companion.getRandom());
        }
    }
}
