package com.emoniph.witchery.predictions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

public class PredictionBuriedTreasure extends PredictionAlwaysForced {
   protected final String chestGenHook;

   public PredictionBuriedTreasure(int id, int itemWeight, double selfFulfillmentProbabilityPerSec, String translationKey, int regularFulfillmentDurationInTicks, double regularFulfillmentProbability, String chestGenHook) {
      super(id, itemWeight, selfFulfillmentProbabilityPerSec, translationKey, regularFulfillmentDurationInTicks, regularFulfillmentProbability);
      this.chestGenHook = chestGenHook;
   }

   public boolean shouldTrySelfFulfill(World world, EntityPlayer player) {
      return false;
   }

   public boolean doSelfFulfillment(World world, EntityPlayer player) {
      return false;
   }

   public boolean checkIfFulfilled(World world, EntityPlayer player, HarvestDropsEvent event, boolean isPastDue, boolean veryOld) {
      if (!event.isCanceled() && (event.block == Blocks.field_150349_c || event.block == Blocks.field_150346_d || event.block == Blocks.field_150354_m || event.block == Blocks.field_150391_bh) && event.y > 6 && this.shouldWeActivate(world, player, isPastDue) && !world.func_147437_c(event.x + 1, event.y - 1, event.z) && !world.func_147437_c(event.x - 1, event.y - 1, event.z) && !world.func_147437_c(event.x, event.y - 1, event.z + 1) && !world.func_147437_c(event.x, event.y - 1, event.z - 1) && !world.func_147437_c(event.x, event.y - 2, event.z)) {
         world.func_147449_b(event.x, event.y - 1, event.z, Blocks.field_150486_ae);
         TileEntity tile = world.func_147438_o(event.x, event.y - 1, event.z);
         if (tile != null && tile instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest)tile;
            ChestGenHooks info = ChestGenHooks.getInfo(this.chestGenHook);
            WeightedRandomChestContent.func_76293_a(world.field_73012_v, info.getItems(world.field_73012_v), chest, info.getCount(world.field_73012_v));
         }

         return true;
      } else {
         return false;
      }
   }
}
