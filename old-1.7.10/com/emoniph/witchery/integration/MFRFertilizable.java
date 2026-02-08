package com.emoniph.witchery.integration;

import com.emoniph.witchery.blocks.BlockWitchSapling;
import com.emoniph.witchery.util.Log;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.FertilizerType;
import powercrystals.minefactoryreloaded.api.IFactoryFertilizable;

public class MFRFertilizable implements IFactoryFertilizable {
   private Block block;
   private int stages;

   public MFRFertilizable(Block block, int stages) {
      this.block = block;
      this.stages = stages;
   }

   public Block getPlant() {
      return this.block;
   }

   public boolean canFertilize(World world, int x, int y, int z, FertilizerType fertilizerType) {
      return fertilizerType == FertilizerType.GrowPlant && (this.stages == 0 || world.func_72805_g(x, y, z) < this.stages);
   }

   public boolean fertilize(World world, Random rand, int x, int y, int z, FertilizerType fertilizerType) {
      Block blockID = world.func_147439_a(x, y, z);
      Log.instance().debug(String.format("Fertilize %d, %d", blockID.func_149739_a(), this.stages));
      if (this.stages > 0) {
         int meta = world.func_72805_g(x, y, z);
         if (meta < this.stages) {
            int output = meta + rand.nextInt(3) + 1;
            if (output > this.stages) {
               output = this.stages;
            }

            world.func_72921_c(x, y, z, output, 3);
            return true;
         }
      } else if (this.block instanceof BlockWitchSapling) {
         BlockWitchSapling var10000 = (BlockWitchSapling)this.block;
         BlockWitchSapling.growTree(world, x, y, z, world.field_73012_v);
         return world.func_147439_a(x, y, z) != this.block;
      }

      return false;
   }
}
