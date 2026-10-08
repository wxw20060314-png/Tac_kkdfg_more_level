package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.BlackMarketContactBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class BlackMarketContactBlockItemRenderer extends GeoItemRenderer<BlackMarketContactBlockItem> {
    public BlackMarketContactBlockItemRenderer() {
        super(new BlackMarketContactBlockItemGeoModel());
    }
}
