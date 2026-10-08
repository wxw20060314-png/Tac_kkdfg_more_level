package com.kkdfg.Tacz_morelevel.integration.jei;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketDataManager;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketItemStackJson;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketTradeEntryJson;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketTradePoolJson;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import java.io.InputStream;
import net.minecraft.world.item.ItemStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 从 {@code data/tacz_morelevel/black_market/buy_*.json}、{@code sell_*.json} 构建 JEI 配方列表。 */
public final class BlackMarketJeiRecipes {

    private static final Gson GSON = new GsonBuilder().create();

    private BlackMarketJeiRecipes() {}

    public static List<BlackMarketBuyRecipe> loadBuyRecipes() {
        BlackMarketDataManager.get().ensureFallbackLoaded();
        List<BlackMarketBuyRecipe> recipes = new ArrayList<>();
        for (int level = 1; level <= BlackMarketDataManager.MAX_MARKET_LEVEL; level++) {
            List<BlackMarketTradeEntryJson> entries = buyEntriesForLevel(level);
            int index = 0;
            for (BlackMarketTradeEntryJson entry : entries) {
                if (entry == null) {
                    continue;
                }
                BlackMarketTradeOffer offer = entry.toOffer();
                if (offer.input().isEmpty() || offer.resolvedOutput().isEmpty()) {
                    continue;
                }
                recipes.add(
                        new BlackMarketBuyRecipe(
                                offer.input(),
                                offer.resolvedOutput(),
                                level,
                                index++));
            }
        }
        return recipes;
    }

    public static List<BlackMarketSellRecipe> loadSellRecipes() {
        BlackMarketDataManager.get().ensureFallbackLoaded();
        List<BlackMarketSellRecipe> recipes = new ArrayList<>();
        for (int level = 1; level <= BlackMarketDataManager.MAX_MARKET_LEVEL; level++) {
            List<BlackMarketTradeEntryJson> entries = sellEntriesForLevel(level);
            int index = 0;
            for (BlackMarketTradeEntryJson entry : entries) {
                if (entry == null) {
                    continue;
                }
                BlackMarketTradeOffer offer = entry.toOffer();
                ItemStack input = BlackMarketItemStackJson.rematerializeGunStack(offer.input());
                if (input.isEmpty() || offer.output().isEmpty()) {
                    continue;
                }
                recipes.add(new BlackMarketSellRecipe(input, offer.output(), level, index++));
            }
        }
        return recipes;
    }

    private static List<BlackMarketTradeEntryJson> buyEntriesForLevel(int level) {
        List<BlackMarketTradeEntryJson> fromManager = BlackMarketDataManager.get().buyEntries(level);
        if (!fromManager.isEmpty()) {
            return fromManager;
        }
        BlackMarketTradePoolJson pool =
                readClasspathJson("buy_" + level + ".json", BlackMarketTradePoolJson.class);
        return pool != null ? pool.allEntries() : Collections.emptyList();
    }

    private static List<BlackMarketTradeEntryJson> sellEntriesForLevel(int level) {
        List<BlackMarketTradeEntryJson> fromManager = BlackMarketDataManager.get().sellEntries(level);
        if (!fromManager.isEmpty()) {
            return fromManager;
        }
        BlackMarketTradePoolJson pool =
                readClasspathJson("sell_" + level + ".json", BlackMarketTradePoolJson.class);
        return pool != null ? pool.allEntries() : Collections.emptyList();
    }

    private static <T> T readClasspathJson(String fileName, Class<T> type) {
        String path = "/data/tacz_morelevel/black_market/" + fileName;
        try (InputStream stream = BlackMarketJeiRecipes.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, type);
            }
        } catch (Exception ignored) {
            return null;
        }
    }
}
