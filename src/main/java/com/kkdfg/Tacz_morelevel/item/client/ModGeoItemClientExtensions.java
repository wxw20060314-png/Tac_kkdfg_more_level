package com.kkdfg.Tacz_morelevel.item.client;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * GeckoLib 物品/护甲的客户端 BEWLR 与穿戴模型；与 common 侧 {@code Item#initializeClient} 委托分离，
 * 避免专用服务器加载 {@link BlockEntityWithoutLevelRenderer}。
 */
@OnlyIn(Dist.CLIENT)
public final class ModGeoItemClientExtensions {

    private ModGeoItemClientExtensions() {}

    public static void registerGrenade(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, GrenadeGeoItemRenderer::new);
    }

    public static void registerFlashbang(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, FlashbangGeoItemRenderer::new);
    }

    public static void registerMolotov(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, MolotovGeoItemRenderer::new);
    }

    public static void registerDetectionGrenade(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, DetectionGrenadeGeoItemRenderer::new);
    }

    public static void registerBodyguardStorage(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, BodyguardStorageGeoItemRenderer::new);
    }

    public static void registerBodyguardContact(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, BodyguardContactGeoItemRenderer::new);
    }

    public static void registerComputerCase(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, ComputerCaseGeoItemRenderer::new);
    }

    public static void registerMonitor(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, MonitorGeoItemRenderer::new);
    }

    public static void registerInfiniteNetworkReceiver(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, InfiniteNetworkReceiverGeoItemRenderer::new);
    }

    public static void registerSpacetimeStone(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, SpacetimeStoneGeoItemRenderer::new);
    }

    public static void registerEnergyCore(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, EnergyCoreGeoItemRenderer::new);
    }

    public static void registerPowerThruster(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, PowerThrusterGeoItemRenderer::new);
    }

    public static void registerLightAbsorbingTube(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, LightAbsorbingTubeGeoItemRenderer::new);
    }

    public static void registerArmorRepairKit(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, ArmorRepairKitGeoItemRenderer::new);
    }

    public static void registerBlackMarketContactBlock(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, BlackMarketContactBlockItemRenderer::new);
    }

    public static void registerPluginWorkbenchBlock(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, PluginWorkbenchBlockItemRenderer::new);
    }

    public static void registerSignalSummoningBlock(Consumer<IClientItemExtensions> consumer) {
        registerGeoItemRenderer(consumer, SignalSummoningBlockItemRenderer::new);
    }

    public static void registerTieredBulletproofArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, TieredBulletproofGeoArmorRenderer::new);
    }

    public static void registerBlastSeventhArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, BlastSeventhBulletproofArmorRenderer::new);
    }

    public static void registerBlazeflameSeventhArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, BlazeflameSeventhBulletproofArmorRenderer::new);
    }

    public static void registerAbyssSeventhArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, AbyssSeventhBulletproofArmorRenderer::new);
    }

    public static void registerRadiationSeventhArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, RadiationSeventhBulletproofArmorRenderer::new);
    }

    public static void registerSculkSeventhArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, SculkSeventhBulletproofArmorRenderer::new);
    }

    public static void registerPunisherArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, PunisherBulletproofArmorRenderer::new);
    }

    public static void registerPowerMechBulletproofArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, PowerMechBulletproofArmorRenderer::new);
    }

    public static void registerMysteriousMerchantBulletproofArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, MysteriousMerchantBulletproofArmorRenderer::new);
    }

    public static void registerOfficerUniformArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, OfficerUniformArmorRenderer::new);
    }

    public static void registerStealthCloakArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, StealthCloakArmorRenderer::new);
    }

    public static void registerStealthSeventhArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, StealthSeventhArmorRenderer::new);
    }

    public static void registerJokerSeventhHelmet(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, JokerSeventhBulletproofHelmetRenderer::new);
    }

    public static void registerSpacetimeBulletproofArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, SpacetimeBulletproofArmorRenderer::new);
    }

    public static void registerSpecialOpsBulletproofArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, SpecialOpsBulletproofArmorRenderer::new);
    }

    public static void registerShockwaveBulletproofArmor(Consumer<IClientItemExtensions> consumer) {
        registerGeoArmorRenderer(consumer, ShockwaveBulletproofArmorRenderer::new);
    }

    private static void registerGeoItemRenderer(
            Consumer<IClientItemExtensions> consumer, Supplier<? extends BlockEntityWithoutLevelRenderer> factory) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (this.renderer == null) {
                            this.renderer = factory.get();
                        }
                        return this.renderer;
                    }
                });
    }

    private static <R extends GeoArmorRenderer<?>> void registerGeoArmorRenderer(
            Consumer<IClientItemExtensions> consumer, Supplier<R> factory) {
        consumer.accept(
                new IClientItemExtensions() {
                    private R renderer;

                    @Override
                    public HumanoidModel<?> getHumanoidArmorModel(
                            LivingEntity livingEntity,
                            ItemStack itemStack,
                            EquipmentSlot equipmentSlot,
                            HumanoidModel<?> original) {
                        if (this.renderer == null) {
                            this.renderer = factory.get();
                        }
                        this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                        return this.renderer;
                    }
                });
    }
}
