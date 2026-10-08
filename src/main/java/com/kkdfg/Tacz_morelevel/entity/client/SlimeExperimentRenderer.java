package com.kkdfg.Tacz_morelevel.entity.client;

import com.kkdfg.Tacz_morelevel.entity.custom.AbstractSlimeExperimentEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 史莱姆实验体：人形持枪渲染（TaCZ 主手图层），按 {@link AbstractSlimeExperimentEntity#getSlimeScale()} 缩放。
 * 机制是史莱姆分裂，外形与士兵同为玩家模型。
 */
@OnlyIn(Dist.CLIENT)
public class SlimeExperimentRenderer extends PlayerLikeHostileRenderer<AbstractSlimeExperimentEntity> {

    public SlimeExperimentRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(AbstractSlimeExperimentEntity entity, PoseStack poseStack, float partialTick) {
        float s = entity.getSlimeScale();
        float base = 0.9375F * s;
        poseStack.scale(base, base, base);
        this.shadowRadius = 0.5F * s;
    }
}
