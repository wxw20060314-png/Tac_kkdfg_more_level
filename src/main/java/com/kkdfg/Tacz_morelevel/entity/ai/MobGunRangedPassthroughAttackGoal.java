package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 与 {@link MobGunRangedAttackGoal} 相同逻辑；交火视线为头→身→脚
 * （{@link MobGunPlayerLikeAimHelper#hasCombatLineOfSight}），索敌仍用眼对眼
 * {@link SoldierGunPassthroughSight#hasLineOfSight}。
 */
public class MobGunRangedPassthroughAttackGoal<T extends Mob & RangedAttackMob> extends Goal implements MobGunSoldierRangedGoal {

    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private final double meleeDistSqThreshold;
    private final int seeTimeToStopMove;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;
    private final int seeTimeToShoot;
    private static final int SEE_TIME_MAX = 1000;
    private int attackTime;
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
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.strafeOuterRingAttackRadiusSqFraction = strafeOuterRingAttackRadiusSqFraction;
        this.strafeInnerRingAttackRadiusSqFraction = strafeInnerRingAttackRadiusSqFraction;
        this.seeTimeToShoot = seeTimeToShoot;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        float f = Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        double meleeR = attackRadius * f;
        this.meleeDistSqThreshold = meleeR * meleeR;
        this.setFlags(this.goalFlags());
    }

    /** 默认 MOVE+LOOK；对掏 Classic 等纯走位可改为仅 MOVE。 */
    protected EnumSet<Flag> goalFlags() {
        return EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK);
    }

    /** 为 false 时本 Goal 只走位，不开火/不控瞄准（由对掏 {@code CeilingDuelGunFireGoal} 负责）。 */
    protected boolean performsRangedAttack() {
        return true;
    }

    /** 侧移轴系数；默认 0.5（原版枪 Goal）。对掏等可覆盖。 */
    protected float strafeAxis() {
        return 0.5F;
    }

    protected T mob() {
        return this.mob;
    }

    /** 有 target 时的射击视线；默认普通穿透。炸弹宝宝等可覆盖为爆破清障视线。 */
    protected boolean hasGunLineOfSight(LivingEntity target) {
        return MobGunPlayerLikeAimHelper.hasCombatLineOfSight(this.mob, target);
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
        this.attackTime = this.attackIntervalMin / 2;
        if (this.performsRangedAttack() && !this.mob.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
            op.initialData();
            op.draw(this.mob::getMainHandItem);
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.strafingTime = -1;
        this.seeTime = 0;
        this.attackTime = 0;
        if (this.performsRangedAttack()) {
            this.mob.setAggressive(false);
            if (!this.mob.level().isClientSide()) {
                IGunOperator.fromLivingEntity(this.mob).aim(false);
            }
        }
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
            this.mob.getMoveControl().strafe(
                    this.strafingBackwards ? -this.strafeAxis() : this.strafeAxis(),
                    this.strafingClockwise ? this.strafeAxis() : -this.strafeAxis());
        }
        if (!this.performsRangedAttack()) {
            return;
        }
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.mob, target);
        if (!this.mob.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
            boolean inAttackRing = hasLineOfSight && distSqr <= (double) this.attackRadiusSqr;
            boolean meleeBand = inAttackRing && distSqr <= this.meleeDistSqThreshold;
            boolean midLongBand = inAttackRing && distSqr > this.meleeDistSqThreshold;
            if (midLongBand) {
                op.aim(true);
            } else {
                op.aim(false);
            }
            if (meleeBand) {
                this.attackTime = 0;
            } else if (this.attackTime > 0) {
                --this.attackTime;
            }
            boolean taczReady = op.getSynDrawCoolDown() == 0L && !op.getSynIsBolting();
            boolean meleeShotOk = meleeBand && taczReady && this.seeTime >= 1;
            boolean midLongShotOk =
                    midLongBand && this.attackTime == 0 && taczReady && this.seeTime >= this.seeTimeToShoot;
            if (meleeShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
            } else if (midLongShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
                this.attackTime = this.attackIntervalMin;
            }
        }
    }
}
