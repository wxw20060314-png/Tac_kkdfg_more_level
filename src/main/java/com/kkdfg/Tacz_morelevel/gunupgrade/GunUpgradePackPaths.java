package com.kkdfg.Tacz_morelevel.gunupgrade;

import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLPaths;

/** 生成包目录：游戏目录 {@code tacz/tacz_morelevel_gun_upgrades/}。 */
public final class GunUpgradePackPaths {

    public static final String PACK_FOLDER_NAME = "tacz_morelevel_gun_upgrades";

    private GunUpgradePackPaths() {}

    public static Path taczRoot(MinecraftServer server) {
        if (server != null) {
            return server.getServerDirectory().toPath().resolve("tacz");
        }
        return FMLPaths.GAMEDIR.get().resolve("tacz");
    }

    public static Path packRoot(MinecraftServer server) {
        return taczRoot(server).resolve(PACK_FOLDER_NAME);
    }

    /** 仅允许删除/写入本包目录。 */
    public static boolean isSafePackRoot(Path packRoot) {
        if (packRoot == null) {
            return false;
        }
        Path normalized = packRoot.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        return PACK_FOLDER_NAME.equals(String.valueOf(normalized.getFileName()))
                && parent != null
                && "tacz".equals(String.valueOf(parent.getFileName()));
    }
}
