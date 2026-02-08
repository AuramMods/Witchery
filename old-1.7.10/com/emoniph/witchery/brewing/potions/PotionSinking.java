package com.emoniph.witchery.brewing.potions;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionSinking extends PotionBase implements IHandleLivingUpdate {
   public PotionSinking(int id, int color) {
      super(id, true, color);
   }

   public void postContructInitialize() {
      this.setPermenant();
      this.setIncurable();
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if (world.field_72995_K) {
            if (player.func_70090_H()) {
               if (player.field_70181_x < -0.03D && !player.field_70122_E) {
                  player.field_70181_x *= 1.5D + Math.min(0.05D * (double)amplifier, 0.2D);
               } else if (!player.field_70122_E && player.func_70055_a(Material.field_151586_h) && player.field_70181_x > 0.0D) {
               }
            } else if (!player.field_71075_bZ.field_75098_d && player.field_71075_bZ.field_75101_c && player.field_71075_bZ.field_75100_b) {
               player.field_70181_x = -0.20000000298023224D;
            }
         }
      } else if (world.field_72995_K && world.func_82737_E() % 20L == 3L && entity.func_70090_H()) {
         if (entity.field_70181_x < 0.0D) {
            entity.field_70181_x *= 1.0D + Math.min(0.1D * (double)(amplifier + 2), 0.4D);
         } else if (entity.field_70181_x > 0.0D) {
            entity.field_70181_x *= 1.0D - Math.min(0.1D * (double)(amplifier + 2), 0.4D);
         }
      }

   }
}
