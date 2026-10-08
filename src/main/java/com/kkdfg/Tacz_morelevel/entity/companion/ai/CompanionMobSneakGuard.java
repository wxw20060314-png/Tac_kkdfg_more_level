package com.kkdfg.Tacz_morelevel.entity.companion.ai;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import net.minecraft.world.entity.Pose;


public final class CompanionMobSneakGuard {

    private CompanionMobSneakGuard() {}

    public static boolean maySneak(AbstractFriendEntity companion) {
        return companion.mayUseCombatCrouchOrCrawl()
                && !companion.isTaczCrawling()
                && !companion.isOrderedToSit();
    }

    public static void clearCrouchIfBlocked(AbstractFriendEntity companion) {
        if (!maySneak(companion) && companion.getPose() == Pose.CROUCHING) {
            companion.setPose(Pose.STANDING);
        }
    }
}
