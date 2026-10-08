package com.kkdfg.Tacz_morelevel.api;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** 响应 TaCZ 枪声并寻路调查的 NPC。 */
public interface TaczGunSoundInvestigatingMob {

    void notifyGunFireHeard(Vec3 sourceWorldPos, double hearRangeSq);

    boolean hasPendingGunSoundInvestigation();

    @Nullable
    BlockPos getGunSoundInvestigatePos();

    void clearGunSoundInvestigation();
}
