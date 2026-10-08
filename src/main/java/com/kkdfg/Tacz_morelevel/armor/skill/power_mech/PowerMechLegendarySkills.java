package com.kkdfg.Tacz_morelevel.armor.skill.power_mech;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.PowerMechOverloadSkill;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 动力机甲传说技能专用 API：套件内固有条件与全局 Registry 分开暴露。
 *
 * <ul>
 *   <li>{@link #canUseOverload} — 仅技能实现层条件（全套、能量等）
 *   <li>{@link #canTryOverload} — 含全局 Gate 的完整可激活检测
 *   <li>{@link #tryOverload} — 走 Registry 完整流程（Gate + 事件 + 执行）
 * </ul>
 */
public final class PowerMechLegendarySkills {

    public static final ResourceLocation OVERLOAD = PowerMechOverloadSkill.ID;

    private PowerMechLegendarySkills() {}

    /** 注册动力机甲技能到 {@link ArmorLegendarySkillRegistry}。 */
    public static void registerSkills() {
        ArmorLegendarySkillRegistry.register(new PowerMechOverloadSkill());
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return PowerMechBulletproofArmorItem.countEquippedPieces(entity)
                >= PowerMechBulletproofArmorItem.FULL_SET_PIECES;
    }

    /** 仅过载技能固有条件，不含 Gate / 事件。 */
    public static boolean canUseOverload(LivingEntity entity) {
        var skill = ArmorLegendarySkillRegistry.get(OVERLOAD);
        return skill != null && skill.canActivate(entity);
    }

    /** 含 Gate 的完整检测，不含 Pre 事件。 */
    public static boolean canTryOverload(LivingEntity entity) {
        return ArmorLegendarySkillRegistry.canActivate(entity, OVERLOAD);
    }

    /** 执行过载：Gate → Pre 事件 → 技能 → Post 事件。 */
    public static boolean tryOverload(LivingEntity entity) {
        return ArmorLegendarySkillRegistry.tryActivate(entity, OVERLOAD);
    }
}
