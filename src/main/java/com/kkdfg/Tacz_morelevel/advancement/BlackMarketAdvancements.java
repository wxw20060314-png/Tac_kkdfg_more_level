package com.kkdfg.Tacz_morelevel.advancement;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketDataManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

/** 黑市等级成就：在对应等级下打开联络方块 UI 时由服务端发放。 */
public final class BlackMarketAdvancements {

    private static final String CRITERION = "grant";

    public static final ResourceLocation LEVEL_1 =
            id("main/black_market_level_1");
    public static final ResourceLocation LEVEL_2 =
            id("main/black_market_level_2");
    public static final ResourceLocation LEVEL_3 =
            id("main/black_market_level_3");
    public static final ResourceLocation LEVEL_4 =
            id("main/black_market_level_4");
    public static final ResourceLocation LEVEL_5 =
            id("main/black_market_level_5");

    private static final ResourceLocation[] LEVELS = {
        LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5
    };

    private BlackMarketAdvancements() {}

    /** 打开黑市 UI 时：按当前世界等级补发 1～N 级成就（未完成的才发）。 */
    public static void onMenuOpened(ServerPlayer player, int marketLevel) {
        int capped =
                Math.min(
                        Math.max(marketLevel, 1), BlackMarketDataManager.MAX_MARKET_LEVEL);
        ServerAdvancementManager manager = player.server.getAdvancements();
        for (int level = 1; level <= capped; level++) {
            grant(manager, player, LEVELS[level - 1]);
        }
    }

    private static void grant(
            ServerAdvancementManager manager, ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = manager.getAdvancement(advancementId);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        player.getAdvancements().award(advancement, CRITERION);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, path);
    }
}
