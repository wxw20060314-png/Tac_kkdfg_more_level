package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.TieredBulletproofGeoArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 裸 {@link GeoModel} 要返回完整资源路径：{@code geo/...geo.json} / {@code animations/...animation.json}。
 * 实体/第一人称贴图见 {@code textures/item/armor/&lt;tier&gt;_bulletproof.png}（与背包 {@code textures/item/&lt;tier&gt;_bulletproof_*.png} 可分开维护）。
 */
public final class TierBulletproofArmorGeoModel extends GeoModel<TieredBulletproofGeoArmorItem> {

    public TierBulletproofArmorGeoModel() {}

    @Override
    public ResourceLocation getModelResource(TieredBulletproofGeoArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + animatable.tier().armorGeoPath() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TieredBulletproofGeoArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID,
                "textures/item/armor/" + animatable.tier().armorGeoPath() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(TieredBulletproofGeoArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID,
                "animations/armor/" + animatable.tier().armorGeoPath() + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            TieredBulletproofGeoArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<TieredBulletproofGeoArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
