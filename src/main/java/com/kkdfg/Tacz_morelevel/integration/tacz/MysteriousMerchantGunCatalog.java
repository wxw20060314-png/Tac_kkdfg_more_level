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
import java.util.List;
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
 * 神秘商人副手藏枪池：{@code data/tacz_morelevel/troop_guns/mysterious_merchant.json}。
 * 逻辑与 {@link TaczOrdinaryGunCatalog} 一致。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MysteriousMerchantGunCatalog
        extends SimplePreparableReloadListener<ImmutableList<ResourceLocation>> {

    public static final String POOL_ID = "mysterious_merchant";

    private static final MysteriousMerchantGunCatalog INSTANCE = new MysteriousMerchantGunCatalog();
    private static final String POOL_FOLDER = "troop_guns";
    private static final String FILE_NAME = POOL_ID + ".json";

    private ImmutableList<ResourceLocation> guns = ImmutableList.of();
    private boolean fallbackLoaded;

    public static MysteriousMerchantGunCatalog get() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    /** 从神秘商人枪械池随机选枪；池空时回退 {@link TaczOrdinaryGunCatalog#FALLBACK_GUN_ID}。 */
    public static ResourceLocation pickRandom(RandomSource random) {
        INSTANCE.ensureFallbackLoaded();
        if (INSTANCE.guns.isEmpty()) {
            return TaczOrdinaryGunCatalog.FALLBACK_GUN_ID;
        }
        return INSTANCE.guns.get(random.nextInt(INSTANCE.guns.size()));
    }

    /** 重载后可用枪 id（只读）；可能为空列表。 */
    public static ImmutableList<ResourceLocation> getGunIds() {
        INSTANCE.ensureFallbackLoaded();
        return INSTANCE.guns;
    }

    private void ensureFallbackLoaded() {
        if (hasPool()) {
            return;
        }
        if (this.fallbackLoaded) {
            return;
        }
        this.fallbackLoaded = true;
        applyGuns(loadFromClasspath());
    }

    private boolean hasPool() {
        return !this.guns.isEmpty();
    }

    @Override
    protected ImmutableList<ResourceLocation> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return resolvePool(readGunIds(resourceManager));
    }

    @Override
    protected void apply(ImmutableList<ResourceLocation> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        applyGuns(data);
        this.fallbackLoaded = hasPool();
    }

    private void applyGuns(ImmutableList<ResourceLocation> data) {
        this.guns = data;
    }

    private static ImmutableList<ResourceLocation> loadFromClasspath() {
        return resolvePool(readClasspathGunIds());
    }

    private static List<String> readGunIds(ResourceManager resourceManager) {
        ResourceLocation location =
                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, POOL_FOLDER + "/" + FILE_NAME);
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

    private static List<String> readClasspathGunIds() {
        String path = "/data/tacz_morelevel/" + POOL_FOLDER + "/" + FILE_NAME;
        try (InputStream stream = MysteriousMerchantGunCatalog.class.getResourceAsStream(path)) {
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
        return TimelessAPI.getCommonGunIndex(TaczOrdinaryGunCatalog.FALLBACK_GUN_ID).isPresent()
                ? ImmutableList.of(TaczOrdinaryGunCatalog.FALLBACK_GUN_ID)
                : ImmutableList.of();
    }

    private static ResourceLocation parseGunId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return ResourceLocation.tryParse(raw.trim());
    }
}
