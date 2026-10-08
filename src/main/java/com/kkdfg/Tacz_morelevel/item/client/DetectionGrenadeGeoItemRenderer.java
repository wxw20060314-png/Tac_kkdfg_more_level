package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.DetectionGrenadeItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class DetectionGrenadeGeoItemRenderer extends GeoItemRenderer<DetectionGrenadeItem> {
    public DetectionGrenadeGeoItemRenderer() {
        super(new DetectionGrenadeGeoModel());
    }
}
