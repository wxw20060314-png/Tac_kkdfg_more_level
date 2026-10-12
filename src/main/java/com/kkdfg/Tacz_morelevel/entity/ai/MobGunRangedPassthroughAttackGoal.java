package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 枪械走位（仅 MOVE）；射击由 {@link MobGunRangedFireGoal} 常驻负责。
 * 交火视线经 {@link GunCombatSight}（默认 {@link GunCombatSight#COMBAT}）。
 */
public class MobGunRangedPassthroughAttackGoal<T extends Mob & RangedAttackMob> extends Goal
        implements MobGunSoldierRangedGoal {

    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private final int seeTimeToStopMove;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;
    private final GunCombatSight sight;
    private static final int SEE_TIME_MAX = 1000;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public MobGunRangedPassthroughAttackGoal(
            T mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int seeTimeToShoot) {
        this(
                mob,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                meleeLinearFracOfRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                seeTimeToShoot,
                GunCombatSight.COMBAT);
    }

    public MobGunRangedPassthroughAttackGoal(
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
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.strafeOuterRingAttackRadiusSqFraction = strafeOuterRingAttackRadiusSqFraction;
        this.strafeInnerRingAttackRadiusSqFraction = strafeInnerRingAttackRadiusSqFraction;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        // melee / seeTimeToShoot 保留构造兼容；走位不用
        Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        this.sight = sight != null ? sight : GunCombatSight.COMBAT;
        this.setFlags(this.goalFlags());
    }

    /** 默认仅 MOVE；对掏 Classic 等同。 */
    protected EnumSet<Flag> goalFlags() {
        return EnumSet.of(Goal.Flag.MOVE);
    }

    /** 侧移轴系数；默认 0.5（原版枪 Goal）。对掏等可覆盖。 */
    protected float strafeAxis() {
        return 0.5F;
    }

    protected T mob() {
        return this.mob;
    }

    protected GunCombatSight sight() {
        return this.sight;
    }

    /** 停走视线；默认 {@link GunCombatSight}。 */
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
        return target != null && target.isAlive() && IGun.mainHandHoldGun(this.mob);
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
