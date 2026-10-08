package com.kkdfg.Tacz_morelevel.integration.jei;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** JEI 展示用：黑市卖出（物品 → 货币）。按 U 查看用途时，售出物为输入槽。 */
public final class BlackMarketSellRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int marketLevel;
    private final ResourceLocation uid;

    public BlackMarketSellRecipe(ItemStack input, ItemStack output, int marketLevel, int index) {
        this.input = input.copy();
        this.output = output.copy();
        this.marketLevel = marketLevel;
        this.uid =
                ResourceLocation.fromNamespaceAndPath(
                        Tacz_MoreLevel.MOD_ID,
                        "black_market_sell_" + marketLevel + "_" + index);
    }

    public ItemStack input() {
        return this.input;
    }

    public ItemStack output() {
        return this.output;
    }

    public int marketLevel() {
        return this.marketLevel;
    }

    public ResourceLocation getUid() {
        return this.uid;
    }
}
