package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import com.tacz.guns.api.entity.IGunOperator;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * 炸弹宝宝榴弹射击（仅 LOOK）：弹道仰角 + {@code performRangedAttack}。
 * 通视切枪由 {@link BombBabyGrenadeWeaponSwapGoal}；本 Goal 不卡看见，
 * 进入前须水平距 ≤ {@link BombBabyBallistics#DIRECT_AIM_RANGE} 或圆族能解出仰角。
 */
public final class BombBabyGrenadeFireGoal extends Goal {

    private final BombBabyEntity mob;
    private boolean hasHeldAim;
    private float heldYawDeg;
    private float heldPitchElevationDeg;

    public BombBabyGrenadeFireGoal(BombBabyEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !this.mob.isHoldingGrenadeLauncher()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return this.hasFireSolution(target);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.hasHeldAim = false;
        this.mob.setAggressive(true);
        if (!this.mob.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
            op.initialData();
            op.draw(this.mob::getMainHandItem);
        }
    }

    @Override
    public void stop() {
        this.hasHeldAim = false;
        this.mob.setAggressive(false);
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
        if (target == null || !target.isAlive()) {
            this.hasHeldAim = false;
            return;
        }
        this.mob.setSprinting(false);
        if (!this.mob.isHoldingGrenadeLauncher()) {
            this.hasHeldAim = false;
            return;
        }
        // 朝向不依赖抽枪 CD；解失败则本 tick 不硬锁仰角
        this.solveAim(target);
        this.lookAtBallisticAim(target);

        IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
        op.aim(false);
        boolean taczReady = op.getSynDrawCoolDown() == 0L && !op.getSynIsBolting();
        if (!taczReady || !this.hasHeldAim) {
            return;
        }
        this.applyHeldAim();
        this.mob.performRangedAttack(target, 1.0F);
    }

    /** 近距直瞄带，或圆族能解出仰角。 */
    private boolean hasFireSolution(LivingEntity target) {
        double range = BombBabyBallistics.horizontalRange(this.mob, target);
        if (range <= BombBabyBallistics.DIRECT_AIM_RANGE) {
            return true;
        }
        return BombBabyBallistics.solveAim(
                        this.mob, target, BombBabyEntity.BACK_GUN, this.mob.getRandom())
                .isPresent();
    }

    private void lookAtBallisticAim(LivingEntity target) {
        Vec3 eye = this.mob.getEyePosition(1.0F);
        Vec3 aim = BombBabyBallistics.aimPoint(target);
        double dx = aim.x - eye.x;
        double dz = aim.z - eye.z;
        double range = Math.sqrt(dx * dx + dz * dz);
        double lookY = aim.y;
        if (this.hasHeldAim && range > 0.05D) {
            lookY = eye.y + range * Math.tan(Math.toRadians(this.heldPitchElevationDeg));
        }
        this.mob.getLookControl().setLookAt(aim.x, lookY, aim.z, 30.0F, 85.0F);
    }

    private boolean solveAim(LivingEntity target) {
        Optional<BombBabyBallistics.AimAngles> aim =
                BombBabyBallistics.solveAim(
                        this.mob, target, BombBabyEntity.BACK_GUN, this.mob.getRandom());
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
