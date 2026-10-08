package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.MysteriousMerchantBulletproofArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class MysteriousMerchantBulletproofArmorRenderer
        extends GeoArmorRenderer<MysteriousMerchantBulletproofArmorItem> {

    public MysteriousMerchantBulletproofArmorRenderer() {
        super(new MysteriousMerchantBulletproofArmorGeoModel());
//        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
