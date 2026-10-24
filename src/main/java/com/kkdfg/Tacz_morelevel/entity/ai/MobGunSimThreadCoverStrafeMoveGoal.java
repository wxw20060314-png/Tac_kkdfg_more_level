package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.ai.simthread.FindCoverInfo;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadCapableMob;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadInfo;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadRequester;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SoldierLocomotionMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * {@link MobGunCoverStrafeMoveGoal} + SimThread 掩体走位：NORMAL 时读邮箱进 COVER；
 * COVER 按标准 Goto：签发一次 {@code moveTo}，到达后 stop → 清接收器 → 回 NORMAL。
 * path 中断且未到达时可补发；不改父类 CoverStrafe。
 */
public class MobGunSimThreadCoverStrafeMoveGoal<T extends Mob & RangedAttackMob>
        extends MobGunCoverStrafeMoveGoal<T> {

    /** 水平到达阈值（格），对齐原版 {@code MoveToBlockGoal#acceptedDistance} 量级。 */
    private static final double ARRIVE_HORIZONTAL = 1.0D;
    private static final double ARRIVE_HORIZONTAL_SQ = ARRIVE_HORIZONTAL * ARRIVE_HORIZONTAL;

    private final double speedModifier;
    /** 本段 COVER 是否已签发过一次 {@code moveTo}（到达后清零）。 */
    private boolean coverMoveIssued;

    public MobGunSimThreadCoverStrafeMoveGoal(
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

    public MobGunSimThreadCoverStrafeMoveGoal(
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
        this.speedModifier = speedModifier;
    }

    @Override
    public void start() {
        super.start();
        this.coverMoveIssued = false;
    }

    @Override
    public void stop() {
        T mob = this.mob();
        if (mob instanceof SimThreadCapableMob capable
                && capable.getLocomotionMode() == SoldierLocomotionMode.COVER) {
            this.finishCoverMove(mob, capable);
        } else {
            this.coverMoveIssued = false;
        }
        super.stop();
    }

    @Override
    public void tick() {
        T mob = this.mob();
        if (mob instanceof SimThreadCapableMob capable) {
//            //test
//            if(capable.getLocomotionMode() == SoldierLocomotionMode.NORMAL)
//            {
//                System.out.println(1);
//            }
//            else if(capable.getLocomotionMode() == SoldierLocomotionMode.COVER)
//            {
//                System.out.println(2);
//            }
//            //test
            if (capable.getLocomotionMode() == SoldierLocomotionMode.NORMAL) {
                this.coverMoveIssued = false;
                SimThreadInfo info = capable.peekSimThreadInfo();
                if (info instanceof FindCoverInfo cover) {
                    capable.clearSimThreadInfo();
                    if (cover.coverPos() != null) {
                        capable.enterCoverMode(cover.coverPos(), cover.crawl());
                    }
                }
            }
            if (capable.getLocomotionMode() == SoldierLocomotionMode.COVER) {
                BlockPos pos = capable.getPendingCoverPos();
                if (pos == null) {
                    this.finishCoverMove(mob, capable);
                    return;
                }
                if (this.arrivedAtCover(mob, pos)) {
                    this.finishCoverMove(mob, capable);
                    return;
                }
                if (!this.coverMoveIssued) {
                    this.issueCoverMoveTo(mob, pos);
                } else if (mob.getNavigation().isDone()) {
                    this.issueCoverMoveTo(mob, pos);
                }
                return;
            }
        }
        super.tick();
    }

    private void issueCoverMoveTo(T mob, BlockPos pos) {
        mob.getNavigation().stop();
        mob.getNavigation()
                .moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, this.speedModifier);
        this.coverMoveIssued = true;
    }

    /**
     * 到达 / 收尾：nav.stop → 清签发标记 → 清接收器（+取消票）→ 切回 NORMAL。
     */
    private void finishCoverMove(T mob, SimThreadCapableMob capable) {
        mob.getNavigation().stop();
        this.coverMoveIssued = false;
        capable.clearSimThreadInfo();
        if (mob.level() instanceof ServerLevel server) {
            SimThreadRequester.cancel(server, mob.getUUID());
        }
        capable.exitCoverMode();
    }

    private boolean arrivedAtCover(T mob, BlockPos pos) {
        double dx = mob.getX() - (pos.getX() + 0.5D);
        double dz = mob.getZ() - (pos.getZ() + 0.5D);
        return dx * dx + dz * dz <= ARRIVE_HORIZONTAL_SQ;
    }
}
