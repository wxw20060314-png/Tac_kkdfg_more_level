package com.kkdfg.Tacz_morelevel.worldgen;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom structure types (beyond vanilla {@code minecraft:jigsaw}).
 *
 * <p>JSON {@code type} field uses {@code tacz_morelevel:<name>}.
 */
public final class ModStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Tacz_MoreLevel.MOD_ID);

    /** End jigsaw that rejects void columns before placing. */
    public static final RegistryObject<StructureType<EndIslandStructure>> END_ISLAND =
            STRUCTURE_TYPES.register("end_island", () -> () -> EndIslandStructure.CODEC);

    /** Nether jigsaw that scans a column for a solid ledge with air above. */
    public static final RegistryObject<StructureType<NetherLedgeStructure>> NETHER_LEDGE =
            STRUCTURE_TYPES.register("nether_ledge", () -> () -> NetherLedgeStructure.CODEC);

    private ModStructureTypes() {}

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}
