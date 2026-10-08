package com.kkdfg.Tacz_morelevel.integration.superbwarfare;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/**
 * 卓越前线枪伤 → 本模组防弹减伤（与 {@code ModForgeGameplayEvents} 对 TaCZ 同思路）。
 *
 * <p>实际减伤 = clamp(sumBulletproof × {@code superbWarfare.t}, 0, {@code bulletproof.maxReduction})。
 * 仅使用 DamageType 命名空间 {@code superbwarfare} 识别，<strong>不</strong> import SBW 类，
 * 因此本类可在「已确认 isLoaded」后安全注册；未安装时根本不会 register。
 */
public final class SuperbWarfareBulletproofBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    private SuperbWarfareBulletproofBridge() {}

    static void register() {
        MinecraftForge.EVENT_BUS.register(SuperbWarfareBulletproofBridge.class);
        LOGGER.debug("[{}] Registered Superb Warfare bulletproof LivingHurt bridge.", Tacz_MoreLevel.MOD_ID);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        if (!isSuperbWarfareDamage(event.getSource(), target)) {
            return;
        }
        float sum = BulletproofArmor.sumBulletproofLevel(target);
        if (sum <= 0f) {
            return;
        }
        float reduction =
                Mth.clamp(
                        sum * Config.superbWarfareBulletproofScale,
                        0f,
                        Config.maxBulletproofReduction);
        event.setAmount(event.getAmount() * (1f - reduction));
    }

    private static boolean isSuperbWarfareDamage(DamageSource source, LivingEntity target) {
        Optional<ResourceKey<DamageType>> key = source.typeHolder().unwrapKey();
        if (key.isEmpty()) {
            key =
                    target.level()
                            .registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getResourceKey(source.type());
        }
        return key.map(
                        k -> {
                            ResourceLocation id = k.location();
                            return SuperbWarfareCompat.MOD_ID.equals(id.getNamespace());
                        })
                .orElse(false);
    }
}
