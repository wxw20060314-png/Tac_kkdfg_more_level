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

public final class BlackMarketSellRecipeCategory extends AbstractRecipeCategory<BlackMarketSellRecipe> {

    public BlackMarketSellRecipeCategory(IGuiHelper guiHelper) {
        super(
                BlackMarketJeiRecipeTypes.BLACK_MARKET_SELL,
                Component.translatable("jei.tacz_morelevel.black_market.sell"),
                guiHelper.createDrawableItemLike(ModItems.BLACK_MARKET_CONTACT.get()),
                82,
                38);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlackMarketSellRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 1)
                .addItemStack(recipe.input())
                .setStandardSlotBackground();
        builder.addOutputSlot(58, 1)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder, BlackMarketSellRecipe recipe, IFocusGroup focuses) {
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
            BlackMarketSellRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {}

    @Override
    public @Nullable ResourceLocation getRegistryName(BlackMarketSellRecipe recipe) {
        return recipe.getUid();
    }
}
