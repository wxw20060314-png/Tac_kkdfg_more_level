package com.kkdfg.Tacz_morelevel.integration.jei;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** JEI 展示用：黑市购买（货币 → 物品）。 */
public final class BlackMarketBuyRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int marketLevel;
    private final ResourceLocation uid;

    public BlackMarketBuyRecipe(ItemStack input, ItemStack output, int marketLevel, int index) {
        this.input = input.copy();
        this.output = output.copy();
        this.marketLevel = marketLevel;
        this.uid =
                ResourceLocation.fromNamespaceAndPath(
                        Tacz_MoreLevel.MOD_ID,
                        "black_market_buy_" + marketLevel + "_" + index);
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
