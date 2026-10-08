package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class PowerMechBulletproofArmorRenderer extends GeoArmorRenderer<PowerMechBulletproofArmorItem> {

    public PowerMechBulletproofArmorRenderer() {
        super(new PowerMechBulletproofArmorGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
