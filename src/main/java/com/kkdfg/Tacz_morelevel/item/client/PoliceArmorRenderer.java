package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.PoliceArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class PoliceArmorRenderer extends GeoArmorRenderer<PoliceArmorItem> {

    public PoliceArmorRenderer() {
        super(new PoliceArmorGeoModel());
    }
}
