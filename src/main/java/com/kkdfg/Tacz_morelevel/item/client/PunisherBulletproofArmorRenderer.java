package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.PunisherBulletproofArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class PunisherBulletproofArmorRenderer extends GeoArmorRenderer<PunisherBulletproofArmorItem> {

    public PunisherBulletproofArmorRenderer() {
        super(new PunisherBulletproofArmorGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
