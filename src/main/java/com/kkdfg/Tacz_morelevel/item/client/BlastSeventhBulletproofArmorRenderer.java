package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.BlastSeventhBulletproofArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class BlastSeventhBulletproofArmorRenderer extends GeoArmorRenderer<BlastSeventhBulletproofArmorItem> {

    public BlastSeventhBulletproofArmorRenderer() {
        super(new BlastSeventhBulletproofArmorGeoModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
