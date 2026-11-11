package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.MechanicalHorseMissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class MechanicalHorseMissileRenderer extends GeoEntityRenderer<MechanicalHorseMissileEntity> {
    public MechanicalHorseMissileRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "mechanical_horse_missile")));
    }

    @Override
    protected void applyRotations(MechanicalHorseMissileEntity missile, PoseStack poses,
            float age, float yaw, float partialTick) {
        Vec3 motion = missile.getDeltaMovement();
        float heading = (float) Math.toDegrees(Math.atan2(-motion.x, -motion.z));
        float pitch = (float) Math.toDegrees(Math.atan2(motion.y, motion.horizontalDistance()));
        poses.mulPose(Axis.YP.rotationDegrees(heading));
        poses.mulPose(Axis.XP.rotationDegrees(pitch));
    }
}
