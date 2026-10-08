package com.kkdfg.Tacz_morelevel.item.medicine;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

/** 用药音效：开始 / 读条中（可选）/ 结束。 */
public record MedicineSounds(
        Supplier<SoundEvent> onStart,
        Supplier<SoundEvent> onChannel,
        Supplier<SoundEvent> onFinish) {

    public static MedicineSounds injection(Supplier<SoundEvent> start, Supplier<SoundEvent> finish) {
        return new MedicineSounds(start, null, finish);
    }

    public static MedicineSounds channeling(
            Supplier<SoundEvent> start, Supplier<SoundEvent> channel, Supplier<SoundEvent> finish) {
        return new MedicineSounds(start, channel, finish);
    }

    public static MedicineSounds silent() {
        return new MedicineSounds(null, null, null);
    }

    /** 读条开始时播放一次，读条中/结束不再播。 */
    public static MedicineSounds once(Supplier<SoundEvent> sound) {
        return new MedicineSounds(sound, null, null);
    }
}
