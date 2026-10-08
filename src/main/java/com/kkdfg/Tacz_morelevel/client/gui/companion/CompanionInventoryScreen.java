package com.kkdfg.Tacz_morelevel.client.gui.companion;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.companion.menu.CompanionInventoryMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 仅使用 {@code companion_inventory.png} 绘制整屏背景；槽位坐标见 {@link CompanionInventoryMenu}。 */
public final class CompanionInventoryScreen extends AbstractContainerScreen<CompanionInventoryMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/gui/companion_inventory.png");

    /** 与 PNG 文件尺寸一致；若你重画更大贴图，同步改这里。 */
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private float xMouse;
    private float yMouse;

    public CompanionInventoryScreen(
            CompanionInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = CompanionInventoryMenu.IMAGE_WIDTH;
        this.imageHeight = CompanionInventoryMenu.IMAGE_HEIGHT;
        this.titleLabelX = -1000;
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(
                Button.builder(Component.literal("X"), button -> this.onClose())
                        .bounds(
                                this.leftPos + CompanionInventoryMenu.CLOSE_BUTTON_X,
                                this.topPos + CompanionInventoryMenu.CLOSE_BUTTON_Y,
                                CompanionInventoryMenu.CLOSE_BUTTON_SIZE,
                                CompanionInventoryMenu.CLOSE_BUTTON_SIZE)
                        .build());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题与「物品栏」文字由 companion_inventory.png 自行绘制
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int left = this.leftPos;
        int top = this.topPos;

        graphics.blit(
                TEXTURE,
                left,
                top,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                left + CompanionInventoryMenu.ENTITY_PREVIEW_X,
                top + CompanionInventoryMenu.ENTITY_PREVIEW_Y,
                20,
                (float) (left + CompanionInventoryMenu.ENTITY_PREVIEW_X) - this.xMouse,
                (float) (top + CompanionInventoryMenu.ENTITY_PREVIEW_Y - 50) - this.yMouse,
                this.menu.companion());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
