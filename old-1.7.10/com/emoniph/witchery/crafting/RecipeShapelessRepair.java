package com.emoniph.witchery.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeShapelessRepair implements IRecipe {
   final ItemStack prototype;
   final ItemStack[] pattern;

   public RecipeShapelessRepair(ItemStack resultPoppet, ItemStack... pattern) {
      this.prototype = resultPoppet;
      this.pattern = pattern;
   }

   public ItemStack func_77571_b() {
      return null;
   }

   public boolean func_77569_a(InventoryCrafting par1InventoryCrafting, World par2World) {
      ArrayList<ItemStack> arraylist = new ArrayList(Arrays.asList(this.pattern));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            ItemStack itemstack = par1InventoryCrafting.func_70463_b(j, i);
            if (itemstack != null) {
               boolean flag = false;
               Iterator iterator = arraylist.iterator();

               while(iterator.hasNext()) {
                  ItemStack itemstack1 = (ItemStack)iterator.next();
                  if (itemstack.func_77973_b() == itemstack1.func_77973_b() && (!itemstack.func_77973_b().func_77614_k() || itemstack.func_77960_j() == itemstack1.func_77960_j())) {
                     flag = true;
                     arraylist.remove(itemstack1);
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }
         }
      }

      return arraylist.isEmpty();
   }

   public ItemStack func_77572_b(InventoryCrafting inv) {
      ItemStack item = this.findRecipeItemStack(inv, this.prototype.func_77973_b());
      ItemStack result = null;
      if (item != null) {
         result = item.func_77946_l();
         result.func_77964_b(0);
      }

      return result;
   }

   private ItemStack findRecipeItemStack(InventoryCrafting inv, Item itemToFind) {
      for(int i = 0; i < inv.func_70302_i_(); ++i) {
         ItemStack stack = inv.func_70301_a(i);
         if (stack != null && stack.func_77973_b() == itemToFind) {
            return stack;
         }
      }

      return null;
   }

   public int func_77570_a() {
      return this.pattern.length;
   }
}
