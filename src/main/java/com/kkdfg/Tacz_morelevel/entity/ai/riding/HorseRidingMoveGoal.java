package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import com.kkdfg.Tacz_morelevel.entity.ai.GunCombatSight;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/**
 * 骑马走位。只占 {@link Flag#MOVE}。
 * 距离和看见时间沿用 {@code MobGunRangedMoveGoal}：半径外靠近，半径内横移。
 * 横移远圈向前，近圈向后。数字先用特种兵默认值。
 * 马下一拍才执行这些输入，所以这里每拍重写一次。
 */
public class HorseRidingMoveGoal extends Goal {

    private static final float STRAFE_AXIS = 0.5F;
    /** 特种兵 {@code gunRadius}。 */
    private static final float ATTACK_RADIUS = 60.0F;
    private static final float ATTACK_RADIUS_SQR = ATTACK_RADIUS * ATTACK_RADIUS;
    /** 特种兵 {@code seeTimeToStopMove}。 */
    private static final int SEE_TIME_TO_STOP_MOVE = 4;
    /** 特种兵外圈、内圈，都是射击半径平方的比例。 */
    private static final float STRAFE_OUTER_RING_SQR_FRAC = 0.4F;
    private static final float STRAFE_INNER_RING_SQR_FRAC = 0.15F;
    private static final int SEE_TIME_MAX = 1000;
    private static final int STRAFE_FLIP_TICKS = 20;

    private final Mob mob;
    private final RidingPathSource paths;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public HorseRidingMoveGoal(Mob mob) {
        if (!(mob instanceof RidingPathSource paths)) {
            throw new IllegalArgumentException("horse riding move requires RidingPathSource");
        }
        this.mob = mob;
        this.paths = paths;
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
        this.strafingTime = -1;
        this.seeTime = 0;
        this.paths.ridingPathNavigation().stop();
        this.paths.stopFootNavigation();
        if (this.mob.getVehicle() instanceof AbstractHorse horse) {
            horse.goalSelector.setControlFlag(Flag.MOVE, true);
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
        horse.goalSelector.setControlFlag(Flag.MOVE, false);
        this.paths.stopFootNavigation();

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

        horse.getNavigation().stop();
        PathNavigation ridingNavigation = this.paths.ridingPathNavigation();
        if (!(distSqr > (double) ATTACK_RADIUS_SQR) && this.seeTime >= SEE_TIME_TO_STOP_MOVE) {
            ridingNavigation.stop();
            ++this.strafingTime;
        } else {
            ridingNavigation.moveTo(target, playerMatchedMoveSpeed(horse));
            // 独立保存的骑乘导航需要显式推进，下一路径点交给骑乘控制器。
            ridingNavigation.tick();
            ridingMove.tick();
            this.strafingTime = -1;
        }
        if (this.strafingTime >= STRAFE_FLIP_TICKS) {
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }
        if (this.strafingTime > -1) {
            if (distSqr > (double) (ATTACK_RADIUS_SQR * STRAFE_OUTER_RING_SQR_FRAC)) {
                this.strafingBackwards = false;
            } else if (distSqr < (double) (ATTACK_RADIUS_SQR * STRAFE_INNER_RING_SQR_FRAC)) {
                this.strafingBackwards = true;
            }
            ridingMove.strafe(
                    this.strafingBackwards ? -STRAFE_AXIS : STRAFE_AXIS,
                    this.strafingClockwise ? STRAFE_AXIS : -STRAFE_AXIS);
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
}
