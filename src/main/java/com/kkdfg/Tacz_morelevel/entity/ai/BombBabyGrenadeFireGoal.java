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
 * 切枪由 {@link BombBabyGrenadeWeaponSwapGoal}，走位由 {@link BombBabyGrenadeMoveGoal}。
 */
public final class BombBabyGrenadeFireGoal extends Goal {

    private final BombBabyEntity mob;
    private boolean hasHeldAim;
    private float heldYawDeg;
    private float heldPitchElevationDeg;
    private Boolean preferHighArc;

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
        // 有爆破视线时应交应已切回 MK14；此处再挡一层
        return !BombBabySight.hasLineOfSight(this.mob, target);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.hasHeldAim = false;
        this.mob.setAggressive(true);
        if (this.preferHighArc == null) {
            this.preferHighArc = this.mob.getRandom().nextBoolean();
        }
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
        // applyHeldAim 写过绝对弹道仰角；推出后清掉，避免抬头卡住
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.mob, target);
        } else {
            this.mob.setXRot(0.0F);
        }
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
        this.lookAtBallisticAim(target);

        if (!this.mob.isHoldingGrenadeLauncher()) {
            this.hasHeldAim = false;
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
