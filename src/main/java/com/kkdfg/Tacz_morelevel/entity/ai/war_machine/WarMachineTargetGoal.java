package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.entity.ai.SoldierHurtByTargetGoal;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 战争机器寻敌：选目标与维持仇恨均用 {@link WarMachineGunSight#hasRpgLineOfSight}（≤4 格实心挡仍算看见）。
 */
public final class WarMachineTargetGoal<T extends LivingEntity> extends TargetGoal {

    protected final Class<T> targetType;
    protected final int randomInterval;
    protected LivingEntity target;
    protected final TargetingConditions targetConditions;

    private int unseenRpgTicks;

    public WarMachineTargetGoal(Mob mob, Class<T> targetType) {
        this(mob, targetType, 10, null);
    }

    public WarMachineTargetGoal(
            Mob mob, Class<T> targetType, int randomInterval, @Nullable Predicate<LivingEntity> predicate) {
        super(mob, true, false);
        this.targetType = targetType;
        this.randomInterval = Goal.reducedTickDelay(randomInterval);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.targetConditions =
                TargetingConditions.forCombat()
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

    protected AABB getTargetSearchArea(double followRange) {
        return this.mob.getBoundingBox().inflate(followRange, 4.0D, followRange);
    }

    protected void findTarget() {
        Level level = this.mob.level();
        double follow = this.getFollowDistance();
        if (this.targetType == Player.class || this.targetType == ServerPlayer.class) {
            this.target = this.findNearestRpgSight(level, Player.class, follow);
        } else {
            this.target = this.findNearestRpgSight(level, this.targetType, follow);
        }
    }

    private <E extends LivingEntity> LivingEntity findNearestRpgSight(
            Level level, Class<E> clazz, double follow) {
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
            if (!WarMachineGunSight.hasRpgLineOfSight(this.mob, candidate)) {
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

    /** 当前目标读本拍缓存；其他人仍当场射。 */
    private static boolean hasRpgSightOnCurrentTarget(Mob mob, LivingEntity target) {
        if (target == mob.getTarget() && mob instanceof WarMachineEntity warMachine) {
            return warMachine.hasRpgGunSight();
        }
        return WarMachineGunSight.hasRpgLineOfSight(mob, target);
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
            if (hasRpgSightOnCurrentTarget(this.mob, t)) {
                this.unseenRpgTicks = 0;
            } else {
                this.unseenRpgTicks++;
                if (this.unseenRpgTicks > Goal.reducedTickDelay(this.unseenMemoryTicks)) {
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
        this.unseenRpgTicks = 0;
    }

    /** 受伤寻仇：维持 target 时同样用 RPG 看见，而非原版 {@code Sensing#hasLineOfSight}。 */
    public static final class HurtBy extends SoldierHurtByTargetGoal {

        private int unseenRpgTicks;

        public HurtBy(PathfinderMob mob, Class<?>... ignoreDamageFrom) {
            super(mob, ignoreDamageFrom);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity t = this.mob.getTarget();
            if (t == null) {
                t = this.targetMob;
            }
            if (t == null || !t.isAlive()) {
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
            if (WarMachineTargetGoal.hasRpgSightOnCurrentTarget(this.mob, t)) {
                this.unseenRpgTicks = 0;
            } else {
                this.unseenRpgTicks++;
                if (this.unseenRpgTicks > Goal.reducedTickDelay(this.unseenMemoryTicks)) {
                    return false;
                }
            }
            this.mob.setTarget(t);
            return true;
        }

        @Override
        public void start() {
            super.start();
            this.unseenRpgTicks = 0;
        }
    }
}
