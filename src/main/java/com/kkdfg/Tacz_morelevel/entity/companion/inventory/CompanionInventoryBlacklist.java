package com.kkdfg.Tacz_morelevel.entity.companion.inventory;

import com.kkdfg.Tacz_morelevel.item.custom.BodyguardStorageItem;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.Tags;

/** {@link IAmmoBox} */
public final class CompanionInventoryBlacklist {

    private CompanionInventoryBlacklist() {}

    public static boolean isBlocked(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof IAmmoBox) {
            return false;
        }
        if (stack.getItem() instanceof BodyguardStorageItem) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"minecraft".equals(id.getNamespace())) {
            return false;
        }
        return isVanillaContainer(stack);
    }

    private static boolean isVanillaContainer(ItemStack stack) {
        if (stack.is(ItemTags.CHEST_BOATS)) {
            return true;
        }
        if (stack.is(Tags.Items.CHESTS) || stack.is(Tags.Items.BARRELS)) {
            return true;
        }
        Item item = stack.getItem();
        if (item instanceof BundleItem) {
            return true;
        }
        if (item == Items.CHEST_MINECART
                || item == Items.HOPPER_MINECART
                || item == Items.FURNACE_MINECART) {
            return true;
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block.defaultBlockState().is(BlockTags.SHULKER_BOXES)) {
                return true;
            }
            if (block instanceof EntityBlock entityBlock) {
                BlockEntity blockEntity =
                        entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
                if (blockEntity != null) {
                    try {
                        return blockEntity instanceof Container;
                    } finally {
                        blockEntity.setRemoved();
                    }
                }
            }
        }
        return false;
    }
}
