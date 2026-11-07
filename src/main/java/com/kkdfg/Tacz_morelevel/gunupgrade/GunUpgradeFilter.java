package com.kkdfg.Tacz_morelevel.gunupgrade;

import java.util.List;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

/**
 * 基础五档：无后缀、{@code _improved}、{@code _refined}、{@code _perfect}、{@code _excellent}。
 * 已是四档后缀的枪不再生成。其余特殊后缀（传说、冲击波等）按卓越档就地改，不另开四档。
 * 普通 {@code tacz:} 底枪仍排除，新枪下一步再纳入。
 */
public final class GunUpgradeFilter {

    /** 已生成的四档，不能再当底枪。 */
    public static final List<String> TIER_SUFFIXES =
            List.of("_improved", "_refined", "_perfect", "_excellent");

    /** 不在基础五档里的特殊枪。路径命中任一后缀即按卓越处理。 */
    public static final List<String> SPECIAL_SUFFIXES =
            List.of(
                    "_legendary",
                    "_shockwave",
                    "_annihilator",
                    "_ceiling",
                    "_blast",
                    "_dragonbreath");

    private GunUpgradeFilter() {}

    public static boolean isSpecialVariant(ResourceLocation gunId) {
        return gunId != null && endsWithAny(gunId.getPath(), SPECIAL_SUFFIXES);
    }

    public static boolean isStandardTier(ResourceLocation gunId) {
        return gunId != null && endsWithAny(gunId.getPath(), TIER_SUFFIXES);
    }

    /** 四档成枪，以及还没纳入的普通 {@code tacz:} 底枪。特殊枪不排除。 */
    public static boolean isExcluded(ResourceLocation gunId) {
        if (gunId == null || isStandardTier(gunId)) {
            return true;
        }
        if (isSpecialVariant(gunId)) {
            return false;
        }
        return "tacz".equals(gunId.getNamespace());
    }

    private static boolean endsWithAny(String path, List<String> suffixes) {
        String lower = path.toLowerCase(Locale.ROOT);
        for (String suffix : suffixes) {
            if (lower.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEligible(ResourceLocation gunId) {
        return gunId != null && !isExcluded(gunId);
    }
}
