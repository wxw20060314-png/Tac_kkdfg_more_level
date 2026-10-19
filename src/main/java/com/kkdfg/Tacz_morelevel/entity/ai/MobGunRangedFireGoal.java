package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 通用枪械射击 Goal：仅占 {@link Flag#LOOK}；走位由 {@link MobGunRangedMoveGoal} 等负责。
 * <p>节奏：默认近战腰射可连发、中远距点射；{@code tapFireRhythm} 时近战也走点射间隔（难民）。
 * <p>通视记忆默认 {@link SeeTimeTune#DEFAULT}（上限 40、断视 −4）；战争机器等用 {@link SeeTimeTune#LEGACY}。
 */
public class MobGunRangedFireGoal<T extends Mob & RangedAttackMob> extends Goal
        implements MobGunSoldierRangedGoal {

    /**
     * 射击通视累计参数。
     *
     * @param max 上限
     * @param decayPerMissTick 断视每 tick 递减
     * @param resetOnLosFlip 通视与「曾看见」翻转时是否清零
     */
    public record SeeTimeTune(int max, int decayPerMissTick, boolean resetOnLosFlip) {
        /** 士兵等：上限 40，断视 −4，无翻转清零。 */
        public static final SeeTimeTune DEFAULT = new SeeTimeTune(40, 4, false);
        /** 旧版：上限 1000，断视 −1，翻转清零（战争机器）。 */
        public static final SeeTimeTune LEGACY = new SeeTimeTune(1000, 1, true);
    }

    private final T mob;
    private final float attackRadiusSqr;
    private final double meleeDistSq;
    private int attackIntervalMin;
    private final int seeTimeToShoot;
    private final GunCombatSight sight;
    private final boolean tapFireRhythm;
    @Nullable private final Predicate<T> extraCanUse;
    private final SeeTimeTune seeTimeTune;

    private int attackTime;
    private int seeTime;

    public MobGunRangedFireGoal(
            T mob,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int attackIntervalMin,
            int seeTimeToShoot) {
        this(
                mob,
                attackRadius,
                meleeLinearFracOfRadius,
                attackIntervalMin,
                seeTimeToShoot,
                GunCombatSight.COMBAT,
                false,
                null);
    }

    public MobGunRangedFireGoal(
            T mob,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int attackIntervalMin,
            int seeTimeToShoot,
            GunCombatSight sight) {
        this(
                mob,
                attackRadius,
                meleeLinearFracOfRadius,
                attackIntervalMin,
                seeTimeToShoot,
                sight,
                false,
                null);
    }

    public MobGunRangedFireGoal(
            T mob,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int attackIntervalMin,
            int seeTimeToShoot,
            GunCombatSight sight,
            boolean tapFireRhythm) {
        this(
                mob,
                attackRadius,
                meleeLinearFracOfRadius,
                attackIntervalMin,
                seeTimeToShoot,
                sight,
                tapFireRhythm,
                null);
    }

    public MobGunRangedFireGoal(
            T mob,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int attackIntervalMin,
            int seeTimeToShoot,
            GunCombatSight sight,
            boolean tapFireRhythm,
            @Nullable Predicate<T> extraCanUse) {
        this(
                mob,
                attackRadius,
                meleeLinearFracOfRadius,
                attackIntervalMin,
                seeTimeToShoot,
                sight,
                tapFireRhythm,
                extraCanUse,
                SeeTimeTune.DEFAULT);
    }

    public MobGunRangedFireGoal(
            T mob,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int attackIntervalMin,
            int seeTimeToShoot,
            GunCombatSight sight,
            boolean tapFireRhythm,
            @Nullable Predicate<T> extraCanUse,
            SeeTimeTune seeTimeTune) {
        this.mob = mob;
        this.attackRadiusSqr = attackRadius * attackRadius;
        float f = Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        double meleeR = attackRadius * f;
        this.meleeDistSq = meleeR * meleeR;
        this.attackIntervalMin = attackIntervalMin;
        this.seeTimeToShoot = seeTimeToShoot;
        this.sight = sight != null ? sight : GunCombatSight.COMBAT;
        this.tapFireRhythm = tapFireRhythm;
        this.extraCanUse = extraCanUse;
        this.seeTimeTune = seeTimeTune != null ? seeTimeTune : SeeTimeTune.DEFAULT;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    protected T mob() {
        return this.mob;
    }

    protected GunCombatSight sight() {
        return this.sight;
    }

    protected boolean hasGunLineOfSight(LivingEntity target) {
        if (this.sight == GunCombatSight.BLAST && this.mob instanceof BombBabyEntity baby) {
            return baby.hasBlastGunSight();
        }
        if (this.sight == GunCombatSight.WAR_STRICT && this.mob instanceof WarMachineEntity warMachine) {
            return warMachine.hasStrictGunSight();
        }
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
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
        this.attackTime = this.attackIntervalMin / 2;
        this.seeTime = 0;
        if (!this.mob.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
            op.initialData();
            op.draw(this.mob::getMainHandItem);
        }
    }

    @Override
    public void stop() {
        this.seeTime = 0;
        this.attackTime = 0;
        this.mob.setAggressive(false);
        if (!this.mob.level().isClientSide()) {
            IGunOperator.fromLivingEntity(this.mob).aim(false);
        }
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
        if (this.seeTimeTune.resetOnLosFlip()) {
            boolean hadSeeTime = this.seeTime > 0;
            if (hasLineOfSight != hadSeeTime) {
                this.seeTime = 0;
            }
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, this.seeTimeTune.max());
        } else {
            this.seeTime = Math.max(this.seeTime - this.seeTimeTune.decayPerMissTick(), 0);
        }
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.mob, target);
        if (this.mob.level().isClientSide()) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
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
        if (this.tapFireRhythm) {
            if (this.attackTime > 0) {
                --this.attackTime;
            }
        } else if (meleeBand) {
            this.attackTime = 0;
        } else if (this.attackTime > 0) {
            --this.attackTime;
        }
        boolean taczReady = op.getSynDrawCoolDown() == 0L && !op.getSynIsBolting();
        if (this.tapFireRhythm) {
            boolean meleeShotOk = meleeBand && this.attackTime == 0 && taczReady && this.seeTime >= 1;
            boolean midLongShotOk =
                    midLongBand
                            && this.attackTime == 0
                            && taczReady
                            && this.seeTime >= this.seeTimeToShoot;
            if (meleeShotOk || midLongShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
                this.attackTime = this.attackIntervalMin;
            }
            return;
        }
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
