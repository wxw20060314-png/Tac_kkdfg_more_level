package com.kkdfg.Tacz_morelevel;

/**
 * 与 {@link Config} 中各 {@code npcAttributes}、{@code troop_guns} 等一一对应。
 * 生成护甲见 {@code data/tacz_morelevel/troop_armor/<poolId>.json}（{@link com.kkdfg.Tacz_morelevel.api.SpawnArmorConfigurableMob#spawnArmorPoolId()}）。
 */
public enum SoldierSpawnArmorProfile {
    MILITIA,
    MERCENARY,
    ASSAULT,
    SPECIAL_FORCES,
    SUPER_SPECIAL,
    REFUGEE
}
