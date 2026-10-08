package com.kkdfg.Tacz_morelevel.integration.jei;

import com.kkdfg.Tacz_morelevel.item.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class BlackMarketBuyRecipeCategory extends AbstractRecipeCategory<BlackMarketBuyRecipe> {

    public BlackMarketBuyRecipeCategory(IGuiHelper guiHelper) {
        super(
                BlackMarketJeiRecipeTypes.BLACK_MARKET_BUY,
                Component.translatable("jei.tacz_morelevel.black_market.buy"),
                guiHelper.createDrawableItemLike(ModItems.BLACK_MARKET_CONTACT.get()),
                82,
                38);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlackMarketBuyRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 1)
                .addItemStack(recipe.input())
                .setStandardSlotBackground();
        builder.addOutputSlot(58, 1)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder, BlackMarketBuyRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(26, 1);
        Component levelText =
                Component.translatable(
                        "jei.tacz_morelevel.black_market.required_level", recipe.marketLevel());
        builder.addText(levelText, getWidth(), 10)
                .setPosition(0, 22)
                .setColor(0xFF808080);
    }

    @Override
    public void draw(
            BlackMarketBuyRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        // 槽位与箭头由 setRecipe / createRecipeExtras 处理
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(BlackMarketBuyRecipe recipe) {
        return recipe.getUid();
    }
}
