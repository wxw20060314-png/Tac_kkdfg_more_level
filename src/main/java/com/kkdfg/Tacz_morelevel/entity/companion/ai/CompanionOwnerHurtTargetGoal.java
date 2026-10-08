package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.api.CompanionMob;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class CompanionOwnerHurtTargetGoal extends TargetGoal {
    private final CompanionMob companion;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public CompanionOwnerHurtTargetGoal(CompanionMob companion) {
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
        this.ownerLastHurt = owner.getLastHurtMob();
        int i = owner.getLastHurtMobTimestamp();
        return i != this.timestamp
                && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT)
                && this.companion.wantsToAttack(this.ownerLastHurt, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity owner = this.companion.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
