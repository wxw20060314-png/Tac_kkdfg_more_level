package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.EnergyCoreItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class EnergyCoreGeoItemRenderer extends GeoItemRenderer<EnergyCoreItem> {

    public EnergyCoreGeoItemRenderer() {
        super(new EnergyCoreGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
