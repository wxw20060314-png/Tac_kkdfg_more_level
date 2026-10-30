package com.kkdfg.Tacz_morelevel.dategen;

import java.util.function.Consumer;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipesProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OBSIDIAN_SWORD.get())
                .pattern("#")
                .pattern("#")
                .pattern("x")
                .define('#', ModItems.OBSIDIAN_ITEM.get())
                .define('x', Items.STICK)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.OBSIDIAN_PICKAXE.get())
                .pattern("###")
                .pattern(" x ")
                .pattern(" x ")
                .define('#', ModItems.OBSIDIAN_ITEM.get())
                .define('x', Items.STICK)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.OBSIDIAN_AXE.get())
                .pattern(" ##")
                .pattern(" x#")
                .pattern(" x ")
                .define('#', ModItems.OBSIDIAN_ITEM.get())
                .define('x', Items.STICK)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.OBSIDIAN_SHOVEL.get())
                .pattern("#")
                .pattern("x")
                .pattern("x")
                .define('#', ModItems.OBSIDIAN_ITEM.get())
                .define('x', Items.STICK)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.OBSIDIAN_HOE.get())
                .pattern(" ##")
                .pattern(" x ")
                .pattern(" x ")
                .define('#', ModItems.OBSIDIAN_ITEM.get())
                .define('x', Items.STICK)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.OBSIDIAN_ITEM.get())
                .pattern("xxx")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.NETHERITE_INGOT)
                .define('x', Items.OBSIDIAN)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OBSIDIAN_HELMET.get())
                .pattern("xxx")
                .pattern("x x")
                .define('x', ModItems.OBSIDIAN_ITEM.get())
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OBSIDIAN_CHESTPLATE.get())
                .pattern("x x")
                .pattern("xxx")
                .pattern("xxx")
                .define('x', ModItems.OBSIDIAN_ITEM.get())
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OBSIDIAN_LEGGINGS.get())
                .pattern("xxx")
                .pattern("x x")
                .pattern("x x")
                .define('x', ModItems.OBSIDIAN_ITEM.get())
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OBSIDIAN_BOOTS.get())
                .pattern("x x")
                .pattern("x x")
                .define('x', ModItems.OBSIDIAN_ITEM.get())
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIRST_BULLETPROOF_HELMET.get())
                .pattern("zxz")
                .pattern("x#x")
                .define('#', Items.LEATHER_HELMET)
                .define('x', ItemTags.PLANKS)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.OAK_PLANKS), has(ItemTags.PLANKS))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIRST_BULLETPROOF_CHESTPLATE.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.LEATHER_CHESTPLATE)
                .define('x', ItemTags.PLANKS)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.OAK_PLANKS), has(ItemTags.PLANKS))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIRST_BULLETPROOF_LEGGINGS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.LEATHER_LEGGINGS)
                .define('x', ItemTags.PLANKS)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.OAK_PLANKS), has(ItemTags.PLANKS))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIRST_BULLETPROOF_BOOTS.get())
                .pattern("x x")
                .pattern("z#z")
                .pattern("x x")
                .define('#', Items.LEATHER_BOOTS)
                .define('x', ItemTags.PLANKS)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.OAK_PLANKS), has(ItemTags.PLANKS))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECOND_BULLETPROOF_HELMET.get())
                .pattern("zxz")
                .pattern("x#x")
                .define('#', Items.LEATHER_HELMET)
                .define('x', Items.COPPER_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECOND_BULLETPROOF_CHESTPLATE.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.LEATHER_CHESTPLATE)
                .define('x', Items.COPPER_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECOND_BULLETPROOF_LEGGINGS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.LEATHER_LEGGINGS)
                .define('x', Items.COPPER_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SECOND_BULLETPROOF_BOOTS.get())
                .pattern("x x")
                .pattern("z#z")
                .pattern("x x")
                .define('#', Items.LEATHER_BOOTS)
                .define('x', Items.COPPER_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.THIRD_BULLETPROOF_HELMET.get())
                .pattern("zxz")
                .pattern("x#x")
                .define('#', Items.IRON_HELMET)
                .define('x', Items.IRON_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.THIRD_BULLETPROOF_CHESTPLATE.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.IRON_CHESTPLATE)
                .define('x', Items.IRON_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.THIRD_BULLETPROOF_LEGGINGS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.IRON_LEGGINGS)
                .define('x', Items.IRON_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.THIRD_BULLETPROOF_BOOTS.get())
                .pattern("x x")
                .pattern("z#z")
                .pattern("x x")
                .define('#', Items.IRON_BOOTS)
                .define('x', Items.IRON_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FOURTH_BULLETPROOF_HELMET.get())
                .pattern("zxz")
                .pattern("x#x")
                .define('#', Items.DIAMOND_HELMET)
                .define('x', Items.DIAMOND)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FOURTH_BULLETPROOF_CHESTPLATE.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.DIAMOND_CHESTPLATE)
                .define('x', Items.DIAMOND)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FOURTH_BULLETPROOF_LEGGINGS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.DIAMOND_LEGGINGS)
                .define('x', Items.DIAMOND)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FOURTH_BULLETPROOF_BOOTS.get())
                .pattern("x x")
                .pattern("z#z")
                .pattern("x x")
                .define('#', Items.DIAMOND_BOOTS)
                .define('x', Items.DIAMOND)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIFTH_BULLETPROOF_HELMET.get())
                .pattern("zxz")
                .pattern("x#x")
                .define('#', Items.NETHERITE_HELMET)
                .define('x', Items.NETHERITE_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIFTH_BULLETPROOF_CHESTPLATE.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.NETHERITE_CHESTPLATE)
                .define('x', Items.NETHERITE_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIFTH_BULLETPROOF_LEGGINGS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.NETHERITE_LEGGINGS)
                .define('x', Items.NETHERITE_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FIFTH_BULLETPROOF_BOOTS.get())
                .pattern("x x")
                .pattern("z#z")
                .pattern("x x")
                .define('#', Items.NETHERITE_BOOTS)
                .define('x', Items.NETHERITE_INGOT)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SIXTH_BULLETPROOF_HELMET.get())
                .pattern("zxz")
                .pattern("x#x")
                .define('#', ModItems.OBSIDIAN_HELMET.get())
                .define('x', ModItems.SIXTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SIXTH_BULLETPROOF_CHESTPLATE.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', ModItems.OBSIDIAN_CHESTPLATE.get())
                .define('x', ModItems.SIXTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SIXTH_BULLETPROOF_LEGGINGS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', ModItems.OBSIDIAN_LEGGINGS.get())
                .define('x', ModItems.SIXTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SIXTH_BULLETPROOF_BOOTS.get())
                .pattern("x x")
                .pattern("z#z")
                .pattern("x x")
                .define('#', ModItems.OBSIDIAN_BOOTS.get())
                .define('x', ModItems.SIXTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SEVENTH_BULLETPROOF_HELMET.get())
                .pattern("zxz")
                .pattern("x#x")
                .define('#', ModItems.OBSIDIAN_HELMET.get())
                .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.SEVENTH_STEEL_INGOT.get()), has(ModItems.SEVENTH_STEEL_INGOT.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SEVENTH_BULLETPROOF_CHESTPLATE.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', ModItems.OBSIDIAN_CHESTPLATE.get())
                .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.SEVENTH_STEEL_INGOT.get()), has(ModItems.SEVENTH_STEEL_INGOT.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SEVENTH_BULLETPROOF_LEGGINGS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', ModItems.OBSIDIAN_LEGGINGS.get())
                .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.SEVENTH_STEEL_INGOT.get()), has(ModItems.SEVENTH_STEEL_INGOT.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SEVENTH_BULLETPROOF_BOOTS.get())
                .pattern("z z")
                .pattern("x#x")
                .pattern("x x")
                .define('#', ModItems.OBSIDIAN_BOOTS.get())
                .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.SEVENTH_STEEL_INGOT.get()), has(ModItems.SEVENTH_STEEL_INGOT.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SPACETIME_BULLETPROOF_HELMET.get())
            .pattern("zxz")
            .pattern("x#x")
            .define('#', ModItems.OBSIDIAN_HELMET.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('z', ModItems.SPACETIME_STONE.get())
            .unlockedBy(getHasName(ModItems.SPACETIME_STONE.get()), has(ModItems.SPACETIME_STONE.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SPACETIME_BULLETPROOF_CHESTPLATE.get())
            .pattern("z z")
            .pattern("x#x")
            .pattern("xxx")
            .define('#', ModItems.OBSIDIAN_CHESTPLATE.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('z', ModItems.SPACETIME_STONE.get())
            .unlockedBy(getHasName(ModItems.SPACETIME_STONE.get()), has(ModItems.SPACETIME_STONE.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SPACETIME_BULLETPROOF_LEGGINGS.get())
            .pattern("z z")
            .pattern("x#x")
            .pattern("xxx")
            .define('#', ModItems.OBSIDIAN_LEGGINGS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('z', ModItems.SPACETIME_STONE.get())
            .unlockedBy(getHasName(ModItems.SPACETIME_STONE.get()), has(ModItems.SPACETIME_STONE.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SPACETIME_BULLETPROOF_BOOTS.get())
            .pattern("z z")
            .pattern("x#x")
            .pattern("x x")
            .define('#', ModItems.OBSIDIAN_BOOTS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('z', ModItems.SPACETIME_STONE.get())
            .unlockedBy(getHasName(ModItems.SPACETIME_STONE.get()), has(ModItems.SPACETIME_STONE.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.POWER_MECH_BULLETPROOF_HELMET.get())
            .pattern("xyx")
            .pattern("x#x")
            .define('#', ModItems.OBSIDIAN_HELMET.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_GOLD.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_GOLD.get()), has(ModItems.ENERGY_CORE_GOLD.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.POWER_MECH_BULLETPROOF_CHESTPLATE.get())
            .pattern("xyx")
            .pattern("x#x")
            .pattern("zxz")
            .define('#', ModItems.OBSIDIAN_CHESTPLATE.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_GOLD.get())
            .define('z', ModItems.POWER_THRUSTER_GOLD.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_GOLD.get()), has(ModItems.ENERGY_CORE_GOLD.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.POWER_MECH_BULLETPROOF_LEGGINGS.get())
            .pattern("xyx")
            .pattern("x#x")
            .pattern("x x")
            .define('#', ModItems.OBSIDIAN_LEGGINGS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_GOLD.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_GOLD.get()), has(ModItems.ENERGY_CORE_GOLD.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.POWER_MECH_BULLETPROOF_BOOTS.get())
            .pattern("zyz")
            .pattern("x#x")
            .pattern("x x")
            .define('#', ModItems.OBSIDIAN_BOOTS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_GOLD.get())
            .define('z', ModItems.POWER_THRUSTER_GOLD.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_GOLD.get()), has(ModItems.ENERGY_CORE_GOLD.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHOCKWAVE_BULLETPROOF_HELMET.get())
            .pattern("xyx")
            .pattern("z#z")
            .define('#', ModItems.OBSIDIAN_HELMET.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_RED.get())
            .define('z', ModItems.POWER_THRUSTER_RED.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_RED.get()), has(ModItems.ENERGY_CORE_RED.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHOCKWAVE_BULLETPROOF_CHESTPLATE.get())
            .pattern("zyz")
            .pattern("x#x")
            .pattern("zxz")
            .define('#', ModItems.OBSIDIAN_CHESTPLATE.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_RED.get())
            .define('z', ModItems.POWER_THRUSTER_RED.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_RED.get()), has(ModItems.ENERGY_CORE_RED.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHOCKWAVE_BULLETPROOF_LEGGINGS.get())
            .pattern("zyz")
            .pattern("z#z")
            .pattern("x x")
            .define('#', ModItems.OBSIDIAN_LEGGINGS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_RED.get())
            .define('z', ModItems.POWER_THRUSTER_RED.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_RED.get()), has(ModItems.ENERGY_CORE_RED.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHOCKWAVE_BULLETPROOF_BOOTS.get())
            .pattern("zyz")
            .pattern("x#x")
            .pattern("x x")
            .define('#', ModItems.OBSIDIAN_BOOTS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.ENERGY_CORE_RED.get())
            .define('z', ModItems.POWER_THRUSTER_RED.get())
            .unlockedBy(getHasName(ModItems.POWER_THRUSTER_RED.get()), has(ModItems.ENERGY_CORE_RED.get()))
            .save(writer);
        //_________________________________________________________________________________________________________
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEALTH_SEVENTH_HELMET.get())
            .pattern("xyx")
            .pattern("y#y")
            .pattern("xyx")
            .define('#', ModItems.STEALTH_HELMET.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.LIGHT_ABSORBING_TUBE.get())
            .unlockedBy(getHasName(ModItems.LIGHT_ABSORBING_TUBE.get()), has(ModItems.LIGHT_ABSORBING_TUBE.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEALTH_SEVENTH_CHESTPLATE.get())
            .pattern("xyx")
            .pattern("y#y")
            .pattern("xyx")
            .define('#', ModItems.STEALTH_CLOAK.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.LIGHT_ABSORBING_TUBE.get())
            .unlockedBy(getHasName(ModItems.LIGHT_ABSORBING_TUBE.get()), has(ModItems.LIGHT_ABSORBING_TUBE.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEALTH_SEVENTH_LEGGINGS.get())
            .pattern("xyx")
            .pattern("y#y")
            .pattern("xyx")
            .define('#', ModItems.STEALTH_LEGGINGS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.LIGHT_ABSORBING_TUBE.get())
            .unlockedBy(getHasName(ModItems.LIGHT_ABSORBING_TUBE.get()), has(ModItems.LIGHT_ABSORBING_TUBE.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEALTH_SEVENTH_BOOTS.get())
            .pattern("xyx")
            .pattern("y#y")
            .pattern("xyx")
            .define('#', ModItems.STEALTH_BOOTS.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', ModItems.LIGHT_ABSORBING_TUBE.get())
            .unlockedBy(getHasName(ModItems.LIGHT_ABSORBING_TUBE.get()), has(ModItems.LIGHT_ABSORBING_TUBE.get()))
            .save(writer);

        //_________________________________________________________________________________________________________
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GRENADE.get())
                .pattern("zyz")
                .pattern("zxz")
                .pattern("zzz")
                .define('x', Items.TNT)
                .define('y', Items.TRIPWIRE_HOOK)
                .define('z', Items.IRON_INGOT)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MOLOTOV.get())
                .pattern(" z ")
                .pattern("yyy")
                .pattern(" x ")
                .define('x', Items.GLASS_BOTTLE)
                .define('y', Items.GUNPOWDER)
                .define('z', Items.STRING)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LANDMINE.get())
                .pattern(" z ")
                .pattern("yyy")
                .pattern("xxx")
                .define('x', Items.TNT)
                .define('y', Items.IRON_INGOT)
                .define('z', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FROZEN_LANDMINE.get())
            .pattern(" z ")
            .pattern("yxy")
            .pattern("yyy")
            .define('x', Items.BLUE_ICE)
            .define('y', Items.IRON_INGOT)
            .define('z', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
            .unlockedBy(getHasName(ModItems.OBSIDIAN_ITEM.get()), has(ModItems.OBSIDIAN_ITEM.get()))
            .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INFINITE_NETWORK_RECEIVER.get())
            .pattern("x x")
            .pattern("xyx")
            .pattern("xxx")
            .define('x', Items.IRON_INGOT)
            .define('y', Items.REPEATER)
            .unlockedBy(getHasName(Items.REPEATER), has(Items.REPEATER))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPUTER_CASE.get())
            .pattern("xyx")
            .pattern("xyx")
            .pattern("xzx")
            .define('x', Items.IRON_INGOT)
            .define('y', Items.REDSTONE)
            .define('z', Items.REDSTONE_BLOCK)
            .unlockedBy(getHasName(Items.REDSTONE_BLOCK), has(Items.REDSTONE_BLOCK))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MONITOR.get())
            .pattern("xzx")
            .pattern("xyx")
            .pattern(" x ")
            .define('x', Items.IRON_INGOT)
            .define('y', Items.GLASS_PANE)
            .define('z', Items.REDSTONE)
            .unlockedBy(getHasName(Items.REDSTONE), has(Items.REDSTONE))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BLACK_MARKET_CONTACT.get())
            .pattern("zxy")
            .pattern("sss")
            .define('s', ItemTags.WOODEN_SLABS)
            .define('x', ModItems.MONITOR.get())
            .define('y', ModItems.INFINITE_NETWORK_RECEIVER.get())
            .define('z', ModItems.COMPUTER_CASE.get())
            .unlockedBy(getHasName(ModItems.MONITOR.get()), has(ModItems.MONITOR.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SIGNAL_SUMMONING_BLOCK.get())
            .pattern("xxx")
            .pattern("xzx")
            .pattern("xyx")
            .define('x', Items.IRON_INGOT)
            .define('y', Items.FIRE_CHARGE)
            .define('z', Items.REDSTONE_BLOCK)
            .unlockedBy(getHasName(Items.REDSTONE_BLOCK), has(Items.REDSTONE_BLOCK))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BODYGUARD_STORAGE.get())
            .pattern("xzx")
            .pattern("xyx")
            .pattern("xxx")
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('y', Items.ENDER_CHEST)
            .define('z', Items.NETHER_STAR)
            .unlockedBy(getHasName(Items.REDSTONE_BLOCK), has(Items.REDSTONE_BLOCK))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.DETECTION_GRENADE.get())
            .pattern("xzx")
            .pattern("aya")
            .pattern(" x ")
            .define('a', ModItems.SIXTH_STEEL_INGOT.get())
            .define('x', Items.IRON_INGOT)
            .define('y', Items.SPECTRAL_ARROW)
            .define('z', Items.TNT)
            .unlockedBy(getHasName(Items.REDSTONE_BLOCK), has(Items.REDSTONE_BLOCK))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FLASHBANG.get())
            .pattern("zyz")
            .pattern("gxg")
            .pattern("zzz")
            .define('z', ModItems.SIXTH_STEEL_INGOT.get())
            .define('y', Items.TRIPWIRE_HOOK)
            .define('x', Items.GLOWSTONE_DUST)
            .define('g', Items.GUNPOWDER)
            .unlockedBy(getHasName(ModItems.SIXTH_STEEL_INGOT.get()), has(ModItems.SIXTH_STEEL_INGOT.get()))
            .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ARMOR_REPAIR_KIT.get())
            .pattern("xxx")
            .pattern("ygy")
            .pattern("zzz")
            .define('z', ModItems.SIXTH_STEEL_INGOT.get())
            .define('y', ModItems.OBSIDIAN_ITEM.get())
            .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
            .define('g', Items.ANVIL)
            .unlockedBy(getHasName(ModItems.SIXTH_STEEL_INGOT.get()), has(ModItems.SEVENTH_STEEL_INGOT.get()))
            .save(writer);

        addSixthSteelCrafting(writer);
        addNeedleCrafting(writer);
        addSeventhVariantSmithing(writer);
        addBlackUniversalCurrencyRecipes(writer);
        addPluginWorkbenchAndArmorPlateCrafting(writer);
    }

    /**
     * 插件装备台：铁锭围工作台。三类护板初级：对应材料围一圈七级钢；中级：初级板再围一圈钢。
     * 高级板与防闪光护目镜无合成。
     */
    private static void addPluginWorkbenchAndArmorPlateCrafting(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PLUGIN_WORKBENCH.get())
                .pattern("xxx")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', Items.CRAFTING_TABLE)
                .define('x', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.CRAFTING_TABLE), has(Items.CRAFTING_TABLE))
                .save(writer);

        var steelUnlock = has(ModItems.SEVENTH_STEEL_INGOT.get());
        addSeventhSteelRingRecipe(
                writer,
                ModItems.CARBON_FIBER_ARMOR_PLATE_JUNIOR.get(),
                Items.DRAGON_EGG,
                steelUnlock,
                "carbon_fiber_armor_plate_junior");
        addSeventhSteelRingRecipe(
                writer,
                ModItems.BALLISTIC_STEEL_ARMOR_PLATE_JUNIOR.get(),
                Items.SCULK_CATALYST,
                steelUnlock,
                "ballistic_steel_armor_plate_junior");
        addSeventhSteelRingRecipe(
                writer,
                ModItems.BLAST_STEEL_ARMOR_PLATE_JUNIOR.get(),
                Items.NETHER_STAR,
                steelUnlock,
                "blast_steel_armor_plate_junior");
        addSeventhSteelRingRecipe(
                writer,
                ModItems.CARBON_FIBER_ARMOR_PLATE_MEDIUM.get(),
                ModItems.CARBON_FIBER_ARMOR_PLATE_JUNIOR.get(),
                steelUnlock,
                "carbon_fiber_armor_plate_medium");
        addSeventhSteelRingRecipe(
                writer,
                ModItems.BALLISTIC_STEEL_ARMOR_PLATE_MEDIUM.get(),
                ModItems.BALLISTIC_STEEL_ARMOR_PLATE_JUNIOR.get(),
                steelUnlock,
                "ballistic_steel_armor_plate_medium");
        addSeventhSteelRingRecipe(
                writer,
                ModItems.BLAST_STEEL_ARMOR_PLATE_MEDIUM.get(),
                ModItems.BLAST_STEEL_ARMOR_PLATE_JUNIOR.get(),
                steelUnlock,
                "blast_steel_armor_plate_medium");
    }

    private static void addSeventhSteelRingRecipe(
            Consumer<FinishedRecipe> writer,
            net.minecraft.world.level.ItemLike result,
            net.minecraft.world.level.ItemLike center,
            net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance steelUnlock,
            String id) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
                .pattern("xxx")
                .pattern("x#x")
                .pattern("xxx")
                .define('#', center)
                .define('x', ModItems.SEVENTH_STEEL_INGOT.get())
                .unlockedBy("has_seventh_steel_ingot", steelUnlock)
                .save(writer, rl(id));
    }

    /** 工作台：金锭 + 下界之星/龙蛋 → 六级钢锭。 */
    private static void addSixthSteelCrafting(Consumer<FinishedRecipe> writer) {
        var unlock = has(Items.GOLD_INGOT);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SIXTH_STEEL_INGOT.get())
                .requires(Items.GOLD_INGOT)
                .requires(Items.NETHER_STAR)
                .unlockedBy("has_gold_ingot", unlock)
                .save(writer, rl("sixth_steel_ingot_from_gold_and_star"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SIXTH_STEEL_INGOT.get())
                .requires(Items.GOLD_INGOT)
                .requires(Items.DRAGON_EGG)
                .unlockedBy("has_gold_ingot", unlock)
                .save(writer, rl("sixth_steel_ingot_from_gold_and_dragon_egg"));
    }

    /**
     * 注射器 + 龙蛋 + 2/4/6 史莱姆血清 → 初/中/高级综合免疫针；
     * 注射器 + 下界之星 + 2/4/6 史莱姆血清 → 初/中/高级硬化针。
     */
    private static void addNeedleCrafting(Consumer<FinishedRecipe> writer) {
        var unlock = has(ModItems.SYRINGE.get());

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.COMPREHENSIVE_IMMUNITY_NEEDLE_JUNIOR.get())
                .requires(ModItems.SYRINGE.get())
                .requires(Items.DRAGON_EGG)
                .requires(ModItems.SLIME_SERUM.get(), 2)
                .unlockedBy("has_syringe", unlock)
                .save(writer, rl("comprehensive_immunity_needle_junior"));
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.COMPREHENSIVE_IMMUNITY_NEEDLE_MEDIUM.get())
                .requires(ModItems.SYRINGE.get())
                .requires(Items.DRAGON_EGG)
                .requires(ModItems.SLIME_SERUM.get(), 4)
                .unlockedBy("has_syringe", unlock)
                .save(writer, rl("comprehensive_immunity_needle_medium"));
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.COMPREHENSIVE_IMMUNITY_NEEDLE_ADVANCED.get())
                .requires(ModItems.SYRINGE.get())
                .requires(Items.DRAGON_EGG)
                .requires(ModItems.SLIME_SERUM.get(), 6)
                .unlockedBy("has_syringe", unlock)
                .save(writer, rl("comprehensive_immunity_needle_advanced"));

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.HARDENING_NEEDLE_JUNIOR.get())
                .requires(ModItems.SYRINGE.get())
                .requires(Items.NETHER_STAR)
                .requires(ModItems.SLIME_SERUM.get(), 2)
                .unlockedBy("has_syringe", unlock)
                .save(writer, rl("hardening_needle_junior"));
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.HARDENING_NEEDLE_MEDIUM.get())
                .requires(ModItems.SYRINGE.get())
                .requires(Items.NETHER_STAR)
                .requires(ModItems.SLIME_SERUM.get(), 4)
                .unlockedBy("has_syringe", unlock)
                .save(writer, rl("hardening_needle_medium"));
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.HARDENING_NEEDLE_ADVANCED.get())
                .requires(ModItems.SYRINGE.get())
                .requires(Items.NETHER_STAR)
                .requires(ModItems.SLIME_SERUM.get(), 6)
                .unlockedBy("has_syringe", unlock)
                .save(writer, rl("hardening_needle_advanced"));
    }

    /** 黑色通用货币：8 张 n 位 → 1 张 (n+1) 位；1 张高一级 → 8 张低一级。 */
    private static void addBlackUniversalCurrencyRecipes(Consumer<FinishedRecipe> writer) {
        var unlock =
                has(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_1.get());

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_2.get())
                .requires(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_1.get(), 8)
                .unlockedBy("has_black_universal_currency_digit_1", unlock)
                .save(writer, rl("currency/digit_1_combine_to_2"));
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_1.get(), 8)
                .requires(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_2.get())
                .unlockedBy("has_black_universal_currency_digit_1", unlock)
                .save(writer, rl("currency/digit_2_split_to_1"));

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_3.get())
                .requires(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_2.get(), 8)
                .unlockedBy("has_black_universal_currency_digit_1", unlock)
                .save(writer, rl("currency/digit_2_combine_to_3"));
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_2.get(), 8)
                .requires(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_3.get())
                .unlockedBy("has_black_universal_currency_digit_1", unlock)
                .save(writer, rl("currency/digit_3_split_to_2"));

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_4.get())
                .requires(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_3.get(), 8)
                .unlockedBy("has_black_universal_currency_digit_1", unlock)
                .save(writer, rl("currency/digit_3_combine_to_4"));
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC, ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_3.get(), 8)
                .requires(ModItems.BLACK_UNIVERSAL_CURRENCY_DIGIT_4.get())
                .unlockedBy("has_black_universal_currency_digit_1", unlock)
                .save(writer, rl("currency/digit_4_split_to_3"));
    }

    /**
     * 锻造台：七级防弹对应部位 + 超级升级模板 + 下界之星 → 爆炎七级；+ 龙蛋 → 深渊；+ 幽匿催发体 → 幽匿七级。
     */
    private static void addSeventhVariantSmithing(Consumer<FinishedRecipe> writer) {
        Ingredient template = Ingredient.of(ModItems.SUPER_UPGRADE_TEMPLATE.get());
        Ingredient star = Ingredient.of(Items.NETHER_STAR);
        Ingredient dragonEgg = Ingredient.of(Items.DRAGON_EGG);
        Ingredient sculkCatalyst = Ingredient.of(Items.SCULK_CATALYST);
        var unlock = has(ModItems.SUPER_UPGRADE_TEMPLATE.get());

        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_HELMET.get()),
                        star,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_HELMET.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_blazeflame_helmet"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_CHESTPLATE.get()),
                        star,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_CHESTPLATE.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_blazeflame_chestplate"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_LEGGINGS.get()),
                        star,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_LEGGINGS.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_blazeflame_leggings"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_BOOTS.get()),
                        star,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_BOOTS.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_blazeflame_boots"));

        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_HELMET.get()),
                        dragonEgg,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_ABYSS_BULLETPROOF_HELMET.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_abyss_helmet"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_CHESTPLATE.get()),
                        dragonEgg,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_ABYSS_BULLETPROOF_CHESTPLATE.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_abyss_chestplate"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_LEGGINGS.get()),
                        dragonEgg,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_ABYSS_BULLETPROOF_LEGGINGS.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_abyss_leggings"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_BOOTS.get()),
                        dragonEgg,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_ABYSS_BULLETPROOF_BOOTS.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_abyss_boots"));

        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_HELMET.get()),
                        sculkCatalyst,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_SCULK_BULLETPROOF_HELMET.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_sculk_helmet"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_CHESTPLATE.get()),
                        sculkCatalyst,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_SCULK_BULLETPROOF_CHESTPLATE.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_sculk_chestplate"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_LEGGINGS.get()),
                        sculkCatalyst,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_SCULK_BULLETPROOF_LEGGINGS.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_sculk_leggings"));
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(ModItems.SEVENTH_BULLETPROOF_BOOTS.get()),
                        sculkCatalyst,
                        RecipeCategory.COMBAT,
                        ModItems.SEVENTH_SCULK_BULLETPROOF_BOOTS.get())
                .unlocks("has_super_upgrade_template", unlock)
                .save(writer, rl("smithing/seventh_to_sculk_boots"));
      
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, path);
    }
}
