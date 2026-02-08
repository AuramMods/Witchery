package com.emoniph.witchery.predictions;

import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.Log;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PredictionFall extends Prediction {
   public PredictionFall(int id, int itemWeight, double selfFulfillmentProbabilityPerSec, String translationKey) {
      super(id, itemWeight, selfFulfillmentProbabilityPerSec, translationKey);
   }

   public boolean doSelfFulfillment(World world, EntityPlayer player) {
      int FALL_DISTANCE = true;
      int RADIUS = true;
      int x0 = MathHelper.func_76128_c(player.field_70165_t);
      int y0 = MathHelper.func_76128_c(player.field_70163_u) - 1;
      int z0 = MathHelper.func_76128_c(player.field_70161_v);
      if (!world.field_72995_K && y0 > 8) {
         int dirtCount = 0;

         int x;
         int z;
         for(x = x0 - 1; x <= x0 + 1; ++x) {
            for(z = z0 - 1; z <= z0 + 1; ++z) {
               Material material = world.func_147439_a(x, y0, z).func_149688_o();
               if (material == Material.field_151578_c || material == Material.field_151577_b) {
                  ++dirtCount;
               }
            }
         }

         if ((double)dirtCount == Math.pow(3.0D, 2.0D)) {
            for(x = x0 - 1; x <= x0 + 1; ++x) {
               for(z = z0 - 1; z <= z0 + 1; ++z) {
                  for(int y = y0; y > y0 - 6; --y) {
                     if (y == y0) {
                        world.func_147449_b(x, y, z, Blocks.field_150351_n);
                     } else if (BlockProtect.canBreak(world.func_147439_a(x, y, z), world)) {
                        world.func_147468_f(x, y, z);
                     }
                  }
               }
            }

            Log.instance().debug(String.format("Prediction for falling has been forced"));
            return true;
         }
      }

      return false;
   }

   public boolean checkIfFulfilled(World world, EntityPlayer player, LivingHurtEvent event, boolean isPastDue, boolean veryOld) {
      if (!event.isCanceled()) {
         boolean fallen = event.source == DamageSource.field_76379_h;
         if (fallen) {
            Log.instance().debug(String.format("Prediction for FALL fulfilled as predicted"));
         }

         return fallen;
      } else {
         return false;
      }
   }
}
