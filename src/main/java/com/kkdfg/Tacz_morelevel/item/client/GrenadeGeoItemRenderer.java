package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.GrenadeItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class GrenadeGeoItemRenderer extends GeoItemRenderer<GrenadeItem> {
    public GrenadeGeoItemRenderer() {
        super(new GrenadeGeoModel());
    }
}
