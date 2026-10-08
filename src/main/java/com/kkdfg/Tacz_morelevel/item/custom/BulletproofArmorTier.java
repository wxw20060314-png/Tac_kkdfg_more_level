package com.kkdfg.Tacz_morelevel.item.custom;

import net.minecraft.resources.ResourceLocation;

/**
 * 防弹套分级：与 {@link ModArmorMaterials}、GeckoLib 路径 {@link #armorGeoPath()} 对应。
 */
public enum BulletproofArmorTier {
    FIRST(1, ModArmorMaterials.FIRST_BULLETPROOF, "first_bulletproof"),
    SECOND(2, ModArmorMaterials.SECOND_BULLETPROOF, "second_bulletproof"),
    THIRD(3, ModArmorMaterials.THIRD_BULLETPROOF, "third_bulletproof"),
    FOURTH(4, ModArmorMaterials.FOURTH_BULLETPROOF, "fourth_bulletproof"),
    FIFTH(5, ModArmorMaterials.FIFTH_BULLETPROOF, "fifth_bulletproof"),
    SIXTH(6, ModArmorMaterials.SIXTH_BULLETPROOF, "sixth_bulletproof"),
    SEVENTH(7, ModArmorMaterials.SEVENTH_BULLETPROOF, "seventh_bulletproof");

    private final int level;
    private final ModArmorMaterials material;
    /** 文件名（无后缀）；模型类会拼成 {@code geo/armor/&lt;this&gt;.geo.json} 与 {@code animations/armor/&lt;this&gt;.animation.json}。 */
    private final String armorGeoPath;

    BulletproofArmorTier(int level, ModArmorMaterials material, String armorGeoPath) {
        this.level = level;
        this.material = material;
        this.armorGeoPath = armorGeoPath;
    }

    public int level() {
        return this.level;
    }

    public ModArmorMaterials material() {
        return this.material;
    }

    /** 资源文件名（无扩展名）。 */
    public String armorGeoPath() {
        return this.armorGeoPath;
    }

    /** 实体盔甲纹理：{@code textures/item/armor/&lt;path&gt;.png} */
    public ResourceLocation armorTextureLocation(String modId) {
        return ResourceLocation.fromNamespaceAndPath(modId, "item/armor/" + this.armorGeoPath);
    }
}
