package com.emoniph.witchery.infusion.infusions.spirit;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.EntityUtil;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.tileentity.TileEntity;

public class InfusedSpiritTwisterEffect extends InfusedSpiritEffect {
   private static final double RANDOM_SPIN_RADIUS = 3.0D;
   private static final double RANDOM_SPIN_RADIUS_SQ = 9.0D;

   public InfusedSpiritTwisterEffect(int id, int spirits, int spectres, int banshees, int poltergeists) {
      super(id, "twister", spirits, spectres, banshees, poltergeists);
   }

   public int getCooldownTicks() {
      return 10;
   }

   public double getRadius() {
      return 8.0D;
   }

   public boolean doUpdateEffect(TileEntity tile, boolean triggered, ArrayList<EntityLivingBase> foundEntities) {
      if (triggered) {
         Iterator i$ = foundEntities.iterator();

         while(true) {
            float rev;
            double diff;
            EntityPlayer player;
            do {
               label49:
               do {
                  while(i$.hasNext()) {
                     EntityLivingBase entity = (EntityLivingBase)i$.next();
                     if (entity instanceof EntityPlayer) {
                        player = (EntityPlayer)entity;
                        continue label49;
                     }

                     if (entity instanceof EntityLiving) {
                        EntityLiving creature = (EntityLiving)entity;
                        if (creature.func_110138_aP() < 50.0F) {
                           EntityUtil.dropAttackTarget(creature);
                           if (foundEntities.size() > 1) {
                              EntityUtil.setTarget(creature, (EntityLivingBase)foundEntities.get(tile.func_145831_w().field_73012_v.nextInt(foundEntities.size())));
                           }
                        }
                     }
                  }

                  return triggered;
               } while(player.field_71071_by.func_70440_f(0) == null && player.field_71071_by.func_70440_f(1) == null && player.field_71071_by.func_70440_f(2) == null && player.field_71071_by.func_70440_f(3) == null && player.func_70694_bm() == null);

               double yawRadians = Math.atan2(player.field_70161_v - (0.5D + (double)tile.field_145849_e), player.field_70165_t - (0.5D + (double)tile.field_145851_c));
               double yaw = Math.toDegrees(yawRadians) + 180.0D;
               double playerYaw = (double)((player.field_70177_z + 90.0F) % 360.0F);
               if (playerYaw < 0.0D) {
                  playerYaw += 360.0D;
               }

               rev = ((float)yaw + 90.0F) % 360.0F;
               double ARC = 45.0D;
               diff = Math.abs(yaw - playerYaw);
            } while(!(360.0D - diff % 360.0D < 45.0D) && !(diff % 360.0D < 45.0D));

            if (player instanceof EntityPlayerMP) {
               S08PacketPlayerPosLook packet = new S08PacketPlayerPosLook(player.field_70165_t, player.field_70163_u, player.field_70161_v, rev, player.field_70125_A, false);
               Witchery.packetPipeline.sendTo((Packet)packet, (EntityPlayer)player);
            }
         }
      } else {
         return triggered;
      }
   }
}
