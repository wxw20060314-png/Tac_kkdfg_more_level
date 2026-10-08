package com.kkdfg.Tacz_morelevel.item.medicine;

import java.util.ArrayList;
import java.util.List;

/** 药品统一配置：读条时长、回血、效果、音效与消耗方式。 */
public final class MedicineSpec {

    private final int useTicks;
    private final HealSpec heal;
    private final List<MedicineEffectSpec> effects;
    private final MedicineSounds sounds;
    private final MedicineConsumeMode consumeMode;
    private final boolean showActionBarProgress;

    private MedicineSpec(Builder builder) {
        this.useTicks = Math.max(1, builder.useTicks);
        this.heal = builder.heal != null ? builder.heal : HealSpec.none();
        this.effects = List.copyOf(builder.effects);
        this.sounds = builder.sounds != null ? builder.sounds : MedicineSounds.silent();
        this.consumeMode = builder.consumeMode != null ? builder.consumeMode : MedicineConsumeMode.SHRINK_ONE;
        this.showActionBarProgress = builder.showActionBarProgress;
    }

    public int useTicks() {
        return useTicks;
    }

    public HealSpec heal() {
        return heal;
    }

    public List<MedicineEffectSpec> effects() {
        return effects;
    }

    public MedicineSounds sounds() {
        return sounds;
    }

    public MedicineConsumeMode consumeMode() {
        return consumeMode;
    }

    public boolean showActionBarProgress() {
        return showActionBarProgress;
    }

    public boolean requiresHealableTarget() {
        return heal.hasHeal();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int useTicks = 20;
        private HealSpec heal = HealSpec.none();
        private final List<MedicineEffectSpec> effects = new ArrayList<>();
        private MedicineSounds sounds = MedicineSounds.silent();
        private MedicineConsumeMode consumeMode = MedicineConsumeMode.SHRINK_ONE;
        private boolean showActionBarProgress = false;

        public Builder useTicks(int useTicks) {
            this.useTicks = useTicks;
            return this;
        }

        public Builder heal(HealSpec heal) {
            this.heal = heal;
            return this;
        }

        public Builder addEffect(MedicineEffectSpec effect) {
            this.effects.add(effect);
            return this;
        }

        public Builder effects(List<MedicineEffectSpec> effects) {
            this.effects.clear();
            if (effects != null) {
                this.effects.addAll(effects);
            }
            return this;
        }

        public Builder sounds(MedicineSounds sounds) {
            this.sounds = sounds;
            return this;
        }

        public Builder consumeMode(MedicineConsumeMode consumeMode) {
            this.consumeMode = consumeMode;
            return this;
        }

        public Builder showActionBarProgress(boolean showActionBarProgress) {
            this.showActionBarProgress = showActionBarProgress;
            return this;
        }

        public MedicineSpec build() {
            return new MedicineSpec(this);
        }
    }
}
