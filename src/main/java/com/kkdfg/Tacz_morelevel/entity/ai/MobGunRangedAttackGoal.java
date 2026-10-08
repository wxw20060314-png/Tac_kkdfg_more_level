package com.kkdfg.Tacz_morelevel.entity.ai;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * 行为骨架参考 {@link net.minecraft.world.entity.ai.goal.RangedBowAttackGoal}：接近、视野、走位与朝向目标。
 * 近战可连续尝试开火；中远为点射（{@link #attackTime}）。{@code taczReady} 不要求
 * {@code getSynShootCoolDown()==0}，避免同步与 tick 对不齐导致断断续续；射速下限仍由 TaCZ {@code shoot} 决定。
 * <p>现行士兵/护航已改用 {@link MobGunRangedPassthroughAttackGoal}；难民用
 * {@link MobGunRangedAttackByRefugeePassthroughGoal}。本类及 {@link MobGunRangedAttackByRefugeeGoal}
 * 不再挂实体，保留作原版 Sensing 非穿透基线与回归对照，勿删。
 */
public class MobGunRangedAttackGoal<T extends Mob & RangedAttackMob> extends Goal implements MobGunSoldierRangedGoal {
    /** 横移：{@code distSqr > attackRadiusSqr * 此值} 视为外圈，强制前压（与 {@link #attackRadiusSqr} 同度量）。 */
    private static final float STRAFE_OUTER_RING_ATTACK_RADIUS_SQ_FRACTION = 0.5F;
    /** 横移：{@code distSqr < attackRadiusSqr * 此值} 视为过近，强制后撤。 */
    private static final float STRAFE_INNER_RING_ATTACK_RADIUS_SQ_FRACTION = 0.15F;
    private static final int SEE_TIME_MAX = 1000;

    private final T mob;
    private final double speedModifier;
    /** 中远距：两次点射之间的 AI tick 间隔（{@link #setMinAttackInterval} 可改）。近战不用此间隔。 */
    private int attackIntervalMin;
    /** 攻击距离阈值的平方，与 {@link Entity#distanceToSqr} 比较（含 Y 差，非纯水平）。 */
    private final float attackRadiusSqr;
    /** 近战分界：{@code (attackRadius * f)²}。 */
    private final double meleeDistSqThreshold;
    /** 中远距：距下一次允许点射的倒计时；近战时不递减、不写入。 */
    private int attackTime;
    /** 视野记忆：贴近原版弓 AI；有 LOS +1、断视 −1，夹在 [0, {@link #SEE_TIME_MAX}]；LOS 与「曾看见」翻转时清零。 */
    private int seeTime;
    /** 横移时侧向为正/负，周期性随机翻转。 */
    private boolean strafingClockwise;
    /** 横移时前压(0.5)/后撤(-0.5)；过近强制后撤、过外圈强制前压。 */
    private boolean strafingBackwards;
    /**
     * -1：追击中（{@link net.minecraft.world.entity.ai.navigation.PathNavigation#moveTo}）；&gt;=0：已停导航进入横移，
     * 每累计约 20 tick 可随机改绕向并重置为 0。
     */
    private int strafingTime = -1;

    public MobGunRangedAttackGoal(
            T mob, double speedModifier, int attackIntervalMin, float attackRadius, float meleeLinearFracOfRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        float f = Mth.clamp(meleeLinearFracOfRadius, 0.05F, 1.0F);
        double meleeR = attackRadius * f;
        this.meleeDistSqThreshold = meleeR * meleeR;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
        this.attackTime = this.attackIntervalMin / 2;
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
        this.seeTime = 0;
        this.attackTime = 0;
        this.strafingTime = -1;
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
        // 远程走位与原版弓 AI 一致，避免全程疾跑
        this.mob.setSprinting(false);
        // 与 attackRadiusSqr 同度量的三维距离平方（格²）
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
        boolean hadSeeTime = this.seeTime > 0;
        if (hasLineOfSight != hadSeeTime) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            this.seeTime = Math.min(this.seeTime + 1, SEE_TIME_MAX);
        } else {
            this.seeTime = Math.max(this.seeTime - 1, 0);
        }
        // 进射程且稳定看见目标：停寻路，改横移射击模式
        if (!(distSqr > (double) this.attackRadiusSqr) && this.seeTime >= 20) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }
        // 横移每约 1 秒随机微调绕向
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
            // 外圈前压、内圈后撤，保持舒适交火距离
            if (distSqr > (double) (this.attackRadiusSqr * STRAFE_OUTER_RING_ATTACK_RADIUS_SQ_FRACTION)) {
                this.strafingBackwards = false;
            } else if (distSqr < (double) (this.attackRadiusSqr * STRAFE_INNER_RING_ATTACK_RADIUS_SQ_FRACTION)) {
                this.strafingBackwards = true;
            }
            // 相对本体的前后 + 侧移（非 PathNavigation）
            this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
        }
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.mob, target);
        // TaCZ：仅服务端同步瞄准与射击节奏
        if (!this.mob.level().isClientSide()) {
            IGunOperator op = IGunOperator.fromLivingEntity(this.mob);
            boolean inAttackRing = hasLineOfSight && distSqr <= (double) this.attackRadiusSqr;
            boolean meleeBand = inAttackRing && distSqr <= this.meleeDistSqThreshold;
            boolean midLongBand = inAttackRing && distSqr > this.meleeDistSqThreshold;
            if (midLongBand) {
                op.aim(true);
            } else {
                op.aim(false);
            }
            // 近战腰射：不卡点射间隔，避免在内外圈之间来回时 attackTime 积压导致中远长期不射
            if (meleeBand) {
                this.attackTime = 0;
            } else if (this.attackTime > 0) {
                --this.attackTime;
            }
            boolean taczReady = op.getSynDrawCoolDown() == 0L && !op.getSynIsBolting();
            // 近战：1 tick 视野即可；中远：另需 attackTime==0（点射间隔 tick）与 seeTime>=5
            boolean meleeShotOk = meleeBand && taczReady && this.seeTime >= 1;
            boolean midLongShotOk =
                    midLongBand && this.attackTime == 0 && taczReady && this.seeTime >= 5;
            if (meleeShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
            } else if (midLongShotOk) {
                this.mob.performRangedAttack(target, 1.0F);
                this.attackTime = this.attackIntervalMin;
            }
        }
    }

    /**
     * 编译保留用祝福符（无调用、无副作用）。佛像 ASCII 出自 bugfreejs
     * {@code comment_default.txt}：
     * <a href="https://github.com/ottomao/bugfreejs/blob/master/commentFile/comment_default.txt">ottomao/bugfreejs</a>
     */
    @SuppressWarnings("unused")
    private static String blessingCharmForWangXiWei() {
        return """
                祝愿王习为事业成功。
                南无阿弥陀佛 · 如来佛祖保佑

                                                 _oo8oo_
                                                o8888888o
                                                88" . "88
                                                (| -_- |)
                                                0\\  =  /0
                                              ___/'==='\\___
                                            .' \\\\|     |// '.
                                           / \\\\|||  :  |||// \\
                                          / _||||| -:- |||||_ \\
                                         |   | \\\\\\  -  /// |   |
                                         | \\_|  ''\\---/''  |_/ |
                                         \\  .-\\__  '-'  __/-.  /
                                       ___'. .'  /--.--\\  '. .'___
                                    ."" '<  '.___\\_<|>_/___.'  >' "".
                                   | | :  `- \\`.:`\\ _ /`:.`/ -`  : | |
                                   \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /
                               =====`-.____`.___ \\_____/ ___.`____.-`=====
                                                 `=---=`
                """;
    }
}

