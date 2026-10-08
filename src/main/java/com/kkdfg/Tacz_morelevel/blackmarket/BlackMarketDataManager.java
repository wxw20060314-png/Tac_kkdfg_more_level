package com.kkdfg.Tacz_morelevel.blackmarket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BlackMarketDataManager extends SimplePreparableReloadListener<BlackMarketDataManager.LoadedData> {

    public static final int BUY_OFFER_COUNT = 3;
    public static final int SELL_OFFER_COUNT = 2;
    public static final int TOTAL_OFFER_COUNT = BUY_OFFER_COUNT + SELL_OFFER_COUNT;
    public static final int MAX_MARKET_LEVEL = 5;

    private static final Gson GSON = new GsonBuilder().create();
    private static final BlackMarketDataManager INSTANCE = new BlackMarketDataManager();

    private final List<List<BlackMarketTradeEntryJson>> buyEntriesByLevel = emptyLevelLists();
    private final List<List<BlackMarketTradeEntryJson>> sellEntriesByLevel = emptyLevelLists();
    private BlackMarketRefreshConfigJson refreshConfig = new BlackMarketRefreshConfigJson();
    private boolean fallbackLoaded;

    public static BlackMarketDataManager get() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public void ensureFallbackLoaded() {
        if (hasAnyTradePool()) {
            return;
        }
        if (this.fallbackLoaded) {
            return;
        }
        this.fallbackLoaded = true;
        LoadedData data = loadFromClasspath();
        this.applyData(data);
    }

    public List<BlackMarketTradeEntryJson> buyEntries(int marketLevel) {
        return entriesForLevel(this.buyEntriesByLevel, marketLevel);
    }

    public List<BlackMarketTradeEntryJson> sellEntries(int marketLevel) {
        return entriesForLevel(this.sellEntriesByLevel, marketLevel);
    }

    public ItemStack manualRefreshCost(int timesAlreadyPaid) {
        return this.refreshConfig.manualRefreshCost(timesAlreadyPaid);
    }

    public long refreshIntervalTicks() {
        return this.refreshConfig.refreshIntervalMinutes() * 60L * 20L;
    }

    public List<BlackMarketTradeOffer> rollOffers(int marketLevel, RandomSource random) {
        if (marketLevel <= 0) {
            return emptyOffers();
        }
        int levelIndex = Math.min(marketLevel, MAX_MARKET_LEVEL) - 1;
        List<BlackMarketTradeEntryJson> buyPool = this.buyEntriesByLevel.get(levelIndex);
        List<BlackMarketTradeEntryJson> sellPool = this.sellEntriesByLevel.get(levelIndex);
        List<BlackMarketTradeOffer> offers = new ArrayList<>(TOTAL_OFFER_COUNT);
        offers.addAll(pickRandom(buyPool, BUY_OFFER_COUNT, random));
        offers.addAll(pickRandom(sellPool, SELL_OFFER_COUNT, random));
        while (offers.size() < TOTAL_OFFER_COUNT) {
            offers.add(new BlackMarketTradeOffer(ItemStack.EMPTY, ItemStack.EMPTY));
        }
        return offers;
    }

    private static List<BlackMarketTradeOffer> emptyOffers() {
        List<BlackMarketTradeOffer> offers = new ArrayList<>(TOTAL_OFFER_COUNT);
        for (int i = 0; i < TOTAL_OFFER_COUNT; i++) {
            offers.add(new BlackMarketTradeOffer(ItemStack.EMPTY, ItemStack.EMPTY));
        }
        return offers;
    }

    private static List<BlackMarketTradeOffer> pickRandom(
            List<BlackMarketTradeEntryJson> pool, int count, RandomSource random) {
        if (pool.isEmpty()) {
            return Collections.nCopies(count, new BlackMarketTradeOffer(ItemStack.EMPTY, ItemStack.EMPTY));
        }
        List<BlackMarketTradeEntryJson> copy = new ArrayList<>(pool);
        List<BlackMarketTradeOffer> picked = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (copy.isEmpty()) {
                picked.add(new BlackMarketTradeOffer(ItemStack.EMPTY, ItemStack.EMPTY));
                continue;
            }
            int index = random.nextInt(copy.size());
            picked.add(copy.remove(index).toOffer());
        }
        return picked;
    }

    private boolean hasAnyTradePool() {
        for (int i = 0; i < MAX_MARKET_LEVEL; i++) {
            if (!this.buyEntriesByLevel.get(i).isEmpty() || !this.sellEntriesByLevel.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static List<BlackMarketTradeEntryJson> entriesForLevel(
            List<List<BlackMarketTradeEntryJson>> byLevel, int marketLevel) {
        if (marketLevel <= 0 || marketLevel > MAX_MARKET_LEVEL) {
            return Collections.emptyList();
        }
        return byLevel.get(marketLevel - 1);
    }

    private static List<List<BlackMarketTradeEntryJson>> emptyLevelLists() {
        List<List<BlackMarketTradeEntryJson>> lists = new ArrayList<>(MAX_MARKET_LEVEL);
        for (int i = 0; i < MAX_MARKET_LEVEL; i++) {
            lists.add(Collections.emptyList());
        }
        return lists;
    }

    @Override
    protected LoadedData prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<List<BlackMarketTradeEntryJson>> buyByLevel = new ArrayList<>(MAX_MARKET_LEVEL);
        List<List<BlackMarketTradeEntryJson>> sellByLevel = new ArrayList<>(MAX_MARKET_LEVEL);
        for (int level = 1; level <= MAX_MARKET_LEVEL; level++) {
            buyByLevel.add(loadPool(resourceManager, poolFileName("buy", level)));
            sellByLevel.add(loadPool(resourceManager, poolFileName("sell", level)));
        }
        return new LoadedData(buyByLevel, sellByLevel, loadRefreshConfig(resourceManager));
    }

    @Override
    protected void apply(LoadedData data, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.applyData(data);
    }

    private void applyData(LoadedData data) {
        this.buyEntriesByLevel.clear();
        this.buyEntriesByLevel.addAll(data.buyEntriesByLevel());
        this.sellEntriesByLevel.clear();
        this.sellEntriesByLevel.addAll(data.sellEntriesByLevel());
        this.refreshConfig = data.refreshConfig();
    }

    private static LoadedData loadFromClasspath() {
        List<List<BlackMarketTradeEntryJson>> buyByLevel = new ArrayList<>(MAX_MARKET_LEVEL);
        List<List<BlackMarketTradeEntryJson>> sellByLevel = new ArrayList<>(MAX_MARKET_LEVEL);
        for (int level = 1; level <= MAX_MARKET_LEVEL; level++) {
            buyByLevel.add(loadPoolFromClasspath(poolFileName("buy", level)));
            sellByLevel.add(loadPoolFromClasspath(poolFileName("sell", level)));
        }
        return new LoadedData(buyByLevel, sellByLevel, loadRefreshFromClasspath());
    }

    private static String poolFileName(String kind, int level) {
        return kind + "_" + level + ".json";
    }

    private static List<BlackMarketTradeEntryJson> loadPool(ResourceManager resourceManager, String fileName) {
        BlackMarketTradePoolJson pool = readJson(resourceManager, fileName, BlackMarketTradePoolJson.class);
        return pool != null ? pool.allEntries() : Collections.emptyList();
    }

    private static List<BlackMarketTradeEntryJson> loadPoolFromClasspath(String fileName) {
        BlackMarketTradePoolJson pool = readClasspathJson(fileName, BlackMarketTradePoolJson.class);
        return pool != null ? pool.allEntries() : Collections.emptyList();
    }

    private static BlackMarketRefreshConfigJson loadRefreshConfig(ResourceManager resourceManager) {
        BlackMarketRefreshConfigJson config =
                readJson(resourceManager, "refresh.json", BlackMarketRefreshConfigJson.class);
        return config != null ? config : new BlackMarketRefreshConfigJson();
    }

    private static BlackMarketRefreshConfigJson loadRefreshFromClasspath() {
        BlackMarketRefreshConfigJson config =
                readClasspathJson("refresh.json", BlackMarketRefreshConfigJson.class);
        return config != null ? config : new BlackMarketRefreshConfigJson();
    }

    private static <T> T readJson(ResourceManager resourceManager, String fileName, Class<T> type) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "black_market/" + fileName);
        try {
            Resource resource = resourceManager.getResourceOrThrow(location);
            try (InputStream stream = resource.open();
                    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, type);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static <T> T readClasspathJson(String fileName, Class<T> type) {
        String path = "/data/tacz_morelevel/black_market/" + fileName;
        try (InputStream stream = BlackMarketDataManager.class.getResourceAsStream(path)) {
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

    record LoadedData(
            List<List<BlackMarketTradeEntryJson>> buyEntriesByLevel,
            List<List<BlackMarketTradeEntryJson>> sellEntriesByLevel,
            BlackMarketRefreshConfigJson refreshConfig) {}

}
