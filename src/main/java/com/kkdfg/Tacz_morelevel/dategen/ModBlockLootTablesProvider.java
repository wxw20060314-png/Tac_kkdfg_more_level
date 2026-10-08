package com.kkdfg.Tacz_morelevel.dategen;

import com.kkdfg.Tacz_morelevel.block.ModBlocks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {

    public ModBlockLootTablesProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.LANDMINE.get());
        this.dropSelf(ModBlocks.FROZEN_LANDMINE.get());
        this.dropSelf(ModBlocks.SIGNAL_SUMMONING_BLOCK.get());
        this.dropSelf(ModBlocks.BLACK_MARKET_CONTACT.get());
        this.dropSelf(ModBlocks.LIMITED_SPAWNER.get());
        this.dropSelf(ModBlocks.EXPLOSION_PROOF_STEEL_BLOCK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                ModBlocks.LANDMINE.get(),
                ModBlocks.FROZEN_LANDMINE.get(),
                ModBlocks.SIGNAL_SUMMONING_BLOCK.get(),
                ModBlocks.BLACK_MARKET_CONTACT.get(),
                ModBlocks.LIMITED_SPAWNER.get(),
                ModBlocks.EXPLOSION_PROOF_STEEL_BLOCK.get());
    }
}
