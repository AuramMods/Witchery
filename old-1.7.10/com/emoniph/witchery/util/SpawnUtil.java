package com.emoniph.witchery.util;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpawnUtil {
   private SpawnUtil() {
   }

   public static void spawnEntityItem(World world, double x, double y, double z, Block block, int quantity) {
      spawnEntityItem(world, x, y, z, Item.func_150898_a(block), quantity, 0);
   }

   public static void spawnEntityItem(World world, double x, double y, double z, Item item, int quantity) {
      spawnEntityItem(world, x, y, z, item, quantity, 0);
   }

   public static void spawnEntityItem(World world, double x, double y, double z, Item item, int quantity, int damageValue) {
      if (!world.field_72995_K) {
         int maxStackSize = item.getItemStackLimit(new ItemStack(item));

         int remainder;
         for(remainder = 0; remainder < quantity / maxStackSize; ++remainder) {
            world.func_72838_d(new EntityItem(world, x, y, z, new ItemStack(item, maxStackSize, damageValue)));
         }

         remainder = quantity % maxStackSize;
         if (remainder > 0) {
            world.func_72838_d(new EntityItem(world, x, y, z, new ItemStack(item, remainder, damageValue)));
         }
      }

   }

   public static void spawnEntityItem(World world, double x, double y, double z, ItemStack stack) {
      world.func_72838_d(new EntityItem(world, x, y, z, stack.func_77946_l()));
   }
}
