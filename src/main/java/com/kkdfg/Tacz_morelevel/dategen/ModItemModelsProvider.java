package com.kkdfg.Tacz_morelevel.dategen;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 背包模型：与 Forge {@link ItemModelProvider#basicItem(Item)} 相同的注册名与 generated 父模型。
 * <p>
 * 防弹甲使用 {@link #basicItem(Item)}，对应 {@code assets/<modid>/textures/item/<registry_path>.png}。
 * 刷怪蛋默认用 {@link #spawnEggItem(Item)}（父模型 {@code template_spawn_egg}），使 {@link net.minecraftforge.common.ForgeSpawnEggItem} 底色/斑点色生效。
 * 潜伏者、毒蝎等自带 {@code textures/item/<id>.png} 的刷怪蛋改用 {@link #basicItem(Item)}。
 */
public class ModItemModelsProvider extends ItemModelProvider {

    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Tacz_MoreLevel.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.ALL_SPAWN_EGGS.forEach(
                ro -> {
                    // 潜伏者 / 毒蝎：自定义 16×16 贴图（src/main/resources/.../textures/item/*_spawn_egg.png）
                    if (ro == ModItems.LURKER_SPAWN_EGG || ro == ModItems.SCORPION_SPAWN_EGG) {
                        basicItem(ro.get());
                        return;
                    }
                    spawnEggItem(ro.get());
                });
        // 护卫刷怪蛋不进创造栏，但仍需 template_spawn_egg 模型
        spawnEggItem(ModItems.GUARD_SPAWN_EGG.get());
        // 护航联络器：Gecko 模型在 src/main/resources/models/item/ 手写（parent builtin/entity），勿 basicItem。
        ModItems.ALL_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        // 爆破七级：背包图标暂用 textures/item/seventh_blast_bulletproof_<部位>.png（穿戴贴图占位）
        ModItems.SEVENTH_BLAST_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.SHOCKWAVE_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        // 爆炎七级：背包图标使用 textures/item/seventh_blazeflame_bulletproof_<部位>.png
        ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.SEVENTH_ABYSS_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.SEVENTH_SCULK_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.PUNISHER_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.SEVENTH_RADIATION_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.POWER_MECH_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.OFFICER_UNIFORM_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.POLICE_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.JOKER_SEVENTH_HELMET_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.SPACETIME_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.SPECIAL_OPS_BULLETPROOF_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.STEALTH_CLOAK_ITEMS.forEach(ro -> basicItem(ro.get()));
        ModItems.STEALTH_SEVENTH_ITEMS.forEach(ro -> basicItem(ro.get()));
        withExistingParent(ModItems.OBSIDIAN_SWORD.getId().getPath(), mcLoc("item/handheld")).texture("layer0", modLoc("item/obsidian_sword"));
        withExistingParent(ModItems.OBSIDIAN_PICKAXE.getId().getPath(), mcLoc("item/handheld")).texture("layer0", modLoc("item/obsidian_pickaxe"));
        withExistingParent(ModItems.OBSIDIAN_AXE.getId().getPath(), mcLoc("item/handheld")).texture("layer0", modLoc("item/obsidian_axe"));
        withExistingParent(ModItems.OBSIDIAN_SHOVEL.getId().getPath(), mcLoc("item/handheld")).texture("layer0", modLoc("item/obsidian_shovel"));
        withExistingParent(ModItems.OBSIDIAN_HOE.getId().getPath(), mcLoc("item/handheld")).texture("layer0", modLoc("item/obsidian_hoe"));

        basicItem(ModItems.OBSIDIAN_ITEM.get());
        basicItem(ModItems.OBSIDIAN_HELMET.get());
        basicItem(ModItems.OBSIDIAN_CHESTPLATE.get());
        basicItem(ModItems.OBSIDIAN_LEGGINGS.get());
        basicItem(ModItems.OBSIDIAN_BOOTS.get());
        basicItem(ModItems.SIXTH_STEEL_INGOT.get());
        basicItem(ModItems.SEVENTH_STEEL_INGOT.get());
        basicItem(ModItems.EXPLOSION_PROOF_STEEL_INGOT.get());
        basicItem(ModItems.SUPER_UPGRADE_TEMPLATE.get());
        ModItems.ALL_BLACK_UNIVERSAL_CURRENCIES.forEach(ro -> basicItem(ro.get()));
        basicItem(ModItems.SUPERPOTIONBOTTLE.get());
        // 维护板 / 护目镜：初/中/高共用一张 base 贴图（textures/item/<base>.png），
        // 不能用 basicItem（那会去找 <注册名>.png，不存在）。
        registerTieredItemModels(
                "carbon_fiber_armor_plate",
                "ballistic_steel_armor_plate",
                "blast_steel_armor_plate",
                "radiation_resist_armor_plate",
                "flash_resist_goggles");
        // 枪械升级券：普通 generated 图标。
        ModItems.ALL_GUN_UPGRADE_VOUCHERS.forEach(ro -> basicItem(ro.get()));
        basicItem(ModItems.BANDAGE.get());
        basicItem(ModItems.SLIME_SERUM.get());
        basicItem(ModItems.SLIME_COMMANDER_FACE.get());
        withExistingParent(
                ModItems.LANDMINE.getId().getPath(),
                modLoc("block/" + ModBlocks.LANDMINE.getId().getPath()));
        withExistingParent(
                ModItems.FROZEN_LANDMINE.getId().getPath(),
                modLoc("block/" + ModBlocks.FROZEN_LANDMINE.getId().getPath()));
        // 防爆钢块：模型由 ModBlockStateProvider.simpleBlockWithItem 生成，勿重复注册。
        // 护航存储器：Gecko 模型在 src/main/resources/models/item/bodyguard_storage.json 手写。
        // GeckoLib 方块物品：模型在 src/main/resources/models/item/ 手写（parent 为 builtin/entity），
        // 勿在此用 withExistingParent(mcLoc("builtin/entity"))，否则 runData 报 Model does not exist。
    }

    private void spawnEggItem(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            return;
        }
        withExistingParent(id.getPath(), mcLoc("item/template_spawn_egg"));
    }

    /**
     * 维护板 / 护目镜：初（junior）/ 中（medium）/ 高（advanced）三档共用同一张
     * {@code textures/item/<base>.png}，因此不能走 {@link #basicItem}。
     */
    private void registerTieredItemModels(String... bases) {
        for (String base : bases) {
            for (String tier : new String[] {"junior", "medium", "advanced"}) {
                withExistingParent(base + "_" + tier, mcLoc("item/generated"))
                        .texture("layer0", modLoc("item/" + base));
            }
        }
    }
}
