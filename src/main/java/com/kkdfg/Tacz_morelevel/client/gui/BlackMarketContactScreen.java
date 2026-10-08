package com.kkdfg.Tacz_morelevel.client.gui;

import com.kkdfg.Tacz_morelevel.menu.BlackMarketContactMenu;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import com.kkdfg.Tacz_morelevel.network.packet.BlackMarketRefreshPacket;
import com.kkdfg.Tacz_morelevel.network.packet.BlackMarketTradePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class BlackMarketContactScreen extends AbstractContainerScreen<BlackMarketContactMenu> {

    public BlackMarketContactScreen(BlackMarketContactMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BlackMarketContactMenu.IMAGE_WIDTH;
        this.imageHeight = BlackMarketContactMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = BlackMarketContactMenu.PLAYER_INV_Y - 10;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = this.leftPos;
        int top = this.topPos;
        graphics.blit(
                BlackMarketGuiTextures.MAIN,
                left,
                top,
                BlackMarketContactMenu.PANEL_TEXTURE_U,
                BlackMarketContactMenu.PANEL_TEXTURE_V,
                this.imageWidth,
                this.imageHeight,
                256,
                256);
        this.renderTradeRows(graphics, left, top, mouseX, mouseY);
        this.renderRefreshPanel(graphics, left, top, mouseX, mouseY);
    }

    private void renderTradeRows(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        int rowX = left + BlackMarketContactMenu.TRADE_START_X;
        int arrowX = rowX + BlackMarketContactMenu.SLOT_SIZE + BlackMarketContactMenu.TRADE_INNER_GAP;
        int outputX = arrowX + BlackMarketContactMenu.ARROW_WIDTH + BlackMarketContactMenu.TRADE_INNER_GAP;
        int statusX = outputX + BlackMarketContactMenu.SLOT_SIZE + 6;

        for (int i = 0; i < this.menu.tradeOffers().size(); i++) {
            int rowY = top + BlackMarketContactMenu.TRADE_START_Y + i * BlackMarketContactMenu.TRADE_ROW_HEIGHT;
            BlackMarketTradeOffer offer = this.menu.tradeOffers().get(i);

            graphics.blit(BlackMarketGuiTextures.SLOT, rowX, rowY, 0, 0, 18, 18, 18, 18);
            graphics.blit(
                    BlackMarketGuiTextures.ARROW,
                    arrowX,
                    rowY + 2,
                    0,
                    0,
                    BlackMarketContactMenu.ARROW_WIDTH,
                    BlackMarketContactMenu.ARROW_HEIGHT,
                    22,
                    15);
            graphics.blit(BlackMarketGuiTextures.SLOT, outputX, rowY, 0, 0, 18, 18, 18, 18);

            this.renderOfferStack(graphics, offer.input(), rowX, rowY);
            this.renderOfferStack(graphics, offer.resolvedOutput(), outputX, rowY);

            boolean hovered = this.isRowHovered(i, mouseX, mouseY);
            boolean canTrade = this.menu.canTrade(i, this.minecraft.player.getInventory());
            if (!canTrade) {
                graphics.blit(BlackMarketGuiTextures.CANNOT_TRADE, statusX, rowY, 0, 0, 18, 18, 18, 18);
            } else if (hovered) {
                graphics.blit(BlackMarketGuiTextures.CAN_TRADE_HOVER, statusX, rowY, 0, 0, 18, 18, 18, 18);
            } else {
                graphics.blit(BlackMarketGuiTextures.CAN_TRADE, statusX, rowY, 0, 0, 18, 18, 18, 18);
            }
        }
    }

    private void renderRefreshPanel(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        int slotX = left + BlackMarketContactMenu.REFRESH_PANEL_X;
        int slotY = top + BlackMarketContactMenu.REFRESH_COST_Y;
        graphics.blit(BlackMarketGuiTextures.SLOT, slotX, slotY, 0, 0, 18, 18, 18, 18);
        this.renderOfferStack(graphics, this.menu.refreshCost(), slotX, slotY);

        int buttonX = slotX;
        int buttonY = top + BlackMarketContactMenu.REFRESH_BUTTON_Y;
        boolean canAfford = this.menu.canAffordRefresh(this.minecraft.player.getInventory());
        boolean hovered = this.isRefreshButtonHovered(buttonX, buttonY, mouseX, mouseY);
        if (!canAfford) {
            graphics.blit(
                    BlackMarketGuiTextures.REFRESH_DISABLED,
                    buttonX,
                    buttonY,
                    0,
                    0,
                    BlackMarketContactMenu.REFRESH_BUTTON_SIZE,
                    BlackMarketContactMenu.REFRESH_BUTTON_SIZE,
                    18,
                    18);
        } else if (hovered) {
            graphics.blit(
                    BlackMarketGuiTextures.REFRESH_HOVER,
                    buttonX,
                    buttonY,
                    0,
                    0,
                    BlackMarketContactMenu.REFRESH_BUTTON_SIZE,
                    BlackMarketContactMenu.REFRESH_BUTTON_SIZE,
                    18,
                    18);
        } else {
            graphics.blit(
                    BlackMarketGuiTextures.REFRESH,
                    buttonX,
                    buttonY,
                    0,
                    0,
                    BlackMarketContactMenu.REFRESH_BUTTON_SIZE,
                    BlackMarketContactMenu.REFRESH_BUTTON_SIZE,
                    18,
                    18);
        }
    }

    private boolean isRefreshButtonHovered(int buttonX, int buttonY, int mouseX, int mouseY) {
        return isSlotHovered(buttonX, buttonY, mouseX, mouseY, BlackMarketContactMenu.REFRESH_BUTTON_SIZE);
    }

    private static boolean isSlotHovered(int slotX, int slotY, int mouseX, int mouseY, int size) {
        return mouseX >= slotX
                && mouseX < slotX + size
                && mouseY >= slotY
                && mouseY < slotY + size;
    }

    private int tradeInputSlotX() {
        return this.leftPos + BlackMarketContactMenu.TRADE_START_X;
    }

    private int tradeOutputSlotX(int rowIndex) {
        int rowX = tradeInputSlotX();
        return rowX
                + BlackMarketContactMenu.SLOT_SIZE
                + BlackMarketContactMenu.TRADE_INNER_GAP
                + BlackMarketContactMenu.ARROW_WIDTH
                + BlackMarketContactMenu.TRADE_INNER_GAP;
    }

    private int tradeRowY(int rowIndex) {
        return this.topPos + BlackMarketContactMenu.TRADE_START_Y + rowIndex * BlackMarketContactMenu.TRADE_ROW_HEIGHT;
    }

    private void renderOfferStack(GuiGraphics graphics, ItemStack stack, int slotX, int slotY) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, slotX + 1, slotY + 1);
        graphics.renderItemDecorations(this.font, stack, slotX + 1, slotY + 1);
    }

    private boolean isRowHovered(int offerIndex, int mouseX, int mouseY) {
        int rowTop = this.topPos + BlackMarketContactMenu.TRADE_START_Y + offerIndex * BlackMarketContactMenu.TRADE_ROW_HEIGHT;
        int rowLeft = this.leftPos + BlackMarketContactMenu.TRADE_START_X;
        int rowRight = rowLeft + BlackMarketContactMenu.TRADE_ROW_WIDTH;
        return mouseX >= rowLeft
                && mouseX < rowRight
                && mouseY >= rowTop
                && mouseY < rowTop + BlackMarketContactMenu.TRADE_ROW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.minecraft != null && this.minecraft.player != null) {
            int buttonX = this.leftPos + BlackMarketContactMenu.REFRESH_PANEL_X;
            int buttonY = this.topPos + BlackMarketContactMenu.REFRESH_BUTTON_Y;
            if (this.isRefreshButtonHovered(buttonX, buttonY, (int) mouseX, (int) mouseY)
                    && this.menu.canAffordRefresh(this.minecraft.player.getInventory())) {
                ModNetwork.CHANNEL.sendToServer(new BlackMarketRefreshPacket(this.menu.blockPos()));
                return true;
            }
            int tradeIndex = this.findHoveredTradableRow((int) mouseX, (int) mouseY);
            if (tradeIndex >= 0) {
                ModNetwork.CHANNEL.sendToServer(new BlackMarketTradePacket(this.menu.blockPos(), tradeIndex));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int findHoveredTradableRow(int mouseX, int mouseY) {
        for (int i = 0; i < this.menu.tradeOffers().size(); i++) {
            if (!this.isRowHovered(i, mouseX, mouseY)) {
                continue;
            }
            if (this.menu.canTrade(i, this.minecraft.player.getInventory())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTradeAreaTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!this.findHoveredOfferStack(mouseX, mouseY).isEmpty()
                || !this.findHoveredHintTooltip(mouseX, mouseY).isEmpty()) {
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    /** 交易区为手绘槽位，须在 {@link #render} 末尾单独绘制 tooltip（避免被遮挡或未调用）。 */
    private void renderTradeAreaTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null || !this.menu.getCarried().isEmpty()) {
            return;
        }
        ItemStack hoveredStack = this.findHoveredOfferStack(mouseX, mouseY);
        if (!hoveredStack.isEmpty()) {
            this.renderItemStackTooltip(graphics, hoveredStack, mouseX, mouseY);
            return;
        }
        List<Component> hint = this.findHoveredHintTooltip(mouseX, mouseY);
        if (!hint.isEmpty()) {
            graphics.renderComponentTooltip(this.font, hint, mouseX, mouseY);
        }
    }

    private void renderItemStackTooltip(
            GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
        TooltipFlag flag =
                this.minecraft.options.advancedItemTooltips
                        ? TooltipFlag.Default.ADVANCED
                        : TooltipFlag.Default.NORMAL;
        List<Component> lines = stack.getTooltipLines(this.minecraft.player, flag);
        if (lines.isEmpty()) {
            return;
        }
        graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY, stack);
    }

    private ItemStack findHoveredOfferStack(int mouseX, int mouseY) {
        int inputX = this.tradeInputSlotX();
        for (int i = 0; i < this.menu.tradeOffers().size(); i++) {
            int rowY = this.tradeRowY(i);
            BlackMarketTradeOffer offer = this.menu.tradeOffers().get(i);
            if (isSlotHovered(inputX, rowY, mouseX, mouseY, BlackMarketContactMenu.SLOT_SIZE)) {
                return offer.input();
            }
            int outputX = this.tradeOutputSlotX(i);
            if (isSlotHovered(outputX, rowY, mouseX, mouseY, BlackMarketContactMenu.SLOT_SIZE)) {
                return offer.resolvedOutput();
            }
        }
        int refreshSlotX = this.leftPos + BlackMarketContactMenu.REFRESH_PANEL_X;
        int refreshSlotY = this.topPos + BlackMarketContactMenu.REFRESH_COST_Y;
        if (isSlotHovered(
                refreshSlotX, refreshSlotY, mouseX, mouseY, BlackMarketContactMenu.SLOT_SIZE)) {
            return this.menu.refreshCost();
        }
        return ItemStack.EMPTY;
    }

    private List<Component> findHoveredHintTooltip(int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        for (int i = 0; i < this.menu.tradeOffers().size(); i++) {
            int rowY = this.tradeRowY(i);
            int statusX =
                    this.tradeOutputSlotX(i) + BlackMarketContactMenu.SLOT_SIZE + 6;
            if (!isSlotHovered(
                    statusX, rowY, mouseX, mouseY, BlackMarketContactMenu.STATUS_SIZE)) {
                continue;
            }
            boolean canTrade = this.menu.canTrade(i, this.minecraft.player.getInventory());
            lines.add(
                    Component.translatable(
                            canTrade
                                    ? "gui.tacz_morelevel.black_market.trade_click"
                                    : "gui.tacz_morelevel.black_market.trade_cannot"));
            return lines;
        }
        int buttonX = this.leftPos + BlackMarketContactMenu.REFRESH_PANEL_X;
        int buttonY = this.topPos + BlackMarketContactMenu.REFRESH_BUTTON_Y;
        if (this.isRefreshButtonHovered(buttonX, buttonY, mouseX, mouseY)) {
            boolean canAfford = this.menu.canAffordRefresh(this.minecraft.player.getInventory());
            lines.add(
                    Component.translatable(
                            canAfford
                                    ? "gui.tacz_morelevel.black_market.refresh_click"
                                    : "gui.tacz_morelevel.black_market.refresh_cannot"));
            return lines;
        }
        return lines;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, titleX, 8, 0x404040, false);
        int inventoryLabelX = (this.imageWidth - this.font.width(this.playerInventoryTitle)) / 2;
        graphics.drawString(
                this.font, this.playerInventoryTitle, inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
