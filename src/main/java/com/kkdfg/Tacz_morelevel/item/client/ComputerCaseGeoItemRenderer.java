package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.ComputerCaseItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class ComputerCaseGeoItemRenderer extends GeoItemRenderer<ComputerCaseItem> {

    public ComputerCaseGeoItemRenderer() {
        super(new ComputerCaseGeoModel());
    }
}
