package com.kkdfg.Tacz_morelevel.integration.jei;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import mezz.jei.api.recipe.RecipeType;

public final class BlackMarketJeiRecipeTypes {

    public static final RecipeType<BlackMarketBuyRecipe> BLACK_MARKET_BUY =
            RecipeType.create(Tacz_MoreLevel.MOD_ID, "black_market_buy", BlackMarketBuyRecipe.class);

    public static final RecipeType<BlackMarketSellRecipe> BLACK_MARKET_SELL =
            RecipeType.create(Tacz_MoreLevel.MOD_ID, "black_market_sell", BlackMarketSellRecipe.class);

    private BlackMarketJeiRecipeTypes() {}
}
