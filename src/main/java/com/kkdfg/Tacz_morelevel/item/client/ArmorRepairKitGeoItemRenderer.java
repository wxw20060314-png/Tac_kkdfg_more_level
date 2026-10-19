package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.ArmorRepairKitItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class ArmorRepairKitGeoItemRenderer extends GeoItemRenderer<ArmorRepairKitItem> {

    public ArmorRepairKitGeoItemRenderer() {
        super(new ArmorRepairKitGeoModel());
    }
}
