package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionEquipmentContainer;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionInventory;
import java.util.Collection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public final class CompanionBodyguardDeathDropHelper {

    private CompanionBodyguardDeathDropHelper() {}

    
    public static void spawnDeathDrops(
            AbstractFriendlyCompanionEntity companion, Collection<ItemEntity> drops, float dropChancePerItem) {
        if (companion.level().isClientSide()) {
            return;
        }
        float chance = clampChance(dropChancePerItem);
        RandomSource random = companion.getRandom();
        Level level = companion.level();

        for (EquipmentSlot slot : CompanionEquipmentContainer.allEquipmentSlots()) {
            tryDropStack(companion, level, random, drops, chance, companion.getItemBySlot(slot));
            companion.setItemSlot(slot, ItemStack.EMPTY);
        }

        CompanionInventory inventory = companion.getCompanionInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            tryDropStack(companion, level, random, drops, chance, inventory.getItem(slot));
            inventory.setItem(slot, ItemStack.EMPTY);
        }
    }

    /** {@link Config#bodyguardDeathItemDropChance()} */
    public static void spawnDeathDrops(AbstractFriendlyCompanionEntity companion, Collection<ItemEntity> drops) {
        spawnDeathDrops(companion, drops, Config.bodyguardDeathItemDropChance());
    }

    private static void tryDropStack(
            AbstractFriendlyCompanionEntity companion,
            Level level,
            RandomSource random,
            Collection<ItemEntity> drops,
            float chance,
            ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (chance <= 0.0F || random.nextFloat() >= chance) {
            return;
        }
        drops.add(createDropEntity(companion, level, stack.copy()));
    }

    private static ItemEntity createDropEntity(AbstractFriendlyCompanionEntity companion, Level level, ItemStack stack) {
        double x = companion.getX() - 0.3D + companion.getRandom().nextDouble() * 0.6D;
        double y = companion.getY() + companion.getEyeHeight() - 0.3D;
        double z = companion.getZ() - 0.3D + companion.getRandom().nextDouble() * 0.6D;
        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
        itemEntity.setDefaultPickUpDelay();
        itemEntity.setDeltaMovement(
                companion.getRandom().nextGaussian() * 0.05D,
                companion.getRandom().nextDouble() * 0.05D + 0.2D,
                companion.getRandom().nextGaussian() * 0.05D);
        return itemEntity;
    }

    private static float clampChance(float chance) {
        if (chance < 0.0F) {
            return 0.0F;
        }
        if (chance > 1.0F) {
            return 1.0F;
        }
        return chance;
    }
}
