package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.MarshalGuardEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** 大帅护卫军：反击伤害自身的敌人（不耦合护航 Companion*）。 */
public class MarshalGuardHurtByTargetGoal extends TargetGoal {
    private final MarshalGuardEntity guard;
    private LivingEntity lastHurtBy;
    private int timestamp;

    public MarshalGuardHurtByTargetGoal(MarshalGuardEntity guard) {
        super(guard, false);
        this.guard = guard;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        this.lastHurtBy = this.guard.getLastHurtByMob();
        int i = this.guard.getLastHurtByMobTimestamp();
        return i != this.timestamp
                && this.canAttack(this.lastHurtBy, TargetingConditions.DEFAULT)
                && this.guard.wantsToAttack(this.lastHurtBy, this.guard.getMarshalOwner());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.lastHurtBy);
        this.timestamp = this.guard.getLastHurtByMobTimestamp();
        super.start();
    }
}
