package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 肩炮被打空血后的特色死亡（非法收回不发此事件）。
 */
public final class ShoulderCannonKilledEvent extends Event {

    private final LivingEntity host;
    private final AbstractShoulderCannonEntity cannon;
    private final DamageSource source;

    public ShoulderCannonKilledEvent(
            LivingEntity host, AbstractShoulderCannonEntity cannon, DamageSource source) {
        this.host = host;
        this.cannon = cannon;
        this.source = source;
    }

    public LivingEntity getHost() {
        return this.host;
    }

    public AbstractShoulderCannonEntity getCannon() {
        return this.cannon;
    }

    public DamageSource getSource() {
        return this.source;
    }
}
