package com.kkdfg.Tacz_morelevel.dategen;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Tacz_MoreLevel.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        ModBlocks.LANDMINE.get(),
                        ModBlocks.FROZEN_LANDMINE.get(),
                        ModBlocks.SIGNAL_SUMMONING_BLOCK.get(),
                        ModBlocks.BLACK_MARKET_CONTACT.get(),
                        ModBlocks.EXPLOSION_PROOF_STEEL_BLOCK.get());
        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(
                        ModBlocks.LANDMINE.get(),
                        ModBlocks.FROZEN_LANDMINE.get(),
                        ModBlocks.SIGNAL_SUMMONING_BLOCK.get());
        this.tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.BLACK_MARKET_CONTACT.get());
        this.tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.EXPLOSION_PROOF_STEEL_BLOCK.get());
    }
}
