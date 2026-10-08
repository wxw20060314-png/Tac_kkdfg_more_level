package com.kkdfg.Tacz_morelevel.menu;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.companion.menu.CompanionInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Tacz_MoreLevel.MOD_ID);

    public static final RegistryObject<MenuType<BlackMarketContactMenu>> BLACK_MARKET_CONTACT_MENU =
            MENUS.register(
                    "black_market_contact",
                    () ->
                            IForgeMenuType.create(
                                    (windowId, playerInventory, extraData) ->
                                            new BlackMarketContactMenu(windowId, playerInventory, extraData)));

    public static final RegistryObject<MenuType<PluginWorkbenchMenu>> PLUGIN_WORKBENCH_MENU =
            MENUS.register(
                    "plugin_workbench",
                    () -> IForgeMenuType.create(PluginWorkbenchMenu::new));

    public static final RegistryObject<MenuType<CompanionInventoryMenu>> COMPANION_INVENTORY_MENU =
            MENUS.register(
                    "companion_inventory",
                    () ->
                            IForgeMenuType.create(
                                    (windowId, playerInventory, extraData) ->
                                            new CompanionInventoryMenu(windowId, playerInventory, extraData)));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

    private ModMenus() {}
}
