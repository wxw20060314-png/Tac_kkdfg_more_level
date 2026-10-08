package com.kkdfg.Tacz_morelevel.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 综合免疫：每 20 tick 清除一次，{@link MobEffectCategory#HARMFUL} 白名单内且等级 ≤ 本效果 amplifier 的 debuff。
 */
public final class ComprehensiveImmunityEffect extends MobEffect {

    public static final int PURGE_INTERVAL_TICKS = 20;

    public static final ResourceLocation MOB_EFFECT_ICON =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "textures/effects/comprehensive_immunity.png");

    /** 可被综合免疫清除的常见持续型负面效果（原版）。辐射由盔甲/免疫折算在辐射事件中处理。 */
    private static final Set<MobEffect> PURGE_TARGETS =
            Set.of(
                    MobEffects.POISON,
                    MobEffects.WITHER,
                    MobEffects.HUNGER,
                    MobEffects.WEAKNESS,
                    MobEffects.MOVEMENT_SLOWDOWN,
                    MobEffects.DIG_SLOWDOWN,
                    MobEffects.CONFUSION,
                    MobEffects.BLINDNESS,
                    MobEffects.DARKNESS,
                    MobEffects.LEVITATION,
                    MobEffects.UNLUCK,
                    MobEffects.GLOWING);

    private static boolean isPurgeTarget(MobEffect effect) {
        return PURGE_TARGETS.contains(effect);
    }

    public ComprehensiveImmunityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55FFAA);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % PURGE_INTERVAL_TICKS == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }
        purgeDebuffs(entity, amplifier);
    }

    /** 清除等级 ≤ {@code immunityAmplifier} 的白名单 debuff；获得效果时可立即调用一次。 */
    public static void purgeDebuffs(LivingEntity entity, int immunityAmplifier) {
        if (entity.level().isClientSide()) {
            return;
        }
        MobEffect self = ModMobEffects.COMPREHENSIVE_IMMUNITY.get();
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            MobEffect effect = inst.getEffect();
            if (effect == self || !isPurgeTarget(effect)) {
                continue;
            }
            if (inst.getAmplifier() <= immunityAmplifier) {
                toRemove.add(effect);
            }
        }
        toRemove.forEach(entity::removeEffect);
    }

    @Override
    public void initializeClient(Consumer<net.minecraftforge.client.extensions.common.IClientMobEffectExtensions> consumer) {
        consumer.accept(
                new net.minecraftforge.client.extensions.common.IClientMobEffectExtensions() {
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
