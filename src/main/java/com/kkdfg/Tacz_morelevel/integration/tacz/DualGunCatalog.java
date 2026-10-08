package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.tacz.guns.api.TimelessAPI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 双枪 NPC 枪械池：{@code data/tacz_morelevel/troop_guns/dual_gun_primary.json}、
 * {@code dual_gun_back.json}，格式与 {@link TaczOrdinaryGunCatalog} 相同（{@code guns} 数组）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DualGunCatalog extends SimplePreparableReloadListener<Map<String, ImmutableList<ResourceLocation>>> {

    public static final String PRIMARY_POOL = "dual_gun_primary";
    public static final String BACK_POOL = "dual_gun_back";

    private static final DualGunCatalog INSTANCE = new DualGunCatalog();
    private static final String POOL_FOLDER = "troop_guns";

    private final Map<String, ImmutableList<ResourceLocation>> pools = new HashMap<>();
    private boolean fallbackLoaded;

    public static DualGunCatalog get() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static ResourceLocation pickRandom(String poolId, RandomSource random) {
        INSTANCE.ensureFallbackLoaded();
        ImmutableList<ResourceLocation> list = INSTANCE.pools.get(poolId);
        if (list == null || list.isEmpty()) {
            return TaczOrdinaryGunCatalog.FALLBACK_GUN_ID;
        }
        return list.get(random.nextInt(list.size()));
    }

    private void ensureFallbackLoaded() {
        if (!pools.isEmpty()) {
            return;
        }
        if (fallbackLoaded) {
            return;
        }
        fallbackLoaded = true;
        applyPools(loadFromClasspath());
    }

    @Override
    protected Map<String, ImmutableList<ResourceLocation>> prepare(
            ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, ImmutableList<ResourceLocation>> loaded = new HashMap<>();
        loaded.put(PRIMARY_POOL, resolvePool(readGunIds(resourceManager, PRIMARY_POOL + ".json")));
        loaded.put(BACK_POOL, resolvePool(readGunIds(resourceManager, BACK_POOL + ".json")));
        return loaded;
    }

    @Override
    protected void apply(
            Map<String, ImmutableList<ResourceLocation>> data,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        pools.clear();
        pools.putAll(data);
        fallbackLoaded = !pools.isEmpty();
    }

    private void applyPools(Map<String, ImmutableList<ResourceLocation>> data) {
        pools.clear();
        pools.putAll(data);
    }

    private static Map<String, ImmutableList<ResourceLocation>> loadFromClasspath() {
        Map<String, ImmutableList<ResourceLocation>> loaded = new HashMap<>();
        loaded.put(PRIMARY_POOL, resolvePool(readClasspathGunIds(PRIMARY_POOL + ".json")));
        loaded.put(BACK_POOL, resolvePool(readClasspathGunIds(BACK_POOL + ".json")));
        return loaded;
    }

    private static List<String> readGunIds(ResourceManager resourceManager, String fileName) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, POOL_FOLDER + "/" + fileName);
        try {
            Resource resource = resourceManager.getResourceOrThrow(location);
            try (InputStream stream = resource.open();
                    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return parseGunIdStrings(JsonParser.parseReader(reader));
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<String> readClasspathGunIds(String fileName) {
        String path = "/data/tacz_morelevel/" + POOL_FOLDER + "/" + fileName;
        try (InputStream stream = DualGunCatalog.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return parseGunIdStrings(JsonParser.parseReader(reader));
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<String> parseGunIdStrings(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            return null;
        }
        JsonObject obj = root.getAsJsonObject();
        if (!obj.has("guns") || !obj.get("guns").isJsonArray()) {
            return null;
        }
        JsonArray arr = obj.getAsJsonArray("guns");
        List<String> ids = new ArrayList<>(arr.size());
        for (JsonElement element : arr) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                ids.add(element.getAsString());
            }
        }
        return ids.isEmpty() ? null : ids;
    }

    private static ImmutableList<ResourceLocation> resolvePool(List<String> gunIds) {
        if (gunIds == null || gunIds.isEmpty()) {
            return fallbackPool();
        }
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        for (String raw : gunIds) {
            ResourceLocation id = ResourceLocation.tryParse(raw.trim());
            if (id != null && TimelessAPI.getCommonGunIndex(id).isPresent()) {
                builder.add(id);
            }
        }
        ImmutableList<ResourceLocation> resolved = builder.build();
        return resolved.isEmpty() ? fallbackPool() : resolved;
    }

    private static ImmutableList<ResourceLocation> fallbackPool() {
        return TimelessAPI.getCommonGunIndex(TaczOrdinaryGunCatalog.FALLBACK_GUN_ID).isPresent()
                ? ImmutableList.of(TaczOrdinaryGunCatalog.FALLBACK_GUN_ID)
                : ImmutableList.of();
    }
}
