package com.kkdfg.Tacz_morelevel.dategen;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Tacz_MoreLevel.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(
                ModBlocks.EXPLOSION_PROOF_STEEL_BLOCK.get(),
                cubeAll(ModBlocks.EXPLOSION_PROOF_STEEL_BLOCK.get()));
    }
}
