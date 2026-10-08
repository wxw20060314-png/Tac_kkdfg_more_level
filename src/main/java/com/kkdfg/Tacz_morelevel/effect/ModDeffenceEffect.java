package com.kkdfg.Tacz_morelevel.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 自定义减伤：药水显示等级记为 {@code x}（I → {@code x=1}，即 {@code amplifier + 1}），
 * 受到伤害时 {@code 原伤害 / (x + 1)}。
 * <p>
 * 挂在 {@link LivingHurtEvent}，优先级 {@link EventPriority#NORMAL}，高于防弹减伤
 * （{@link com.kkdfg.Tacz_morelevel.event.ModForgeGameplayEvents} 的 {@code LOW}），故先于防弹结算。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModDeffenceEffect extends MobEffect {

    /** HUD / 背包效果格图标（18×18） */
    public static final ResourceLocation MOB_EFFECT_ICON =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/effects/deffence.png");

    public ModDeffenceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x6BA3FF);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new IClientMobEffectExtensions() {
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
        MobEffectInstance inst = entity.getEffect(ModMobEffects.MOD_DEFFENCE.get());
        if (inst == null) {
            return;
        }
        int x = Math.max(1, inst.getAmplifier() + 1);
        int divisor = x + 1;
        event.setAmount(event.getAmount() / (float) divisor);
    }
}
