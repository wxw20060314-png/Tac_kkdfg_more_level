package com.kkdfg.Tacz_morelevel.blackmarket;

public enum BlackMarketFeedback {
    NONE,
    REFRESH_SUCCESS,
    REFRESH_FAIL,
    TRADE_SUCCESS,
    TRADE_FAIL;

    public static BlackMarketFeedback fromId(int id) {
        BlackMarketFeedback[] values = values();
        if (id < 0 || id >= values.length) {
            return NONE;
        }
        return values[id];
    }

    public int id() {
        return this.ordinal();
    }
}
