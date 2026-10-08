package com.kkdfg.Tacz_morelevel.blackmarket;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class BlackMarketManualRefreshCostJson {

    @SerializedName("item")
    private String itemId = "minecraft:gold_block";

    @SerializedName("base_count")
    private int baseCount = 1;

    @SerializedName("max_count")
    private int maxCount = 64;

    @SerializedName("double_each_time")
    private boolean doubleEachTime = true;

    public ItemStack costForPaidCount(int timesAlreadyPaid) {
        Item item = resolveItem();
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        int count = this.computeCount(Math.max(0, timesAlreadyPaid));
        return count <= 0 ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    private int computeCount(int timesAlreadyPaid) {
        int base = Math.max(1, this.baseCount);
        int max = Math.max(base, this.maxCount);
        if (!this.doubleEachTime) {
            return Math.min(max, base);
        }
        long scaled = (long) base << timesAlreadyPaid;
        if (scaled > max) {
            return max;
        }
        return (int) scaled;
    }

    private Item resolveItem() {
        if (this.itemId == null || this.itemId.isBlank()) {
            return Items.GOLD_BLOCK;
        }
        ResourceLocation id = ResourceLocation.tryParse(this.itemId);
        if (id == null) {
            return Items.GOLD_BLOCK;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item != null ? item : Items.GOLD_BLOCK;
    }
}
