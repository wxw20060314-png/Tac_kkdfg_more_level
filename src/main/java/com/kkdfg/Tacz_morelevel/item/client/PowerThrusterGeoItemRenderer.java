package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.PowerThrusterItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class PowerThrusterGeoItemRenderer extends GeoItemRenderer<PowerThrusterItem> {

    public PowerThrusterGeoItemRenderer() {
        super(new PowerThrusterGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
