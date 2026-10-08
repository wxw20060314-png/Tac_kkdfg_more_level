package com.kkdfg.Tacz_morelevel.gunupgrade;

import java.util.List;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

/**
 * 第一层筛选：当前索引全集上剔除 {@code tacz:} 与已有档位后缀枪。
 */
public final class GunUpgradeFilter {

    public static final List<String> TIER_SUFFIXES =
            List.of("_improved", "_refined", "_perfect", "_excellent", "_legendary");

    private GunUpgradeFilter() {}

    /** 命中黑名单（开头或结尾任一）则排除。 */
    public static boolean isExcluded(ResourceLocation gunId) {
        if (gunId == null) {
            return true;
        }
        if ("tacz".equals(gunId.getNamespace())) {
            return true;
        }
        String path = gunId.getPath().toLowerCase(Locale.ROOT);
        for (String suffix : TIER_SUFFIXES) {
            if (path.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEligible(ResourceLocation gunId) {
        return gunId != null && !isExcluded(gunId);
    }
}
