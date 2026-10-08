package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.SpecialOpsBulletproofArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class SpecialOpsBulletproofArmorRenderer
        extends GeoArmorRenderer<SpecialOpsBulletproofArmorItem> {

    public SpecialOpsBulletproofArmorRenderer() {
        super(new SpecialOpsBulletproofArmorGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
