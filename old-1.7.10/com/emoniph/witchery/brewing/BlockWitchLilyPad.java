package com.emoniph.witchery.brewing;

import com.emoniph.witchery.util.BlockUtil;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockWitchLilyPad extends BlockLilyPad {
   private static final int[][] DIRECTIONS = new int[][]{{-1, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, -1}, {1, 0}, {1, 1}, {1, 1}};

   public BlockWitchLilyPad() {
      this.func_149711_c(0.0F);
      this.func_149672_a(field_149779_h);
   }

   public Block func_149663_c(String blockName) {
      this.func_149647_a((CreativeTabs)null);
      BlockUtil.registerBlock(this, blockName);
      return super.func_149663_c(blockName);
   }

   public void func_149726_b(World world, int x, int y, int z) {
      world.func_147464_a(x, y, z, this, 20);
   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      if (!world.field_72995_K) {
         int meta = world.func_72805_g(x, y, z);
         if (this.canSpread(meta)) {
            int[] d = DIRECTIONS[world.field_73012_v.nextInt(DIRECTIONS.length)];
            if (this.func_149718_j(world, x + d[0], y, z + d[1])) {
               int growth = meta >> 2 & 3;
               int facing = meta & 3;
               if (world.field_73012_v.nextInt(growth) == 0) {
                  meta = ((growth - 1 & 3) << 2) + facing;
                  world.func_72921_c(x, y, z, meta, 3);
               }

               world.func_147465_d(x + d[0], y, z + d[1], this, (growth - 1 & 3) << 2, 3);
            }

            world.func_147464_a(x, y, z, this, 20);
         }
      }

      super.func_149674_a(world, x, y, z, rand);
   }

   private boolean canSpread(int meta) {
      boolean flag = (meta >> 2 & 3) > 0;
      return flag;
   }

   public int func_149745_a(Random rand) {
      return 1;
   }

   public Item func_149650_a(int metadata, Random rand, int fortune) {
      return rand.nextInt(4) == 0 ? Item.func_150898_a(Blocks.field_150392_bi) : null;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return new ItemStack(Blocks.field_150392_bi);
   }

   protected boolean func_149700_E() {
      return false;
   }
}
