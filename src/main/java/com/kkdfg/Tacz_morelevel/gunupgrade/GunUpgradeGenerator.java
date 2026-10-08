package com.kkdfg.Tacz_morelevel.gunupgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.mojang.logging.LogUtils;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.resource.pojo.GunIndexPOJO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

/**
 * 按 {@code scripts/gen_new_tacz118_tiers.py} / {@code gen_tier_gun_recipes.py} 规则，
 * 将筛选后的底枪写出到 {@code tacz/tacz_morelevel_gun_upgrades/}（不触发 reload）。
 */
public final class GunUpgradeGenerator {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private static final String[] TIERS = {"improved", "refined", "perfect", "excellent"};
    private static final double[] DAMAGE_MULT = {1.5D, 2.0D, 2.5D, 3.0D};
    private static final double[] COMFORT_MULT = {0.85D, 0.7D, 0.55D, 0.4D};
    private static final double[] ARMOR_K = {0.5D, 2.0D / 3.0D, 0.75D, 0.8D};
    private static final double[] RECIPE_RATIO = {0.1D, 0.2D, 0.3D, 0.4D};
    private static final String[] VOUCHERS = {
        "improved_gun_upgrade_voucher",
        "refined_gun_upgrade_voucher",
        "perfect_gun_upgrade_voucher",
        "excellent_gun_upgrade_voucher"
    };
    private static final String[] TIER_SUFFIX_EN = {"Improved", "Refined", "Perfect", "Excellent"};
    private static final String[] TIER_SUFFIX_ZH = {"改进", "精致", "完美", "卓越"};
    private static final String[] TIER_COLOR = {"§a", "§9", "§5", "§c"};

    private GunUpgradeGenerator() {}

    public record ProbeResult(int scanned, int eligible, int withRecipe, int withoutRecipe) {}

    public record GenerateResult(
            int scanned, int eligible, int writtenFiles, int gunsOk, int gunsSkipped, int recipesWritten) {}

    public static ProbeResult probe(MinecraftServer server) {
        List<ResourceLocation> all = listAllGunIds();
        int eligible = 0;
        int withRecipe = 0;
        Map<ResourceLocation, JsonObject> recipeByGun = indexGunRecipes(server);
        for (ResourceLocation id : all) {
            if (!GunUpgradeFilter.isEligible(id)) {
                continue;
            }
            eligible++;
            if (hasUsableRecipe(recipeByGun.get(id))) {
                withRecipe++;
            }
        }
        return new ProbeResult(all.size(), eligible, withRecipe, eligible - withRecipe);
    }

    public static boolean clearPack(MinecraftServer server) throws IOException {
        Path packRoot = GunUpgradePackPaths.packRoot(server);
        if (!Files.exists(packRoot)) {
            return true;
        }
        if (!GunUpgradePackPaths.isSafePackRoot(packRoot)) {
            throw new IOException("Refusing to delete unsafe path: " + packRoot);
        }
        deleteRecursively(packRoot);
        return true;
    }

    public static GenerateResult generateAll(MinecraftServer server) throws IOException {
        Path packRoot = GunUpgradePackPaths.packRoot(server);
        if (Files.exists(packRoot)) {
            if (!GunUpgradePackPaths.isSafePackRoot(packRoot)) {
                throw new IOException("Refusing to clear unsafe path: " + packRoot);
            }
            deleteRecursively(packRoot);
        }
        Files.createDirectories(packRoot);

        writePackMeta(packRoot);
        Map<String, String> langEn = new LinkedHashMap<>();
        Map<String, String> langZh = new LinkedHashMap<>();

        List<ResourceLocation> all = listAllGunIds();
        Map<ResourceLocation, JsonObject> recipeByGun = indexGunRecipes(server);
        CommonAssetsManager assets = CommonAssetsManager.getInstance();
        Map<DataType, Map<ResourceLocation, String>> cache =
                assets == null ? Map.of() : assets.getNetworkCache();
        Map<ResourceLocation, String> gunDataCache =
                cache.getOrDefault(DataType.GUN_DATA, Map.of());
        Map<ResourceLocation, String> gunIndexCache =
                cache.getOrDefault(DataType.GUN_INDEX, Map.of());
        Map<ResourceLocation, String> attachmentTagsCache =
                cache.getOrDefault(DataType.ATTACHMENT_TAGS, Map.of());

        int writtenFiles = 0;
        int gunsOk = 0;
        int gunsSkipped = 0;
        int recipesWritten = 0;
        int eligible = 0;

        for (ResourceLocation gunId : all) {
            if (!GunUpgradeFilter.isEligible(gunId)) {
                continue;
            }
            eligible++;
            try {
                JsonObject recipeJson = recipeByGun.get(gunId);
                int[] counts =
                        generateOneGun(
                                server,
                                packRoot,
                                gunId,
                                gunDataCache,
                                gunIndexCache,
                                attachmentTagsCache,
                                hasUsableRecipe(recipeJson) ? recipeJson : null,
                                langEn,
                                langZh);
                writtenFiles += counts[0];
                recipesWritten += counts[1];
                gunsOk++;
            } catch (Exception e) {
                gunsSkipped++;
                LOGGER.warn("Gun upgrade skip {}: {}", gunId, e.toString());
            }
        }

        writtenFiles += writeLang(packRoot, "en_us", langEn);
        writtenFiles += writeLang(packRoot, "zh_cn", langZh);

        return new GenerateResult(
                all.size(), eligible, writtenFiles, gunsOk, gunsSkipped, recipesWritten);
    }

    private static int[] generateOneGun(
            MinecraftServer server,
            Path packRoot,
            ResourceLocation gunId,
            Map<ResourceLocation, String> gunDataCache,
            Map<ResourceLocation, String> gunIndexCache,
            Map<ResourceLocation, String> attachmentTagsCache,
            @Nullable JsonObject baseRecipe,
            Map<String, String> langEn,
            Map<String, String> langZh)
            throws IOException {
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gunId);
        if (indexOpt.isEmpty()) {
            throw new IllegalStateException("missing gun index");
        }
        CommonGunIndex index = indexOpt.get();
        GunIndexPOJO pojo = index.getPojo();
        if (pojo == null || pojo.getData() == null || pojo.getDisplay() == null) {
            throw new IllegalStateException("incomplete gun index pojo");
        }

        String dataJson = gunDataCache.get(pojo.getData());
        if (dataJson == null || dataJson.isBlank()) {
            throw new IllegalStateException("missing gun data json for " + pojo.getData());
        }
        JsonObject baseData = JsonParser.parseString(dataJson).getAsJsonObject();

        String indexJson = gunIndexCache.get(gunId);
        JsonObject baseIndex;
        if (indexJson != null && !indexJson.isBlank()) {
            baseIndex = JsonParser.parseString(indexJson).getAsJsonObject();
        } else {
            baseIndex = pojoToIndexJson(pojo);
        }

        JsonArray allowAttachments = loadAllowAttachments(server, gunId, attachmentTagsCache);

        int baseSort = 100;
        if (baseIndex.has("sort") && baseIndex.get("sort").isJsonPrimitive()) {
            baseSort = baseIndex.get("sort").getAsInt();
        }

        String pretty = gunId.getPath().replace('_', ' ');
        int files = 0;
        int recipes = 0;

        for (int ti = 0; ti < TIERS.length; ti++) {
            String tier = TIERS[ti];
            ResourceLocation tierGunId =
                    ResourceLocation.fromNamespaceAndPath(gunId.getNamespace(), gunId.getPath() + "_" + tier);
            ResourceLocation tierDataId =
                    ResourceLocation.fromNamespaceAndPath(
                            gunId.getNamespace(), gunId.getPath() + "_" + tier + "_data");

            JsonObject tierData = deepCopy(baseData);
            applyTier(tierData, ti);
            files +=
                    writeJson(
                            packRoot.resolve("data")
                                    .resolve(gunId.getNamespace())
                                    .resolve("data")
                                    .resolve("guns")
                                    .resolve(tierGunId.getPath() + "_data.json"),
                            tierData);

            JsonObject tierIndex = deepCopy(baseIndex);
            String nameKey =
                    Tacz_MoreLevel.MOD_ID
                            + ".gun."
                            + gunId.getNamespace()
                            + "."
                            + tierGunId.getPath()
                            + ".name";
            String descKey =
                    Tacz_MoreLevel.MOD_ID
                            + ".gun."
                            + gunId.getNamespace()
                            + "."
                            + tierGunId.getPath()
                            + ".desc";
            tierIndex.addProperty("name", nameKey);
            tierIndex.addProperty("tooltip", descKey);
            tierIndex.addProperty("display", pojo.getDisplay().toString());
            tierIndex.addProperty("data", tierDataId.toString());
            tierIndex.addProperty("sort", baseSort + ti + 1);
            if (!tierIndex.has("type")) {
                tierIndex.addProperty("type", index.getType());
            }
            files +=
                    writeJson(
                            packRoot.resolve("data")
                                    .resolve(gunId.getNamespace())
                                    .resolve("index")
                                    .resolve("guns")
                                    .resolve(tierGunId.getPath() + ".json"),
                            tierIndex);

            if (allowAttachments != null) {
                files +=
                        writeJson(
                                packRoot.resolve("data")
                                        .resolve(gunId.getNamespace())
                                        .resolve("tacz_tags")
                                        .resolve("attachments")
                                        .resolve("allow_attachments")
                                        .resolve(tierGunId.getPath() + ".json"),
                                allowAttachments);
            }

            langEn.put(
                    nameKey,
                    TIER_COLOR[ti] + "§o" + capitalizeWords(pretty) + " " + TIER_SUFFIX_EN[ti] + "§r");
            langZh.put(
                    nameKey, TIER_COLOR[ti] + "§o" + pretty + TIER_SUFFIX_ZH[ti] + "§r");
            langEn.putIfAbsent(descKey, "");
            langZh.putIfAbsent(descKey, "");

            if (baseRecipe != null) {
                JsonObject recipe = buildTierRecipe(baseRecipe, gunId, tierGunId, ti);
                files +=
                        writeJson(
                                packRoot.resolve("data")
                                        .resolve(gunId.getNamespace())
                                        .resolve("recipes")
                                        .resolve("gun")
                                        .resolve(tierGunId.getPath() + ".json"),
                                recipe);
                recipes++;
            }
        }
        return new int[] {files, recipes};
    }

    private static JsonObject buildTierRecipe(
            JsonObject baseRecipe, ResourceLocation baseGunId, ResourceLocation outGunId, int tierIndex) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "tacz:gun_smith_table_crafting");

        JsonArray materials = new JsonArray();
        JsonArray template =
                baseRecipe.has("materials") && baseRecipe.get("materials").isJsonArray()
                        ? baseRecipe.getAsJsonArray("materials")
                        : new JsonArray();
        double ratio = RECIPE_RATIO[tierIndex];
        for (JsonElement el : template) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject m = deepCopy(el.getAsJsonObject());
            int count = m.has("count") ? m.get("count").getAsInt() : 1;
            int scaled = (int) Math.floor(count * ratio);
            if (scaled <= 0) {
                continue;
            }
            m.addProperty("count", scaled);
            materials.add(m);
        }

        String inputSuffix =
                switch (tierIndex) {
                    case 0 -> "";
                    case 1 -> "_improved";
                    case 2 -> "_refined";
                    default -> "_perfect";
                };
        ResourceLocation inputGunId =
                ResourceLocation.fromNamespaceAndPath(
                        baseGunId.getNamespace(), baseGunId.getPath() + inputSuffix);

        materials.add(gunIngredient(inputGunId));
        materials.add(voucherIngredient(VOUCHERS[tierIndex]));
        recipe.add("materials", materials);

        JsonObject result = new JsonObject();
        result.addProperty("type", "gun");
        result.addProperty("id", outGunId.toString());
        recipe.add("result", result);
        return recipe;
    }

    private static JsonObject gunIngredient(ResourceLocation gunId) {
        JsonObject nbt = new JsonObject();
        nbt.addProperty("GunId", gunId.toString());
        JsonObject item = new JsonObject();
        item.addProperty("type", "forge:partial_nbt");
        item.addProperty("item", "tacz:modern_kinetic_gun");
        item.add("nbt", nbt);
        JsonObject mat = new JsonObject();
        mat.add("item", item);
        mat.addProperty("count", 1);
        return mat;
    }

    private static JsonObject voucherIngredient(String path) {
        JsonObject item = new JsonObject();
        item.addProperty("item", Tacz_MoreLevel.MOD_ID + ":" + path);
        JsonObject mat = new JsonObject();
        mat.add("item", item);
        mat.addProperty("count", 1);
        return mat;
    }

    private static void applyTier(JsonObject gun, int tierIdx) {
        double dm = DAMAGE_MULT[tierIdx];
        double cm = COMFORT_MULT[tierIdx];
        double ak = ARMOR_K[tierIdx];
        double headMul = 1.0D + 0.1D * (tierIdx + 1);

        if (!gun.has("bullet") || !gun.get("bullet").isJsonObject()) {
            return;
        }
        JsonObject bullet = gun.getAsJsonObject("bullet");
        if (bullet.has("damage") && bullet.get("damage").isJsonPrimitive()) {
            bullet.addProperty("damage", round4(bullet.get("damage").getAsDouble() * dm));
        }
        if (bullet.has("extra_damage") && bullet.get("extra_damage").isJsonObject()) {
            JsonObject ed = bullet.getAsJsonObject("extra_damage");
            double v = ed.has("armor_ignore") ? ed.get("armor_ignore").getAsDouble() : 0.0D;
            ed.addProperty("armor_ignore", round4(Math.min(0.92D, (1.0D - v) * ak + v)));
            double hs = ed.has("head_shot_multiplier") ? ed.get("head_shot_multiplier").getAsDouble() : 1.5D;
            ed.addProperty("head_shot_multiplier", round4(hs * headMul));
            if (ed.has("damage_adjust") && ed.get("damage_adjust").isJsonArray()) {
                for (JsonElement rowEl : ed.getAsJsonArray("damage_adjust")) {
                    if (rowEl.isJsonObject()) {
                        JsonObject row = rowEl.getAsJsonObject();
                        if (row.has("damage") && row.get("damage").isJsonPrimitive()) {
                            row.addProperty("damage", round4(row.get("damage").getAsDouble() * dm));
                        }
                    }
                }
            }
        }
        if (bullet.has("gravity") && bullet.get("gravity").isJsonPrimitive()) {
            bullet.addProperty("gravity", round4(bullet.get("gravity").getAsDouble() * cm));
        }
        if (gun.has("inaccuracy") && gun.get("inaccuracy").isJsonObject()) {
            JsonObject inc = gun.getAsJsonObject("inaccuracy");
            for (String key : List.of("stand", "move", "sneak", "lie")) {
                if (inc.has(key) && inc.get(key).isJsonPrimitive()) {
                    inc.addProperty(key, round4(inc.get(key).getAsDouble() * cm));
                }
            }
        }
    }

    /**
     * 原样复制底枪 {@code allow_attachments} JSON 数组。缺失或空列表返回 {@code null}，
     * 调用方不得写出文件（避免把可装配件写成 {@code []}）。
     */
    @Nullable
    private static JsonArray loadAllowAttachments(
            @Nullable MinecraftServer server,
            ResourceLocation gunId,
            Map<ResourceLocation, String> attachmentTagsCache) {
        ResourceLocation cacheKey =
                ResourceLocation.fromNamespaceAndPath(
                        gunId.getNamespace(), "allow_attachments/" + gunId.getPath());
        String cached = attachmentTagsCache != null ? attachmentTagsCache.get(cacheKey) : null;
        JsonArray fromCache = parseAllowAttachmentsArray(cached);
        if (fromCache != null) {
            return fromCache;
        }

        if (server != null) {
            try {
                ResourceLocation fileRl =
                        ResourceLocation.fromNamespaceAndPath(
                                gunId.getNamespace(),
                                "tacz_tags/attachments/allow_attachments/"
                                        + gunId.getPath()
                                        + ".json");
                Optional<Resource> opt = server.getResourceManager().getResource(fileRl);
                if (opt.isPresent()) {
                    try (var reader = opt.get().openAsReader()) {
                        StringBuilder sb = new StringBuilder();
                        char[] buf = new char[4096];
                        int n;
                        while ((n = reader.read(buf)) >= 0) {
                            sb.append(buf, 0, n);
                        }
                        JsonArray fromDisk = parseAllowAttachmentsArray(sb.toString());
                        if (fromDisk != null) {
                            return fromDisk;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("allow_attachments resource read failed for {}: {}", gunId, e.toString());
            }
        }

        LOGGER.warn("No allow_attachments for {}; tier guns will omit allow list files", gunId);
        return null;
    }

    @Nullable
    private static JsonArray parseAllowAttachmentsArray(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonElement el = JsonParser.parseString(stripJsonComments(raw));
            if (el != null && el.isJsonArray()) {
                JsonArray arr = el.getAsJsonArray();
                if (!arr.isEmpty()) {
                    return arr;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to parse allow_attachments json: {}", e.toString());
        }
        return null;
    }

    private static Map<ResourceLocation, JsonObject> indexGunRecipes(MinecraftServer server) {
        Map<ResourceLocation, JsonObject> map = new LinkedHashMap<>();
        if (server == null) {
            return map;
        }

        // 1) TaCZ network cache (if present)
        try {
            CommonAssetsManager assets = CommonAssetsManager.getInstance();
            if (assets != null) {
                Map<ResourceLocation, String> recipes =
                        assets.getNetworkCache().getOrDefault(DataType.RECIPES, Map.of());
                for (Map.Entry<ResourceLocation, String> e : recipes.entrySet()) {
                    tryPutGunRecipe(map, e.getValue());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Recipe cache scan failed: {}", e.toString());
        }

        // 2) Datapack / gun-pack recipe JSON on disk via ResourceManager
        try {
            var listed =
                    server.getResourceManager()
                            .listResources("recipes", rl -> rl.getPath().endsWith(".json"));
            for (var entry : listed.entrySet()) {
                try (var reader = entry.getValue().openAsReader()) {
                    StringBuilder sb = new StringBuilder();
                    char[] buf = new char[4096];
                    int n;
                    while ((n = reader.read(buf)) >= 0) {
                        sb.append(buf, 0, n);
                    }
                    tryPutGunRecipe(map, stripJsonComments(sb.toString()));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            LOGGER.warn("ResourceManager recipe scan failed: {}", e.toString());
        }

        return map;
    }

    private static void tryPutGunRecipe(Map<ResourceLocation, JsonObject> map, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        try {
            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
            if (!obj.has("type")
                    || !"tacz:gun_smith_table_crafting".equals(obj.get("type").getAsString())) {
                return;
            }
            if (!obj.has("result") || !obj.get("result").isJsonObject()) {
                return;
            }
            JsonObject result = obj.getAsJsonObject("result");
            if (!result.has("type")
                    || !"gun".equals(result.get("type").getAsString())
                    || !result.has("id")) {
                return;
            }
            ResourceLocation id = ResourceLocation.tryParse(result.get("id").getAsString());
            if (id != null && hasUsableRecipe(obj)) {
                map.putIfAbsent(id, obj);
            }
        } catch (Exception ignored) {
            // skip malformed recipe
        }
    }

    /** Strip line and block comments outside of JSON strings (TaCZ pack JSON). */
    private static String stripJsonComments(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        boolean inStr = false;
        boolean esc = false;
        while (i < n) {
            char c = text.charAt(i);
            if (inStr) {
                out.append(c);
                if (esc) {
                    esc = false;
                } else if (c == '\\') {
                    esc = true;
                } else if (c == '"') {
                    inStr = false;
                }
                i++;
                continue;
            }
            if (c == '"') {
                inStr = true;
                out.append(c);
                i++;
                continue;
            }
            if (c == '/' && i + 1 < n && text.charAt(i + 1) == '/') {
                i += 2;
                while (i < n && text.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }
            if (c == '/' && i + 1 < n && text.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < n && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/')) {
                    i++;
                }
                i = Math.min(i + 2, n);
                continue;
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static boolean hasUsableRecipe(@Nullable JsonObject recipe) {
        return recipe != null
                && recipe.has("materials")
                && recipe.get("materials").isJsonArray();
    }

    private static List<ResourceLocation> listAllGunIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        try {
            for (Map.Entry<ResourceLocation, CommonGunIndex> e : TimelessAPI.getAllCommonGunIndex()) {
                if (e.getKey() != null) {
                    ids.add(e.getKey());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to list guns: {}", e.toString());
        }
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        return ids;
    }

    private static JsonObject pojoToIndexJson(GunIndexPOJO pojo) {
        JsonObject o = new JsonObject();
        if (pojo.getName() != null) {
            o.addProperty("name", pojo.getName());
        }
        if (pojo.getTooltip() != null) {
            o.addProperty("tooltip", pojo.getTooltip());
        }
        if (pojo.getDisplay() != null) {
            o.addProperty("display", pojo.getDisplay().toString());
        }
        if (pojo.getData() != null) {
            o.addProperty("data", pojo.getData().toString());
        }
        if (pojo.getType() != null) {
            o.addProperty("type", pojo.getType());
        }
        if (pojo.getItemType() != null) {
            o.addProperty("item_type", pojo.getItemType());
        }
        o.addProperty("sort", pojo.getSort());
        return o;
    }

    private static void writePackMeta(Path packRoot) throws IOException {
        JsonObject meta = new JsonObject();
        meta.addProperty("namespace", Tacz_MoreLevel.MOD_ID);
        writeJson(packRoot.resolve("gunpack.meta.json"), meta);

        JsonObject info = new JsonObject();
        info.addProperty("version", "1.0.0");
        info.addProperty("name", "pack.tacz_morelevel.gun_upgrades.name");
        info.addProperty("desc", "pack.tacz_morelevel.gun_upgrades.desc");
        info.addProperty("license", "All Rights Reserved");
        JsonArray authors = new JsonArray();
        authors.add("tacz_morelevel");
        info.add("authors", authors);
        info.addProperty("date", "2026-09-09");
        writeJson(
                packRoot.resolve("assets")
                        .resolve(Tacz_MoreLevel.MOD_ID)
                        .resolve("gunpack_info.json"),
                info);
    }

    private static int writeLang(Path packRoot, String code, Map<String, String> entries)
            throws IOException {
        if (entries.isEmpty()) {
            return 0;
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("pack.tacz_morelevel.gun_upgrades.name", "TaCZ MoreLevel Gun Upgrades");
        obj.addProperty(
                "pack.tacz_morelevel.gun_upgrades.desc",
                "Generated tiered guns (improved/refined/perfect/excellent). Run /tacz reload to load.");
        for (Map.Entry<String, String> e : entries.entrySet()) {
            obj.addProperty(e.getKey(), e.getValue());
        }
        writeJson(
                packRoot.resolve("assets")
                        .resolve(Tacz_MoreLevel.MOD_ID)
                        .resolve("lang")
                        .resolve(code + ".json"),
                obj);
        return 1;
    }

    private static int writeJson(Path path, JsonElement json) throws IOException {
        Files.createDirectories(path.getParent());
        String text = GSON.toJson(json);
        if (!text.endsWith("\n")) {
            text = text + "\n";
        }
        Files.writeString(path, text, StandardCharsets.UTF_8);
        return 1;
    }

    private static JsonObject deepCopy(JsonObject src) {
        return JsonParser.parseString(src.toString()).getAsJsonObject();
    }

    private static double round4(double v) {
        return Math.round(v * 10000.0D) / 10000.0D;
    }

    private static String capitalizeWords(String s) {
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1));
            }
        }
        return sb.toString();
    }

    private static void deleteRecursively(Path root) throws IOException {
        Files.walkFileTree(
                root,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }
}
