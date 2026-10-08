package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.api.CompanionMob;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** {@link CompanionMob#wantsToAttack} */
public class CompanionHurtByTargetGoal extends TargetGoal {
    private final CompanionMob companion;
    private LivingEntity lastHurtBy;
    private int timestamp;

    public CompanionHurtByTargetGoal(CompanionMob companion) {
        super((net.minecraft.world.entity.Mob) companion, false);
        this.companion = companion;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.companion.isTame()) {
            return false;
        }
        LivingEntity owner = this.companion.getOwner();
        if (owner == null) {
            return false;
        }
        LivingEntity self = (LivingEntity) this.companion;
        this.lastHurtBy = self.getLastHurtByMob();
        int i = self.getLastHurtByMobTimestamp();
        return i != this.timestamp
                && this.canAttack(this.lastHurtBy, TargetingConditions.DEFAULT)
                && this.companion.wantsToAttack(this.lastHurtBy, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.lastHurtBy);
        this.timestamp = ((LivingEntity) this.companion).getLastHurtByMobTimestamp();
        super.start();
    }
}
