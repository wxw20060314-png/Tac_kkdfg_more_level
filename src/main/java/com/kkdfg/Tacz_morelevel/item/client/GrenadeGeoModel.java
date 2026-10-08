package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.GrenadeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class GrenadeGeoModel extends GeoModel<GrenadeItem> {
    @Override
    public ResourceLocation getModelResource(GrenadeItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "geo/item/grenade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GrenadeItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "textures/item/grenade.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GrenadeItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/grenade.animation.json");
    }
}
