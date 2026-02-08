package com.emoniph.witchery.util;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public abstract class MultiItemBlock extends ItemBlock {
   private String[] names = null;

   public MultiItemBlock(Block block) {
      super(block);
      this.func_77656_e(0);
      this.func_77627_a(true);
   }

   private String[] internalGetNames() {
      if (this.names == null) {
         this.names = this.getNames();
      }

      return this.names;
   }

   protected abstract String[] getNames();

   public int func_77647_b(int par1) {
      return par1;
   }

   public String func_77667_c(ItemStack par1ItemStack) {
      int i = par1ItemStack.func_77960_j();
      String[] names = this.internalGetNames();
      if (i < 0 || i >= names.length) {
         i = 0;
      }

      return super.func_77658_a() + "." + names[i];
   }
}
