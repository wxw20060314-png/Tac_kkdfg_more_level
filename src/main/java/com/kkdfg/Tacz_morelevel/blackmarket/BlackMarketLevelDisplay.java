package com.kkdfg.Tacz_morelevel.blackmarket;

import net.minecraft.network.chat.Component;

/** 黑市联络系统显示名：一级～五级，颜色为白 / 绿 / 蓝 / 紫 / 红。 */
public final class BlackMarketLevelDisplay {

    private static final String[] TITLE_KEYS = {
        "black_market.tacz_morelevel.name.level_1",
        "black_market.tacz_morelevel.name.level_2",
        "black_market.tacz_morelevel.name.level_3",
        "black_market.tacz_morelevel.name.level_4",
        "black_market.tacz_morelevel.name.level_5"
    };

    private BlackMarketLevelDisplay() {}

    public static Component title(int marketLevel) {
        int index = clampLevel(marketLevel) - 1;
        return Component.translatable(TITLE_KEYS[index]);
    }

    public static int clampLevel(int marketLevel) {
        return Math.min(Math.max(marketLevel, 1), BlackMarketDataManager.MAX_MARKET_LEVEL);
    }
}
