package com.emoniph.witchery;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class WitcheryCreativeTab extends CreativeTabs {
   public static final WitcheryCreativeTab INSTANCE = new WitcheryCreativeTab();

   private WitcheryCreativeTab() {
      super("tabWitchery");
   }

   public Item func_78016_d() {
      return Witchery.Items.POPPET;
   }

   public ItemStack func_151244_d() {
      return Witchery.Items.GENERIC.itemBroomEnchanted.createStack();
   }
}
