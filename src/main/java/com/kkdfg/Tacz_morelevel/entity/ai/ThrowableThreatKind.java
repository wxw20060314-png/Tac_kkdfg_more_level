package com.kkdfg.Tacz_morelevel.entity.ai;

/** 士兵投掷物警戒种类；与警惕值接口无关。 */
public enum ThrowableThreatKind {
    NONE,
    /** {@link com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity} 且非闪。 */
    GRENADE,
    /** {@link com.kkdfg.Tacz_morelevel.entity.custom.FlashbangEntity}。 */
    FLASHBANG,
    /** {@link com.kkdfg.Tacz_morelevel.entity.custom.MolotovEntity}。 */
    MOLOTOV
}
