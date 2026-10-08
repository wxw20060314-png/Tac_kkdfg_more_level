package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.ComputerCaseItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ComputerCaseGeoModel extends GeoModel<ComputerCaseItem> {

    @Override
    public ResourceLocation getModelResource(ComputerCaseItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/item/computer_case.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ComputerCaseItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/computer_case.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ComputerCaseItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/item/computer_case.animation.json");
    }
}
