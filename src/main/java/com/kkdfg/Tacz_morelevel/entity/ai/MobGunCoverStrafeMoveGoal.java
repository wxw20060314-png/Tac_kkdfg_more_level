package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.ai.pathing.SoldierWalkNodeEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 只在从追击切进横移的那一拍、距离不超过近战半径与外圈半径的平均值、且冷却已好时扫一次九格并写下方向。
 * 之后每一拍都走基类原来的内外圈和 30% 翻面。10 tick 内再次切入不再扫。
 */
public class MobGunCoverStrafeMoveGoal<T extends Mob & RangedAttackMob>
        extends MobGunRangedPassthroughAttackGoal<T> {

    private static final int COVER_TIER = 2;
    private static final int RETARGET_COOLDOWN = 10;

    /** 近战半径与外圈半径的平均值，平方后和 {@code distSqr} 比。 */
    private final float scanRadiusSqr;
    private int coverCooldown;
    private boolean wasStrafing;

    public MobGunCoverStrafeMoveGoal(
            T mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int seeTimeToShoot) {
        this(
                mob,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                meleeLinearFracOfRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                seeTimeToShoot,
                GunCombatSight.COMBAT);
    }

    public MobGunCoverStrafeMoveGoal(
            T mob,
            double speedModifier,
            int attackIntervalMin,
            float attackRadius,
            float meleeLinearFracOfRadius,
            int seeTimeToStopMove,
            float strafeOuterRingAttackRadiusSqFraction,
            float strafeInnerRingAttackRadiusSqFraction,
            int seeTimeToShoot,
            GunCombatSight sight) {
        super(
                mob,
                speedModifier,
                attackIntervalMin,
                attackRadius,
                meleeLinearFracOfRadius,
                seeTimeToStopMove,
                strafeOuterRingAttackRadiusSqFraction,
                strafeInnerRingAttackRadiusSqFraction,
                seeTimeToShoot,
                sight);
        float frac = Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        float meleeRadius = attackRadius * frac;
        float outerRadius = attackRadius * Mth.sqrt(Math.max(0.0F, strafeOuterRingAttackRadiusSqFraction));
        float scanRadius = (meleeRadius + outerRadius) * 0.5F;
        this.scanRadiusSqr = scanRadius * scanRadius;
    }

    @Override
    public void start() {
        super.start();
        this.wasStrafing = false;
    }

    @Override
    public void stop() {
        super.stop();
        this.wasStrafing = false;
    }

    @Override
    public void tick() {
        if (this.coverCooldown > 0) {
            this.coverCooldown--;
        }
        super.tick();
        this.wasStrafing = this.isStrafing();
    }

    @Override
    protected void issueStrafe(LivingEntity target, double distSqr) {
        boolean prevMoveTo = !this.wasStrafing;
        boolean nowStrafe = this.isStrafing();
        if (prevMoveTo
                && nowStrafe
                && this.coverCooldown == 0
                && distSqr <= (double) this.scanRadiusSqr
                && this.mob().getMoveControl() instanceof SoldierMoveControl control) {
            this.retarget(control, target);
            this.coverCooldown = RETARGET_COOLDOWN;
        }
        super.issueStrafe(target, distSqr);
    }

    private void retarget(SoldierMoveControl control, LivingEntity target) {
        System.out.println("1");
        BlockPos feet = this.mob().blockPosition();
        int[] pointed = this.facingStep();
        if (pointed != null
                && this.canStrafeTo(control, feet.getX() + pointed[0], feet.getZ() + pointed[1])) {
            StrafeCellScore.Result pointedScore =
                    this.scoreAt(target, feet.getX() + pointed[0], feet.getY(), feet.getZ() + pointed[1]);
            if (pointedScore != null && pointedScore.tierRank() >= COVER_TIER) {
                return;
            }
        }

        StrafeCellScore.Result best = null;
        int bestDx = 0;
        int bestDz = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = feet.getX() + dx;
                int z = feet.getZ() + dz;
                if ((dx != 0 || dz != 0) && !this.canStrafeTo(control, x, z)) {
                    continue;
                }
                StrafeCellScore.Result score = this.scoreAt(target, x, feet.getY(), z);
                if (StrafeCellScore.compare(score, best) > 0) {
                    best = score;
                    bestDx = dx;
                    bestDz = dz;
                }
            }
        }
        if (best == null || (bestDx == 0 && bestDz == 0)) {
            return;
        }
        if (pointed != null && bestDx == pointed[0] && bestDz == pointed[1]) {
            return;
        }
        float[] axes = this.axesToward(bestDx, bestDz);
        if (axes == null) {
            return;
        }
        this.setStrafingBackwards(axes[0] < 0.0F);
        this.setStrafingClockwise(axes[1] > 0.0F);
    }

    private StrafeCellScore.Result scoreAt(LivingEntity target, int x, int y, int z) {
        return StrafeCellScore.score(this.mob(), target, x, y, z, this.crawlAt(x, y, z));
    }

    private boolean crawlAt(int x, int y, int z) {
        PathNavigation navigation = this.mob().getNavigation();
        if (navigation != null && navigation.getNodeEvaluator() instanceof SoldierWalkNodeEvaluator eval) {
            return eval.needsCrawlPoseAt(this.mob(), x, y, z);
        }
        return false;
    }

    private boolean canStrafeTo(SoldierMoveControl control, int x, int z) {
        float relativeX = (float) (x + 0.5D - this.mob().getX());
        float relativeZ = (float) (z + 0.5D - this.mob().getZ());
        return control.isWalkable(relativeX, relativeZ);
    }

    /** 当前前后、左右符号对应的相邻格。对向或侧向接近 0 时没有唯一格。 */
    private int[] facingStep() {
        float yaw = this.mob().getYRot() * ((float) Math.PI / 180F);
        float sin = Mth.sin(yaw);
        float cos = Mth.cos(yaw);
        float forward = this.strafingBackwards() ? -1.0F : 1.0F;
        float right = this.strafingClockwise() ? 1.0F : -1.0F;
        float worldX = forward * cos - right * sin;
        float worldZ = right * cos + forward * sin;
        int dx = (int) Math.signum(worldX);
        int dz = (int) Math.signum(worldZ);
        if (dx == 0 && dz == 0) {
            return null;
        }
        return new int[] {dx, dz};
    }

    /** 朝方块偏移的横移轴。分量过小则该轴为 0。两轴都为 0 时返回 null。 */
    private float[] axesToward(int dx, int dz) {
        float yaw = this.mob().getYRot() * ((float) Math.PI / 180F);
        float sin = Mth.sin(yaw);
        float cos = Mth.cos(yaw);
        float forwardRaw = dx * cos + dz * sin;
        float rightRaw = -dx * sin + dz * cos;
        float axis = this.strafeAxis();
        float forward = 0.0F;
        float right = 0.0F;
        if (forwardRaw > 1.0E-3F) {
            forward = axis;
        } else if (forwardRaw < -1.0E-3F) {
            forward = -axis;
        }
        if (rightRaw > 1.0E-3F) {
            right = axis;
        } else if (rightRaw < -1.0E-3F) {
            right = -axis;
        }
        if (forward == 0.0F && right == 0.0F) {
            return null;
        }
        return new float[] {forward, right};
    }
}
