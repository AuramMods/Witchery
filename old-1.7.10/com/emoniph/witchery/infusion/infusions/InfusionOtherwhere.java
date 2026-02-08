package com.emoniph.witchery.infusion.infusions;

import com.emoniph.witchery.brewing.potions.PotionEnderInhibition;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.DimensionalLocation;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class InfusionOtherwhere extends Infusion {
   private static final String RECALL_LOCATON_KEY = "WITCRecall";
   private static final int SAVE_RECALL_POINT_THRESHOLD = 60;

   public InfusionOtherwhere(int infusionID) {
      super(infusionID);
   }

   public IIcon getPowerBarIcon(EntityPlayer player, int index) {
      return Blocks.field_150427_aO.func_149691_a(0, 0);
   }

   public void onLeftClickEntity(ItemStack itemstack, World world, EntityPlayer player, Entity otherEntity) {
      if (!world.field_72995_K) {
         if (otherEntity instanceof EntityLivingBase) {
            EntityLivingBase otherLivingEntity = (EntityLivingBase)otherEntity;
            if (player.func_70093_af()) {
               DimensionalLocation recallLocation = this.recallLocation(getNBT(player), "WITCRecall");
               if (recallLocation != null && recallLocation.dimension != Config.instance().dimensionDreamID && recallLocation.dimension != Config.instance().dimensionTormentID && recallLocation.dimension != Config.instance().dimensionMirrorID && world.field_73011_w.field_76574_g != Config.instance().dimensionDreamID && world.field_73011_w.field_76574_g != Config.instance().dimensionTormentID && world.field_73011_w.field_76574_g != Config.instance().dimensionMirrorID && !PotionEnderInhibition.isActive(player, 2) && this.consumeCharges(world, player, 4, false)) {
                  if (player instanceof EntityPlayerMP && !isConnectionClosed((EntityPlayerMP)player)) {
                     player.field_70143_R = 0.0F;
                     ItemGeneral.teleportToLocation(world, recallLocation.posX, recallLocation.posY, recallLocation.posZ, recallLocation.dimension, player, true);
                     otherLivingEntity.field_70143_R = 0.0F;
                     if (!PotionEnderInhibition.isActive(otherLivingEntity, 2)) {
                        ItemGeneral.teleportToLocation(world, recallLocation.posX, recallLocation.posY, recallLocation.posZ, recallLocation.dimension, otherLivingEntity, true);
                     }
                  }
               } else {
                  world.func_72956_a(player, "note.snare", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
               }
            } else if (!PotionEnderInhibition.isActive(player, 2) && this.consumeCharges(world, player, 2, true)) {
               double HIKE_HEIGHT = 8.0D;
               MovingObjectPosition hitMOP = raytraceUpBlocks(world, player, true, 8.0D);
               double hikeModified = hitMOP == null ? 8.0D : Math.min((double)hitMOP.field_72312_c - otherLivingEntity.field_70163_u - 2.0D, 8.0D);
               MovingObjectPosition hitMOP2 = raytraceUpBlocks(world, otherLivingEntity, true, 8.0D);
               double hikeModified2 = hitMOP2 == null ? 8.0D : Math.min((double)hitMOP2.field_72312_c - otherLivingEntity.field_70163_u - 2.0D, 8.0D);
               if (player instanceof EntityPlayerMP && !isConnectionClosed((EntityPlayerMP)player) && hikeModified > 0.0D && hikeModified2 > 0.0D) {
                  ItemGeneral.teleportToLocation(world, player.field_70165_t, player.field_70163_u + hikeModified, player.field_70161_v, player.field_71093_bK, player, true);
                  if (!PotionEnderInhibition.isActive(otherLivingEntity, 2)) {
                     ItemGeneral.teleportToLocation(world, otherLivingEntity.field_70165_t, otherLivingEntity.field_70163_u + hikeModified2, otherLivingEntity.field_70161_v, otherLivingEntity.field_71093_bK, otherLivingEntity, true);
                  }
               }
            }
         }

      }
   }

   public void onUsingItemTick(ItemStack itemstack, World world, EntityPlayer player, int countdown) {
      int elapsedTicks = this.getMaxItemUseDuration(itemstack) - countdown;
      if (player.func_70093_af() && elapsedTicks == 60) {
         if (!world.field_72995_K) {
            ChatUtil.sendTranslated(EnumChatFormatting.GRAY, player, "witchery.infuse.cansetrecall");
         }

         player.field_70170_p.func_72956_a(player, "note.pling", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
      } else if (!player.func_70093_af() && elapsedTicks > 0 && elapsedTicks % 20 == 0) {
         int MAX_TELEPORT_DISTANCE = 40 + 20 * (elapsedTicks / 20);
         MovingObjectPosition hitMOP = doCustomRayTrace(world, player, true, (double)MAX_TELEPORT_DISTANCE);
         if (hitMOP != null) {
            player.field_70170_p.func_72956_a(player, "random.orb", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
            if (!world.field_72995_K) {
               ChatUtil.sendTranslated(EnumChatFormatting.GRAY, player, "witchery.infuse.canteleport");
            }
         } else {
            player.field_70170_p.func_72956_a(player, "random.pop", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
         }
      }

   }

   public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int countdown) {
      if (!world.field_72995_K) {
         int elapsedTicks = this.getMaxItemUseDuration(itemstack) - countdown;
         if (player.func_70093_af() && elapsedTicks >= 60) {
            this.storeLocation(getNBT(player), "WITCRecall", player);
            SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
         } else if (player.func_70093_af()) {
            DimensionalLocation recallLocation = this.recallLocation(getNBT(player), "WITCRecall");
            if (recallLocation != null && recallLocation.dimension != Config.instance().dimensionDreamID && recallLocation.dimension != Config.instance().dimensionTormentID && recallLocation.dimension != Config.instance().dimensionMirrorID && world.field_73011_w.field_76574_g != Config.instance().dimensionDreamID && world.field_73011_w.field_76574_g != Config.instance().dimensionTormentID && world.field_73011_w.field_76574_g != Config.instance().dimensionMirrorID && !PotionEnderInhibition.isActive(player, 2) && this.consumeCharges(world, player, 2, false)) {
               if (player instanceof EntityPlayerMP && !isConnectionClosed((EntityPlayerMP)player)) {
                  player.field_70143_R = 0.0F;
                  ItemGeneral.teleportToLocation(world, recallLocation.posX, recallLocation.posY, recallLocation.posZ, recallLocation.dimension, player, true);
                  Infusion.setCooldown(world, itemstack, 1500);
               }
            } else {
               world.func_72956_a(player, "note.snare", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
            }
         } else {
            int MAX_TELEPORT_DISTANCE = 40 + 20 * (elapsedTicks / 20);
            MovingObjectPosition hitMOP = doCustomRayTrace(world, player, true, (double)MAX_TELEPORT_DISTANCE);
            if (hitMOP != null && !PotionEnderInhibition.isActive(player, 2) && this.consumeCharges(world, player, 1, false)) {
               ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, player, 0.5D, 2.0D, 16);
               teleportEntity(player, hitMOP);
               ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, player, 0.5D, 2.0D, 16);
               Infusion.setCooldown(world, itemstack, 1500);
            } else {
               world.func_72956_a(player, "note.snare", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
               if (hitMOP == null && !world.field_72995_K) {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.cannotteleport");
               }
            }
         }

      }
   }

   private void storeLocation(NBTTagCompound nbt, String key, EntityPlayer player) {
      DimensionalLocation location = new DimensionalLocation(player);
      location.saveToNBT(nbt, key);
      if (!player.field_70170_p.field_72995_K) {
         ChatUtil.sendTranslated(EnumChatFormatting.GRAY, player, "witchery.infuse.setrecall", player.field_70170_p.field_73011_w.func_80007_l(), Integer.valueOf(MathHelper.func_76128_c(location.posX)).toString(), Integer.valueOf(MathHelper.func_76128_c(location.posY)).toString(), Integer.valueOf(MathHelper.func_76128_c(location.posZ)).toString());
      }

   }

   private DimensionalLocation recallLocation(NBTTagCompound nbtTag, String key) {
      DimensionalLocation location = new DimensionalLocation(nbtTag, key);
      return !location.isValid ? null : location;
   }

   public static void teleportEntity(EntityPlayer entityPlayer, MovingObjectPosition hitMOP) {
      if (hitMOP != null && entityPlayer instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)entityPlayer;
         if (!isConnectionClosed(player)) {
            switch(hitMOP.field_72313_a) {
            case ENTITY:
               player.func_70634_a(hitMOP.field_72307_f.field_72450_a, hitMOP.field_72307_f.field_72448_b, hitMOP.field_72307_f.field_72449_c);
               break;
            case BLOCK:
               double hitx = hitMOP.field_72307_f.field_72450_a;
               double hity = hitMOP.field_72307_f.field_72448_b;
               double hitz = hitMOP.field_72307_f.field_72449_c;
               switch(hitMOP.field_72310_e) {
               case 0:
                  hity -= 2.0D;
               case 1:
               default:
                  break;
               case 2:
                  hitz -= 0.5D;
                  break;
               case 3:
                  hitz += 0.5D;
                  break;
               case 4:
                  hitx -= 0.5D;
                  break;
               case 5:
                  hitx += 0.5D;
               }

               player.field_70143_R = 0.0F;
               player.func_70634_a(hitx, hity, hitz);
            }
         }
      }

   }

   public static MovingObjectPosition doCustomRayTrace(World world, EntityPlayer player, boolean collisionFlag, double reachDistance) {
      MovingObjectPosition pickedBlock = raytraceBlocks(world, player, collisionFlag, reachDistance);
      MovingObjectPosition pickedEntity = raytraceEntities(world, player, collisionFlag, reachDistance);
      if (pickedBlock == null) {
         return pickedEntity;
      } else if (pickedEntity == null) {
         return pickedBlock;
      } else {
         Vec3 playerPosition = Vec3.func_72443_a(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v);
         double dBlock = pickedBlock.field_72307_f.func_72438_d(playerPosition);
         double dEntity = pickedEntity.field_72307_f.func_72438_d(playerPosition);
         return dEntity < dBlock ? pickedEntity : pickedBlock;
      }
   }

   public static MovingObjectPosition raytraceEntities(World world, EntityPlayer player, boolean collisionFlag, double reachDistance) {
      MovingObjectPosition pickedEntity = null;
      Vec3 playerPosition = Vec3.func_72443_a(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v);
      Vec3 playerLook = player.func_70040_Z();
      Vec3 playerViewOffset = Vec3.func_72443_a(playerPosition.field_72450_a + playerLook.field_72450_a * reachDistance, playerPosition.field_72448_b + playerLook.field_72448_b * reachDistance, playerPosition.field_72449_c + playerLook.field_72449_c * reachDistance);
      double playerBorder = 1.1D * reachDistance;
      AxisAlignedBB boxToScan = player.field_70121_D.func_72314_b(playerBorder, playerBorder, playerBorder);
      List entitiesHit = world.func_72839_b(player, boxToScan);
      double closestEntity = reachDistance;
      if (entitiesHit != null && !entitiesHit.isEmpty()) {
         Iterator i$ = entitiesHit.iterator();

         while(true) {
            Entity entityHit;
            MovingObjectPosition hitMOP;
            do {
               while(true) {
                  AxisAlignedBB aabb;
                  do {
                     do {
                        do {
                           do {
                              if (!i$.hasNext()) {
                                 return pickedEntity;
                              }

                              entityHit = (Entity)i$.next();
                           } while(entityHit == null);
                        } while(!entityHit.func_70067_L());
                     } while(entityHit.field_70121_D == null);

                     float border = entityHit.func_70111_Y();
                     aabb = entityHit.field_70121_D.func_72314_b((double)border, (double)border, (double)border);
                     hitMOP = aabb.func_72327_a(playerPosition, playerViewOffset);
                  } while(hitMOP == null);

                  if (aabb.func_72318_a(playerPosition)) {
                     break;
                  }

                  double distance = playerPosition.func_72438_d(hitMOP.field_72307_f);
                  if (distance < closestEntity || closestEntity == 0.0D) {
                     pickedEntity = new MovingObjectPosition(entityHit);
                     pickedEntity.field_72307_f = hitMOP.field_72307_f;
                     closestEntity = distance;
                  }
               }
            } while(!(0.0D < closestEntity) && closestEntity != 0.0D);

            pickedEntity = new MovingObjectPosition(entityHit);
            if (pickedEntity != null) {
               pickedEntity.field_72307_f = hitMOP.field_72307_f;
               closestEntity = 0.0D;
            }
         }
      } else {
         return null;
      }
   }

   private static boolean isConnectionClosed(EntityPlayerMP player) {
      return !player.field_71135_a.field_147371_a.func_150724_d();
   }

   public static MovingObjectPosition raytraceBlocks(World world, EntityPlayer player, boolean collisionFlag, double reachDistance) {
      Vec3 playerPosition = Vec3.func_72443_a(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v);
      Vec3 playerLook = player.func_70040_Z();
      Vec3 playerViewOffset = Vec3.func_72443_a(playerPosition.field_72450_a + playerLook.field_72450_a * reachDistance, playerPosition.field_72448_b + playerLook.field_72448_b * reachDistance, playerPosition.field_72449_c + playerLook.field_72449_c * reachDistance);
      return world.func_147447_a(playerPosition, playerViewOffset, collisionFlag, !collisionFlag, false);
   }

   private static MovingObjectPosition raytraceUpBlocks(World world, EntityLivingBase player, boolean collisionFlag, double reachDistance) {
      Vec3 playerPosition = Vec3.func_72443_a(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v);
      Vec3 playerUp = Vec3.func_72443_a(0.0D, 1.0D, 0.0D);
      Vec3 playerViewOffset = Vec3.func_72443_a(playerPosition.field_72450_a + playerUp.field_72450_a * reachDistance, playerPosition.field_72448_b + playerUp.field_72448_b * reachDistance, playerPosition.field_72449_c + playerUp.field_72449_c * reachDistance);
      return world.func_147447_a(playerPosition, playerViewOffset, collisionFlag, !collisionFlag, false);
   }
}
