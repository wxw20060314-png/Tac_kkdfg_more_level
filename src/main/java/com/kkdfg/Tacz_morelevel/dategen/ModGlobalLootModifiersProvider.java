package com.kkdfg.Tacz_morelevel.dategen;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {

    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, Tacz_MoreLevel.MOD_ID);
    }

    @Override
    protected void start() {}
}
