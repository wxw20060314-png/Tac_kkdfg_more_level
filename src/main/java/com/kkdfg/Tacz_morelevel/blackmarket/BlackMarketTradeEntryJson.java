package com.kkdfg.Tacz_morelevel.blackmarket;

import com.google.gson.annotations.SerializedName;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;

public final class BlackMarketTradeEntryJson {

    @SerializedName("input")
    private BlackMarketItemStackJson input;

    @SerializedName("output")
    private BlackMarketItemStackJson output;

    public BlackMarketTradeOffer toOffer() {
        BlackMarketItemStackJson in = this.input != null ? this.input : new BlackMarketItemStackJson();
        BlackMarketItemStackJson out = this.output != null ? this.output : new BlackMarketItemStackJson();
        return new BlackMarketTradeOffer(in.toStack(), out.toStack());
    }
}
