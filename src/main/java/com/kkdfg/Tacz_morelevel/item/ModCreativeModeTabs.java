package com.kkdfg.Tacz_morelevel.item;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Tacz_MoreLevel.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register(
                    "main",
                    () ->
                            CreativeModeTab.builder()
                                    .icon(() -> new ItemStack(ModItems.SEVENTH_BULLETPROOF_HELMET.get()))
                                    .title(Component.translatable("itemGroup.tacz_morelevel.main"))
                                    .displayItems(
                                            (params, output) -> {
                                                output.accept(ModItems.GRENADE.get());
                                                output.accept(ModItems.FLASHBANG.get());
                                                output.accept(ModItems.MOLOTOV.get());
                                                output.accept(ModItems.DETECTION_GRENADE.get());
                                                output.accept(ModItems.LANDMINE.get());
                                                output.accept(ModItems.FROZEN_LANDMINE.get());
                                                output.accept(ModItems.SUPERPOTIONBOTTLE.get());
                                                ModItems.ALL_GUN_UPGRADE_VOUCHERS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.ALL_BLACK_UNIVERSAL_CURRENCIES.forEach(ro -> output.accept(ro.get()));
                                                output.accept(ModItems.SIXTH_STEEL_INGOT.get());
                                                output.accept(ModItems.SEVENTH_STEEL_INGOT.get());
                                                output.accept(ModItems.EXPLOSION_PROOF_STEEL_INGOT.get());
                                                ModItems.ALL_ARMOR_PLATE_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                output.accept(ModItems.SLIME_SERUM.get());
                                                output.accept(ModItems.EXPLOSION_PROOF_STEEL_BLOCK.get());
                                                output.accept(ModItems.PLUGIN_WORKBENCH.get());
                                                output.accept(ModItems.SUPER_UPGRADE_TEMPLATE.get());
                                                output.accept(ModItems.OBSIDIAN_SWORD.get());
                                                output.accept(ModItems.OBSIDIAN_PICKAXE.get());
                                                output.accept(ModItems.OBSIDIAN_AXE.get());
                                                output.accept(ModItems.OBSIDIAN_SHOVEL.get());
                                                output.accept(ModItems.OBSIDIAN_HOE.get());
                                                output.accept(ModItems.OBSIDIAN_ITEM.get());
                                                output.accept(ModItems.OBSIDIAN_HELMET.get());
                                                output.accept(ModItems.OBSIDIAN_CHESTPLATE.get());
                                                output.accept(ModItems.OBSIDIAN_LEGGINGS.get());
                                                output.accept(ModItems.OBSIDIAN_BOOTS.get());
                                                output.accept(ModItems.SIGNAL_SUMMONING_BLOCK.get());
                                                output.accept(ModItems.BLACK_MARKET_CONTACT.get());
                                                output.accept(ModItems.LIMITED_SPAWNER.get());
                                                output.accept(ModItems.INFINITE_NETWORK_RECEIVER.get());
                                                output.accept(ModItems.MONITOR.get());
                                                output.accept(ModItems.COMPUTER_CASE.get());
                                                ModItems.ALL_SPAWN_EGGS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.ALL_BODYGUARD_CONTACT_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                output.accept(ModItems.BODYGUARD_STORAGE.get());
                                                ModItems.ALL_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SEVENTH_BLAST_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SHOCKWAVE_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SEVENTH_ABYSS_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SEVENTH_SCULK_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.PUNISHER_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SEVENTH_RADIATION_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.POWER_MECH_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SPECIAL_OPS_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.OFFICER_UNIFORM_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.STEALTH_CLOAK_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.STEALTH_SEVENTH_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.JOKER_SEVENTH_HELMET_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                ModItems.SPACETIME_BULLETPROOF_ITEMS.forEach(ro -> output.accept(ro.get()));
                                                output.accept(ModItems.SPACETIME_STONE.get());
                                                output.accept(ModItems.ENERGY_CORE_GOLD.get());
                                                output.accept(ModItems.ENERGY_CORE_RED.get());
                                                output.accept(ModItems.POWER_THRUSTER_GOLD.get());
                                                output.accept(ModItems.POWER_THRUSTER_RED.get());
                                                output.accept(ModItems.LIGHT_ABSORBING_TUBE.get());
                                                output.accept(ModItems.SYRINGE.get());
                                                output.accept(ModItems.BANDAGE.get());
                                                output.accept(ModItems.FIRST_AID_KIT.get());
                                                output.accept(ModItems.MEDICAL_BOX.get());
                                                output.accept(ModItems.HARDENING_NEEDLE_JUNIOR.get());
                                                output.accept(ModItems.HARDENING_NEEDLE_MEDIUM.get());
                                                output.accept(ModItems.HARDENING_NEEDLE_ADVANCED.get());
                                                output.accept(ModItems.COMPREHENSIVE_IMMUNITY_NEEDLE_JUNIOR.get());
                                                output.accept(ModItems.COMPREHENSIVE_IMMUNITY_NEEDLE_MEDIUM.get());
                                                output.accept(ModItems.COMPREHENSIVE_IMMUNITY_NEEDLE_ADVANCED.get());
                                            })
                                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                                    .build());

//    /** 手榴弹、燃烧瓶等投掷物 */
//    public static final RegistryObject<CreativeModeTab> PROPS_TAB =
//            CREATIVE_MODE_TABS.register(
//                    "props",
//                    () ->
//                            CreativeModeTab.builder()
//                                    .icon(() -> new ItemStack(ModItems.GRENADE.get()))
//                                    .title(Component.translatable("itemGroup.tacz_morelevel.props"))
//                                    .displayItems(
//                                            (params, output) -> {
//                                                output.accept(ModItems.GRENADE.get());
//                                                output.accept(ModItems.MOLOTOV.get());
//                                                output.accept(ModItems.LANDMINE.get());
//                                                output.accept(ModItems.FROZEN_LANDMINE.get());
//                                            })
//                                    .withTabsAfter(
//                                            ResourceKey.create(
//                                                    Registries.CREATIVE_MODE_TAB,
//                                                    ResourceLocation.fromNamespaceAndPath(
//                                                            Tacz_MoreLevel.MOD_ID, "main")))
//                                    .build());
//    public static final RegistryObject<CreativeModeTab> TACZ_MUSICTAB =
//            CREATIVE_MODE_TABS.register("tacz_music_tab", () -> CreativeModeTab.builder()
//                    .icon(() -> new ItemStack(ModItems.OBSIDIAN_ITEM.get()))
//                    .title(Component.translatable("tacz_music_tab"))
//                    .displayItems((pParameters, pOutput) -> {
//                        ModItems.ALL_GUN_UPGRADE_VOUCHERS.forEach(ro -> pOutput.accept(ro.get()));
//                        ModItems.ALL_BLACK_UNIVERSAL_CURRENCIES.forEach(ro -> pOutput.accept(ro.get()));
//                        pOutput.accept(ModItems.SEVENTH_STEEL_INGOT.get());
//                        pOutput.accept(ModItems.SUPER_UPGRADE_TEMPLATE.get());
//                        pOutput.accept(ModItems.OBSIDIAN_SWORD.get());
//                        pOutput.accept(ModItems.OBSIDIAN_PICKAXE.get());
//                        pOutput.accept(ModItems.OBSIDIAN_AXE.get());
//                        pOutput.accept(ModItems.OBSIDIAN_SHOVEL.get());
//                        pOutput.accept(ModItems.OBSIDIAN_HOE.get());
//                        pOutput.accept(ModItems.OBSIDIAN_ITEM.get());
//                        pOutput.accept(ModItems.OBSIDIAN_HELMET.get());
//                        pOutput.accept(ModItems.OBSIDIAN_CHESTPLATE.get());
//                        pOutput.accept(ModItems.OBSIDIAN_LEGGINGS.get());
//                        pOutput.accept(ModItems.OBSIDIAN_BOOTS.get());
//                        pOutput.accept(ModItems.SIGNAL_SUMMONING_BLOCK.get());
//                        pOutput.accept(ModItems.BLACK_MARKET_CONTACT.get());
//                        pOutput.accept(ModItems.INFINITE_NETWORK_RECEIVER.get());
//                        pOutput.accept(ModItems.MONITOR.get());
//                        pOutput.accept(ModItems.COMPUTER_CASE.get());
//                    })
//                    .withTabsBefore(MAIN_TAB.getKey())
//                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private ModCreativeModeTabs() {}
}
