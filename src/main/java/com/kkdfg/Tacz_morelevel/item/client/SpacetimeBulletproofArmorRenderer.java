package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.SpacetimeBulletproofArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class SpacetimeBulletproofArmorRenderer extends GeoArmorRenderer<SpacetimeBulletproofArmorItem> {

    public SpacetimeBulletproofArmorRenderer() {
        super(new SpacetimeBulletproofArmorGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
