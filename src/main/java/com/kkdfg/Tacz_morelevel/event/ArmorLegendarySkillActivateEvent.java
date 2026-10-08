package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * 盔甲传说技能激活事件：{@link Pre} 可取消以阻止技能；{@link Post} 在成功执行后发布。
 * 适用于任意 {@link LivingEntity}。
 */
public abstract class ArmorLegendarySkillActivateEvent extends Event {

    private final LivingEntity entity;
    private final ResourceLocation skillId;
    private final ArmorLegendarySkill skill;

    protected ArmorLegendarySkillActivateEvent(
            LivingEntity entity, ResourceLocation skillId, ArmorLegendarySkill skill) {
        this.entity = entity;
        this.skillId = skillId;
        this.skill = skill;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ResourceLocation getSkillId() {
        return skillId;
    }

    public ArmorLegendarySkill getSkill() {
        return skill;
    }

    @Cancelable
    public static final class Pre extends ArmorLegendarySkillActivateEvent {
        public Pre(LivingEntity entity, ResourceLocation skillId, ArmorLegendarySkill skill) {
            super(entity, skillId, skill);
        }
    }

    public static final class Post extends ArmorLegendarySkillActivateEvent {
        public Post(LivingEntity entity, ResourceLocation skillId, ArmorLegendarySkill skill) {
            super(entity, skillId, skill);
        }
    }
}
