package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kkdfg.Tacz_morelevel.SoldierSpawnArmorProfile;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.tacz.guns.api.TimelessAPI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
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
 * 兵种主手枪械池：{@code data/tacz_morelevel/troop_guns/<poolId>.json} 的 {@code guns} 为完整 TaCZ 枪 id
 * （如 {@code tacz:ak47_improved}），原样 {@link ResourceLocation#tryParse}；读入失败或池空时回退 {@link #FALLBACK_GUN_ID}。
 * <p>
 * {@link SoldierSpawnArmorProfile} 对应同名小写文件；额外池（如 {@code marshal_guard}）见 {@link #EXTRA_POOL_IDS}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TaczOrdinaryGunCatalog extends SimplePreparableReloadListener<Map<String, ImmutableList<ResourceLocation>>> {

    public static final String TACZ_NAMESPACE = "tacz";
    private static final TaczOrdinaryGunCatalog INSTANCE = new TaczOrdinaryGunCatalog();

    /** 列表为空或装配失败时的回退 id */
    public static final ResourceLocation FALLBACK_GUN_ID =
            ResourceLocation.fromNamespaceAndPath(TACZ_NAMESPACE, "aug");

    private static final String POOL_FOLDER = "troop_guns";

    /** 非 {@link SoldierSpawnArmorProfile} 枚举、但仍走本目录的枪池 id。 */
    private static final String[] EXTRA_POOL_IDS = {
        "marshal_guard", "slime_commander", "slime_warrior"
    };

    private final Map<String, ImmutableList<ResourceLocation>> gunsByPoolId = new HashMap<>();
    private final Map<SoldierSpawnArmorProfile, ImmutableList<ResourceLocation>> gunsByProfile =
            new EnumMap<>(SoldierSpawnArmorProfile.class);

    private boolean fallbackLoaded;

    public static TaczOrdinaryGunCatalog get() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    /** 从指定池文件名（不含 {@code .json}）随机选枪。 */
    public static ResourceLocation pickRandomForPoolId(RandomSource random, String poolId) {
        INSTANCE.ensureFallbackLoaded();
        ImmutableList<ResourceLocation> list =
                poolId == null ? null : INSTANCE.gunsByPoolId.get(poolId);
        if (list == null || list.isEmpty()) {
            return FALLBACK_GUN_ID;
        }
        return list.get(random.nextInt(list.size()));
    }

    /** 从指定兵种缓存池随机选枪；池空时回退 {@link #FALLBACK_GUN_ID}。 */
    public static ResourceLocation pickRandomForProfile(RandomSource random, SoldierSpawnArmorProfile profile) {
        return pickRandomForPoolId(random, profilePoolId(profile));
    }

    /** 重载后某兵种可用枪 id（只读）；可能为空列表。 */
    public static ImmutableList<ResourceLocation> getGunIdsForProfile(SoldierSpawnArmorProfile profile) {
        INSTANCE.ensureFallbackLoaded();
        ImmutableList<ResourceLocation> list = INSTANCE.gunsByProfile.get(profile);
        return list != null ? list : ImmutableList.of();
    }

    public static ImmutableList<ResourceLocation> getGunIdsForPoolId(String poolId) {
        INSTANCE.ensureFallbackLoaded();
        ImmutableList<ResourceLocation> list = INSTANCE.gunsByPoolId.get(poolId);
        return list != null ? list : ImmutableList.of();
    }

    private void ensureFallbackLoaded() {
        if (hasAnyPool()) {
            return;
        }
        if (this.fallbackLoaded) {
            return;
        }
        this.fallbackLoaded = true;
        applyPools(loadFromClasspath());
    }

    private boolean hasAnyPool() {
        for (ImmutableList<ResourceLocation> list : this.gunsByPoolId.values()) {
            if (list != null && !list.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Map<String, ImmutableList<ResourceLocation>> prepare(
            ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, ImmutableList<ResourceLocation>> pools = new HashMap<>();
        for (String poolId : allPoolIds()) {
            pools.put(poolId, resolvePool(readGunIds(resourceManager, poolId + ".json")));
        }
        return pools;
    }

    @Override
    protected void apply(
            Map<String, ImmutableList<ResourceLocation>> data,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        applyPools(data);
        this.fallbackLoaded = hasAnyPool();
    }

    private void applyPools(Map<String, ImmutableList<ResourceLocation>> data) {
        this.gunsByPoolId.clear();
        this.gunsByPoolId.putAll(data);
        this.gunsByProfile.clear();
        for (SoldierSpawnArmorProfile profile : SoldierSpawnArmorProfile.values()) {
            ImmutableList<ResourceLocation> list = data.get(profilePoolId(profile));
            this.gunsByProfile.put(profile, list != null ? list : ImmutableList.of());
        }
    }

    private static Map<String, ImmutableList<ResourceLocation>> loadFromClasspath() {
        Map<String, ImmutableList<ResourceLocation>> pools = new HashMap<>();
        for (String poolId : allPoolIds()) {
            pools.put(poolId, resolvePool(readClasspathGunIds(poolId + ".json")));
        }
        return pools;
    }

    private static List<String> allPoolIds() {
        List<String> ids = new ArrayList<>();
        for (SoldierSpawnArmorProfile profile : SoldierSpawnArmorProfile.values()) {
            ids.add(profilePoolId(profile));
        }
        for (String extra : EXTRA_POOL_IDS) {
            ids.add(extra);
        }
        return ids;
    }

    private static String profilePoolId(SoldierSpawnArmorProfile profile) {
        return profile.name().toLowerCase();
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
        try (InputStream stream = TaczOrdinaryGunCatalog.class.getResourceAsStream(path)) {
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
            ResourceLocation id = parseGunId(raw);
            if (id != null && TimelessAPI.getCommonGunIndex(id).isPresent()) {
                builder.add(id);
            }
        }
        ImmutableList<ResourceLocation> resolved = builder.build();
        return resolved.isEmpty() ? fallbackPool() : resolved;
    }

    private static ImmutableList<ResourceLocation> fallbackPool() {
        return TimelessAPI.getCommonGunIndex(FALLBACK_GUN_ID).isPresent()
                ? ImmutableList.of(FALLBACK_GUN_ID)
                : ImmutableList.of();
    }

    /** JSON 条目须为完整 id，如 {@code tacz:ak47_improved}，不做拼接或后缀替换。 */
    private static ResourceLocation parseGunId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return ResourceLocation.tryParse(raw.trim());
    }
}
