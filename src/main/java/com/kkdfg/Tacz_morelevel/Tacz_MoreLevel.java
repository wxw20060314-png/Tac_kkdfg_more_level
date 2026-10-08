package com.kkdfg.Tacz_morelevel;

import com.kkdfg.Tacz_morelevel.block.ModBlockEntities;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.entity.ModSpawnPlacements;
import com.kkdfg.Tacz_morelevel.entity.client.GrenadeEntityRenderer;
import com.kkdfg.Tacz_morelevel.entity.client.PlayerLikeHostileRenderer;
import com.kkdfg.Tacz_morelevel.entity.client.ScorpionRenderer;
import com.kkdfg.Tacz_morelevel.client.gui.ArmorPluginClientTooltip;
import com.kkdfg.Tacz_morelevel.client.gui.BlackMarketContactScreen;
import com.kkdfg.Tacz_morelevel.client.gui.PluginWorkbenchScreen;
import com.kkdfg.Tacz_morelevel.armor.plugin.ArmorPluginTooltip;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.menu.ModMenus;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import com.kkdfg.Tacz_morelevel.client.particle.DianbaoParticle;
import com.kkdfg.Tacz_morelevel.particle.ModParticles;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import com.kkdfg.Tacz_morelevel.worldgen.ModStructureTypes;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.kkdfg.Tacz_morelevel.config.ArmorConfig;
import com.kkdfg.Tacz_morelevel.config.BossConfig;
import com.kkdfg.Tacz_morelevel.config.NpcConfig;
import com.kkdfg.Tacz_morelevel.integration.superbwarfare.SuperbWarfareCompat;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Tacz_MoreLevel.MOD_ID)
public class Tacz_MoreLevel
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "tacz_morelevel";
    // Direct reference to a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    public Tacz_MoreLevel(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModParticles.register(modEventBus);
        ModStructureTypes.register(modEventBus);

        modEventBus.addListener(this::commonSetup);



        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        context.registerConfig(ModConfig.Type.COMMON, NpcConfig.SPEC, "tacz_morelevel/npc.toml");
        context.registerConfig(ModConfig.Type.COMMON, BossConfig.SPEC, "tacz_morelevel/bosses.toml");
        context.registerConfig(ModConfig.Type.COMMON, ArmorConfig.SPEC, "tacz_morelevel/armor.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetwork.init();
        SuperbWarfareCompat.bootstrap();
        event.enqueueWork(
                () -> {
                    ModSpawnPlacements.register();
                    for (var ro : ModItems.ALL_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SEVENTH_BLAST_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SHOCKWAVE_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SEVENTH_ABYSS_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SEVENTH_SCULK_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SEVENTH_RADIATION_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.POWER_MECH_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SPECIAL_OPS_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.SPACETIME_BULLETPROOF_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.STEALTH_CLOAK_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    for (var ro : ModItems.STEALTH_SEVENTH_ITEMS) {
                        GeoItem.registerSyncedAnimatable(
                                (software.bernie.geckolib.core.animatable.GeoAnimatable) ro.get());
                    }
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.JOKER_SEVENTH_BULLETPROOF_HELMET.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable) ModItems.GRENADE.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable) ModItems.FLASHBANG.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable) ModItems.MOLOTOV.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.DETECTION_GRENADE.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.SIGNAL_SUMMONING_BLOCK.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.BLACK_MARKET_CONTACT.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.PLUGIN_WORKBENCH.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.INFINITE_NETWORK_RECEIVER.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.SPACETIME_STONE.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.ENERGY_CORE_GOLD.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.ENERGY_CORE_RED.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.POWER_THRUSTER_GOLD.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.POWER_THRUSTER_RED.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.LIGHT_ABSORBING_TUBE.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable) ModItems.MONITOR.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.COMPUTER_CASE.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.BEAUTY_BODYGUARD_CONTACT.get());
                    GeoItem.registerSyncedAnimatable(
                            (software.bernie.geckolib.core.animatable.GeoAnimatable)
                                    ModItems.BODYGUARD_STORAGE.get());
                });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(
                    () -> {
                        MenuScreens.register(
                                ModMenus.BLACK_MARKET_CONTACT_MENU.get(),
                                BlackMarketContactScreen::new);
                        MenuScreens.register(
                                ModMenus.PLUGIN_WORKBENCH_MENU.get(),
                                PluginWorkbenchScreen::new);
                    });
        }

        @SubscribeEvent
        public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(ArmorPluginTooltip.class, ArmorPluginClientTooltip::new);
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(
                    ModParticles.DIANBAO.get(), DianbaoParticle.Provider::new);
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                    ModBlockEntities.SIGNAL_SUMMONING_BLOCK_ENTITY.get(),
                    ctx ->
                            new GeoBlockRenderer<>(
                                    new DefaultedBlockGeoModel<>(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    Tacz_MoreLevel.MOD_ID, "signal_summoning_block"))));
            event.registerBlockEntityRenderer(
                    ModBlockEntities.BLACK_MARKET_CONTACT_BLOCK_ENTITY.get(),
                    ctx ->
                            new GeoBlockRenderer<>(
                                    new DefaultedBlockGeoModel<>(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    Tacz_MoreLevel.MOD_ID, "black_market_contact"))));
            event.registerBlockEntityRenderer(
                    ModBlockEntities.PLUGIN_WORKBENCH_BLOCK_ENTITY.get(),
                    ctx ->
                            new GeoBlockRenderer<>(
                                    new DefaultedBlockGeoModel<>(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    Tacz_MoreLevel.MOD_ID, "plugin_workbench"))));
            event.registerBlockEntityRenderer(
                    ModBlockEntities.LIMITED_SPAWNER_BLOCK_ENTITY.get(),
                    com.kkdfg.Tacz_morelevel.block.client.LimitedSpawnerRenderer::new);
            event.registerEntityRenderer(ModEntities.REFUGEE.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.MILITIA.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.MERCENARY.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.MYSTERIOUS_MERCHANT.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.ASSAULT_SOLDIER.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(
                    ModEntities.LURKER.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.SPECIAL_FORCES.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.SUPER_SPECIAL_SOLDIER.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.LEV_MARSHAL.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.MARSHAL_GUARD.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.WAR_MACHINE.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.ANNIHILATOR.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.SCORPION.get(), ScorpionRenderer::new);
            event.registerEntityRenderer(
                    ModEntities.SLIME_COMMANDER.get(),
                    com.kkdfg.Tacz_morelevel.entity.client.SlimeExperimentRenderer::new);
            event.registerEntityRenderer(
                    ModEntities.SLIME_WARRIOR.get(),
                    com.kkdfg.Tacz_morelevel.entity.client.SlimeExperimentRenderer::new);
            event.registerEntityRenderer(ModEntities.BOMB_BABY.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.CEILING_DUEL.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.BOMB_SIGHT.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.PUNISHER.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(ModEntities.GUARD.get(), PlayerLikeHostileRenderer::new);
            event.registerEntityRenderer(
                    ModEntities.BOMB_CART.get(),
                    com.kkdfg.Tacz_morelevel.entity.client.BombCartRenderer::new);
            event.registerEntityRenderer(
                    ModEntities.PUNISHER_SHOULDER_CANNON.get(),
                    com.kkdfg.Tacz_morelevel.entity.client.PunisherShoulderCannonRenderer::new);
            event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.FLASHBANG.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.MOLOTOV.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(
                    ModEntities.DETECTION_GRENADE.get(), ThrownItemRenderer::new);
        }
    }
}
