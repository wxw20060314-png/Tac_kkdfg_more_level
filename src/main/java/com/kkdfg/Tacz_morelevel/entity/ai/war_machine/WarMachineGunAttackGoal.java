package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.entity.ai.MobGunPlayerLikeAimHelper;
import com.kkdfg.Tacz_morelevel.entity.ai.MobGunSoldierRangedGoal;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractDualGunSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 战争机器统一射击：每 tick 一次 {@link WarMachineGunSight}，严格看见 10 tick 加特林，否则 RPG 看见则 RPG 直接射击。
 */
public final class WarMachineGunAttackGoal extends Goal implements MobGunSoldierRangedGoal {

    public static final int STRICT_SEE_TICKS_FOR_MINIGUN = 3;
    public static final int NO_STRICT_SEE_TICKS_FOR_RPG = 10;
    public static final int RPG_SEE_TICKS_TO_SHOOT = 3;
    private static final int SWAP_COOLDOWN_TICKS = 4;

    private final WarMachineEntity mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private final double meleeDistSqThreshold;
    private final int seeTimeToStopMove;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;
    private final int minigunSeeTimeToShoot;

    private int attackTime;
    private int strictSeeTime;
    private int noStrictSeeTime;
    private int rpgSeeTime;
    private boolean lastStrictSee;
    private boolean lastCombatSee;
    private int strafingTime = -1;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int swapCooldown;

    public WarMachineGunAttackGoal(
            WarMachineEntity mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int minigunSeeTimeToShoot) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.strafeOuterRingAttackRadiusSqFraction = strafeOuterRingAttackRadiusSqFraction;
        this.strafeInnerRingAttackRadiusSqFraction = strafeInnerRingAttackRadiusSqFraction;
        this.minigunSeeTimeToShoot = minigunSeeTimeToShoot;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        float f = Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        double meleeR = attackRadius * f;
        this.meleeDistSqThreshold = meleeR * meleeR;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return !this.mob.isPowerMechFlying()
                && target != null
                && target.isAlive()
                && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isPowerMechFlying()
                && (this.canUse() || !this.mob.getNavigation().isDone())
                && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
        this.attackTime = this.attackIntervalMin / 2;
        this.strictSeeTime = 0;
        this.noStrictSeeTime = 0;
        this.rpgSeeTime = 0;
        this.lastStrictSee = false;
        this.lastCombatSee = false;
        this.strafingTime = -1;
        this.swapCooldown = 0;
        if (!this.mob.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
            op.initialData();
            op.draw(this.mob::getMainHandItem);
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.attackTime = 0;
        this.strictSeeTime = 0;
        this.noStrictSeeTime = 0;
        this.rpgSeeTime = 0;
        this.strafingTime = -1;
        this.lastStrictSee = false;
        this.lastCombatSee = false;
        if (!this.mob.level().isClientSide()) {
            IGunOperator.fromLivingEntity(this.mob).aim(false);
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
        if (this.swapCooldown > 0) {
            this.swapCooldown--;
        }

        this.mob.setSprinting(false);
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

        WarMachineGunSight.Result sight = WarMachineGunSight.evaluate(this.mob, target);
        boolean strictSee = sight.strictLineOfSight();
        boolean rpgSee = sight.rpgLineOfSight();
        boolean combatSee = strictSee || rpgSee;

        if (strictSee != this.lastStrictSee) {
            this.strictSeeTime = 0;
            this.noStrictSeeTime = 0;
        } else if (strictSee) {
            this.strictSeeTime++;
        } else {
            this.noStrictSeeTime++;
        }
        this.lastStrictSee = strictSee;

        if (combatSee != this.lastCombatSee) {
            this.rpgSeeTime = 0;
        }
        if (combatSee) {
            this.rpgSeeTime++;
        }
        this.lastCombatSee = combatSee;

        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.mob, target);

        boolean wantMinigun = strictSee && this.strictSeeTime >= STRICT_SEE_TICKS_FOR_MINIGUN;
        boolean wantRpg =
                rpgSee
                        && !wantMinigun
                        && this.noStrictSeeTime >= NO_STRICT_SEE_TICKS_FOR_RPG;
        if (!this.mob.level().isClientSide()) {
            this.trySwapWeapon(wantMinigun, wantRpg);
        }

        boolean holdingMinigun = this.isMainHandGun(WarMachineEntity.MAIN_GUN);
        boolean holdingRpg = this.isMainHandGun(WarMachineEntity.BACK_GUN);

        int moveSeeTime = this.rpgSeeTime;
        if (!(distSqr > (double) this.attackRadiusSqr) && moveSeeTime >= this.seeTimeToStopMove) {
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
                            this.strafingBackwards ? -0.5F : 0.5F,
                            this.strafingClockwise ? 0.5F : -0.5F);
        }

        if (this.mob.level().isClientSide()) {
            return;
        }

        IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
        boolean inAttackRing = combatSee && distSqr <= (double) this.attackRadiusSqr;
        boolean meleeBand = inAttackRing && distSqr <= this.meleeDistSqThreshold;
        boolean midLongBand = inAttackRing && distSqr > this.meleeDistSqThreshold;

        if (holdingRpg && wantRpg) {
            op.aim(rpgSee && midLongBand);
        } else if (midLongBand) {
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
        if (holdingMinigun && wantMinigun) {
            boolean meleeShotOk = meleeBand && taczReady && this.strictSeeTime >= 1;
            boolean midLongShotOk =
                    midLongBand
                            && this.attackTime == 0
                            && taczReady
                            && strictSee
                            && this.strictSeeTime >= this.minigunSeeTimeToShoot;
            if (meleeShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
            } else if (midLongShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
                this.attackTime = this.attackIntervalMin;
            }
        } else if (holdingRpg && wantRpg) {
            boolean rpgShotOk =
                    inAttackRing
                            && taczReady
                            && rpgSee
                            && this.rpgSeeTime >= RPG_SEE_TICKS_TO_SHOOT
                            && this.attackTime == 0;
            if (rpgShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
                this.attackTime = this.attackIntervalMin;
            }
        }
    }

    private void trySwapWeapon(boolean wantMinigun, boolean wantRpg) {
        if (this.swapCooldown > 0) {
            return;
        }
        boolean holdingMinigun = this.isMainHandGun(WarMachineEntity.MAIN_GUN);
        boolean holdingRpg = this.isMainHandGun(WarMachineEntity.BACK_GUN);
        if (wantMinigun && !holdingMinigun && holdingRpg) {
            if (this.mob.swapActiveAndBackGun()) {
                this.swapCooldown = SWAP_COOLDOWN_TICKS;
            }
        } else if (wantRpg && !holdingRpg && holdingMinigun) {
            if (this.mob.swapActiveAndBackGun()) {
                this.swapCooldown = SWAP_COOLDOWN_TICKS;
            }
        }
    }

    private boolean isMainHandGun(ResourceLocation gunId) {
        ResourceLocation held = mainHandGunId(this.mob);
        return gunId.equals(held);
    }

    @Nullable
    private static ResourceLocation mainHandGunId(AbstractDualGunSoldierEntity mob) {
        IGun gun = IGun.getIGunOrNull(mob.getMainHandItem());
        return gun == null ? null : gun.getGunId(mob.getMainHandItem());
    }
}
