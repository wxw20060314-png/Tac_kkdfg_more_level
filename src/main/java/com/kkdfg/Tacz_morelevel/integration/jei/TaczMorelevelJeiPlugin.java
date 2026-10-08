package com.kkdfg.Tacz_morelevel.integration.jei;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI 可选集成：购买配方用于「获得方式」(R)，卖出配方用于「用途」(U)。
 * 本类仅由 JEI 加载；主模组勿引用，以免未安装 JEI 时触发类加载错误。
 */
@JeiPlugin
public final class TaczMorelevelJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new BlackMarketBuyRecipeCategory(guiHelper),
                new BlackMarketSellRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                BlackMarketJeiRecipeTypes.BLACK_MARKET_BUY, BlackMarketJeiRecipes.loadBuyRecipes());
        registration.addRecipes(
                BlackMarketJeiRecipeTypes.BLACK_MARKET_SELL, BlackMarketJeiRecipes.loadSellRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack catalyst = new ItemStack(ModItems.BLACK_MARKET_CONTACT.get());
        registration.addRecipeCatalyst(catalyst, BlackMarketJeiRecipeTypes.BLACK_MARKET_BUY);
        registration.addRecipeCatalyst(catalyst, BlackMarketJeiRecipeTypes.BLACK_MARKET_SELL);
    }
}
