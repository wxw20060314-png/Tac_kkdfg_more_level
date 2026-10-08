package com.kkdfg.Tacz_morelevel.effect;

import java.util.UUID;
import java.util.function.Consumer;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 硬化：第 {@code x} 级（amplifier，从 0 起）减伤 {@code (x+1)*20%}、减速 {@code (x+1)*10%}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HardeningEffect extends MobEffect {

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString("c4e8f1a2-3b5d-4e6f-9a0b-1c2d3e4f5678");

    public static final ResourceLocation MOB_EFFECT_ICON =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/effects/hardening.png");

    public HardeningEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B7355);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID.toString(),
                -0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    /** 第 x 级减伤比例（0~1）。 */
    public static float damageReduction(int amplifier) {
        return Mth.clamp((amplifier + 1) * 0.10F, 0.0F, 1.0F);
    }

    /** 第 x 级减速比例（0~1）。 */
    public static float speedReduction(int amplifier) {
        return Mth.clamp((amplifier + 1) * 0.10F, 0.0F, 1.0F);
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

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        MobEffectInstance inst = entity.getEffect(ModMobEffects.HARDENING.get());
        if (inst == null) {
            return;
        }
        float reduction = damageReduction(inst.getAmplifier());
        event.setAmount(event.getAmount() * (1.0F - reduction));
    }
}
