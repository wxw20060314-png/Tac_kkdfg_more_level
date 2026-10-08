package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.api.CompanionMob;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class CompanionOwnerHurtByTargetGoal extends TargetGoal {
    private final CompanionMob companion;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public CompanionOwnerHurtByTargetGoal(CompanionMob companion) {
        super((net.minecraft.world.entity.Mob) companion, false);
        this.companion = companion;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.companion.isTame() || this.companion.isOrderedToSit()) {
            return false;
        }
        LivingEntity owner = this.companion.getOwner();
        if (owner == null) {
            return false;
        }
        this.ownerLastHurtBy = owner.getLastHurtByMob();
        int i = owner.getLastHurtByMobTimestamp();
        return i != this.timestamp
                && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT)
                && this.companion.wantsToAttack(this.ownerLastHurtBy, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.companion.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
