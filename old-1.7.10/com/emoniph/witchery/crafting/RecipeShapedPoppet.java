package com.emoniph.witchery.crafting;

import com.emoniph.witchery.Witchery;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeShapedPoppet implements IRecipe {
   final ItemStack prototype;
   final Item[] pattern;

   public RecipeShapedPoppet(ItemStack resultPoppet, Item[] pattern) {
      this.prototype = resultPoppet;
      this.pattern = pattern;
   }

   public boolean func_77569_a(InventoryCrafting inv, World world) {
      for(int i = 0; i < inv.func_70302_i_(); ++i) {
         ItemStack stack = inv.func_70301_a(i);
         if (this.pattern[i] != null || stack != null) {
            if (stack == null || this.pattern[i] == null) {
               return false;
            }

            if (stack.func_77973_b() != this.pattern[i]) {
               return false;
            }

            if (stack.func_77973_b() == Witchery.Items.TAGLOCK_KIT && stack.func_77960_j() != 1) {
               return false;
            }
         }
      }

      return true;
   }

   public ItemStack func_77572_b(InventoryCrafting inv) {
      ItemStack stackPoppet = this.prototype.func_77946_l();
      ItemStack stackTaglockKit = this.findTaglockKit(inv);
      if (stackTaglockKit != null) {
         Witchery.Items.TAGLOCK_KIT.addTagLockToPoppet(stackTaglockKit, stackPoppet, 1);
      }

      return stackPoppet;
   }

   private ItemStack findTaglockKit(InventoryCrafting inv) {
      for(int i = 0; i < inv.func_70302_i_(); ++i) {
         ItemStack stack = inv.func_70301_a(1);
         if (stack.func_77973_b() == Witchery.Items.TAGLOCK_KIT) {
            return stack;
         }
      }

      return null;
   }

   public int func_77570_a() {
      return this.pattern.length;
   }

   public ItemStack func_77571_b() {
      return null;
   }
}
