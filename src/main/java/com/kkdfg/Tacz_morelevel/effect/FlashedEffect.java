package com.kkdfg.Tacz_morelevel.effect;

import java.util.function.Consumer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * 闪光（散光）：无 HUD / 背包图标。玩家白屏见客户端 Overlay；NPC 由 {@code FlashedStunGoal} 占通道。
 */
public final class FlashedEffect extends MobEffect {

    public FlashedEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(
                new IClientMobEffectExtensions() {
                    @Override
                    public boolean isVisibleInGui(MobEffectInstance instance) {
                        return false;
                    }

                    @Override
                    public boolean isVisibleInInventory(MobEffectInstance instance) {
                        return false;
                    }

                    @Override
                    public boolean renderGuiIcon(
                            MobEffectInstance instance,
                            Gui gui,
                            GuiGraphics guiGraphics,
                            int x,
                            int y,
                            float z,
                            float alpha) {
                        return true;
                    }

                    @Override
                    public boolean renderInventoryIcon(
                            MobEffectInstance instance,
                            EffectRenderingInventoryScreen<?> screen,
                            GuiGraphics guiGraphics,
                            int x,
                            int y,
                            int blitOffset) {
                        return true;
                    }
                });
    }
}
