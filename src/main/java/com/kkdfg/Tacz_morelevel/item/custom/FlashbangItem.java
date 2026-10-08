package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * 闪光弹：捏雷/投掷控制复用手雷通道；独立 Geo 模型；碰撞即爆见 {@link
 * com.kkdfg.Tacz_morelevel.entity.custom.FlashbangEntity}。
 */
public class FlashbangItem extends GrenadeItem {

    public FlashbangItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerFlashbang(consumer);
    }
}
