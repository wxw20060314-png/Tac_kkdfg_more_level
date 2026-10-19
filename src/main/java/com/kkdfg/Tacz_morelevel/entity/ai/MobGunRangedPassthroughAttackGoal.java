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
    private static final int SIDE_FLIP_COOLDOWN = 20;
    private static final int HURT_FLIP_WINDOW = 10;
    private int seeTime;
    private GunSightMode sightMode = GunSightMode.NOT_SEEING;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    /** 上次左右变相之后的冷却。和横移节拍、掩体定向冷却都不是同一个计数。 */
    private int sideFlipCooldown;
    private int hurtFlipWindowTick;
    private float healthAtWindowStart;

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

    protected boolean isStrafing() {
        return this.strafingTime > -1;
    }

    protected boolean strafingBackwards() {
        return this.strafingBackwards;
    }

    protected boolean strafingClockwise() {
        return this.strafingClockwise;
    }

    protected void setStrafingBackwards(boolean backwards) {
        this.strafingBackwards = backwards;
    }

    protected void setStrafingClockwise(boolean clockwise) {
        this.strafingClockwise = clockwise;
    }

    /**
     * 左右变相请求。冷却已好则必定对调 {@code strafingClockwise} 并进入 20 tick 冷却；冷却中本次请求无效。
     * 不改前后，也不动掩体定向。
     */
    protected void requestSideFlip() {
        if (this.sideFlipCooldown > 0) {
            return;
        }
        this.strafingClockwise = !this.strafingClockwise;
        this.sideFlipCooldown = SIDE_FLIP_COOLDOWN;
    }

    /**
     * 每 {@value #HURT_FLIP_WINDOW} 拍看一次血量。只计相对最大生命少掉的部分，少掉多少比例就以这个比例请求一次左右变相。
     */
    private void tickHurtSideFlip() {
        if (++this.hurtFlipWindowTick < HURT_FLIP_WINDOW) {
            return;
        }
        this.hurtFlipWindowTick = 0;
        float health = this.mob.getHealth();
        float start = this.healthAtWindowStart;
        this.healthAtWindowStart = health;
        float maxHealth = this.mob.getMaxHealth();
        if (maxHealth <= 0.0F) {
            return;
        }
        float loss = (start - health) / maxHealth;
        if (loss <= 0.0F) {
            return;
        }
        if (loss > 1.0F) {
            loss = 1.0F;
        }
        if (this.mob.getRandom().nextFloat() < loss) {
            this.requestSideFlip();
        }
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
        this.sightMode = GunSightMode.NOT_SEEING;
        this.strafingTime = -1;
        this.hurtFlipWindowTick = 0;
        this.healthAtWindowStart = this.mob.getHealth();
    }

    @Override
    public void stop() {
        super.stop();
        this.strafingTime = -1;
        this.seeTime = 0;
        this.sightMode = GunSightMode.NOT_SEEING;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.sideFlipCooldown > 0) {
            this.sideFlipCooldown--;
        }
        this.tickHurtSideFlip();
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        this.mob.setSprinting(false);
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.hasGunLineOfSight(target);
        GunSightMode next = hasLineOfSight ? GunSightMode.SEEING : GunSightMode.NOT_SEEING;
        if (next != this.sightMode) {
            this.seeTime = 0;
            this.sightMode = next;
        }
        if (this.sightMode == GunSightMode.SEEING) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime =0;
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
                this.requestSideFlip();
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
            this.issueStrafe(target, distSqr);
        }
    }

    /** 内外圈已经写好前后之后调用。子类可改方向再发出横移。 */
    protected void issueStrafe(LivingEntity target, double distSqr) {
        this.mob.getMoveControl()
                .strafe(
                        this.strafingBackwards ? -this.strafeAxis() : this.strafeAxis(),
                        this.strafingClockwise ? this.strafeAxis() : -this.strafeAxis());
    }
}
