package com.kkdfg.Tacz_morelevel.api;

/** 与 {@link com.kkdfg.Tacz_morelevel.integration.tacz.SoldierTaczCrawlHelper} 配合的趴下同步接口。 */
public interface TaczCombatCrawlMob {

    boolean isTaczCrawlingSynced();

    void syncTaczCrawlingFlag(boolean crawling);
}
