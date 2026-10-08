package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.PluginWorkbenchBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class PluginWorkbenchBlockItemRenderer extends GeoItemRenderer<PluginWorkbenchBlockItem> {
    public PluginWorkbenchBlockItemRenderer() {
        super(new PluginWorkbenchBlockItemGeoModel());
    }
}
