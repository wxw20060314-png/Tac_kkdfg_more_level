package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.FlashbangItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class FlashbangGeoItemRenderer extends GeoItemRenderer<FlashbangItem> {
    public FlashbangGeoItemRenderer() {
        super(new FlashbangGeoModel());
    }
}
