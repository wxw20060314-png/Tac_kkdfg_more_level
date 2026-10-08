package com.kkdfg.Tacz_morelevel.mysteriousmerchant;

import com.google.gson.annotations.SerializedName;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketItemStackJson;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class MysteriousMerchantTradeEntryJson {

    @SerializedName("input")
    private BlackMarketItemStackJson input;

    @SerializedName("output")
    private BlackMarketItemStackJson output;

    @SerializedName("max_uses")
    private int maxUses = 12;

    public MerchantOffer toMerchantOffer() {
        BlackMarketItemStackJson in = this.input != null ? this.input : new BlackMarketItemStackJson();
        BlackMarketItemStackJson out = this.output != null ? this.output : new BlackMarketItemStackJson();
        ItemStack cost = in.toStack();
        ItemStack result = BlackMarketItemStackJson.rematerializeGunStack(out.toStack());
        if (cost.isEmpty() || result.isEmpty()) {
            return null;
        }
        int uses = Math.max(1, this.maxUses);
        return new MerchantOffer(cost, result, uses, 1, 0.05F);
    }
}
