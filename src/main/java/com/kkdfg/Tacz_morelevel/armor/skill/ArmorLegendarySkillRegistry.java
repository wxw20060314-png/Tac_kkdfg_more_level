package com.kkdfg.Tacz_morelevel.armor.skill;

import com.kkdfg.Tacz_morelevel.armor.skill.power_mech.PowerMechLegendarySkills;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.BlastSummonBombCartSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.SpacetimeBlinkSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthCloakSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthSeventhSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.officer.OfficerSummonGuardSkill;
import com.kkdfg.Tacz_morelevel.event.ArmorLegendarySkillActivateEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

/**
 * 传说技能总入口：注册技能 / Gate，对外暴露 {@link #canActivate} 与 {@link #tryActivate}。
 * 检测顺序：Gate → {@link ArmorLegendarySkillActivateEvent.Pre} → skill.canActivate → skill.activate → Post。
 */
public final class ArmorLegendarySkillRegistry {

    private static final Map<ResourceLocation, ArmorLegendarySkill> SKILLS = new LinkedHashMap<>();
    private static final List<ArmorLegendarySkillGate> GATES = new ArrayList<>();

    static {
        PowerMechLegendarySkills.registerSkills();
        SpacetimeBlinkSkill.register();
        BlastSummonBombCartSkill.register();
        ShockwaveDashSkill.register();
        OfficerSummonGuardSkill.register();
        StealthCloakSkill.register();
        StealthSeventhSkill.register();
    }

    private ArmorLegendarySkillRegistry() {}

    public static void register(ArmorLegendarySkill skill) {
        SKILLS.put(skill.id(), skill);
    }

    public static void registerGate(ArmorLegendarySkillGate gate) {
        GATES.add(gate);
    }

    public static ArmorLegendarySkill get(ResourceLocation id) {
        return SKILLS.get(id);
    }

    public static Map<ResourceLocation, ArmorLegendarySkill> getAll() {
        return Collections.unmodifiableMap(SKILLS);
    }

    /** 是否通过全局 Gate（不含技能固有条件）。 */
    public static boolean passesGates(
            LivingEntity entity, ResourceLocation skillId, ArmorLegendarySkill skill) {
        for (ArmorLegendarySkillGate gate : GATES) {
            if (!gate.mayActivate(entity, skillId, skill)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 服务端：是否可激活（Gate + 技能固有条件；不含 Pre 事件）。
     */
    public static boolean canActivate(LivingEntity entity, ResourceLocation skillId) {
        if (entity.level().isClientSide()) {
            return false;
        }
        ArmorLegendarySkill skill = SKILLS.get(skillId);
        if (skill == null) {
            return false;
        }
        if (!passesGates(entity, skillId, skill)) {
            return false;
        }
        return skill.canActivate(entity);
    }

    /** 服务端尝试激活；成功时发布 {@link ArmorLegendarySkillActivateEvent.Post}。 */
    public static boolean tryActivate(LivingEntity entity, ResourceLocation skillId) {
        if (entity.level().isClientSide()) {
            return false;
        }
        ArmorLegendarySkill skill = SKILLS.get(skillId);
        if (skill == null) {
            return false;
        }
        if (!passesGates(entity, skillId, skill)) {
            return false;
        }
        ArmorLegendarySkillActivateEvent.Pre pre =
                new ArmorLegendarySkillActivateEvent.Pre(entity, skillId, skill);
        if (MinecraftForge.EVENT_BUS.post(pre)) {
            return false;
        }
        if (!skill.canActivate(entity)) {
            return false;
        }
        if (!skill.activate(entity)) {
            return false;
        }
        MinecraftForge.EVENT_BUS.post(new ArmorLegendarySkillActivateEvent.Post(entity, skillId, skill));
        return true;
    }
}
