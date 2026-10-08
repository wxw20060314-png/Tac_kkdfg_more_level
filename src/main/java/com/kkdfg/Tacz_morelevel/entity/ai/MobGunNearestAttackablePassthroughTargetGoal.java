package com.kkdfg.Tacz_morelevel.entity.ai;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 结构对齐 {@link net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal}：
 * 相同构造函数签名与 {@link #findTarget()} / {@link #start()} 流程，
 * 但将原版 {@link net.minecraft.world.entity.ai.sensing.Sensing#hasLineOfSight} 与
 * {@link TargetingConditions} 内的视线判定替换为 {@link SoldierGunPassthroughSight}（可穿过玻璃、栅栏等）。
 */
public class MobGunNearestAttackablePassthroughTargetGoal<T extends LivingEntity> extends TargetGoal {

    protected final Class<T> targetType;
    protected final int randomInterval;
    protected LivingEntity target;
    protected final TargetingConditions targetConditions;

    /** 与 {@link TargetGoal} 中 unseen 记忆类似，在 {@link #mustSee} 为真时用穿透视线累计“看不见”的 tick。 */
    private int unseenPassthroughTicks;

    public MobGunNearestAttackablePassthroughTargetGoal(Mob mob, Class<T> targetType, boolean mustSee) {
        this(mob, targetType, 10, mustSee, false, null);
    }

    public MobGunNearestAttackablePassthroughTargetGoal(
            Mob mob, Class<T> targetType, boolean mustSee, @Nullable Predicate<LivingEntity> predicate) {
        this(mob, targetType, 10, mustSee, false, predicate);
    }

    public MobGunNearestAttackablePassthroughTargetGoal(Mob mob, Class<T> targetType, boolean mustSee, boolean mustReach) {
        this(mob, targetType, 10, mustSee, mustReach, null);
    }

    public MobGunNearestAttackablePassthroughTargetGoal(
            Mob mob,
            Class<T> targetType,
            int randomInterval,
            boolean mustSee,
            boolean mustReach,
            @Nullable Predicate<LivingEntity> predicate) {
        super(mob, mustSee, mustReach);
        this.targetType = targetType;
        this.randomInterval = Goal.reducedTickDelay(randomInterval);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat()
                .range(this.getFollowDistance())
                .ignoreLineOfSight()
                .selector(predicate);
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }
        this.findTarget();
        return this.target != null;
    }

    /**
     * 与 {@link net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal#getTargetSearchArea} 一致。
     */
    protected AABB getTargetSearchArea(double followRange) {
        return this.mob.getBoundingBox().inflate(followRange, 4.0D, followRange);
    }

    protected void findTarget() {
        Level level = this.mob.level();
        double follow = this.getFollowDistance();
        if (this.targetType == Player.class || this.targetType == ServerPlayer.class) {
            this.target = this.findNearestPassing(level, Player.class, follow);
        } else {
            this.target = this.findNearestPassing(level, this.targetType, follow);
        }
    }

    private <E extends LivingEntity> LivingEntity findNearestPassing(Level level, Class<E> clazz, double follow) {
        AABB area = this.getTargetSearchArea(follow);
        List<? extends E> list = level.getEntitiesOfClass(clazz, area, LivingEntity::isAlive);
        LivingEntity best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (E candidate : list) {
            if (candidate instanceof Player p && (p.isSpectator() || p.isCreative())) {
                continue;
            }
            if (!this.targetConditions.test(this.mob, candidate)) {
                continue;
            }
            if (!SoldierGunPassthroughSight.hasLineOfSight(this.mob, candidate)) {
                continue;
            }
            double d = this.mob.distanceToSqr(candidate);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = this.mob.getTarget();
        if (t == null) {
            t = this.targetMob;
        }
        if (t == null) {
            return false;
        }
        if (!this.mob.canAttack(t)) {
            return false;
        }
        if (this.mob.getTeam() != null && t.getTeam() == this.mob.getTeam()) {
            return false;
        }
        double follow = this.getFollowDistance();
        if (this.mob.distanceToSqr(t) > follow * follow) {
            return false;
        }
        if (this.mustSee) {
            if (SoldierGunPassthroughSight.hasLineOfSight(this.mob, t)) {
                this.unseenPassthroughTicks = 0;
            } else {
                this.unseenPassthroughTicks++;
                if (this.unseenPassthroughTicks > Goal.reducedTickDelay(this.unseenMemoryTicks)) {
                    return false;
                }
            }
        }
        this.mob.setTarget(t);
        return true;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
        this.unseenPassthroughTicks = 0;
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
    }
}
