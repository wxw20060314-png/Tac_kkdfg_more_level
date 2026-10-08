package com.kkdfg.Tacz_morelevel.menu;

import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketInventoryHelper;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.block.entity.BlackMarketContactBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BlackMarketContactMenu extends AbstractContainerMenu {

    /** main.png 中可见灰底面板区域（256 纹理内左侧），用于屏幕居中。 */
    public static final int PANEL_TEXTURE_U = 1;
    public static final int PANEL_TEXTURE_V = 1;
    public static final int IMAGE_WIDTH = 174;
    public static final int IMAGE_HEIGHT = 220;
    public static final int PANEL_PADDING = 8;

    public static final int TRADE_ROW_COUNT = 5;
    public static final int TRADE_START_Y = 18;
    public static final int TRADE_ROW_HEIGHT = 22;
    public static final int SLOT_SIZE = 18;
    public static final int ARROW_WIDTH = 22;
    public static final int ARROW_HEIGHT = 15;
    public static final int STATUS_SIZE = 18;
    public static final int TRADE_INNER_GAP = 4;
    public static final int TRADE_ROW_WIDTH =
            SLOT_SIZE
                    + TRADE_INNER_GAP
                    + ARROW_WIDTH
                    + TRADE_INNER_GAP
                    + SLOT_SIZE
                    + 6
                    + STATUS_SIZE;
    /** 主 UI 上半区左侧：交易行。 */
    public static final int TRADE_START_X = PANEL_PADDING;

    /** 与 main.png 内玩家背包格对齐。 */
    public static final int PLAYER_INV_X = 7;
    public static final int PLAYER_INV_Y = 140;
    public static final int HOTBAR_Y = 198;

    public static final int REFRESH_BUTTON_SIZE = 18;
    public static final int REFRESH_PANEL_GAP = 4;
    public static final int REFRESH_PANEL_HEIGHT = SLOT_SIZE + REFRESH_PANEL_GAP + REFRESH_BUTTON_SIZE;
    public static final int TRADE_AREA_HEIGHT = TRADE_ROW_COUNT * TRADE_ROW_HEIGHT;
    /** 主 UI 上半区右侧：刷新费用与按钮。 */
    public static final int REFRESH_PANEL_X = IMAGE_WIDTH - PANEL_PADDING - SLOT_SIZE;
    public static final int REFRESH_COST_Y =
            TRADE_START_Y + (TRADE_AREA_HEIGHT - REFRESH_PANEL_HEIGHT) / 2;
    public static final int REFRESH_BUTTON_Y = REFRESH_COST_Y + SLOT_SIZE + REFRESH_PANEL_GAP;

    private final ContainerLevelAccess access;
    private final BlockPos blockPos;
    private final List<BlackMarketTradeOffer> tradeOffers = new ArrayList<>();
    private ItemStack refreshCost = ItemStack.EMPTY;

    public BlackMarketContactMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenus.BLACK_MARKET_CONTACT_MENU.get(), containerId);
        this.blockPos = extraData.readBlockPos();
        Level level = playerInventory.player.level();
        this.access = ContainerLevelAccess.create(level, this.blockPos);
        this.tradeOffers.addAll(BlackMarketContactBlockEntity.readOffers(extraData));
        this.refreshCost = extraData.readItem();
        if (extraData.readableBytes() > 0) {
            extraData.readVarInt();
        }
        this.addPlayerSlots(playerInventory);
    }

    public BlackMarketContactMenu(
            int containerId, Inventory playerInventory, BlackMarketContactBlockEntity blockEntity) {
        super(ModMenus.BLACK_MARKET_CONTACT_MENU.get(), containerId);
        this.blockPos = blockEntity.getBlockPos();
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), this.blockPos);
        this.syncFromBlockEntity(blockEntity);
        this.addPlayerSlots(playerInventory);
    }

    public void syncFromBlockEntity(BlackMarketContactBlockEntity blockEntity) {
        this.tradeOffers.clear();
        this.tradeOffers.addAll(blockEntity.activeOffers());
        this.refreshCost = blockEntity.refreshCost().copy();
    }

    public void applyClientState(List<BlackMarketTradeOffer> offers, ItemStack cost) {
        this.tradeOffers.clear();
        this.tradeOffers.addAll(offers);
        this.refreshCost = cost.copy();
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(
                        new Slot(
                                playerInventory,
                                col + row * 9 + 9,
                                PLAYER_INV_X + col * SLOT_SIZE,
                                PLAYER_INV_Y + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * SLOT_SIZE, HOTBAR_Y));
        }
    }

    public BlockPos blockPos() {
        return this.blockPos;
    }

    public List<BlackMarketTradeOffer> tradeOffers() {
        return this.tradeOffers;
    }

    public boolean canTrade(int offerIndex, Inventory inventory) {
        if (offerIndex < 0 || offerIndex >= this.tradeOffers.size()) {
            return false;
        }
        return this.tradeOffers.get(offerIndex).canTrade(inventory);
    }

    public ItemStack refreshCost() {
        return this.refreshCost;
    }

    public boolean canAffordRefresh(Inventory inventory) {
        if (this.refreshCost.isEmpty()) {
            return true;
        }
        return BlackMarketInventoryHelper.hasItems(inventory, this.refreshCost);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(stack, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.BLACK_MARKET_CONTACT.get());
    }
}
