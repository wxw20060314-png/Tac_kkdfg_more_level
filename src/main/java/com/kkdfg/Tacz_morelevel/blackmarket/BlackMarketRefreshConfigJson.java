package com.kkdfg.Tacz_morelevel.blackmarket;

import com.google.gson.annotations.SerializedName;
import net.minecraft.world.item.ItemStack;

public final class BlackMarketRefreshConfigJson {

    @SerializedName("refresh_interval_minutes")
    private int refreshIntervalMinutes = 10;

    /** 旧字段，仅作 {@link #manual_refresh_cost} 缺失时的回退。 */
    @SerializedName("cost")
    private BlackMarketItemStackJson legacyCost;

    @SerializedName("manual_refresh_cost")
    private BlackMarketManualRefreshCostJson manualRefreshCost;

    public int refreshIntervalMinutes() {
        return Math.max(1, this.refreshIntervalMinutes);
    }

    public ItemStack manualRefreshCost(int timesAlreadyPaid) {
        if (this.manualRefreshCost != null) {
            return this.manualRefreshCost.costForPaidCount(timesAlreadyPaid);
        }
        if (this.legacyCost != null) {
            return this.legacyCost.toStack();
        }
        return new BlackMarketManualRefreshCostJson().costForPaidCount(timesAlreadyPaid);
    }
}
