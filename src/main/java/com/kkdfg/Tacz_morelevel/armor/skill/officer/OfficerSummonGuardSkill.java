package com.kkdfg.Tacz_morelevel.armor.skill.officer;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.item.custom.OfficerUniformArmorItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 军官制服传说键：从胸甲储备召唤一只护卫入名册（最多 4 槽）。 */
public final class OfficerSummonGuardSkill implements ArmorLegendarySkill {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "officer_summon_guard");

    private static final OfficerSummonGuardSkill INSTANCE = new OfficerSummonGuardSkill();

    private OfficerSummonGuardSkill() {}

    public static void register() {
        ArmorLegendarySkillRegistry.register(INSTANCE);
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return OfficerUniformArmorItem.hasFullSet(entity);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean canActivate(LivingEntity entity) {
        if (!(entity instanceof Player player) || player.isSpectator()) {
            return false;
        }
        if (!hasFullSet(player)) {
            return false;
        }
        ItemStack chest = OfficerGuardRosterHelper.getEquippedChest(player);
        if (chest == null) {
            return false;
        }
        OfficerGuardRosterHelper.ensureInitialized(chest);
        return OfficerGuardRosterHelper.firstEmptySlot(chest) >= 0
                && OfficerGuardRosterHelper.getReserve(chest) > 0;
    }

    @Override
    public boolean activate(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        return OfficerGuardRosterHelper.trySummonGuard(player);
    }
}
