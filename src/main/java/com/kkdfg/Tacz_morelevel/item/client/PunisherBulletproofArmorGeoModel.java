package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PunisherBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** 判罚者套：{@code geo/armor/punisher_bulletproof.geo.json} + 贴图/发光层。 */
public final class PunisherBulletproofArmorGeoModel extends GeoModel<PunisherBulletproofArmorItem> {

    private static final String PUNISHER = "punisher_bulletproof";

    @Override
    public ResourceLocation getModelResource(PunisherBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "geo/armor/" + PUNISHER + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PunisherBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "textures/item/armor/" + PUNISHER + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(PunisherBulletproofArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Tacz_MoreLevel.MOD_ID, "animations/armor/" + PUNISHER + ".animation.json");
    }

    @Nullable
    @Override
    public ResourceLocation getModelResource(
            PunisherBulletproofArmorItem animatable,
            @Nullable software.bernie.geckolib.renderer.GeoRenderer<PunisherBulletproofArmorItem> renderer) {
        return getModelResource(animatable);
    }
}
