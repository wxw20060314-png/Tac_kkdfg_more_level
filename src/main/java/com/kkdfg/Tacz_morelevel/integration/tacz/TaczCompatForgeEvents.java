package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 依赖 TaCZ API 的 Forge 事件；与 {@link com.kkdfg.Tacz_morelevel.event.ModForgeGameplayEvents} 分离，避免 TaCZ 未加载或
 * API 类缺失时导致整个事件类加载失败，进而使与 TaCZ 无关的逻辑（如枪弹防护附魔）一并失效。
 * <p>
 * 升级凭证与黑市货币见实体战利品表 {@code loot_tables/entities/*.json}（{@link com.kkdfg.Tacz_morelevel.dategen.ModEntityLootTablesProvider}）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TaczCompatForgeEvents {

    private TaczCompatForgeEvents() {}

    /** 拾取怪掉落枪等：换弹后 TaCZ 可能把 {@link com.tacz.guns.api.item.gun.FireMode} 写成非法值，每 tick 校正主手。 */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }
        if (IGun.mainHandHoldGun(player)) {
            TaczGunMobHelper.normalizeFireModeIfInvalid(player.getMainHandItem());
        }
    }
}
