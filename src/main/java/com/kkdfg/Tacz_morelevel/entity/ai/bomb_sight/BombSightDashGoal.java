package com.kkdfg.Tacz_morelevel.entity.ai.bomb_sight;

import com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill;
import com.kkdfg.Tacz_morelevel.entity.custom.BombSightEntity;
import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 炸弹视野 NPC 冲击波传说（与玩家 {@link ShockwaveDashSkill#tickCharge} 路径分离）：
 * <ul>
 *   <li>有 target 时转换动能（能量耗尽不自动放）</li>
 *   <li>抛物线在 x=x0 的 y1 落入目标头高带，或动能满 → 预发射（立刻冷却）→ 跳 → 1～3 tick 冲量</li>
 *   <li>抛物线估距仍瞄头部；释放冲量时瞄头脚中点；无 target：动能清 0、预发射复位</li>
 * </ul>
 */
public final class BombSightDashGoal extends Goal {

    private final BombSightEntity mob;
    private final BombSightDashPrefire prefire = new BombSightDashPrefire();

    public BombSightDashGoal(BombSightEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    public BombSightDashPrefire prefire() {
        return this.prefire;
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !ShockwaveDashSkill.hasFullSet(this.mob)) {
            return false;
        }
        if (this.prefire.isArmed()) {
            return true;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && !ShockwaveDashSkill.isOnCooldown(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.level().isClientSide() || !ShockwaveDashSkill.hasFullSet(this.mob)) {
            return false;
        }
        if (this.prefire.isArmed()) {
            return true;
        }
        if (ShockwaveDashSkill.isOnCooldown(this.mob)) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {
        this.abortChargeKeepCooldown();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.mob.level().isClientSide()) {
            return;
        }

        LivingEntity target = this.mob.getTarget();
        boolean hasTarget = target != null && target.isAlive();

        if (this.prefire.isArmed()) {
            this.tickPrefire(hasTarget ? target : null);
            return;
        }

        if (!hasTarget) {
            this.abortChargeKeepCooldown();
            return;
        }

        this.lookAtHead(target);

        if (ShockwaveDashSkill.isOnCooldown(this.mob)) {
            ShockwaveDashSkill.clearChargeSession(this.mob);
            return;
        }

        if (!ShockwaveBulletproofArmorItem.isCharging(ShockwaveDashSkill.chest(this.mob))) {
            ShockwaveDashSkill.beginCharge(this.mob);
        }

        ShockwaveDashSkill.tickConvertKineticNoAutoRelease(this.mob);

        int kinetic =
                ShockwaveBulletproofArmorItem.getKinetic(ShockwaveDashSkill.chest(this.mob));
        if (kinetic <= 0) {
            return;
        }

        boolean arcOk = BombSightDashPredictor.isTrajectoryFeasible(this.mob, target, kinetic);
        boolean maxKinetic =
                kinetic >= ShockwaveBulletproofArmorItem.maxKineticFromFullCharge();

        if (arcOk || maxKinetic) {
            this.armAndJump(target, kinetic);
        }
    }

    private void tickPrefire(LivingEntity target) {
        if (target == null) {
            this.prefire.reset();
            ShockwaveDashSkill.clearChargeSession(this.mob);
            return;
        }
        // 蓄力等待仍看头；真正释放瞬间瞄头脚中点（估距仍用头部）
        this.lookAtHead(target);
        if (this.prefire.tickShouldRelease()) {
            int k = this.prefire.kineticSnapshot();
            this.prefire.reset();
            this.aimReleaseAtBodyMid(target);
            ShockwaveDashSkill.applyImpulseFromKinetic(this.mob, k, false);
            this.mob.setTaczCrawling(true);
        }
    }

    private void armAndJump(LivingEntity target, int kinetic) {
        int delay = 1 + this.mob.getRandom().nextInt(3);
        this.prefire.arm(kinetic, delay);
        ShockwaveDashSkill.clearChargeSession(this.mob);
        ShockwaveDashSkill.startCooldown(this.mob);
        this.lookAtHead(target);
        this.applyYawPitchToward(target.getX(), target.getEyeY(), target.getZ());
        this.mob.getJumpControl().jump();
    }

    private void lookAtHead(LivingEntity target) {
        this.mob.getLookControl()
                .setLookAt(target.getX(), target.getEyeY(), target.getZ(), 30.0F, 30.0F);
    }

    /** 释放冲量：瞄准目标头与脚之间的中点。 */
    private void aimReleaseAtBodyMid(LivingEntity target) {
        double midY = (target.getY() + target.getEyeY()) * 0.5D;
        this.mob.getLookControl().setLookAt(target.getX(), midY, target.getZ(), 30.0F, 30.0F);
        this.applyYawPitchToward(target.getX(), midY, target.getZ());
    }

    private void applyYawPitchToward(double tx, double ty, double tz) {
        double dx = tx - this.mob.getX();
        double dy = ty - this.mob.getEyeY();
        double dz = tz - this.mob.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0E-6D && Math.abs(dy) < 1.0E-6D) {
            return;
        }
        float yaw = (float) (Math.atan2(-dx, dz) * (180.0D / Math.PI));
        float pitch = (float) (-(Math.atan2(dy, horiz) * (180.0D / Math.PI)));
        this.mob.setYRot(yaw);
        this.mob.setXRot(pitch);
        this.mob.yBodyRot = yaw;
        this.mob.yHeadRot = yaw;
    }

    private void abortChargeKeepCooldown() {
        this.prefire.reset();
        ShockwaveDashSkill.clearChargeSession(this.mob);
    }
}
