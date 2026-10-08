package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.TieredBulletproofGeoArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public final class TieredBulletproofGeoArmorRenderer extends GeoArmorRenderer<TieredBulletproofGeoArmorItem> {

    public TieredBulletproofGeoArmorRenderer() {
        super(new TierBulletproofArmorGeoModel());
    }
}
