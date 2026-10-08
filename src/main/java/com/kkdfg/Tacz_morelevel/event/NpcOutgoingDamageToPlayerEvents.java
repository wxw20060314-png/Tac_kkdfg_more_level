package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractNeutralGunNpcEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractSlimeExperimentEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 本模组 NPC 打玩家：{@code tacz_morelevel-common.toml} {@code npcOutgoingDamageToPlayerScale}。
 * 只看 {@code DamageSource#getEntity()}，不依赖 TaCZ 枪械事件。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcOutgoingDamageToPlayerEvents {

    private NpcOutgoingDamageToPlayerEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        float scale = Config.npcOutgoingDamageToPlayerScale;
        if (scale == 1.0f) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (!isConfiguredNpc(attacker)) {
            return;
        }
        event.setAmount(event.getAmount() * scale);
    }

    private static boolean isConfiguredNpc(Entity entity) {
        return entity instanceof AbstractPlayerLikeSoldierEntity
                || entity instanceof AbstractNeutralGunNpcEntity
                || entity instanceof AbstractSlimeExperimentEntity
                || entity instanceof AbstractFriendEntity;
    }
}
