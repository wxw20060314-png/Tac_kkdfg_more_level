package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.entity.IGunOperator;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 难民用：近战与中远距离均为点射，两次开火之间固定 {@value #TAP_FIRE_INTERVAL_TICKS} tick；
 * {@link #setMinAttackInterval} 恒维持该间隔（不受难度动态缩短）。
 * <p>
 * 父类 {@link MobGunRangedAttackGoal} 的状态字段为 private，无法在子类 tick 中访问；此处用本类自有字段驱动走位与开火节奏，
 * {@link #start()} / {@link #stop()} / {@link #tick()} 与父类并行维护同一实体上的行为。
 */
public final class MobGunRangedAttackByRefugeeGoal<T extends Mob & RangedAttackMob>
        extends MobGunRangedAttackGoal<T> {

    private static final int TAP_FIRE_INTERVAL_TICKS = 5;

    private static final float STRAFE_OUTER_RING_ATTACK_RADIUS_SQ_FRACTION = 0.5F;
    private static final float STRAFE_INNER_RING_ATTACK_RADIUS_SQ_FRACTION = 0.15F;

    /** 与传给 {@code super(...)} 的 mob 同一引用，用于 tick（不可访问父类 private {@code mob}）。 */
    private final T self;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final double meleeDistSqThreshold;

    private static final int SEE_TIME_MAX = 1000;
    private int attackTime;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public MobGunRangedAttackByRefugeeGoal(
            T mob, double speedModifier, float attackRadius, float meleeLinearFracOfRadius) {
        super(mob, speedModifier, TAP_FIRE_INTERVAL_TICKS, attackRadius, meleeLinearFracOfRadius);
        this.self = mob;
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadius * attackRadius;
        float f = Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        double meleeR = attackRadius * f;
        this.meleeDistSqThreshold = meleeR * meleeR;
    }

    public void setMinAttackInterval(int attackCooldown) {
        super.setMinAttackInterval(TAP_FIRE_INTERVAL_TICKS);
    }

    @Override
    public void start() {
        super.start();
        this.attackTime = TAP_FIRE_INTERVAL_TICKS / 2;
        this.seeTime = 0;
        this.strafingTime = -1;
    }

    @Override
    public void stop() {
        super.stop();
        this.seeTime = 0;
        this.attackTime = 0;
        this.strafingTime = -1;
    }

    @Override
    public void tick() {
        LivingEntity target = this.self.getTarget();
        if (target == null) {
            return;
        }
        this.self.setSprinting(false);
        double distSqr = this.self.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.self.getSensing().hasLineOfSight(target);
        boolean hadSeeTime = this.seeTime > 0;
        if (hasLineOfSight != hadSeeTime) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }
        if (!(distSqr > (double) this.attackRadiusSqr) && this.seeTime >= 20) {
            this.self.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.self.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }
        if (this.strafingTime >= 20) {
            if ((double) this.self.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if ((double) this.self.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }
        if (this.strafingTime > -1) {
            if (distSqr > (double) (this.attackRadiusSqr * STRAFE_OUTER_RING_ATTACK_RADIUS_SQ_FRACTION)) {
                this.strafingBackwards = false;
            } else if (distSqr < (double) (this.attackRadiusSqr * STRAFE_INNER_RING_ATTACK_RADIUS_SQ_FRACTION)) {
                this.strafingBackwards = true;
            }
            this.self.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
        }
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.self, target);
        if (!this.self.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.self);
            boolean inAttackRing = hasLineOfSight && distSqr <= (double) this.attackRadiusSqr;
            boolean meleeBand = inAttackRing && distSqr <= this.meleeDistSqThreshold;
            boolean midLongBand = inAttackRing && distSqr > this.meleeDistSqThreshold;
            if (midLongBand) {
                op.aim(true);
            } else {
                op.aim(false);
            }
            if (this.attackTime > 0) {
                --this.attackTime;
            }
            boolean taczReady = op.getSynDrawCoolDown() == 0L && !op.getSynIsBolting();
            boolean meleeShotOk =
                    meleeBand && this.attackTime == 0 && taczReady && this.seeTime >= 1;
            boolean midLongShotOk =
                    midLongBand && this.attackTime == 0 && taczReady && this.seeTime >= 5;
            if (meleeShotOk || midLongShotOk) {
                this.self.performRangedAttack(target, 1.0F);
                this.attackTime = TAP_FIRE_INTERVAL_TICKS;
            }
        }
    }
}
