package com.emoniph.witchery.predictions;

import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Log;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PredictionNetherTrip extends Prediction {
   public PredictionNetherTrip(int id, int itemWeight, double selfFulfillmentProbabilityPerSec, String translationKey) {
      super(id, itemWeight, selfFulfillmentProbabilityPerSec, translationKey);
   }

   public boolean isPredictionPossible(World world, EntityPlayer player) {
      try {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         boolean wasInNether = nbtPlayer != null && nbtPlayer.func_74764_b("WITCVisitedNether") && nbtPlayer.func_74767_n("WITCVisitedNether");
         boolean isPossible = player.field_71093_bK != -1 && wasInNether;
         return isPossible;
      } catch (Throwable var7) {
         Log.instance().warning(var7, "Error occurred while checking if a nether visit has occurred for the nether prediction.");
         return false;
      }
   }

   public boolean doSelfFulfillment(World world2, EntityPlayer player) {
      int FALL_DISTANCE = true;
      int RADIUS = true;
      int x = MathHelper.func_76128_c(player.field_70165_t);
      int y = MathHelper.func_76128_c(player.field_70163_u) - 1;
      int z = MathHelper.func_76128_c(player.field_70161_v);
      if (!world2.field_72995_K && player.field_71093_bK != -1) {
         ChatUtil.sendTranslated(EnumChatFormatting.LIGHT_PURPLE, player, "witchery.prediction.tothenether.summoned");
         player.func_70063_aa();
         World world = player.field_70170_p;
         int MAX_DISTANCE = true;
         int MIN_DISTANCE = true;
         int activeRadius = 2;
         int ax = world.field_73012_v.nextInt(activeRadius * 2 + 1);
         if (ax > activeRadius) {
            ax += 4;
         }

         int nx = x - 4 + ax;
         int az = world.field_73012_v.nextInt(activeRadius * 2 + 1);
         if (az > activeRadius) {
            az += 4;
         }

         int nz = z - 4 + az;

         int ny;
         for(ny = y; !world.func_147437_c(nx, ny, nz) && ny < y + 8; ++ny) {
         }

         while(world.func_147437_c(nx, ny, nz) && ny > 0) {
            --ny;
         }

         int hy;
         for(hy = 0; world.func_147437_c(nx, ny + hy + 1, nz) && hy < 6; ++hy) {
         }

         EntityBlaze entity = new EntityBlaze(world);
         if ((float)hy >= entity.field_70131_O) {
            entity.func_70012_b((double)nx, (double)ny, (double)nz, 0.0F, 0.0F);
            world.func_72838_d(entity);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean checkIfFulfilled(World world, EntityPlayer player, LivingUpdateEvent event, boolean isPastDue, boolean veryOld) {
      if (player.field_71093_bK == -1) {
         Log.instance().debug(String.format("Prediction for got to nether fulfilled as predicted"));
         return true;
      } else {
         return false;
      }
   }
}
