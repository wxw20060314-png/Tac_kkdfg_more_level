package com.kkdfg.Tacz_morelevel.particle;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Tacz_MoreLevel.MOD_ID);

    /** 电爆：探测手雷触发时的电击扩散粒子。 */
    public static final RegistryObject<SimpleParticleType> DIANBAO =
            PARTICLE_TYPES.register("dianbao", () -> new SimpleParticleType(false));

    private ModParticles() {}

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
