package com.kkdfg.Tacz_morelevel.dategen;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;

import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(
            PackOutput packOutput,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTags, Tacz_MoreLevel.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModItemTags.TACZ_ARMOR)
                .add(ModItems.FIRST_BULLETPROOF_BOOTS.get())
                .add(ModItems.FIRST_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.FIRST_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.FIRST_BULLETPROOF_HELMET.get())
                .add(ModItems.SECOND_BULLETPROOF_HELMET.get())
                .add(ModItems.SECOND_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SECOND_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SECOND_BULLETPROOF_BOOTS.get())
                .add(ModItems.THIRD_BULLETPROOF_HELMET.get())
                .add(ModItems.THIRD_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.THIRD_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.THIRD_BULLETPROOF_BOOTS.get())
                .add(ModItems.FOURTH_BULLETPROOF_HELMET.get())
                .add(ModItems.FOURTH_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.FOURTH_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.FOURTH_BULLETPROOF_BOOTS.get())
                .add(ModItems.FIFTH_BULLETPROOF_HELMET.get())
                .add(ModItems.FIFTH_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.FIFTH_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.FIFTH_BULLETPROOF_BOOTS.get())
                .add(ModItems.SIXTH_BULLETPROOF_HELMET.get())
                .add(ModItems.SIXTH_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SIXTH_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SIXTH_BULLETPROOF_BOOTS.get())
                .add(ModItems.SEVENTH_BULLETPROOF_HELMET.get())
                .add(ModItems.SEVENTH_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SEVENTH_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SEVENTH_BULLETPROOF_BOOTS.get())
                .add(ModItems.SEVENTH_BLAST_BULLETPROOF_HELMET.get())
                .add(ModItems.SEVENTH_BLAST_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SEVENTH_BLAST_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SEVENTH_BLAST_BULLETPROOF_BOOTS.get())
                .add(ModItems.SHOCKWAVE_BULLETPROOF_HELMET.get())
                .add(ModItems.SHOCKWAVE_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SHOCKWAVE_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SHOCKWAVE_BULLETPROOF_BOOTS.get())
                .add(ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_HELMET.get())
                .add(ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_BOOTS.get())
                .add(ModItems.SEVENTH_ABYSS_BULLETPROOF_HELMET.get())
                .add(ModItems.SEVENTH_ABYSS_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SEVENTH_ABYSS_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SEVENTH_ABYSS_BULLETPROOF_BOOTS.get())
                .add(ModItems.SEVENTH_SCULK_BULLETPROOF_HELMET.get())
                .add(ModItems.SEVENTH_SCULK_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SEVENTH_SCULK_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SEVENTH_SCULK_BULLETPROOF_BOOTS.get())
                .add(ModItems.SEVENTH_RADIATION_BULLETPROOF_HELMET.get())
                .add(ModItems.SEVENTH_RADIATION_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SEVENTH_RADIATION_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SEVENTH_RADIATION_BULLETPROOF_BOOTS.get())
                .add(ModItems.POWER_MECH_BULLETPROOF_HELMET.get())
                .add(ModItems.POWER_MECH_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.POWER_MECH_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.POWER_MECH_BULLETPROOF_BOOTS.get())
                .add(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_HELMET.get())
                .add(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_BOOTS.get())
                .add(ModItems.SPECIAL_OPS_BULLETPROOF_HELMET.get())
                .add(ModItems.SPECIAL_OPS_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SPECIAL_OPS_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SPECIAL_OPS_BULLETPROOF_BOOTS.get())
                .add(ModItems.SPACETIME_BULLETPROOF_HELMET.get())
                .add(ModItems.SPACETIME_BULLETPROOF_CHESTPLATE.get())
                .add(ModItems.SPACETIME_BULLETPROOF_LEGGINGS.get())
                .add(ModItems.SPACETIME_BULLETPROOF_BOOTS.get())
                .add(ModItems.JOKER_SEVENTH_BULLETPROOF_HELMET.get());
        tag(ItemTags.TRIMMABLE_ARMOR)
                .addTags(ModItemTags.TACZ_ARMOR);
    }
}
