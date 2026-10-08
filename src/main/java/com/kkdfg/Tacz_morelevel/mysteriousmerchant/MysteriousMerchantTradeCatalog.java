package com.kkdfg.Tacz_morelevel.mysteriousmerchant;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 神秘商人交易池：{@code data/tacz_morelevel/mysterious_merchant/trades.json}。
 * 生成时按流浪商人逻辑从 common / rare 池随机抽取。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MysteriousMerchantTradeCatalog
        extends SimplePreparableReloadListener<MysteriousMerchantTradesJson> {

    private static final MysteriousMerchantTradeCatalog INSTANCE = new MysteriousMerchantTradeCatalog();
    private static final Gson GSON = new GsonBuilder().create();
    private static final ResourceLocation TRADES_LOCATION =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "mysterious_merchant/trades.json");

    private MysteriousMerchantTradesJson trades = new MysteriousMerchantTradesJson();
    private boolean fallbackLoaded;

    public static MysteriousMerchantTradeCatalog get() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static MerchantOffers rollOffers(RandomSource random) {
        INSTANCE.ensureFallbackLoaded();
        return INSTANCE.rollOffersInternal(random);
    }

    private void ensureFallbackLoaded() {
        if (fallbackLoaded && (!commonPool().isEmpty() || !rarePool().isEmpty())) {
            return;
        }
        if (fallbackLoaded) {
            return;
        }
        fallbackLoaded = true;
        MysteriousMerchantTradesJson fromClasspath = readClasspath();
        if (fromClasspath != null) {
            this.trades = fromClasspath;
        }
    }

    private MerchantOffers rollOffersInternal(RandomSource random) {
        MerchantOffers offers = new MerchantOffers();
        addRandomOffers(offers, commonPool(), trades.commonRollCount(), random);
        if (!rarePool().isEmpty() && trades.rareRollCount() > 0) {
            int index = random.nextInt(rarePool().size());
            MerchantOffer offer = rarePool().get(index).toMerchantOffer();
            if (offer != null) {
                offers.add(offer);
            }
        }
        return offers;
    }

    private static void addRandomOffers(
            MerchantOffers target, List<MysteriousMerchantTradeEntryJson> pool, int count, RandomSource random) {
        if (pool.isEmpty() || count <= 0) {
            return;
        }
        Set<Integer> picked = new HashSet<>();
        if (pool.size() > count) {
            while (picked.size() < count) {
                picked.add(random.nextInt(pool.size()));
            }
        } else {
            for (int i = 0; i < pool.size(); i++) {
                picked.add(i);
            }
        }
        for (int index : picked) {
            MerchantOffer offer = pool.get(index).toMerchantOffer();
            if (offer != null) {
                target.add(offer);
            }
        }
    }

    private List<MysteriousMerchantTradeEntryJson> commonPool() {
        return trades.commonEntries();
    }

    private List<MysteriousMerchantTradeEntryJson> rarePool() {
        return trades.rareEntries();
    }

    @Override
    protected MysteriousMerchantTradesJson prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        MysteriousMerchantTradesJson loaded = readFromManager(resourceManager);
        return loaded != null ? loaded : readClasspath();
    }

    @Override
    protected void apply(MysteriousMerchantTradesJson data, ResourceManager resourceManager, ProfilerFiller profiler) {
        if (data != null) {
            this.trades = data;
        }
        this.fallbackLoaded = true;
    }

    @Nullable
    private static MysteriousMerchantTradesJson readFromManager(ResourceManager resourceManager) {
        try {
            Resource resource = resourceManager.getResourceOrThrow(TRADES_LOCATION);
            try (InputStream stream = resource.open();
                    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, MysteriousMerchantTradesJson.class);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static MysteriousMerchantTradesJson readClasspath() {
        String path = "/data/tacz_morelevel/mysterious_merchant/trades.json";
        try (InputStream stream = MysteriousMerchantTradeCatalog.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, MysteriousMerchantTradesJson.class);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public static ImmutableList<MysteriousMerchantTradeEntryJson> debugCommonPool() {
        INSTANCE.ensureFallbackLoaded();
        return ImmutableList.copyOf(INSTANCE.commonPool());
    }
}
