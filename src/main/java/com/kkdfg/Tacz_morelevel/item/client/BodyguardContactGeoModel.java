package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.BodyguardContactItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 四档联络器共用 {@link BodyguardContactItem} 一类；贴图/模型/动画按 {@link BodyguardContactItem#getTier()} 选择，
 * 避免 GeckoLib 按类注册时全部落在同一套资源上。
 */
public final class BodyguardContactGeoModel extends GeoModel<BodyguardContactItem> {

    @Override
    public ResourceLocation getModelResource(BodyguardContactItem animatable) {
        return asset(animatable, "geo/item", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BodyguardContactItem animatable) {
        return asset(animatable, "textures/item", ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BodyguardContactItem animatable) {
        return asset(animatable, "animations/item", ".animation.json");
    }

    private static ResourceLocation asset(BodyguardContactItem animatable, String folder, String suffix) {
        String base = BodyguardContactItem.tierAssetName(animatable.getTier());
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, folder + "/" + base + suffix);
    }
}
