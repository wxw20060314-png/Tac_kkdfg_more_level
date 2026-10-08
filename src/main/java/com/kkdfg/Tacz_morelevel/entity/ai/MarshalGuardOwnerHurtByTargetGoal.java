package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.MarshalGuardEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** 大帅护卫军：主人被打时协助反击（独立实现，不引用 Companion*）。 */
public class MarshalGuardOwnerHurtByTargetGoal extends TargetGoal {
    private final MarshalGuardEntity guard;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public MarshalGuardOwnerHurtByTargetGoal(MarshalGuardEntity guard) {
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
        this.ownerLastHurtBy = owner.getLastHurtByMob();
        int i = owner.getLastHurtByMobTimestamp();
        return i != this.timestamp
                && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT)
                && this.guard.wantsToAttack(this.ownerLastHurtBy, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.guard.getMarshalOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
