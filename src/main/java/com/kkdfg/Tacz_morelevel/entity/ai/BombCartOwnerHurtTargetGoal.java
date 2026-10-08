package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/** 炸弹小车：将主人正在攻击的目标设为自己的 target（无主不启用）。 */
public class BombCartOwnerHurtTargetGoal extends TargetGoal {

    private final BombCartEntity cart;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public BombCartOwnerHurtTargetGoal(BombCartEntity cart) {
        super(cart, false);
        this.cart = cart;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.cart.getOwner();
        if (owner == null) {
            return false;
        }
        this.ownerLastHurt = owner.getLastHurtMob();
        int i = owner.getLastHurtMobTimestamp();
        return i != this.timestamp
                && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT)
                && this.ownerLastHurt != owner
                && !this.cart.isOwnedBy(this.ownerLastHurt);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity owner = this.cart.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
