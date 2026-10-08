package com.kkdfg.Tacz_morelevel.item.medicine;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** 一次用药流程的上下文，供事件与 {@link MedicineFinishHandler} 使用。 */
public record MedicineContext(
        ServerPlayer player,
        ItemStack stack,
        MedicineSpec spec,
        InteractionHand hand,
        int channelPulseCount,
        boolean healedDuringUse) {}
