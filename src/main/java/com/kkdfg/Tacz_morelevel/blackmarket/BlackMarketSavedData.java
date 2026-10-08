package com.kkdfg.Tacz_morelevel.blackmarket;

import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 全维度共享的黑市运行时状态。
 * 1～5 级各有一套预选货架；每个联络方块按本地信标显示对应等级表，不重 roll。
 * 自动刷新重 roll 全部等级表；手动刷新仅重 roll 该方块当前等级表。
 */
public final class BlackMarketSavedData extends SavedData {

    private static final String DATA_ID = "tacz_morelevel_black_market";
    /** 旧存档单表字段，读档时迁入一级。 */
    private static final String TAG_OFFERS_LEGACY = "Offers";
    private static final String TAG_OFFERS_BY_LEVEL = "OffersByLevel";
    private static final String TAG_LEVEL_OFFERS = "Offers";
    private static final String TAG_LAST_REFRESH = "LastRefreshTick";
    private static final String TAG_MANUAL_REFRESH_PAID_COUNT = "ManualRefreshPaidCount";

    private final List<List<BlackMarketTradeOffer>> offersByLevel = createEmptyOffersByLevel();
    private long lastRefreshGameTime;
    private int manualRefreshPaidCount;
    private long lastTickProcessed = -1L;

    public static BlackMarketSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlackMarketSavedData::load, BlackMarketSavedData::new, DATA_ID);
    }

    private static List<List<BlackMarketTradeOffer>> createEmptyOffersByLevel() {
        List<List<BlackMarketTradeOffer>> byLevel = new ArrayList<>(BlackMarketDataManager.MAX_MARKET_LEVEL);
        for (int i = 0; i < BlackMarketDataManager.MAX_MARKET_LEVEL; i++) {
            byLevel.add(new ArrayList<>());
        }
        return byLevel;
    }

    private BlackMarketSavedData() {}

    private static BlackMarketSavedData load(CompoundTag tag) {
        BlackMarketSavedData data = new BlackMarketSavedData();
        if (tag.isEmpty()) {
            return data;
        }
        if (tag.contains(TAG_OFFERS_BY_LEVEL)) {
            loadOffersByLevel(data, tag.getList(TAG_OFFERS_BY_LEVEL, Tag.TAG_COMPOUND));
        } else if (tag.contains(TAG_OFFERS_LEGACY)) {
            loadLegacyOffers(data, tag.getList(TAG_OFFERS_LEGACY, Tag.TAG_COMPOUND));
        }
        data.lastRefreshGameTime = tag.getLong(TAG_LAST_REFRESH);
        data.manualRefreshPaidCount = tag.getInt(TAG_MANUAL_REFRESH_PAID_COUNT);
        data.rematerializeAllGunOffers();
        return data;
    }

    private static void loadOffersByLevel(BlackMarketSavedData data, ListTag levelList) {
        for (int i = 0; i < BlackMarketDataManager.MAX_MARKET_LEVEL && i < levelList.size(); i++) {
            readOfferList(data.offersByLevel.get(i), levelList.getCompound(i).getList(TAG_LEVEL_OFFERS, Tag.TAG_COMPOUND));
        }
    }

    private static void loadLegacyOffers(BlackMarketSavedData data, ListTag offerList) {
        readOfferList(data.offersByLevel.get(0), offerList);
    }

    private static void readOfferList(List<BlackMarketTradeOffer> target, ListTag offerList) {
        target.clear();
        for (int i = 0; i < offerList.size(); i++) {
            target.add(BlackMarketTradeOffer.read(offerList.getCompound(i)));
        }
    }

    private static int normalizeMarketLevel(int level) {
        if (level <= 0) {
            return 1;
        }
        return Math.min(level, BlackMarketDataManager.MAX_MARKET_LEVEL);
    }

    private static int levelIndex(int marketLevel) {
        return normalizeMarketLevel(marketLevel) - 1;
    }

    private List<BlackMarketTradeOffer> tableForLevel(int marketLevel) {
        return this.offersByLevel.get(levelIndex(marketLevel));
    }

    /** 每个世界刻只处理一次自动刷新（任意方块实体 tick 均可触发）。 */
    public void serverTick(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (this.lastTickProcessed == gameTime) {
            return;
        }
        this.lastTickProcessed = gameTime;
        this.ensureInitialized(level);
        long interval = BlackMarketDataManager.get().refreshIntervalTicks();
        if (level.getGameTime() - this.lastRefreshGameTime >= interval) {
            this.refreshOffersAutomatic(level, BlackMarketFeedback.REFRESH_SUCCESS);
        }
    }

    /** 联络方块本地信标等级变化时，确保该等级货架已预选（不重 roll 已有表）。 */
    public void ensureLevelTableReady(ServerLevel level, int marketLevel) {
        this.ensureInitialized(level);
        if (this.ensureLevelTable(level, marketLevel)) {
            this.setDirty();
        }
    }

    /** 首次运行：为 1～5 级各预选一批货物（仅空表才 roll）。 */
    public void ensureInitialized(ServerLevel level) {
        BlackMarketDataManager.get().ensureFallbackLoaded();
        boolean anyRolled = false;
        for (int marketLevel = 1; marketLevel <= BlackMarketDataManager.MAX_MARKET_LEVEL; marketLevel++) {
            if (this.ensureLevelTable(level, marketLevel)) {
                anyRolled = true;
            }
        }
        if (anyRolled && this.lastRefreshGameTime == 0L) {
            this.lastRefreshGameTime = level.getGameTime();
        }
        if (anyRolled) {
            this.setDirty();
        }
    }

    public void performManualRefresh(ServerLevel level, int marketLevel) {
        BlackMarketDataManager.get().ensureFallbackLoaded();
        this.rollLevelOffers(level, marketLevel, BlackMarketFeedback.NONE, false);
        this.manualRefreshPaidCount++;
        this.setDirty();
        BlackMarketMenuBroadcaster.syncAllViewers(level, BlackMarketFeedback.REFRESH_SUCCESS);
    }

    private void refreshOffersAutomatic(ServerLevel level, BlackMarketFeedback feedback) {
        BlackMarketDataManager.get().ensureFallbackLoaded();
        for (int marketLevel = 1; marketLevel <= BlackMarketDataManager.MAX_MARKET_LEVEL; marketLevel++) {
            this.rollLevelOffers(level, marketLevel, BlackMarketFeedback.NONE, false);
        }
        this.lastRefreshGameTime = level.getGameTime();
        this.manualRefreshPaidCount = 0;
        this.setDirty();
        if (feedback != BlackMarketFeedback.NONE) {
            BlackMarketMenuBroadcaster.syncAllViewers(level, feedback);
        }
    }

    /** 重 roll 指定等级的五个槽位。 */
    private void rollLevelOffers(
            ServerLevel serverLevel,
            int marketLevel,
            BlackMarketFeedback feedback,
            boolean resetManualRefreshCost) {
        int normalizedLevel = normalizeMarketLevel(marketLevel);
        List<BlackMarketTradeOffer> table = this.tableForLevel(normalizedLevel);
        table.clear();
        table.addAll(BlackMarketDataManager.get().rollOffers(normalizedLevel, serverLevel.getRandom()));
        rematerializeGunOffers(table);
        if (resetManualRefreshCost) {
            this.manualRefreshPaidCount = 0;
        }
        this.setDirty();
        if (feedback != BlackMarketFeedback.NONE) {
            BlackMarketMenuBroadcaster.syncAllViewers(serverLevel, feedback);
        }
    }

    /**
     * 确保某等级货架已预选。返回 true 表示本次新 roll 了该等级。
     */
    private boolean ensureLevelTable(ServerLevel level, int marketLevel) {
        List<BlackMarketTradeOffer> table = this.tableForLevel(marketLevel);
        if (!table.isEmpty()) {
            return false;
        }
        table.addAll(BlackMarketDataManager.get().rollOffers(marketLevel, level.getRandom()));
        rematerializeGunOffers(table);
        return true;
    }

    private void rematerializeAllGunOffers() {
        for (List<BlackMarketTradeOffer> table : this.offersByLevel) {
            rematerializeGunOffers(table);
        }
    }

    private static void rematerializeGunOffers(List<BlackMarketTradeOffer> offers) {
        for (int i = 0; i < offers.size(); i++) {
            BlackMarketTradeOffer offer = offers.get(i);
            ItemStack input = BlackMarketItemStackJson.rematerializeGunStack(offer.input());
            ItemStack output = BlackMarketItemStackJson.rematerializeGunStack(offer.output());
            if (!ItemStack.matches(input, offer.input()) || !ItemStack.matches(output, offer.output())) {
                offers.set(i, new BlackMarketTradeOffer(input, output));
            }
        }
    }

    public List<BlackMarketTradeOffer> offersForLevel(int marketLevel) {
        return List.copyOf(this.tableForLevel(marketLevel));
    }

    public BlackMarketTradeOffer offerForLevel(int marketLevel, int index) {
        List<BlackMarketTradeOffer> table = this.tableForLevel(marketLevel);
        if (index < 0 || index >= table.size()) {
            return null;
        }
        return table.get(index);
    }

    public ItemStack refreshCost() {
        return BlackMarketDataManager.get().manualRefreshCost(this.manualRefreshPaidCount).copy();
    }

    public int manualRefreshPaidCount() {
        return this.manualRefreshPaidCount;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag levelList = new ListTag();
        for (List<BlackMarketTradeOffer> table : this.offersByLevel) {
            CompoundTag levelTag = new CompoundTag();
            ListTag offerList = new ListTag();
            for (BlackMarketTradeOffer offer : table) {
                CompoundTag offerTag = new CompoundTag();
                offer.write(offerTag);
                offerList.add(offerTag);
            }
            levelTag.put(TAG_LEVEL_OFFERS, offerList);
            levelList.add(levelTag);
        }
        tag.put(TAG_OFFERS_BY_LEVEL, levelList);
        tag.putLong(TAG_LAST_REFRESH, this.lastRefreshGameTime);
        tag.putInt(TAG_MANUAL_REFRESH_PAID_COUNT, this.manualRefreshPaidCount);
        return tag;
    }
}
