package com.kkdfg.Tacz_morelevel.block;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.block.custom.FrozenLandmineBlock;
import com.kkdfg.Tacz_morelevel.block.custom.LandmineBlock;
import com.kkdfg.Tacz_morelevel.block.custom.LimitedSpawnerBlock;
import com.kkdfg.Tacz_morelevel.block.custom.MolotovFireBlock;
import com.kkdfg.Tacz_morelevel.block.custom.BlackMarketContactBlock;
import com.kkdfg.Tacz_morelevel.block.custom.PluginWorkbenchBlock;
import com.kkdfg.Tacz_morelevel.block.custom.SignalSummoningBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 模组方块注册。
 *
 * <p>编码约定见仓库根目录 {@code Tacz_morelevel规范手册.txt}。
 */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Tacz_MoreLevel.MOD_ID);

    public static final RegistryObject<Block> MOLOTOV_FIRE =
            BLOCKS.register(
                    "molotov_fire",
                    () -> new MolotovFireBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_FIRE)));

    public static final RegistryObject<Block> LANDMINE =
            BLOCKS.register(
                    "landmine",
                    () ->
                            new LandmineBlock(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.COLOR_GRAY)
                                            .sound(SoundType.METAL)
                                            .strength(2.0F, 6.0F)
                                            .noOcclusion()));

    public static final RegistryObject<Block> FROZEN_LANDMINE =
            BLOCKS.register(
                    "frozen_landmine",
                    () ->
                            new FrozenLandmineBlock(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.ICE)
                                            .sound(SoundType.METAL)
                                            .strength(2.0F, 6.0F)
                                            .noOcclusion()));

    /** 特种兵召唤方块：与附魔台相同的硬度/爆炸抗性；非完整碰撞箱。 */
    public static final RegistryObject<Block> SIGNAL_SUMMONING_BLOCK =
            BLOCKS.register(
                    "signal_summoning_block",
                    () ->
                            new SignalSummoningBlock(
                                    BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)));

    /** 黑市联络系统：与附魔台相同的硬度/爆炸抗性；需铁镐挖掘。 */
    public static final RegistryObject<Block> BLACK_MARKET_CONTACT =
            BLOCKS.register(
                    "black_market_contact",
                    () ->
                            new BlackMarketContactBlock(
                                    BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)));

    /** 限次刷怪笼：外观与原版刷怪笼相同。 */
    public static final RegistryObject<Block> LIMITED_SPAWNER =
            BLOCKS.register(
                    "limited_spawner",
                    () -> new LimitedSpawnerBlock(BlockBehaviour.Properties.copy(Blocks.SPAWNER)));

    /** 插件装备台：装上/拆下防弹盔甲插件；GeckoLib 外观。 */
    public static final RegistryObject<Block> PLUGIN_WORKBENCH =
            BLOCKS.register(
                    "plugin_workbench",
                    () ->
                            new PluginWorkbenchBlock(
                                    BlockBehaviour.Properties.copy(Blocks.SMITHING_TABLE)
                                            .noOcclusion()));

    /** 防爆钢块：硬度 100，爆炸抗性 600；需正确工具掉落。 */
    public static final RegistryObject<Block> EXPLOSION_PROOF_STEEL_BLOCK =
            BLOCKS.register(
                    "explosion_proof_steel_block",
                    () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.METAL)
                                            .sound(SoundType.METAL)
                                            .strength(500.0F, 10.0F)
                                            .requiresCorrectToolForDrops()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

    private ModBlocks() {}
}
