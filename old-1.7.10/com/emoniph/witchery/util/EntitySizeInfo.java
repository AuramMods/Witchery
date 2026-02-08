package com.emoniph.witchery.util;

import com.emoniph.witchery.common.ExtendedPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class EntitySizeInfo {
   public final float defaultWidth;
   public final float defaultHeight;
   public final float eyeHeight;
   public final float stepSize;
   public final boolean isDefault;
   public final TransformCreature creature;

   public EntitySizeInfo(EntityLivingBase entity) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         this.creature = ExtendedPlayer.get(player).getCreatureType();
         NBTTagCompound nbtEntity = entity.getEntityData();
         switch(this.creature) {
         case NONE:
         case PLAYER:
         default:
            this.isDefault = true;
            this.defaultWidth = 0.6F;
            this.defaultHeight = 1.8F;
            this.stepSize = 0.5F;
            this.eyeHeight = player.getDefaultEyeHeight();
            break;
         case WOLF:
            this.isDefault = false;
            this.defaultWidth = 0.6F;
            this.defaultHeight = 0.8F;
            this.eyeHeight = this.defaultHeight * 0.92F;
            this.stepSize = 1.0F;
            break;
         case WOLFMAN:
            this.isDefault = true;
            this.defaultWidth = 0.6F;
            this.defaultHeight = 1.8F;
            this.eyeHeight = player.getDefaultEyeHeight();
            this.stepSize = 1.0F;
            break;
         case BAT:
            this.isDefault = false;
            this.defaultWidth = 0.3F;
            this.defaultHeight = 0.6F;
            this.eyeHeight = this.defaultHeight * 0.8F;
            this.stepSize = 0.5F;
            break;
         case TOAD:
            this.isDefault = false;
            this.defaultWidth = 0.3F;
            this.defaultHeight = 0.5F;
            this.eyeHeight = this.defaultHeight * 0.92F;
            this.stepSize = 0.5F;
         }
      } else {
         NBTTagCompound nbtEntity = entity.getEntityData();
         this.defaultWidth = nbtEntity.func_74760_g("WITCInitialWidth");
         this.defaultHeight = nbtEntity.func_74760_g("WITCInitialHeight");
         this.stepSize = !(entity instanceof EntityHorse) && !(entity instanceof EntityEnderman) ? 0.5F : 1.0F;
         this.eyeHeight = 0.12F;
         this.isDefault = true;
         this.creature = TransformCreature.NONE;
      }

   }
}
