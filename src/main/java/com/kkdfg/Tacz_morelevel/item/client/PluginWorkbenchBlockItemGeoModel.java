package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PluginWorkbenchBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class PluginWorkbenchBlockItemGeoModel extends GeoModel<PluginWorkbenchBlockItem> {
    @Override
    public ResourceLocation getModelResource(PluginWorkbenchBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/block/plugin_workbench.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PluginWorkbenchBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/block/plugin_workbench.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PluginWorkbenchBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/block/plugin_workbench.animation.json");
    }
}
