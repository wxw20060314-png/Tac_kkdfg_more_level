package com.kkdfg.Tacz_morelevel;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.kkdfg.Tacz_morelevel.dategen.*;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 数据生成入口，布局与注册顺序对齐参考 MDK
 * {@code E:\\mc-kaifa\\forge_mdk\\forge-1.20.1-47.4.16-mdk} 中 {@code com.kkdfg.test1.ModDataGenerator}；本模组无
 * {@code RegistrySetBuilder} / 自定义群系修改等，故不注册 {@code DatapackBuiltinEntriesProvider} 与 biome modifier。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModDataGenerator {

    private ModDataGenerator() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 服务端数据生成________________________________________________________________________________
        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                        packOutput,
                        Set.of(),
                        List.of(
                                new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ModEntityLootTablesProvider::new, LootContextParamSets.ENTITY))));
        generator.addProvider(event.includeServer(), new ModRecipesProvider(packOutput));
        BlockTagsProvider blockTagsProvider =
                generator.addProvider(
                        event.includeServer(), new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(
                event.includeServer(),
                new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput));
        generator.addProvider(event.includeServer(), new ModPoiTypeTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(
                event.includeServer(), new ModAdvancementsProvider(packOutput, lookupProvider, existingFileHelper));

        // 客户端数据生成________________________________________________________________________________
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelsProvider(packOutput, existingFileHelper));
//        generator.addProvider(event.includeClient(), new ModZhCNLangProvider(packOutput));
//        generator.addProvider(event.includeClient(), new ModEnUSLangProvider(packOutput));
    }
}
