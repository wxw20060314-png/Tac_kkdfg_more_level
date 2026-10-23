package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadCapableMob;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadEventType;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SimThreadServices;
import com.kkdfg.Tacz_morelevel.entity.ai.simthread.SoldierLocomotionMode;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 向模拟线程希望排队提交 {@link SimThreadEventType#FIND_COVER}。
 * 计时归零后：非半血按 {@link SimThreadServices#queueSize} 抽签；半血跳过抽签。
 * 中签（或半血）且距离闸通过才 submit（UDP）；失败也重置计时。
 * 均值 {@link #MEAN_INTERVAL_TICKS}，归初带 ±{@link #INTERVAL_JITTER}。
 * <p>紧急度取自 {@link SimThreadCapableMob#getSimThreadUrgency()}；半血以下 ×1.5。
 * 默认构造不设距离闸；带 near/far 的构造写入票供 CPU 上机前再验。
 */
public class MobGunFindCoverRequestGoal<T extends Mob & SimThreadCapableMob> extends Goal {

    /** 均值请求间隔（tick）。 */
    public static final int MEAN_INTERVAL_TICKS = 20;
    /** 归初相对偏移（0.2 → 间隔 ∈ [0.8, 1.2]×均值）。 */
    public static final float INTERVAL_JITTER = 0.2F;

    private final T mob;
    private final int meanIntervalTicks;
    /** {@code <0} 不限近。 */
    private final float coverNear;
    /** {@code <0} 不限远。 */
    private final float coverFar;
    private int cooldown;

    public MobGunFindCoverRequestGoal(T mob) {
        this(mob, MEAN_INTERVAL_TICKS, -1.0F, -1.0F);
    }

    public MobGunFindCoverRequestGoal(T mob, float coverNear, float coverFar) {
        this(mob, MEAN_INTERVAL_TICKS, coverNear, coverFar);
    }

    public MobGunFindCoverRequestGoal(
            T mob, int meanIntervalTicks, float coverNear, float coverFar) {
        this.mob = mob;
        this.meanIntervalTicks = Math.max(1, meanIntervalTicks);
        this.coverNear = coverNear;
        this.coverFar = coverFar;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getLocomotionMode() != SoldierLocomotionMode.NORMAL) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.cooldown = this.nextIntervalTicks();
    }

    @Override
    public void tick() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return;
        }
        // 冷却先归初：抽签失败、距离闸失败均为 UDP，不打断循环
        this.cooldown = this.nextIntervalTicks();
        if (!(this.mob.level() instanceof ServerLevel server)) {
            return;
        }
        if (this.mob.getLocomotionMode() != SoldierLocomotionMode.NORMAL) {
            return;
        }
        boolean lowHp = this.mob.getHealth() < this.mob.getMaxHealth() * 0.5F;
        if (!lowHp) {
            double p = SimThreadServices.queueSize(server);
            if (this.mob.getRandom().nextDouble() >= p) {
                return;
            }
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (this.coverNear >= 0.0F && distSqr < (double) this.coverNear * (double) this.coverNear) {
            return;
        }
        if (this.coverFar >= 0.0F && distSqr > (double) this.coverFar * (double) this.coverFar) {
            return;
        }
        int base = Math.max(0, this.mob.getSimThreadUrgency());
        int urgency = lowHp ? Math.round(base * 1.5F) : base;
        this.mob.requestSimThread(
                server, SimThreadEventType.FIND_COVER, urgency, this.coverNear, this.coverFar);
    }

    /** {@code mean × (1 + U[-jitter,+jitter])}，至少 1。 */
    private int nextIntervalTicks() {
        float u = (this.mob.getRandom().nextFloat() * 2.0F - 1.0F) * INTERVAL_JITTER;
        float scaled = this.meanIntervalTicks * (1.0F + u);
        return Math.max(1, Mth.floor(scaled + 0.5F));
    }
}
