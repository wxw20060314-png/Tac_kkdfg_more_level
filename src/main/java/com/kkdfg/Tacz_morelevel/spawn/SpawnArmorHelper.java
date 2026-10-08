package com.kkdfg.Tacz_morelevel.spawn;

import com.kkdfg.Tacz_morelevel.api.SpawnArmorConfigurableMob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.util.RandomSource;

/** 生成护甲：附魔与穿甲编排，由 {@link SpawnArmorConfigurableMob} 调用。 */
public final class SpawnArmorHelper {

    private static final int PROTECTION_WEIGHT_SUM = 4 + 3 + 2 + 1;
    /** 固定按原 Hard：每件护甲独立掷骰附加「保护」的概率。 */
    private static final double PROTECTION_CHANCE_PER_PIECE = 0.6D;
    private static final ResourceLocation PROTECTION_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "protection");

    private SpawnArmorHelper() {}

    public static void applyFromPool(LivingEntity entity, String poolId, RandomSource random) {
        if (!(entity instanceof SpawnArmorConfigurableMob armorMob)) {
            return;
        }
        armorMob.clearSpawnArmorSlots();
        SpawnArmorPick pick = SoldierSpawnArmorCatalog.pick(random, poolId);
        if (pick == null || pick.bare()) {
            return;
        }
        armorMob.equipSpawnArmorSet(pick.helmet(), pick.chestplate(), pick.leggings(), pick.boots(), random);
    }

    public static void maybeEnchantProtection(ItemStack stack, RandomSource random) {
        if (random.nextDouble() >= PROTECTION_CHANCE_PER_PIECE) {
            return;
        }
        Enchantment protection = BuiltInRegistries.ENCHANTMENT.get(PROTECTION_ID);
        if (protection == null) {
            return;
        }
        stack.enchant(protection, rollProtectionLevel(random));
    }

    public static int rollProtectionLevel(RandomSource random) {
        int roll = random.nextInt(PROTECTION_WEIGHT_SUM);
        if (roll < 4) {
            return 1;
        }
        if (roll < 4 + 3) {
            return 2;
        }
        if (roll < 4 + 3 + 2) {
            return 3;
        }
        return 4;
    }
}
