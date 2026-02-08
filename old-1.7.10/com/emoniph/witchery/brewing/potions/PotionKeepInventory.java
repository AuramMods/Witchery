package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.common.ExtendedPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

public class PotionKeepInventory extends PotionBase implements IHandlePlayerDrops, IHandleLivingDeath {
   public PotionKeepInventory(int id, int color) {
      super(id, color);
   }

   public void postContructInitialize() {
   }

   public void onPlayerDrops(World world, EntityPlayer player, PlayerDropsEvent event, int amplifier) {
      if (!event.entityPlayer.field_70170_p.field_72995_K) {
         if (event.entityPlayer.field_70170_p.func_82736_K().func_82766_b("keepInventory")) {
            return;
         }

         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         if (playerEx != null) {
            playerEx.cachePlayerInventory();
         }

         event.setCanceled(true);
      }

   }

   public void onLivingDeath(World world, EntityLivingBase entity, LivingDeathEvent event, int amplifier) {
      if (!event.entityLiving.field_70170_p.field_72995_K && event.entityLiving instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)event.entityLiving;
         if (player.field_70170_p.func_82736_K().func_82766_b("keepInventory")) {
            return;
         }

         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         if (playerEx != null) {
            playerEx.backupPlayerInventory();
         }
      }

   }
}
