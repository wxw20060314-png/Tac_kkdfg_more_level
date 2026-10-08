package com.kkdfg.Tacz_morelevel.api;

import com.kkdfg.Tacz_morelevel.entity.SoldierTaczCrawlState;

/** 与士兵 TaCZ 趴/蹲状态签名一致，供渲染与未来 AI 扩展；基础友好 NPC 可仅 stub。 */
public interface TaczCrawlCapableMob {

    boolean isGunCombatAiming();

    boolean isTaczCrawlingSynced();

    boolean isTaczCrawling();

    SoldierTaczCrawlState getTaczCrawlState();
}
