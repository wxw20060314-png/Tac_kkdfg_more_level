package com.kkdfg.Tacz_morelevel.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.damagesource.ModDamageTypes;
import java.util.function.Consumer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * 燃烧瓶燃烧：每 {@link #TICK_INTERVAL} tick 造成 {@code amplifier + 1} 点伤害；
 * 无视护甲与普通保护；仅火焰保护按原版 EPF 公式减伤（上限 80%）。
 * 客户端每 tick 刷火焰粒子；扣血时 {@code setSecondsOnFire(3)}。
 */
public final class MolotovBurnEffect extends MobEffect {

    public static final int TICK_INTERVAL = 20;
    /** 火块每次累加时长（3 秒）。 */
    public static final int APPLY_DURATION_TICKS = 40;
    /** 燃烧瓶施加等级：显示 V → amplifier 4，伤害 5。 */
    public static final int APPLY_AMPLIFIER = 4;

    public static final ResourceLocation MOB_EFFECT_ICON =
            ResourceLocation.fromNamespaceAndPath(
                    Tacz_MoreLevel.MOD_ID, "textures/effects/molotov_burn.png");

    public MolotovBurnEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF6A00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            spawnBurnParticles(entity);
            return;
        }
        MobEffectInstance self = entity.getEffect(this);
        if (self == null || self.getDuration() % TICK_INTERVAL != 0) {
            return;
        }
        entity.setSecondsOnFire(3);
        float damage = applyFireProtectionReduction(entity, amplifier + 1);
        if (damage <= 0.0F) {
            return;
        }
        DamageSource source = ModDamageTypes.molotovBurn(entity.level());
        entity.hurt(source, damage);
    }

    private static void spawnBurnParticles(LivingEntity entity) {
        if (entity.tickCount % 2 != 0) {
            return;
        }
        double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();
        double y = entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight();
        double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();
        entity.level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.02D, 0.0D);
        if (entity.getRandom().nextInt(3) == 0) {
            entity.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }

    /**
     * 只计火焰保护 EPF（每级 2，与原版一致），再走 {@link CombatRules#getDamageAfterMagicAbsorb}；
     * EPF 封顶 20 → 最多减 80%。普通保护不参与。
     */
    private static float applyFireProtectionReduction(LivingEntity entity, float damage) {
        int epf = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_PROTECTION, stack);
            if (level > 0) {
                epf += level * 2;
            }
        }
        if (epf <= 0) {
            return damage;
        }
        return CombatRules.getDamageAfterMagicAbsorb(damage, (float) Mth.clamp(epf, 0, 20));
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
