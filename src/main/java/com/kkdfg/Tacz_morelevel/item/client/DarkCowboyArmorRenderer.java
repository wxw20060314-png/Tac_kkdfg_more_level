package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.DarkCowboyArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class DarkCowboyArmorRenderer extends GeoArmorRenderer<DarkCowboyArmorItem> {

    public DarkCowboyArmorRenderer() {
        super(new DarkCowboyArmorGeoModel());
    }
}
