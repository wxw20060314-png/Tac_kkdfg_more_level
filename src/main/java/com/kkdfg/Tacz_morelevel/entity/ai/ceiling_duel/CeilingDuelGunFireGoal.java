package com.kkdfg.Tacz_morelevel.entity.ai.ceiling_duel;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 对掏常驻射击：占 {@link Flag#LOOK}，不随 BASE/SKILL/BRIDGE 切换 stop。
 * 走位由各阶段 MOVE Goal 负责。
 */
public final class CeilingDuelGunFireGoal extends Goal {

    private final CeilingDuelEntity boss;
    private final float attackRadiusSqr;
    private final double meleeDistSq;
    private final int attackIntervalMin;
    private final int seeTimeToShoot;

    private static final int SEE_TIME_MAX = 1000;
    private int attackTime;
    private int seeTime;

    public CeilingDuelGunFireGoal(
            CeilingDuelEntity boss,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int attackIntervalMin,
            int seeTimeToShoot) {
        this.boss = boss;
        this.attackRadiusSqr = attackRadius * attackRadius;
        float f = Math.max(0.05F, Math.min(1.0F, meleeLinearFracOfRadius));
        double meleeR = attackRadius * f;
        this.meleeDistSq = meleeR * meleeR;
        this.attackIntervalMin = attackIntervalMin;
        this.seeTimeToShoot = seeTimeToShoot;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.boss.getTarget();
        return target != null && target.isAlive() && IGun.mainHandHoldGun(this.boss);
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
        this.boss.setAggressive(true);
        this.attackTime = this.attackIntervalMin / 2;
        this.seeTime = 0;
        // 不强制 draw：阶段切换时由本 Goal 常驻，避免反复拔枪冷却
    }

    @Override
    public void stop() {
        this.seeTime = 0;
        this.attackTime = 0;
        this.boss.setAggressive(false);
        if (!this.boss.level().isClientSide()) {
            IGunOperator.fromLivingEntity(this.boss).aim(false);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = this.boss.getTarget();
        if (target == null) {
            return;
        }
        this.boss.setSprinting(false);
        double distSqr = this.boss.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = MobGunPlayerLikeAimHelper.hasCombatLineOfSight(this.boss, target);
        boolean hadSeeTime = this.seeTime > 0;
        if (hasLineOfSight != hadSeeTime) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.boss, target);
        if (this.boss.level().isClientSide()) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this.boss);
        if (op.getSynReloadState().getStateType().isReloading()) {
            op.aim(false);
            return;
        }
        boolean inAttackRing = hasLineOfSight && distSqr <= (double) this.attackRadiusSqr;
        boolean meleeBand = inAttackRing && distSqr <= this.meleeDistSq;
        boolean midLongBand = inAttackRing && distSqr > this.meleeDistSq;
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
            this.boss.performRangedAttack(target, 1.0F);
        } else if (midLongShotOk) {
            this.boss.performRangedAttack(target, 1.0F);
            this.attackTime = this.attackIntervalMin;
        }
    }
}
