package com.kkdfg.Tacz_morelevel.entity;

import com.kkdfg.Tacz_morelevel.Config;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 在实体生成 / 读档后把 {@link Config.NpcAttributeStats} 写入属性实例。
 * {@code createAttributes()} 注册过早时常读到默认值；此处在 Config 已加载后现读变量并
 * {@link AttributeInstance#setBaseValue}。
 */
public final class ConfiguredLivingAttributes {

    private ConfiguredLivingAttributes() {}

    /**
     * @param fillHealth true=生成时拉满；false=读档时仅钳制不超过新上限
     */
    public static void apply(
            LivingEntity entity, @Nullable Config.NpcAttributeStats stats, boolean fillHealth) {
        if (entity.level().isClientSide() || stats == null) {
            return;
        }
        setBase(entity, Attributes.MAX_HEALTH, stats.maxHealth);
        setBase(entity, Attributes.MOVEMENT_SPEED, stats.movementSpeed);
        setBase(entity, Attributes.ATTACK_DAMAGE, stats.attackDamage);
        setBase(entity, Attributes.FOLLOW_RANGE, stats.followRange);
        if (fillHealth) {
            entity.setHealth(entity.getMaxHealth());
        } else if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private static void setBase(
            LivingEntity entity,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            double value) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
