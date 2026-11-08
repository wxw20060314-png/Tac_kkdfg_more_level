package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/**
 * 骑马走位。只占 {@link Flag#MOVE}。
 * 距离和看见时间沿用 {@code MobGunRangedMoveGoal}：半径外或还没看见够久就寻路靠近，
 * 半径内看见够久就沿马头直线走。远战圈外夹角收到 {@link #FAR_HEADING_MAX} 内，两圈之间保持
 * {@link #MID_HEADING_MIN} 到 {@link #MID_HEADING_MAX}。进入近战圈后锁住当时的马头，沿这个方向冲。
 * 远近圈用配置里的 {@code gunRadius}、{@code gunStrafeOuterRingFrac}、{@code gunStrafeInnerRingFrac}，
 * 平方边界在这里算。
 */
public class HorseRidingMoveGoal extends Goal {

    private static final float FORWARD = 1.0F;
    /** 相对 moveTo 实际速度的比例。速度字段为该值乘马的最终移速。 */
    private static final double STRAFE_SPEED = 0.9D;
    private static final float FAR_HEADING_MAX = 10.0F;
    private static final float MID_HEADING_MIN = 10.0F;
    private static final float MID_HEADING_MAX = 20.0F;
    /** {@link #turnPerTick(double)} 里，剩余角度越大，这一拍转得越多。 */
    private static final double TURN_GAIN = 0.1D;
    /** 还没贴到边界时，每拍至少转这么多度。 */
    private static final double TURN_MIN = 0.5D;
    private static final int SEE_TIME_MAX = 1000;

    private final Mob mob;
    private final RidingPathSource paths;
    /** {@code gunRadius²}。 */
    private final double attackRadiusSqr;
    /** {@code gunRadius² × gunStrafeOuterRingFrac}。 */
    private final double farRingSqr;
    /** {@code gunRadius² × gunStrafeInnerRingFrac}。 */
    private final double nearRingSqr;
    private final int seeTimeToStopMove;
    private PausedHorseMoveControl pausedHorseControl;
    /** 已经进入近战圈并锁住冲锋方向。 */
    private boolean nearCharge;
    private float nearChargeYaw;
    private int seeTime;

    public HorseRidingMoveGoal(
            Mob mob, double gunRadius, double outerRingFrac, double innerRingFrac, int seeTimeToStopMove) {
        if (!(mob instanceof RidingPathSource paths)) {
            throw new IllegalArgumentException("horse riding move requires RidingPathSource");
        }
        this.mob = mob;
        this.paths = paths;
        double radiusSqr = gunRadius * gunRadius;
        this.attackRadiusSqr = radiusSqr;
        this.farRingSqr = radiusSqr * outerRingFrac;
        this.nearRingSqr = radiusSqr * innerRingFrac;
        this.seeTimeToStopMove = seeTimeToStopMove;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.canKeepRiding();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canKeepRiding();
    }

    private boolean canKeepRiding() {
        if (this.paths.ridingMoveControl() == null || !HorseRidingNpc.suppressesFootGoals(this.mob)) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        this.nearCharge = false;
        this.seeTime = 0;
        this.paths.ridingPathNavigation().stop();
        this.paths.stopFootNavigation();
        this.releaseHorseControl();
    }

    private void releaseHorseControl() {
        if (this.pausedHorseControl != null) {
            this.pausedHorseControl.restore();
            this.pausedHorseControl = null;
        }
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !(this.mob.getVehicle() instanceof AbstractHorse horse)) {
            return;
        }
        RidingMoveControl ridingMove = this.paths.ridingMoveControl();
        if (ridingMove == null) {
            return;
        }
        if (this.pausedHorseControl == null || !this.pausedHorseControl.isInstalledOn(horse)) {
            this.releaseHorseControl();
            this.pausedHorseControl = PausedHorseMoveControl.pause(horse);
        }
        horse.goalSelector.setControlFlag(Flag.MOVE, false);
        this.paths.stopFootNavigation();
        horse.getNavigation().stop();

        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = GunCombatSight.COMBAT.hasLineOfSight(this.mob, target);
        boolean hadSeeTime = this.seeTime > 0;
        if (hasLineOfSight != hadSeeTime) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }

        PathNavigation ridingNavigation = this.paths.ridingPathNavigation();
        if (!(distSqr > this.attackRadiusSqr) && this.seeTime >= this.seeTimeToStopMove+5) {
            ridingNavigation.stop();
            this.steerHorse(horse, target);
            ridingMove.strafe(FORWARD, 0.0F, STRAFE_SPEED);
            ridingMove.tick();
        } else {
            this.nearCharge = false;
            ridingNavigation.moveTo(target, playerMatchedMoveSpeed(horse));
            ridingNavigation.tick();
            ridingMove.tick();
        }
    }

    /**
     * 马的 {@code setSpeed(m × a)} 会把速度字段和前进输入写成同一个数。
     * {@code a ≤ 1} 时实际位移约 {@code m²a²}，取 {@code m = 1/√a} 后等于玩家满向前的 {@code a}。
     * {@code a > 1} 时输入被收成 1，实际位移约 {@code m×a}，取 {@code m = 1}。
     */
    private static double playerMatchedMoveSpeed(AbstractHorse horse) {
        double a = horse.getAttributeValue(Attributes.MOVEMENT_SPEED);
        if (!(a > 0.0D) || a > 1.0D) {
            return 1.0D;
        }
        return 1.0D / Math.sqrt(a);
    }

    /**
     * 这一拍最多转多少度。{@code n} 是离最近允许边界还差的度数。
     * {@code n} 大就转得快，快到边界就转得慢，并且不会超过 {@code n}。
     */
    public double turnPerTick(double n) {
        if (!(n > 0.0D)) {
            return 0.0D;
        }
        return 3.0D;
    }

    private void steerHorse(AbstractHorse horse, LivingEntity target) {
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (distSqr <= this.nearRingSqr) {
            if (!this.nearCharge) {
                this.nearCharge = true;
                this.nearChargeYaw = horse.getYRot();
            }
            horse.setYRot(this.nearChargeYaw);
            horse.setYHeadRot(this.nearChargeYaw);
            horse.yBodyRot = this.nearChargeYaw;
            return;
        }
        this.nearCharge = false;
        double dx = target.getX() - horse.getX();
        double dz = target.getZ() - horse.getZ();
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float signed = Mth.wrapDegrees(horse.getYRot() - targetYaw);
        float abs = Math.abs(signed);
        float minAbs;
        float maxAbs;
        if (distSqr > this.farRingSqr) {
            minAbs = 0.0F;
            maxAbs = FAR_HEADING_MAX;
        } else {
            minAbs = MID_HEADING_MIN;
            maxAbs = MID_HEADING_MAX;
        }
        if (abs >= minAbs && abs <= maxAbs) {
            return;
        }

        float desired;
        double n;
        if (abs > maxAbs) {
            desired = Math.copySign(maxAbs, signed);
            n = abs - maxAbs;
        } else {
            float toPositive = Math.abs(minAbs - signed);
            float toNegative = Math.abs(-minAbs - signed);
            if (toPositive < toNegative) {
                desired = minAbs;
                n = toPositive;
            } else {
                // 一样近时顺时针，也就是减小 yRot。
                desired = -minAbs;
                n = toNegative;
            }
        }

        float step = (float) this.turnPerTick(n);
        if (step > (float) n) {
            step = (float) n;
        }
        if (step < 0.0F) {
            step = 0.0F;
        }
        float delta = desired - signed;
        if (delta > step) {
            delta = step;
        } else if (delta < -step) {
            delta = -step;
        }
        float newYaw = Mth.wrapDegrees(horse.getYRot() + delta);
        horse.setYRot(newYaw);
        horse.setYHeadRot(newYaw);
        horse.yBodyRot = newYaw;
    }
}
