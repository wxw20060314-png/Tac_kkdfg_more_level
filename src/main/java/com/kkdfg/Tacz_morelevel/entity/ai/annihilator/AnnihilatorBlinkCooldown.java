package com.kkdfg.Tacz_morelevel.entity.ai.annihilator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/** 歼灭者空间传送冷却（TTL tick，非世界时间）。 */
public final class AnnihilatorBlinkCooldown {

    public static final String NBT_TAG = "AnnihilatorBlinkCooldownTtl";
    public static final int MAX = 40;

    private int ttl;

    public int getTtl() {
        return this.ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = Mth.clamp(ttl, 0, MAX);
    }

    public boolean isOnCooldown() {
        return this.ttl > 0;
    }

    public void trigger() {
        this.setTtl(MAX);
    }

    public void tick() {
        if (this.ttl > 0) {
            this.ttl--;
        }
    }

    public void read(CompoundTag tag) {
        this.setTtl(tag.getInt(NBT_TAG));
    }

    public void write(CompoundTag tag) {
        if (this.ttl > 0) {
            tag.putInt(NBT_TAG, this.ttl);
        }
    }
}
