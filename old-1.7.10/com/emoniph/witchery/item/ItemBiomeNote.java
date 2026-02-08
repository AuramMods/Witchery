package com.emoniph.witchery.item;

import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;

public class ItemBiomeNote extends ItemBase {
   public ItemBiomeNote() {
      this.func_77627_a(true);
      this.func_77656_e(0);
   }

   public String func_77653_i(ItemStack stack) {
      String name = super.func_77653_i(stack);
      BiomeGenBase biome = ItemBook.getSelectedBiome(stack.func_77960_j());
      return biome != null ? String.format(name, biome.field_76791_y) : String.format(name, "").trim();
   }
}
