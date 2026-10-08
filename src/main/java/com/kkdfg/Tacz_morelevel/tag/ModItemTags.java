package com.kkdfg.Tacz_morelevel.tag;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
   public static final TagKey<Item> TACZ_ARMOR = bind("tacz_armor");
   private static TagKey<Item> bind(String pName) {
      return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID,pName));
   }
}
