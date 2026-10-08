package com.kkdfg.Tacz_morelevel.api;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * 生成护甲附魔扩展：{@link SpawnArmorConfigurableMob} 默认走保护附魔，
 * 双枪等子类可 override 各槽位是否附魔及附魔方式。
 */
public interface SpawnArmorEnchantableMob {

    /** 该槽位生成护甲是否尝试附魔。 */
    default boolean shouldEnchantSpawnArmorPiece(EquipmentSlot slot, ItemStack stack, RandomSource random) {
        return true;
    }

    /** 对单件生成护甲施加附魔；默认等同 {@link SpawnArmorConfigurableMob#maybeEnchantSpawnArmorPiece}。 */
    default void enchantSpawnArmorPiece(ItemStack stack, EquipmentSlot slot, RandomSource random) {
        if (this instanceof SpawnArmorConfigurableMob mob) {
            mob.maybeEnchantSpawnArmorPiece(stack, random);
        }
    }
}
