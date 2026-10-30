package com.kkdfg.Tacz_morelevel.entity.ai;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
public class SoldierHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private double followDistance=25;
    private boolean alertSameType;
    /**
     * Store the previous revengeTimer value
     */
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    @Nullable
    private Class<?>[] toIgnoreAlert;
    /** 额外招呼的友军类型（与同类一并喊人）；默认空。 */
    @SuppressWarnings("unchecked")
    private Class<? extends Mob>[] alertAllyTypes = new Class[0];

    public SoldierHurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
        super(pMob, true);
        this.toIgnoreDamage = pToIgnoreDamage;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public SoldierHurtByTargetGoal(PathfinderMob pMob, double pFollowDistance, Class<?>... pToIgnoreDamage) {
        super(pMob, true);
        this.toIgnoreDamage = pToIgnoreDamage;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.followDistance = pFollowDistance;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingentity = this.mob.getLastHurtByMob();
        if (i != this.timestamp && livingentity != null) {
            if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                for (Class<?> oclass : this.toIgnoreDamage) {
                    if (oclass.isAssignableFrom(livingentity.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(livingentity, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public SoldierHurtByTargetGoal setAlertOthers(Class<?>... pReinforcementTypes) {
        this.alertSameType = true;
        this.toIgnoreAlert = pReinforcementTypes;
        return this;
    }

    /**
     * 在同类集体仇恨之外，再招呼这些类型（如警察 ↔ 判罚者）。会顺带打开 {@link #alertSameType}。
     */
    @SafeVarargs
    public final SoldierHurtByTargetGoal setAlertAllyTypes(Class<? extends Mob>... allyTypes) {
        this.alertSameType = true;
        this.alertAllyTypes = allyTypes != null ? allyTypes : new Class[0];
        return this;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.start();
    }

    protected void alertOthers() {
        double d0 = this.followDistance;
        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, ALERT_RANGE_Y, d0);
        this.alertClass(this.mob.getClass(), aabb);
        for (Class<? extends Mob> ally : this.alertAllyTypes) {
            if (ally != null && ally != this.mob.getClass()) {
                this.alertClass(ally, aabb);
            }
        }
    }

    private void alertClass(Class<? extends Mob> type, AABB aabb) {
        List<? extends Mob> list =
                this.mob.level().getEntitiesOfClass(type, aabb, EntitySelector.NO_SPECTATORS);
        LivingEntity revenge = this.mob.getLastHurtByMob();
        for (Mob other : list) {
            if (!this.shouldAlert(other, revenge)) {
                continue;
            }
            this.alertOther(other, revenge);
        }
    }

    private boolean shouldAlert(Mob other, @Nullable LivingEntity revenge) {
        if (this.mob == other || other.getTarget() != null) {
            return false;
        }
        if (this.mob instanceof TamableAnimal selfTame) {
            if (!(other instanceof TamableAnimal otherTame)
                    || selfTame.getOwner() != otherTame.getOwner()) {
                return false;
            }
        }
        if (revenge != null && other.isAlliedTo(revenge)) {
            return false;
        }
        if (this.toIgnoreAlert == null) {
            return true;
        }
        for (Class<?> oclass : this.toIgnoreAlert) {
            if (other.getClass() == oclass) {
                return false;
            }
        }
        return true;
    }

    protected void alertOther(Mob pMob, LivingEntity pTarget) {
        pMob.setTarget(pTarget);
    }
}
//    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
//    private static final int ALERT_RANGE_Y = 10;
//    private boolean alertSameType;
//    private double followDistance=25;
//    /** Store the previous revengeTimer value */
//    private int timestamp;
//    private final Class<?>[] toIgnoreDamageWithoutotherTarget;
//    @Nullable
//    private Class<?>[] toIgnoreAlert;
//
//    public SoldierHurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
//        super(pMob, true);
//        this.toIgnoreDamageWithoutotherTarget = pToIgnoreDamage;
//        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
//    }
//    public SoldierHurtByTargetGoal(PathfinderMob pMob, double pFollowDistance,Class<?>... pToIgnoreDamage) {
//        super(pMob, true);
//        this.toIgnoreDamageWithoutotherTarget = pToIgnoreDamage;
//        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
//        this.followDistance=pFollowDistance;
//    }
//
//    /**
//     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
//     * method as well.
//     */
//    public boolean canUse() {
//        int i = this.mob.getLastHurtByMobTimestamp();
//        LivingEntity livingentity = this.mob.getLastHurtByMob();
//        if (i != this.timestamp && livingentity != null) {
//            if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
//                return false;
//            } else {
//                for(Class<?> oclass : this.toIgnoreDamageWithoutotherTarget) {
//                    if (oclass.isAssignableFrom(livingentity.getClass())&&mob.getTarget()!=null&&mob.getTarget().getClass()!=livingentity.getClass()) {
//
//                        return false;
//                    }
//                }
//
//                return this.canAttack(livingentity, HURT_BY_TARGETING);
//            }
//        } else {
//            return false;
//        }
//    }
//
//    public SoldierHurtByTargetGoal setAlertOthers(Class<?>... pReinforcementTypes) {
//        this.alertSameType = true;
//        this.toIgnoreAlert = pReinforcementTypes;
//        return this;
//    }
//
//    /**
//     * Execute a one shot task or start executing a continuous task
//     */
//    public void start() {
//        this.mob.setTarget(this.mob.getLastHurtByMob());
//        this.targetMob = this.mob.getTarget();
//        this.timestamp = this.mob.getLastHurtByMobTimestamp();
//        this.unseenMemoryTicks = 500;
//        if (this.alertSameType) {
//            this.alertOthers();
//        }
//
//        super.start();
//    }
//
//    protected void alertOthers() {
//        double d0 = this.followDistance;
//        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, ALERT_RANGE_Y, d0);
//        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), aabb, EntitySelector.NO_SPECTATORS);
//        Iterator iterator = list.iterator();
//
//        while(true) {
//            Mob mob;
//            while(true) {
//                if (!iterator.hasNext()) {
//                    return;
//                }
//
//                mob = (Mob)iterator.next();
//                // 留空(!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)mob).getOwner())
//                if (this.mob != mob && mob.getTarget() == null && !mob.isAlliedTo(this.mob.getLastHurtByMob())&&this.mob.getTarget()!=null&&mob.getClass()!=this.mob.getTarget().getClass()) {
//                    if (this.toIgnoreAlert == null) {
//                        break;
//                    }
//
//                    boolean flag = false;
//                    for(Class<?> oclass : this.toIgnoreAlert) {
//                        if (mob.getClass() == oclass) {
//                            flag = true;
//                            break;
//                        }
//                    }
//
//                    if (!flag) {
//                        break;
//                    }
//                }
//            }
//
//            this.alertOther(mob, this.mob.getLastHurtByMob());
//        }
//    }
//
//    protected void alertOther(Mob pMob, LivingEntity pTarget) {
//        pMob.setTarget(pTarget);
//    }

