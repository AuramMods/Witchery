package com.emoniph.witchery.util;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public enum ClothColor {
   WHITE(0),
   ORANGE(1),
   MAGENTA(2),
   LIGHT_BLUE(3),
   YELLOW(4),
   LIME(5),
   PINK(6),
   GRAY(7),
   LIGHT_GRAY(8),
   CYAN(9),
   PURPLE(10),
   BLUE(11),
   BROWN(12),
   GREEN(13),
   RED(14),
   BLACK(15);

   public final int damageValue;

   private ClothColor(int damageValue) {
      this.damageValue = damageValue;
   }

   public ItemStack createStack() {
      return this.createStack(1);
   }

   private ItemStack createStack(int quantity) {
      return new ItemStack(Blocks.field_150325_L, quantity, this.damageValue);
   }
}
