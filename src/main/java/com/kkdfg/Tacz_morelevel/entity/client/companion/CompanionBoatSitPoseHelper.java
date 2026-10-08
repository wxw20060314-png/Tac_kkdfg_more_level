package com.kkdfg.Tacz_morelevel.entity.client.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 护航陆上「指令停留」举手角度（见 {@link CompanionPlayerLikeHostileModel}）。
 * 与坐船无关；乘客时不会启用（{@link AbstractFriendlyCompanionEntity#shouldUseOrderedSitVisual()}）。
 */
@OnlyIn(Dist.CLIENT)
public final class CompanionBoatSitPoseHelper {

    /** 左手向前上举（弧度，负 {@code xRot}）。 */
    public static final float ORDERED_SIT_LEFT_ARM_X_ROT = (float) (-Math.PI / 4.0 * 3);

    private CompanionBoatSitPoseHelper() {}

    public static boolean useOrderedSitVisual(AbstractFriendlyCompanionEntity entity) {
        return entity.shouldUseOrderedSitVisual();
    }
}
