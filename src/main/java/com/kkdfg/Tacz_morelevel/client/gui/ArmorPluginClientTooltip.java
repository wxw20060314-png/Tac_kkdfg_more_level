package com.kkdfg.Tacz_morelevel.client.gui;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.plugin.ArmorPluginTooltip;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Renders label + 18×18 plugin slots in item tooltips.
 * Slot frame: {@code textures/gui/armor_plugin_slot.png}.
 */
public final class ArmorPluginClientTooltip implements ClientTooltipComponent {

    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/gui/armor_plugin_slot.png");

    private static final Component LABEL =
            Component.translatable("tooltip.tacz_morelevel.armor_plugin.label");

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int PAD = 2;
    private static final int LABEL_GAP = 4;

    private final List<ItemStack> plugins;

    public ArmorPluginClientTooltip(ArmorPluginTooltip tooltip) {
        this.plugins = tooltip.plugins();
    }

    private int slotsWidth() {
        int n = Math.max(1, this.plugins.size());
        return n * SLOT_SIZE + Math.max(0, n - 1) * SLOT_GAP;
    }

    private int labelWidth(Font font) {
        return font.width(LABEL);
    }

    @Override
    public int getWidth(Font font) {
        return PAD * 2 + labelWidth(font) + LABEL_GAP + slotsWidth();
    }

    @Override
    public int getHeight() {
        return PAD * 2 + SLOT_SIZE;
    }

    @Override
    public void renderText(
            Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        int textY = y + PAD + (SLOT_SIZE - font.lineHeight) / 2;
        font.drawInBatch(
                LABEL.getVisualOrderText(),
                x + PAD,
                textY,
                0xFFAAAAAA,
                true,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                0xF000F0);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int drawX = x + PAD + labelWidth(font) + LABEL_GAP;
        int drawY = y + PAD;
        for (int i = 0; i < this.plugins.size(); i++) {
            renderSlot(graphics, font, drawX + i * (SLOT_SIZE + SLOT_GAP), drawY, this.plugins.get(i));
        }
    }

    private static void renderSlot(GuiGraphics graphics, Font font, int x, int y, ItemStack stack) {
        RenderSystem.enableBlend();
        graphics.blit(SLOT_TEXTURE, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        RenderSystem.disableBlend();

        if (!stack.isEmpty()) {
            RenderSystem.enableDepthTest();
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }
    }
}
