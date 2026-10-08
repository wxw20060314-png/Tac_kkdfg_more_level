package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class ShockwaveBulletproofArmorRenderer extends GeoArmorRenderer<ShockwaveBulletproofArmorItem> {

    public ShockwaveBulletproofArmorRenderer() {
        super(new ShockwaveBulletproofArmorGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
