package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import net.minecraft.world.entity.ai.navigation.PathNavigation;

/** 士兵自己保存的骑行寻路。步行寻路仍留在 {@code Mob} 的 navigation 字段里。 */
public interface RidingPathSource {
    PathNavigation ridingPathNavigation();

    /** 士兵自己保存的骑行移动控制。马可以暂时没有，{@code tick} 里再取。 */
    RidingMoveControl ridingMoveControl();

    /** 停下 {@code Mob.navigation} 那条步行路径。骑马时 {@code getNavigation()} 转到马上，停不到这条。 */
    void stopFootNavigation();
}
