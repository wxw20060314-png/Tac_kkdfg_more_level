package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import com.tacz.guns.api.entity.IGunOperator;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * 炸弹宝宝榴弹：{@link BombBabySight} 连续不通达 {@link #NO_SIGHT_TICKS} 后切 M320。
 * 走位抄普通枪 Goal 的 strafe；射击节奏与 {@link MobGunRangedPassthroughAttackGoal} 相同
 * （{@code initialData}+{@code draw}，仅 {@code taczReady} 时 {@code performRangedAttack}）。
 * 弹道解算只在准备开枪的那一刻定朝向；视线恢复或 Goal 结束时切回 MK14。
 */
public final class BombBabyGrenadeAttackGoal extends Goal {

    /** BombSight 连续不通达多少 tick 后启用榴弹。 */
    public static final int NO_SIGHT_TICKS = 20;

    private final BombBabyEntity mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final float strafeOuterRingAttackRadiusSqFraction;
    private final float strafeInnerRingAttackRadiusSqFraction;

    private int noSightTicks;
    private boolean hasHeldAim;
    private float heldYawDeg;
    private float heldPitchElevationDeg;
    private Boolean preferHighArc;

    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public BombBabyGrenadeAttackGoal(BombBabyEntity mob) {
        this.mob = mob;
        this.speedModifier = 1.2D;
        float attackRadius = Config.gunRadiusFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.strafeOuterRingAttackRadiusSqFraction =
                Config.gunStrafeOuterRingFracFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
        this.strafeInnerRingAttackRadiusSqFraction =
                Config.gunStrafeInnerRingFracFor(SoldierSpawnArmorProfile.SUPER_SPECIAL);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            this.noSightTicks = 0;
            return false;
        }
        boolean sight = BombBabySight.hasLineOfSight(this.mob, target);
        if (sight) {
            this.noSightTicks = 0;
            // 已握榴弹时继续本 Goal，便于射完/视线恢复后切回
            return this.mob.isHoldingGrenadeLauncher();
        }
        this.noSightTicks++;
        if (this.noSightTicks < NO_SIGHT_TICKS && !this.mob.isHoldingGrenadeLauncher()) {
            return false;
        }
        return this.mob.hasBackGun() || this.mob.isHoldingGrenadeLauncher();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.hasHeldAim = false;
        this.strafingTime = -1;
        this.mob.setAggressive(true);
        if (this.preferHighArc == null) {
            this.preferHighArc = this.mob.getRandom().nextBoolean();
        }
        if (!this.mob.level().isClientSide()) {
            this.mob.ensureGrenadeLauncherDrawn();
            IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
            op.initialData();
            op.draw(this.mob::getMainHandItem);
        }
    }

    @Override
    public void stop() {
        this.hasHeldAim = false;
        this.strafingTime = -1;
        this.mob.setAggressive(false);
        if (!this.mob.level().isClientSide()) {
            IGunOperator.fromLivingEntity(this.mob).aim(false);
            this.mob.ensurePrimaryGunDrawn();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            this.hasHeldAim = false;
            this.noSightTicks = 0;
            return;
        }

        this.mob.setSprinting(false);
        this.tickStrafe(target);

        if (BombBabySight.hasLineOfSight(this.mob, target)) {
            this.noSightTicks = 0;
            if (this.mob.isHoldingGrenadeLauncher()) {
                this.mob.ensurePrimaryGunDrawn();
            }
            return;
        }
        this.noSightTicks++;

        this.lookAtBallisticAim(target);

        if (!this.mob.isHoldingGrenadeLauncher()) {
            this.hasHeldAim = false;
            this.mob.ensureGrenadeLauncherDrawn();
            return;
        }

        IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
        op.aim(false);
        boolean taczReady = op.getSynDrawCoolDown() == 0L && !op.getSynIsBolting();
        if (!taczReady) {
            return;
        }

        if (!this.solveAim(target)) {
            return;
        }
        this.applyHeldAim();
        this.mob.performRangedAttack(target, 1.0F);
    }

    /** 与 {@link MobGunRangedPassthroughAttackGoal} 相同的距离环 strafe；不通视时也允许进环停走侧移。 */
    private void tickStrafe(LivingEntity target) {
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (distSqr <= (double) this.attackRadiusSqr) {
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
                    this.strafingBackwards ? -0.5F : 0.5F,
                    this.strafingClockwise ? 0.5F : -0.5F);
        }
    }

    /**
     * 用上次弹道仰角抬高瞄点（仅表现）；真正发射朝向在开火前 {@link #applyHeldAim}。
     */
    private void lookAtBallisticAim(LivingEntity target) {
        Vec3 eye = this.mob.getEyePosition(1.0F);
        Vec3 aim = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        double dx = aim.x - eye.x;
        double dz = aim.z - eye.z;
        double range = Math.sqrt(dx * dx + dz * dz);
        double lookY = aim.y;
        if (this.hasHeldAim && range > 0.05D) {
            lookY = eye.y + range * Math.tan(Math.toRadians(this.heldPitchElevationDeg));
        }
        this.mob.getLookControl().setLookAt(aim.x, lookY, aim.z, 30.0F, 85.0F);
    }

    /** @return 是否解出可用仰角 */
    private boolean solveAim(LivingEntity target) {
        if (this.mob.getRandom().nextFloat() < 0.15F) {
            this.preferHighArc = this.preferHighArc == null || !this.preferHighArc;
        }
        Optional<BombBabyBallistics.AimAngles> aim =
                BombBabyBallistics.solveAim(
                        this.mob,
                        target,
                        BombBabyEntity.BACK_GUN,
                        this.preferHighArc,
                        this.mob.getRandom());
        if (aim.isEmpty()) {
            this.hasHeldAim = false;
            return false;
        }
        BombBabyBallistics.AimAngles angles = aim.get();
        this.heldYawDeg = angles.yawDeg();
        this.heldPitchElevationDeg = angles.pitchElevationDeg();
        this.hasHeldAim = true;
        return true;
    }

    private void applyHeldAim() {
        float xRot = Mth.clamp(-this.heldPitchElevationDeg, -90.0F, 90.0F);
        this.mob.setYRot(this.heldYawDeg);
        this.mob.setYHeadRot(this.heldYawDeg);
        this.mob.setYBodyRot(this.heldYawDeg);
        this.mob.setXRot(xRot);
    }
}
