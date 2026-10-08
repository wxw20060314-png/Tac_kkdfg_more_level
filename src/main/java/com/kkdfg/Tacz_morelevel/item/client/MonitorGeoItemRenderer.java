package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.MonitorItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class MonitorGeoItemRenderer extends GeoItemRenderer<MonitorItem> {

    public MonitorGeoItemRenderer() {
        super(new MonitorGeoModel());
    }
}
