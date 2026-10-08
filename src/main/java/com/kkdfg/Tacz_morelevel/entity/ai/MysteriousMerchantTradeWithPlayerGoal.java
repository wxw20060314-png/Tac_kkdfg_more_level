package com.kkdfg.Tacz_morelevel.entity.ai;

import com.kkdfg.Tacz_morelevel.entity.custom.MysteriousMerchantEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** 交易时站定，逻辑同原版 {@link net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal}。 */
public final class MysteriousMerchantTradeWithPlayerGoal extends Goal {

    private final MysteriousMerchantEntity merchant;

    public MysteriousMerchantTradeWithPlayerGoal(MysteriousMerchantEntity merchant) {
        this.merchant = merchant;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.merchant.isAlive() || this.merchant.isInWater() || !this.merchant.onGround() || this.merchant.hurtMarked) {
            return false;
        }
        Player player = this.merchant.getTradingPlayer();
        if (player == null || this.merchant.distanceToSqr(player) > 16.0D) {
            return false;
        }
        return player.containerMenu != null;
    }

    @Override
    public void start() {
        this.merchant.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.merchant.setTradingPlayer(null);
    }
}
