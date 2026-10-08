package com.kkdfg.Tacz_morelevel.signal;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 特种兵召唤方块：从 {@code data/tacz_morelevel/signal_summoning/offerings.json} 读入
 * 物品→实体与数量区间的查表，以及搜索/倒计时参数。
 *
 * <p>编码约定见仓库根目录 {@code Tacz_morelevel规范手册.txt}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SignalSummoningCatalog
        extends SimplePreparableReloadListener<SignalSummoningCatalog.LoadedConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignalSummoningCatalog.class);
    private static final SignalSummoningCatalog INSTANCE = new SignalSummoningCatalog();

    private static final String FOLDER = "signal_summoning";
    /** 数据包资源：{@code data/<modid>/signal_summoning/offerings.json}。 */
    private static final ResourceLocation OFFERINGS_ID =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, FOLDER + "/offerings.json");
    private static final String CLASSPATH_FALLBACK =
            "/data/" + Tacz_MoreLevel.MOD_ID + "/" + FOLDER + "/offerings.json";

    private static final int DEFAULT_SEARCH_RADIUS = 32;
    private static final int DEFAULT_SEARCH_UP = 4;
    private static final int DEFAULT_AIR_BLOCKS = 2;
    private static final int DEFAULT_COUNTDOWN_TICKS = 200;

    private ImmutableMap<ResourceLocation, Offering> offeringsByItem = ImmutableMap.of();
    private int searchRadius = DEFAULT_SEARCH_RADIUS;
    private int searchUpBlocks = DEFAULT_SEARCH_UP;
    private int airBlocks = DEFAULT_AIR_BLOCKS;
    private int countdownTicks = DEFAULT_COUNTDOWN_TICKS;
    private boolean classpathFallbackLoaded;

    public record Offering(EntityType<? extends Mob> entityType, int min, int max) {
        public int rollCount(RandomSource random) {
            int lo = Math.min(this.min, this.max);
            int hi = Math.max(this.min, this.max);
            if (hi <= lo) {
                return Math.max(1, lo);
            }
            return lo + random.nextInt(hi - lo + 1);
        }
    }

    public record LoadedConfig(
            int searchRadius,
            int searchUpBlocks,
            int airBlocks,
            int countdownTicks,
            ImmutableMap<ResourceLocation, Offering> offerings) {}

    public static SignalSummoningCatalog get() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public int searchRadius() {
        ensureClasspathFallback();
        return this.searchRadius;
    }

    public int searchUpBlocks() {
        ensureClasspathFallback();
        return this.searchUpBlocks;
    }

    public int airBlocks() {
        ensureClasspathFallback();
        return this.airBlocks;
    }

    public int countdownTicks() {
        ensureClasspathFallback();
        return this.countdownTicks;
    }

    public Optional<Offering> findOffering(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return findOffering(stack.getItem());
    }

    public Optional<Offering> findOffering(Item item) {
        ensureClasspathFallback();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.offeringsByItem.get(id));
    }

    private void ensureClasspathFallback() {
        if (!this.offeringsByItem.isEmpty()) {
            return;
        }
        if (this.classpathFallbackLoaded) {
            return;
        }
        this.classpathFallbackLoaded = true;
        applyConfig(loadClasspathConfig());
    }

    @Override
    protected LoadedConfig prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Optional<Resource> resource = resourceManager.getResource(OFFERINGS_ID);
        if (resource.isEmpty()) {
            LOGGER.warn("Missing {}, using classpath fallback", OFFERINGS_ID);
            return loadClasspathConfig();
        }
        try (InputStream stream = resource.get().open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return parseConfig(JsonParser.parseReader(reader));
        } catch (Exception e) {
            LOGGER.warn("Failed to load {}: {}", OFFERINGS_ID, e.getMessage());
            return loadClasspathConfig();
        }
    }

    @Override
    protected void apply(LoadedConfig data, ResourceManager resourceManager, ProfilerFiller profiler) {
        applyConfig(data);
        this.classpathFallbackLoaded = !this.offeringsByItem.isEmpty();
    }

    private void applyConfig(LoadedConfig data) {
        this.searchRadius = Math.max(0, data.searchRadius());
        this.searchUpBlocks = Math.max(0, data.searchUpBlocks());
        this.airBlocks = Math.max(1, data.airBlocks());
        this.countdownTicks = Math.max(1, data.countdownTicks());
        this.offeringsByItem = data.offerings();
    }

    private static LoadedConfig loadClasspathConfig() {
        try (InputStream stream = SignalSummoningCatalog.class.getResourceAsStream(CLASSPATH_FALLBACK)) {
            if (stream == null) {
                LOGGER.warn("Classpath fallback missing: {}", CLASSPATH_FALLBACK);
                return emptyConfig();
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return parseConfig(JsonParser.parseReader(reader));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed classpath fallback for signal summoning: {}", e.getMessage());
            return emptyConfig();
        }
    }

    private static LoadedConfig emptyConfig() {
        return new LoadedConfig(
                DEFAULT_SEARCH_RADIUS,
                DEFAULT_SEARCH_UP,
                DEFAULT_AIR_BLOCKS,
                DEFAULT_COUNTDOWN_TICKS,
                ImmutableMap.of());
    }

    private static LoadedConfig parseConfig(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            return emptyConfig();
        }
        JsonObject obj = root.getAsJsonObject();
        int searchRadius = readInt(obj, "search_radius", DEFAULT_SEARCH_RADIUS);
        int searchUp = readInt(obj, "search_up_blocks", DEFAULT_SEARCH_UP);
        int airBlocks = Math.max(1, readInt(obj, "air_blocks", DEFAULT_AIR_BLOCKS));
        int countdown = Math.max(1, readInt(obj, "countdown_ticks", DEFAULT_COUNTDOWN_TICKS));

        Map<ResourceLocation, Offering> offerings = new HashMap<>();
        if (obj.has("offerings") && obj.get("offerings").isJsonObject()) {
            JsonObject map = obj.getAsJsonObject("offerings");
            for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                Offering offering = parseOffering(entry.getKey(), entry.getValue());
                if (offering == null) {
                    continue;
                }
                ResourceLocation itemId = ResourceLocation.tryParse(entry.getKey());
                // tryParse：JSON 键为任意 "ns:path" 字符串，不能用 fromNamespaceAndPath（需已拆好的两段）。
                if (itemId == null) {
                    LOGGER.warn("Invalid offering item id: {}", entry.getKey());
                    continue;
                }
                offerings.put(itemId, offering);
            }
        }
        return new LoadedConfig(
                searchRadius, searchUp, airBlocks, countdown, ImmutableMap.copyOf(offerings));
    }

    @Nullable
    private static Offering parseOffering(String itemKey, JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            LOGGER.warn("Offering for {} must be an object", itemKey);
            return null;
        }
        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("entity") || !obj.get("entity").isJsonPrimitive()) {
            LOGGER.warn("Offering {} missing entity", itemKey);
            return null;
        }
        ResourceLocation entityId = ResourceLocation.tryParse(obj.get("entity").getAsString());
        if (entityId == null) {
            LOGGER.warn("Offering {} has invalid entity id", itemKey);
            return null;
        }
        Optional<EntityType<?>> typeOpt = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);
        if (typeOpt.isEmpty()) {
            LOGGER.warn("Offering {} unknown entity: {}", itemKey, entityId);
            return null;
        }
        @SuppressWarnings("unchecked")
        EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) typeOpt.get();

        int min = obj.has("min") ? obj.get("min").getAsInt() : 3;
        int max = obj.has("max") ? obj.get("max").getAsInt() : 5;
        min = Math.max(1, min);
        max = Math.max(1, max);
        return new Offering(mobType, min, max);
    }

    private static int readInt(JsonObject obj, String key, int fallback) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return fallback;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return fallback;
        }
    }
}
