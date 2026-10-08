package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.MysteriousMerchantEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

/** 交易时看向玩家，逻辑同原版 {@link net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal}。 */
public final class MysteriousMerchantLookAtTradingPlayerGoal extends LookAtPlayerGoal {

    private final MysteriousMerchantEntity merchant;

    public MysteriousMerchantLookAtTradingPlayerGoal(MysteriousMerchantEntity merchant) {
        super(merchant, Player.class, 8.0F);
        this.merchant = merchant;
    }

    @Override
    public boolean canUse() {
        if (this.merchant.isTrading()) {
            this.lookAt = this.merchant.getTradingPlayer();
            return this.lookAt != null;
        }
        return false;
    }
}
