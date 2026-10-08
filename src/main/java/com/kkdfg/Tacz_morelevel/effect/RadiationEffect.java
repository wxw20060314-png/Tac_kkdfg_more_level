package com.kkdfg.Tacz_morelevel.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.plugin.RadiationResistArmorPlateItem;
import java.util.function.Consumer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * 辐射：每 {@link #TICK_INTERVAL} tick 按有效等级 {@code k} 扣当前生命 {@code k/20}；
 * {@code a = amplifier + 1}，{@code k = a - 盔甲/免疫抗性}，{@code k ≤ 0} 不扣。
 */
public final class RadiationEffect extends MobEffect {

    public static final int TICK_INTERVAL = 10;

    public static final ResourceLocation MOB_EFFECT_ICON =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/effects/radiation.png");

    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFDD00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % TICK_INTERVAL == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }
        float a = Math.max(1, amplifier + 1);
        float k = a - RadiationResistArmorPlateItem.totalResistance(entity);
        if (k <= 0.0F) {
            return;
        }
        float damage = entity.getHealth() * (k / 20.0F);
        if (damage <= 0.0F) {
            return;
        }
        entity.hurt(entity.damageSources().magic(), damage);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(
                new IClientMobEffectExtensions() {
                    @Override
                    public boolean renderGuiIcon(
                            MobEffectInstance instance,
                            Gui gui,
                            GuiGraphics guiGraphics,
                            int x,
                            int y,
                            float z,
                            float alpha) {
                        guiGraphics.blit(MOB_EFFECT_ICON, x + 3, y + 3, 0, 0, 18, 18, 18, 18);
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
                        guiGraphics.blit(MOB_EFFECT_ICON, x, y + 8, 0, 0, 18, 18, 18, 18);
                        return true;
                    }
                });
    }
}
