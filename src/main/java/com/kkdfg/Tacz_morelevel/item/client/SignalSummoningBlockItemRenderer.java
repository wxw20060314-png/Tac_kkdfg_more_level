package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.SignalSummoningBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class SignalSummoningBlockItemRenderer extends GeoItemRenderer<SignalSummoningBlockItem> {
    public SignalSummoningBlockItemRenderer() {
        super(new SignalSummoningBlockItemGeoModel());
    }
}
