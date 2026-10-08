package com.kkdfg.Tacz_morelevel.menu;

import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketInventoryHelper;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketItemStackJson;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BlackMarketTradeOffer {

    private final ItemStack input;
    private final ItemStack output;

    public BlackMarketTradeOffer(ItemStack input, ItemStack output) {
        this.input = input.copy();
        this.output = output.copy();
    }

    public ItemStack input() {
        return this.input;
    }

    public ItemStack output() {
        return this.output;
    }

    public boolean isEmpty() {
        return this.input.isEmpty() && this.output.isEmpty();
    }

    public boolean canTrade(Inventory inventory) {
        if (this.input.isEmpty() || this.resolvedOutput().isEmpty()) {
            return false;
        }
        return BlackMarketInventoryHelper.hasItems(inventory, this.input);
    }

    /** 输出为 TaCZ 枪时，在交易判定瞬间再次解析，避免加载阶段 API 未就绪导致空栈。 */
    public ItemStack resolvedOutput() {
        return BlackMarketItemStackJson.rematerializeGunStack(this.output);
    }

    public boolean executeTrade(Player player) {
        Inventory inventory = player.getInventory();
        ItemStack output = this.resolvedOutput();
        if (this.input.isEmpty() || output.isEmpty()) {
            return false;
        }
        if (!BlackMarketInventoryHelper.hasItems(inventory, this.input)) {
            return false;
        }
        if (!BlackMarketInventoryHelper.consumeItems(inventory, this.input)) {
            return false;
        }
        ItemStack reward = output.copy();
        if (!inventory.add(reward)) {
            player.drop(reward, false);
        }
        return true;
    }

    public void write(CompoundTag tag) {
        tag.put("Input", this.input.save(new CompoundTag()));
        tag.put("Output", this.output.save(new CompoundTag()));
    }

    public static BlackMarketTradeOffer read(CompoundTag tag) {
        ItemStack input = ItemStack.of(tag.getCompound("Input"));
        ItemStack output = ItemStack.of(tag.getCompound("Output"));
        return new BlackMarketTradeOffer(input, output);
    }
}
