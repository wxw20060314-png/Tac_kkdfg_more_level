package com.kkdfg.Tacz_morelevel.spawn;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 解析后的单条生成护甲配置。 */
record ArmorSpawnOption(
        int weight,
        boolean bare,
        @Nullable Item helmet,
        @Nullable Item chestplate,
        @Nullable Item leggings,
        @Nullable Item boots) {

    SpawnArmorPick toPick() {
        if (bare) {
            return SpawnArmorPick.noArmor();
        }
        return new SpawnArmorPick(false, helmet, chestplate, leggings, boots);
    }
}

/** 权重抽取结果：裸甲或四件护甲。 */
record SpawnArmorPick(
        boolean bare,
        @Nullable Item helmet,
        @Nullable Item chestplate,
        @Nullable Item leggings,
        @Nullable Item boots) {

    static SpawnArmorPick noArmor() {
        return new SpawnArmorPick(true, null, null, null, null);
    }
}

/**
 * 兵种生成护甲池：{@code data/tacz_morelevel/troop_armor/<poolId>.json}。
 * 每条含 weight 与四件护甲 id；纯注册表识别，不做套装映射。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoldierSpawnArmorCatalog
        extends SimplePreparableReloadListener<Map<String, ImmutableList<ArmorSpawnOption>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoldierSpawnArmorCatalog.class);
    private static final SoldierSpawnArmorCatalog INSTANCE = new SoldierSpawnArmorCatalog();

    private static final String POOL_FOLDER = "troop_armor";
    private static final String POOL_PREFIX = POOL_FOLDER + "/";

    private final Map<String, ImmutableList<ArmorSpawnOption>> poolsById = new HashMap<>();
    private boolean classpathFallbackLoaded;

    public static SoldierSpawnArmorCatalog get() {
        return INSTANCE;
    }

    /**
     * 按 poolId 权重抽取一套护甲。
     *
     * @return {@code null} 无池或空池；{@link SpawnArmorPick#bare()} 为 true 表示裸甲
     */
    @Nullable
    public static SpawnArmorPick pick(RandomSource random, String poolId) {
        INSTANCE.ensureClasspathFallback();
        if (poolId == null || poolId.isBlank()) {
            return null;
        }
        ImmutableList<ArmorSpawnOption> pool = INSTANCE.poolsById.get(poolId);
        if (pool == null || pool.isEmpty()) {
            return null;
        }
        int sum = 0;
        for (ArmorSpawnOption option : pool) {
            sum += option.weight();
        }
        if (sum <= 0) {
            return null;
        }
        double u = random.nextDouble();
        double acc = 0.0;
        ArmorSpawnOption last = null;
        for (ArmorSpawnOption option : pool) {
            last = option;
            acc += (double) option.weight() / (double) sum;
            if (u < acc) {
                return option.toPick();
            }
        }
        return last != null ? last.toPick() : null;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    private void ensureClasspathFallback() {
        if (!this.poolsById.isEmpty()) {
            return;
        }
        if (this.classpathFallbackLoaded) {
            return;
        }
        this.classpathFallbackLoaded = true;
        applyPools(loadClasspathPools());
    }

    @Override
    protected Map<String, ImmutableList<ArmorSpawnOption>> prepare(
            ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, ImmutableList<ArmorSpawnOption>> pools = new HashMap<>();
        Map<ResourceLocation, Resource> resources =
                resourceManager.listResources(POOL_FOLDER, id -> id.getPath().endsWith(".json"));
        for (ResourceLocation location : resources.keySet()) {
            if (!Tacz_MoreLevel.MOD_ID.equals(location.getNamespace())) {
                continue;
            }
            String path = location.getPath();
            if (!path.startsWith(POOL_PREFIX) || path.length() <= POOL_PREFIX.length() + ".json".length()) {
                continue;
            }
            String poolId = path.substring(POOL_PREFIX.length(), path.length() - ".json".length());
            if (poolId.isEmpty() || poolId.contains("/")) {
                continue;
            }
            try {
                Resource resource = resourceManager.getResourceOrThrow(location);
                try (InputStream stream = resource.open();
                        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    ImmutableList<ArmorSpawnOption> parsed = parsePool(poolId, JsonParser.parseReader(reader));
                    if (parsed != null && !parsed.isEmpty()) {
                        pools.put(poolId, parsed);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load troop armor pool {}: {}", location, e.getMessage());
            }
        }
        return pools;
    }

    @Override
    protected void apply(
            Map<String, ImmutableList<ArmorSpawnOption>> data,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        applyPools(data);
        this.classpathFallbackLoaded = !this.poolsById.isEmpty();
    }

    private void applyPools(Map<String, ImmutableList<ArmorSpawnOption>> data) {
        this.poolsById.clear();
        this.poolsById.putAll(data);
    }

    private static Map<String, ImmutableList<ArmorSpawnOption>> loadClasspathPools() {
        Map<String, ImmutableList<ArmorSpawnOption>> pools = new HashMap<>();
        String[] knownPools = {
            "militia", "mercenary", "assault", "special_forces", "super_special", "refugee"
        };
        for (String poolId : knownPools) {
            String path = "/data/tacz_morelevel/" + POOL_FOLDER + "/" + poolId + ".json";
            try (InputStream stream = SoldierSpawnArmorCatalog.class.getResourceAsStream(path)) {
                if (stream == null) {
                    continue;
                }
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    ImmutableList<ArmorSpawnOption> parsed = parsePool(poolId, JsonParser.parseReader(reader));
                    if (parsed != null && !parsed.isEmpty()) {
                        pools.put(poolId, parsed);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load classpath troop armor pool {}: {}", poolId, e.getMessage());
            }
        }
        return pools;
    }

    @Nullable
    private static ImmutableList<ArmorSpawnOption> parsePool(String poolId, JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            LOGGER.warn("troop_armor/{}.json: root must be an object", poolId);
            return null;
        }
        JsonObject obj = root.getAsJsonObject();
        if (!obj.has("armor") || !obj.get("armor").isJsonArray()) {
            LOGGER.warn("troop_armor/{}.json: missing \"armor\" array", poolId);
            return null;
        }
        JsonArray arr = obj.getAsJsonArray("armor");
        ImmutableList.Builder<ArmorSpawnOption> builder = ImmutableList.builder();
        for (JsonElement element : arr) {
            if (!element.isJsonObject()) {
                continue;
            }
            ArmorSpawnOption option = parseEntry(poolId, element.getAsJsonObject());
            if (option != null) {
                builder.add(option);
            }
        }
        ImmutableList<ArmorSpawnOption> list = builder.build();
        return list.isEmpty() ? null : list;
    }

    @Nullable
    private static ArmorSpawnOption parseEntry(String poolId, JsonObject entry) {
        if (!entry.has("weight") || !entry.get("weight").isJsonPrimitive()) {
            LOGGER.warn("troop_armor/{}.json: entry missing numeric weight", poolId);
            return null;
        }
        int weight = entry.get("weight").getAsInt();
        if (weight <= 0) {
            return null;
        }
        if (weight > 100) {
            LOGGER.warn("troop_armor/{}.json: weight {} out of range 1-100, skipping entry", poolId, weight);
            return null;
        }

        Item helmet = resolveArmorItem(entry.get("helmet"), poolId);
        Item chest = resolveArmorItem(entry.get("chestplate"), poolId);
        Item legs = resolveArmorItem(entry.get("leggings"), poolId);
        Item boots = resolveArmorItem(entry.get("boots"), poolId);

        if (helmet == null && chest == null && legs == null && boots == null) {
            return new ArmorSpawnOption(weight, true, null, null, null, null);
        }
        if (helmet == null || chest == null || legs == null || boots == null) {
            LOGGER.warn("troop_armor/{}.json: armor entry must have all four slots or all null", poolId);
            return null;
        }
        return new ArmorSpawnOption(weight, false, helmet, chest, legs, boots);
    }

    @Nullable
    private static Item resolveArmorItem(@Nullable JsonElement element, String poolId) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            LOGGER.warn("troop_armor/{}.json: armor id must be a string or null", poolId);
            return null;
        }
        String raw = element.getAsString();
        if (raw == null || raw.isBlank()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(raw.trim());
        if (id == null) {
            LOGGER.warn("troop_armor/{}.json: invalid item id '{}'", poolId, raw);
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            LOGGER.warn("troop_armor/{}.json: unknown item '{}'", poolId, id);
            return null;
        }
        if (!(item instanceof ArmorItem)) {
            LOGGER.warn("troop_armor/{}.json: '{}' is not armor", poolId, id);
            return null;
        }
        return item;
    }
}
