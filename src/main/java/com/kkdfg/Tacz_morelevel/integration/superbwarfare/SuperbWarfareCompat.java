package com.kkdfg.Tacz_morelevel.integration.superbwarfare;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

/**
 * 卓越前线（Superb Warfare）软依赖入口。
 *
 * <p>三层锁：{@code mods.toml} mandatory=false；Gradle {@code compileOnly}；此处
 * {@link ModList#isLoaded} 为真才进入 {@link #initWhenPresent()}。
 *
 * <p>本类<strong>不得</strong> import SBW 包；真正引用 SBW 类型的逻辑放在
 * {@link SuperbWarfareBulletproofBridge}、{@link SuperbWarfareFnLegendaryVehicleSkill}
 * 等仅在 {@link #initWhenPresent} 内加载的类中。
 */
public final class SuperbWarfareCompat {

    public static final String MOD_ID = "superbwarfare";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean present;
    private static boolean initialized;

    private SuperbWarfareCompat() {}

    /** 是否已检测到并完成适配注册。 */
    public static boolean isPresent() {
        return present;
    }

    /**
     * 主模组 {@code commonSetup} 调用：未安装 SBW 时直接返回，不触碰任何 SBW 类。
     */
    public static void bootstrap() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!ModList.get().isLoaded(MOD_ID)) {
            LOGGER.debug("[{}] Superb Warfare not present; soft compat skipped.", Tacz_MoreLevel.MOD_ID);
            present = false;
            return;
        }
        present = true;
        initWhenPresent();
        LOGGER.info("[{}] Superb Warfare soft compat enabled.", Tacz_MoreLevel.MOD_ID);
    }

    /**
     * 仅在 {@link ModList#isLoaded} 为真时进入。这里的类引用会加载 SBW 类型，
     * 未安装时不得走到这一步。
     */
    private static void initWhenPresent() {
        SuperbWarfareBulletproofBridge.register();
        SuperbWarfareFnLegendaryVehicleSkill.register();
    }
}
