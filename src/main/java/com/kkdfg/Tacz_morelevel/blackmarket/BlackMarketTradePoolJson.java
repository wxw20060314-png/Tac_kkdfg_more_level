package com.kkdfg.Tacz_morelevel.blackmarket;

import com.google.gson.annotations.SerializedName;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlackMarketTradePoolJson {

    @SerializedName("trades")
    private List<BlackMarketTradeEntryJson> trades = Collections.emptyList();

    public List<BlackMarketTradeEntryJson> allEntries() {
        if (this.trades == null || this.trades.isEmpty()) {
            return Collections.emptyList();
        }
        List<BlackMarketTradeEntryJson> entries = new ArrayList<>();
        for (BlackMarketTradeEntryJson entry : this.trades) {
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }
}
