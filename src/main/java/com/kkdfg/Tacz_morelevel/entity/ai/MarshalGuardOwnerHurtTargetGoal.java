package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.MarshalGuardEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** 大帅护卫军：主人出手时跟打同一目标（独立实现，不引用 Companion*）。 */
public class MarshalGuardOwnerHurtTargetGoal extends TargetGoal {
    private final MarshalGuardEntity guard;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public MarshalGuardOwnerHurtTargetGoal(MarshalGuardEntity guard) {
        super(guard, false);
        this.guard = guard;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.guard.getMarshalOwner();
        if (owner == null) {
            return false;
        }
        this.ownerLastHurt = owner.getLastHurtMob();
        int i = owner.getLastHurtMobTimestamp();
        return i != this.timestamp
                && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT)
                && this.guard.wantsToAttack(this.ownerLastHurt, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity owner = this.guard.getMarshalOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
