package com.kkdfg.Tacz_morelevel.armor.plugin;

import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Tooltip payload for installed armor plugins (bound to armor ItemStack NBT). */
public record ArmorPluginTooltip(List<ItemStack> plugins) implements TooltipComponent {}
