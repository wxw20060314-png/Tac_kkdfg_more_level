package com.kkdfg.Tacz_morelevel.damagesource;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/** 模组自定义伤害类型。 */
public final class ModDamageTypes {

    public static final ResourceKey<DamageType> MOLOTOV_BURN =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "molotov_burn"));

    private ModDamageTypes() {}

    /** 燃烧瓶 DoT：数据包 tags 标为无视护甲 / 无视附魔保护。 */
    public static DamageSource molotovBurn(Level level) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(MOLOTOV_BURN));
    }
}
