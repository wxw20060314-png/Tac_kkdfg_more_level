package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 通用枪械走位 Goal：仅占 {@link Flag#MOVE}；射击由 {@link MobGunRangedFireGoal} 负责。
 */
public class MobGunRangedMoveGoal<T extends Mob & RangedAttackMob> extends Goal
        implements MobGunSoldierRangedGoal {

    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private final int seeTimeToStopMove;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;
    private final GunCombatSight sight;
    @Nullable private final Predicate<T> extraCanUse;

    private static final int SEE_TIME_MAX = 1000;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public MobGunRangedMoveGoal(
            T mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadiusIgnored,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int seeTimeToShootIgnored) {
        this(
                mob,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                GunCombatSight.COMBAT,
                null);
    }

    public MobGunRangedMoveGoal(
            T mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            GunCombatSight sight) {
        this(
                mob,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                sight,
                null);
    }

    public MobGunRangedMoveGoal(
            T mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            GunCombatSight sight,
            @Nullable Predicate<T> extraCanUse) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.strafeOuterRingAttackRadiusSqFraction = strafeOuterRingAttackRadiusSqFraction;
        this.strafeInnerRingAttackRadiusSqFraction = strafeInnerRingAttackRadiusSqFraction;
        this.sight = sight != null ? sight : GunCombatSight.COMBAT;
        this.extraCanUse = extraCanUse;
        this.setFlags(this.goalFlags());
    }

    /** 兼容旧 Passthrough 九参构造（melee/seeTimeToShoot 仅占位）。 */
    public static <T extends Mob & RangedAttackMob> MobGunRangedMoveGoal<T> fromLegacyNineArgs(
            T mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int seeTimeToShoot,
            GunCombatSight sight) {
        return new MobGunRangedMoveGoal<>(
                mob,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                sight,
                null);
    }

    protected EnumSet<Flag> goalFlags() {
        return EnumSet.of(Flag.MOVE);
    }

    protected float strafeAxis() {
        return 0.5F;
    }

    protected T mob() {
        return this.mob;
    }

    protected GunCombatSight sight() {
        return this.sight;
    }

    protected boolean hasGunLineOfSight(LivingEntity target) {
        return this.sight.hasLineOfSight(this.mob, target);
    }

    @Override
    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !IGun.mainHandHoldGun(this.mob)) {
            return false;
        }
        return this.extraCanUse == null || this.extraCanUse.test(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
        this.seeTime = 0;
        this.strafingTime = -1;
    }

    @Override
    public void stop() {
        super.stop();
        this.strafingTime = -1;
        this.seeTime = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        this.mob.setSprinting(false);
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.hasGunLineOfSight(target);
        boolean hadSeeTime = this.seeTime > 0;
        if (hasLineOfSight != hadSeeTime) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }
        if (!(distSqr > (double) this.attackRadiusSqr) && this.seeTime >= this.seeTimeToStopMove) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }
        if (this.strafingTime >= 20) {
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }
        if (this.strafingTime > -1) {
            if (distSqr > (double) (this.attackRadiusSqr * this.strafeOuterRingAttackRadiusSqFraction)) {
                this.strafingBackwards = false;
            } else if (distSqr < (double) (this.attackRadiusSqr * this.strafeInnerRingAttackRadiusSqFraction)) {
                this.strafingBackwards = true;
            }
            this.mob.getMoveControl()
                    .strafe(
                            this.strafingBackwards ? -this.strafeAxis() : this.strafeAxis(),
                            this.strafingClockwise ? this.strafeAxis() : -this.strafeAxis());
        }
    }
}
