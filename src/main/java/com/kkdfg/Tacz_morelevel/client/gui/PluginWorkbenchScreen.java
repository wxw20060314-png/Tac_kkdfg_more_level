package com.kkdfg.Tacz_morelevel.client.gui;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.menu.PluginWorkbenchMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class PluginWorkbenchScreen extends AbstractContainerScreen<PluginWorkbenchMenu> {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "textures/gui/container/plugin_workbench.png");

    /** UV of the invalid-recipe icon in the 256×256 atlas (right of the 176×166 panel). */
    private static final int ERROR_U = 176;
    private static final int ERROR_V = 0;
    private static final int ERROR_W = 28;
    private static final int ERROR_H = 18;
    /** Drawn over the center arrow between input and output slots. */
    private static final int ERROR_X = 72;
    private static final int ERROR_Y = 39;

    public PluginWorkbenchScreen(PluginWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = this.leftPos;
        int top = this.topPos;
        graphics.blit(BG, left, top, 0, 0, this.imageWidth, this.imageHeight);
        if (this.hasIncompleteRecipe()) {
            graphics.blit(BG, left + ERROR_X, top + ERROR_Y, ERROR_U, ERROR_V, ERROR_W, ERROR_H);
        }
    }

    /** Inputs present but no craftable output (same idea as vanilla smithing). */
    private boolean hasIncompleteRecipe() {
        Slot a1 = this.menu.getSlot(PluginWorkbenchMenu.SLOT_A1);
        Slot b1 = this.menu.getSlot(PluginWorkbenchMenu.SLOT_B1);
        Slot a2 = this.menu.getSlot(PluginWorkbenchMenu.SLOT_A2);
        Slot b2 = this.menu.getSlot(PluginWorkbenchMenu.SLOT_B2);
        boolean anyInput = a1.hasItem() || b1.hasItem();
        boolean anyOutput = a2.hasItem() || b2.hasItem();
        return anyInput && !anyOutput;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
