package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.MolotovItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class MolotovGeoItemRenderer extends GeoItemRenderer<MolotovItem> {
    public MolotovGeoItemRenderer() {
        super(new MolotovGeoModel());
    }
}
