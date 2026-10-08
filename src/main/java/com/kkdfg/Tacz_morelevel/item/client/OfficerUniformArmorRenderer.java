package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.OfficerUniformArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class OfficerUniformArmorRenderer extends GeoArmorRenderer<OfficerUniformArmorItem> {

    public OfficerUniformArmorRenderer() {
        super(new OfficerUniformArmorGeoModel());
    }
}
