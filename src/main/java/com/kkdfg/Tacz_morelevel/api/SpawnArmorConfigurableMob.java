package com.kkdfg.Tacz_morelevel.api;

import com.kkdfg.Tacz_morelevel.spawn.SpawnArmorHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

/**
 * 生成时从 {@code data/tacz_morelevel/troop_armor/<poolId>.json} 按权重穿甲。
 * 实现类通过 {@link #spawnArmorPoolId()} 指定 JSON 文件名（不含 {@code .json}）。
 */
public interface SpawnArmorConfigurableMob {

    /** {@code troop_armor/{poolId}.json} 中的 {@code poolId}。 */
    String spawnArmorPoolId();

    default boolean appliesSpawnArmorOnFinalize() {
        return true;
    }

    /** 生成时入口：清槽 → 读 JSON 抽奖 → 穿甲 → 附魔。 */
    default void applySpawnArmorFromJson(RandomSource random) {
        if (!(this instanceof LivingEntity living)) {
            return;
        }
        SpawnArmorHelper.applyFromPool(living, spawnArmorPoolId(), random);
    }

    default void maybeEnchantSpawnArmorPiece(ItemStack stack, RandomSource random) {
        SpawnArmorHelper.maybeEnchantProtection(stack, random);
    }

    default void equipSpawnArmorPiece(EquipmentSlot slot, Item item, RandomSource random) {
        if (!(this instanceof LivingEntity living)) {
            return;
        }
        ItemStack stack = new ItemStack(item);
        if (this instanceof SpawnArmorEnchantableMob enchantable) {
            if (enchantable.shouldEnchantSpawnArmorPiece(slot, stack, random)) {
                enchantable.enchantSpawnArmorPiece(stack, slot, random);
            }
        } else {
            maybeEnchantSpawnArmorPiece(stack, random);
        }
        living.setItemSlot(slot, stack);
    }

    default void equipSpawnArmorSet(Item head, Item chest, Item legs, Item feet, RandomSource random) {
        equipSpawnArmorPiece(EquipmentSlot.HEAD, head, random);
        equipSpawnArmorPiece(EquipmentSlot.CHEST, chest, random);
        equipSpawnArmorPiece(EquipmentSlot.LEGS, legs, random);
        equipSpawnArmorPiece(EquipmentSlot.FEET, feet, random);
    }

    default void clearSpawnArmorSlots() {
        if (!(this instanceof LivingEntity living)) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                living.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}
