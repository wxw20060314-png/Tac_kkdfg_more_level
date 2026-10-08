package com.kkdfg.Tacz_morelevel.item.medicine;

/** 药品消耗方式。 */
public enum MedicineConsumeMode {
    /** 读条过程中按 HealSpec 扣耐久，结束不再额外消耗。 */
    DURABILITY_DURING_CHANNEL,
    /** 读条结束扣 1 点耐久。 */
    DURABILITY_ON_FINISH,
    /** 读条结束消耗 1 个。 */
    SHRINK_ONE,
    /** 不消耗（创造模式等由 Item 逻辑另行处理）。 */
    NONE
}
