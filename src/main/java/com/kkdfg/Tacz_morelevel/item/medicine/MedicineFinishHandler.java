package com.kkdfg.Tacz_morelevel.item.medicine;

/**
 * 可选：由具体 Item 子类实现，在默认 spec 逻辑之前处理特殊结束效果。
 *
 * <p>返回 {@link Result#HANDLED} 时跳过 {@link ChannelMedicineItem} 的默认结束逻辑。
 */
public interface MedicineFinishHandler {

    enum Result {
        /** 继续执行默认 spec 逻辑。 */
        PASS,
        /** 已自行处理，跳过默认逻辑。 */
        HANDLED
    }

    Result onMedicineFinish(MedicineContext context);
}
