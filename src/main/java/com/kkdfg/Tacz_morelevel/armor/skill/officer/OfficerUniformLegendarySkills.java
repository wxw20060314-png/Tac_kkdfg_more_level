package com.kkdfg.Tacz_morelevel.armor.skill.officer;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.entity.custom.LevMarshalEntity;
import com.kkdfg.Tacz_morelevel.item.custom.OfficerUniformArmorItem;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 军官制服传说技能「仆从的守卫」。
 *
 * <ul>
 *   <li>玩家：胸甲名册链路 TTL&gt;0 的槽数 z；{@code L = min(max(z-1, 0), 2)}（1→0，2→1，3+→2）</li>
 *   <li>大帅：名册在线护卫，{@code L = min(10z+30, 100)}</li>
 * </ul>
 * 玩家侧名册保活与防护同 tick；计数靠槽 TTL，无需 AABB 扫描间隔。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OfficerUniformLegendarySkills {

    /** 每 tick 刷新时的效果时长（短于链路 TTL，靠持续刷新保活）。 */
    public static final int EFFECT_DURATION_TICKS = 5;

    public static final int MARSHAL_SCAN_INTERVAL_TICKS = 20;
    public static final int MARSHAL_EFFECT_DURATION_TICKS = 25;

    public static final int MARSHAL_A = 10;
    public static final int MARSHAL_B = 30;
    public static final int MARSHAL_T = 100;

    /** 玩家防护等级封顶。 */
    public static final int PLAYER_DEFENCE_CAP = 2;

    private OfficerUniformLegendarySkills() {}

    public static boolean hasFullSet(LivingEntity entity) {
        return OfficerUniformArmorItem.hasFullSet(entity);
    }

    /**
     * 玩家：{@code L = min(max(z - 1, 0), 2)}；{@code z <= 1} 为 0。
     */
    public static int playerDefenceLevel(int z) {
        if (z <= 1) {
            return 0;
        }
        return Mth.clamp(z - 1, 0, PLAYER_DEFENCE_CAP);
    }

    /** {@code L = min(a·z+b, t)}；{@code z <= 0} 返回 0。 */
    public static int defenceLevel(int z, int a, int b, int t) {
        if (z <= 0) {
            return 0;
        }
        return Mth.clamp(a * z + b, 1, t);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (!hasFullSet(entity)) {
            return;
        }

        if (entity instanceof LevMarshalEntity marshal) {
            if (entity.tickCount % MARSHAL_SCAN_INTERVAL_TICKS != 0) {
                return;
            }
            int z = marshal.countOnlineMinions();
            int level = defenceLevel(z, MARSHAL_A, MARSHAL_B, MARSHAL_T);
            applyDefence(entity, level, MARSHAL_EFFECT_DURATION_TICKS);
            return;
        }

        if (entity instanceof Player player) {
            ItemStack chest = OfficerGuardRosterHelper.getEquippedChest(player);
            if (chest == null) {
                return;
            }
            OfficerGuardRosterHelper.tickRoster(player, chest);
            int z = OfficerGuardRosterHelper.countLinkedSlots(chest)+ OfficerGuardRosterHelper.getReserve(chest);
            int level = playerDefenceLevel(z);
            applyDefence(player, level, EFFECT_DURATION_TICKS);
        }
    }

    private static void applyDefence(LivingEntity entity, int level, int duration) {
        if (level <= 0) {
            return;
        }
        entity.addEffect(
                new MobEffectInstance(
                        ModMobEffects.MOD_DEFFENCE.get(),
                        duration,
                        level - 1,
                        false,
                        false,
                        true));
    }
}
