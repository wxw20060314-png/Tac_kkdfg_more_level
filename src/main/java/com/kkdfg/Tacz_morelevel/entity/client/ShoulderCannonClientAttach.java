package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** 把肩炮模型画在宿主插值坐标上，避免 20 tick 传送卡顿。 */
public final class ShoulderCannonClientAttach {

    private ShoulderCannonClientAttach() {}

    public static void translateToHost(AbstractShoulderCannonEntity cannon, float partialTick, PoseStack poseStack) {
        LivingEntity host = cannon.getHost();
        if (host == null) {
            return;
        }
        Vec3 attach =
                AbstractShoulderCannonEntity.attachPos(
                        host, cannon.getBbWidth(), cannon.getBbHeight(), partialTick);
        double cx = Mth.lerp(partialTick, cannon.xo, cannon.getX());
        double cy = Mth.lerp(partialTick, cannon.yo, cannon.getY());
        double cz = Mth.lerp(partialTick, cannon.zo, cannon.getZ());
        poseStack.translate(attach.x - cx, attach.y - cy, attach.z - cz);
    }
}
