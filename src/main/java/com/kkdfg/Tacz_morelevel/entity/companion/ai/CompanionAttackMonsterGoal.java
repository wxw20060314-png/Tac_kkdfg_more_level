package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class CompanionAttackMonsterGoal extends NearestAttackableTargetGoal<Monster> {
    private final AbstractFriendEntity companion;

    public CompanionAttackMonsterGoal(AbstractFriendEntity companion) {
        super(companion, Monster.class, true);
        this.companion = companion;
    }

    @Override
    public boolean canUse() {
        return this.companion.isTame()
                && !this.companion.isOrderedToSit()
                && super.canUse();
    }
}
