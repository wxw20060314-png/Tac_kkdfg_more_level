package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import net.minecraft.client.model.geom.ModelPart;

/** 默认玩家皮肤预览用宽臂模型。 */
public class VanillaSkinCompanionModel extends CompanionPlayerLikeHostileModel<AbstractFriendlyCompanionEntity> {

    public VanillaSkinCompanionModel(ModelPart root, boolean slim) {
        super(root, slim);
    }
}
