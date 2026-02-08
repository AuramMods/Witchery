package com.emoniph.witchery.crafting;

import com.emoniph.witchery.Witchery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class RecipeShapelessAddKeys implements IRecipe {
   final ItemStack prototype;
   final ItemStack[] pattern;

   public RecipeShapelessAddKeys(ItemStack result, ItemStack... pattern) {
      this.prototype = result;
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
      ItemStack ring = this.findRecipeItemStack(inv, this.prototype.func_77973_b(), this.prototype.func_77960_j());
      ItemStack result = ring != null ? ring.func_77946_l() : this.prototype.func_77946_l();

      for(int j = 0; j < inv.func_70302_i_(); ++j) {
         ItemStack key = inv.func_70301_a(j);
         if (key != null && Witchery.Items.GENERIC.itemDoorKey.isMatch(key) && key.func_77942_o()) {
            if (!result.func_77942_o()) {
               result.func_77982_d(new NBTTagCompound());
            }

            NBTTagCompound sourceTag = key.func_77978_p();
            int sourceX = sourceTag.func_74762_e("doorX");
            int sourceY = sourceTag.func_74762_e("doorY");
            int sourceZ = sourceTag.func_74762_e("doorZ");
            boolean sourceHasD = sourceTag.func_74764_b("doorD") && sourceTag.func_74764_b("doorDN");
            NBTTagCompound nbtTag = result.func_77978_p();
            if (!nbtTag.func_74764_b("doorKeys")) {
               nbtTag.func_74782_a("doorKeys", new NBTTagList());
            }

            NBTTagList keyList = nbtTag.func_150295_c("doorKeys", 10);

            for(int i = 0; i < keyList.func_74745_c(); ++i) {
               NBTTagCompound keyTag = keyList.func_150305_b(i);
               if (keyTag != null && keyTag.func_74764_b("doorX") && keyTag.func_74764_b("doorY") && keyTag.func_74764_b("doorZ")) {
                  int doorX = keyTag.func_74762_e("doorX");
                  int doorY = keyTag.func_74762_e("doorY");
                  int doorZ = keyTag.func_74762_e("doorZ");
                  boolean doorHasD = keyTag.func_74764_b("doorD") && keyTag.func_74764_b("doorDN");
                  if (doorX == sourceX && doorY == sourceY && doorZ == sourceZ && sourceHasD == doorHasD && (!sourceHasD || sourceTag.func_74762_e("doorD") == keyTag.func_74762_e("doorD"))) {
                     return result;
                  }
               }
            }

            NBTTagCompound nbtNewKey = new NBTTagCompound();
            nbtNewKey.func_74768_a("doorX", sourceX);
            nbtNewKey.func_74768_a("doorY", sourceY);
            nbtNewKey.func_74768_a("doorZ", sourceZ);
            if (sourceHasD) {
               nbtNewKey.func_74768_a("doorD", sourceTag.func_74762_e("doorD"));
               nbtNewKey.func_74778_a("doorDN", sourceTag.func_74779_i("doorDN"));
            }

            keyList.func_74742_a(nbtNewKey);
         }
      }

      return result;
   }

   private ItemStack findRecipeItemStack(InventoryCrafting inv, Item itemToFind, int meta) {
      for(int i = 0; i < inv.func_70302_i_(); ++i) {
         ItemStack stack = inv.func_70301_a(i);
         if (stack != null && stack.func_77973_b() == itemToFind && stack.func_77960_j() == meta) {
            return stack;
         }
      }

      return null;
   }

   public int func_77570_a() {
      return this.pattern.length;
   }
}
