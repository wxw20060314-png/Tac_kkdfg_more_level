package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;

/** 冰冻地雷仅能放置在方块顶面上。 */
public final class FrozenLandmineBlockItem extends BlockItem {

    public FrozenLandmineBlockItem(Item.Properties properties) {
        super(ModBlocks.FROZEN_LANDMINE.get(), properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }
}
