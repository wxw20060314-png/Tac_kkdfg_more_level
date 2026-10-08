package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 不依赖 TaCZ 字节码。TaCZ 动能弹同一命中会拆两次 {@link LivingHurtEvent}，本逻辑对每次伤害独立乘算。
 *
 * <p>减伤：全身 {@link BulletproofArmor} 按档位从 Config 动态累加为总减伤比例，伤害×(1−和)，
 * 和上限见 {@code armor.toml} {@code bulletproof.maxReduction}（默认 0.98）。
 * 优先级 {@link EventPriority#LOW}，晚于 {@link com.kkdfg.Tacz_morelevel.effect.ModDeffenceEffect}（{@code NORMAL}）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModForgeGameplayEvents {

    private static final TagKey<DamageType> TACZ_BULLETS =
            TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("tacz:bullets"));

    private ModForgeGameplayEvents() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        if (!isTaczBulletDamage(source, target)) {
            return;
        }
        float sum = BulletproofArmor.sumBulletproofLevel(target);
        if (sum <= 0f) {
            return;
        }
        float reduction = Mth.clamp(sum, 0f, Config.maxBulletproofReduction);
        float multiplier = 1f - reduction;
        event.setAmount(event.getAmount() * multiplier);
    }

    private static boolean isTaczBulletDamage(DamageSource source, LivingEntity target) {
        if (source.typeHolder().is(TACZ_BULLETS)) {
            return true;
        }
        Optional<ResourceKey<DamageType>> key = damageTypeKey(source, target);
        return key
                .map(
                        k -> {
                            ResourceLocation id = k.location();
                            return "tacz".equals(id.getNamespace()) && id.getPath().startsWith("bullet");
                        })
                .orElse(false);
    }

    private static Optional<ResourceKey<DamageType>> damageTypeKey(DamageSource source, LivingEntity target) {
        Optional<ResourceKey<DamageType>> fromHolder = source.typeHolder().unwrapKey();
        if (fromHolder.isPresent()) {
            return fromHolder;
        }
        return target
                .level()
                .registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getResourceKey(source.type());
    }
}
