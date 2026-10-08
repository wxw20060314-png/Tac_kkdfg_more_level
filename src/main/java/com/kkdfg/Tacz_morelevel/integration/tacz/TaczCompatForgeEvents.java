package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractDualGunSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AbstractPlayerLikeSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AnnihilatorEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AssaultSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.LurkerEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MarshalGuardEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MercenarySoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MilitiaEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MysteriousMerchantEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.RefugeeEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SpecialForcesSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SuperSpecialSoldierEntity;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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

    /**
     * 死亡时弹匣子弹百分比的全局倍率；默认 1.0，由 {@link com.kkdfg.Tacz_morelevel.Config} 的 {@code presentToDrop} 写入。
     */
    public static double PRESENT_TO_DROP = 1.0D;

    private TaczCompatForgeEvents() {}

    /**
     * 人形士兵死亡：主手为 TaCZ 枪时，按兵种掉落与该枪弹匣匹配的子弹，数量为「有效弹匣容量 × 百分比 / 100」向下取整。
     * 战争机器、歼灭者、神秘商人不参与。凭证/货币由原版 {@code dropFromLootTable} 处理。
     */
    @SubscribeEvent
    public static void onSoldierDeathMagazineAmmo(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof AbstractPlayerLikeSoldierEntity soldier)) {
            return;
        }
        if (soldier instanceof AbstractDualGunSoldierEntity
                || soldier instanceof AnnihilatorEntity
                || soldier instanceof CeilingDuelEntity
                || soldier instanceof MarshalGuardEntity) {
            return;
        }
        if (soldier.level().isClientSide()) {
            return;
        }

        int magazinePercent100;
        if (soldier instanceof RefugeeEntity) {
            magazinePercent100 = (int) (40 * PRESENT_TO_DROP);
        } else if (soldier instanceof MilitiaEntity) {
            magazinePercent100 = (int) (80 * PRESENT_TO_DROP);
        } else if (soldier instanceof MercenarySoldierEntity) {
            magazinePercent100 = (int) (120 * PRESENT_TO_DROP);
        } else if (soldier instanceof AssaultSoldierEntity
                || soldier instanceof LurkerEntity) {
            magazinePercent100 = (int) (160 * PRESENT_TO_DROP);
        } else if (soldier instanceof SpecialForcesSoldierEntity) {
            magazinePercent100 = (int) (200 * PRESENT_TO_DROP);
        } else if (soldier instanceof SuperSpecialSoldierEntity) {
            magazinePercent100 = (int) (200 * PRESENT_TO_DROP);
        } else {
            return;
        }

        TaczGunMobHelper.spawnMagazineAmmoDeathDrop(soldier, magazinePercent100);
    }

    /**
     * 死亡掉落：人形士兵与神秘商人的 TaCZ 枪替换为空匣白板（无虚拟备弹）；
     * 战争机器、歼灭者、神秘商人额外剔除 TaCZ 子弹掉落。
     */
    @SubscribeEvent
    public static void onGunMobLivingDrops(LivingDropsEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof MarshalGuardEntity) {
            event.getDrops().clear();
            return;
        }
        boolean sanitizeGuns =
                entity instanceof AbstractPlayerLikeSoldierEntity || entity instanceof MysteriousMerchantEntity;
        if (!sanitizeGuns) {
            return;
        }

        TaczGunMobHelper.replaceDroppedGunStacksWithEmptyMag(event.getDrops());

        if (entity instanceof MysteriousMerchantEntity
                || entity instanceof AnnihilatorEntity
                || entity instanceof CeilingDuelEntity
                || entity instanceof AbstractDualGunSoldierEntity) {
            event.getDrops().removeIf(drop -> TaczGunMobHelper.isTaczAmmoItem(drop.getItem()));
        }
    }

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
