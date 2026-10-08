package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.LightAbsorbingTubeItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class LightAbsorbingTubeGeoItemRenderer
        extends GeoItemRenderer<LightAbsorbingTubeItem> {

    public LightAbsorbingTubeGeoItemRenderer() {
        super(new LightAbsorbingTubeGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
