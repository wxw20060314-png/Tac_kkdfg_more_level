package com.kkdfg.Tacz_morelevel.block;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.block.entity.BlackMarketContactBlockEntity;
import com.kkdfg.Tacz_morelevel.block.entity.LimitedSpawnerBlockEntity;
import com.kkdfg.Tacz_morelevel.block.entity.PluginWorkbenchBlockEntity;
import com.kkdfg.Tacz_morelevel.block.entity.SignalSummoningBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组方块实体类型注册。
 *
 * <p>编码约定见仓库根目录 {@code Tacz_morelevel规范手册.txt}。
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Tacz_MoreLevel.MOD_ID);

    public static final RegistryObject<BlockEntityType<SignalSummoningBlockEntity>> SIGNAL_SUMMONING_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "signal_summoning_block",
                    () ->
                            BlockEntityType.Builder.of(
                                            SignalSummoningBlockEntity::new,
                                            ModBlocks.SIGNAL_SUMMONING_BLOCK.get())
                                    .build(null));

    public static final RegistryObject<BlockEntityType<BlackMarketContactBlockEntity>>
            BLACK_MARKET_CONTACT_BLOCK_ENTITY =
                    BLOCK_ENTITY_TYPES.register(
                            "black_market_contact",
                            () ->
                                    BlockEntityType.Builder.of(
                                                    BlackMarketContactBlockEntity::new,
                                                    ModBlocks.BLACK_MARKET_CONTACT.get())
                                            .build(null));

    public static final RegistryObject<BlockEntityType<LimitedSpawnerBlockEntity>>
            LIMITED_SPAWNER_BLOCK_ENTITY =
                    BLOCK_ENTITY_TYPES.register(
                            "limited_spawner",
                            () ->
                                    BlockEntityType.Builder.of(
                                                    LimitedSpawnerBlockEntity::new,
                                                    ModBlocks.LIMITED_SPAWNER.get())
                                            .build(null));

    public static final RegistryObject<BlockEntityType<PluginWorkbenchBlockEntity>>
            PLUGIN_WORKBENCH_BLOCK_ENTITY =
                    BLOCK_ENTITY_TYPES.register(
                            "plugin_workbench",
                            () ->
                                    BlockEntityType.Builder.of(
                                                    PluginWorkbenchBlockEntity::new,
                                                    ModBlocks.PLUGIN_WORKBENCH.get())
                                            .build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    private ModBlockEntities() {}
}
