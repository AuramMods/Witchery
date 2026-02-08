package com.emoniph.witchery.util;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class EarthItems {
   private static final EarthItems INSTANCE = new EarthItems();
   private final List<Item> itemList;

   public static EarthItems instance() {
      return INSTANCE;
   }

   private EarthItems() {
      Item[] list = new Item[]{Items.field_151040_l, Items.field_151010_B, Items.field_151036_c, Items.field_151006_E, Items.field_151035_b, Items.field_151005_D, Items.field_151013_M, Items.field_151019_K, Items.field_151037_a, Items.field_151011_C, Items.field_151043_k, Items.field_151074_bl, Items.field_151042_j, Items.field_151028_Y, Items.field_151030_Z, Items.field_151165_aa, Items.field_151167_ab, Items.field_151169_ag, Items.field_151149_ai, Items.field_151171_ah, Items.field_151151_aj, Items.field_151020_U, Items.field_151022_W, Items.field_151023_V, Items.field_151029_X};
      this.itemList = Arrays.asList(list);
   }

   public boolean isMatch(ItemStack itemstack) {
      return itemstack == null ? false : this.itemList.contains(itemstack.func_77973_b());
   }

   public boolean isOre(Block block) {
      return block == Blocks.field_150366_p || block == Blocks.field_150352_o;
   }

   public Item oreToIngot(Block block) {
      if (block == Blocks.field_150366_p) {
         return Items.field_151042_j;
      } else if (block == Blocks.field_150352_o) {
         return Items.field_151043_k;
      } else {
         int oreID = OreDictionary.getOreID(new ItemStack(block));
         if (oreID != -1) {
            String oreName = OreDictionary.getOreName(oreID);
            if (oreName.startsWith("ore")) {
               String ingotName = oreName.replace("ore", "ingot");
               String[] oreNames = OreDictionary.getOreNames();
               if (Arrays.asList(oreNames).contains(ingotName)) {
                  int ingotID = OreDictionary.getOreID(ingotName);
                  List<ItemStack> ingotStacks = OreDictionary.getOres(ingotID);
                  if (!ingotStacks.isEmpty()) {
                     return ((ItemStack)ingotStacks.get(0)).func_77973_b();
                  }
               }
            }
         }

         return null;
      }
   }
}
