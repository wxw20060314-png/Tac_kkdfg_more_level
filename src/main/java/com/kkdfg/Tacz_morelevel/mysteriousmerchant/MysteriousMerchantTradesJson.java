package com.kkdfg.Tacz_morelevel.mysteriousmerchant;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public final class MysteriousMerchantTradesJson {

    @SerializedName("common_roll_count")
    private int commonRollCount = 5;

    @SerializedName("rare_roll_count")
    private int rareRollCount = 1;

    @SerializedName("common")
    private List<MysteriousMerchantTradeEntryJson> common = Collections.emptyList();

    @SerializedName("rare")
    private List<MysteriousMerchantTradeEntryJson> rare = Collections.emptyList();

    public int commonRollCount() {
        return Math.max(0, this.commonRollCount);
    }

    public int rareRollCount() {
        return Math.max(0, this.rareRollCount);
    }

    public List<MysteriousMerchantTradeEntryJson> commonEntries() {
        return this.common != null ? this.common : Collections.emptyList();
    }

    public List<MysteriousMerchantTradeEntryJson> rareEntries() {
        return this.rare != null ? this.rare : Collections.emptyList();
    }
}
