package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityAIMateWithPlayer extends EntityAIBase {
   private EntityVillager villagerObj;
   private EntityPlayer mate;
   private World worldObj;
   private int matingTimeout;
   private boolean forceExecute;

   public EntityAIMateWithPlayer(EntityVillager par1EntityVillager) {
      this.villagerObj = par1EntityVillager;
      this.worldObj = par1EntityVillager.field_70170_p;
      this.func_75248_a(3);
   }

   public void forceTask(EntityPlayer player) {
      this.forceExecute = true;
      this.mate = player;
   }

   public boolean func_75250_a() {
      if (this.villagerObj.func_70874_b() != 0) {
         return false;
      } else if (!this.forceExecute && this.villagerObj.func_70681_au().nextInt(500) != 0) {
         return false;
      } else if (!this.forceExecute) {
         Entity entity = this.worldObj.func_72857_a(EntityPlayer.class, this.villagerObj.field_70121_D.func_72314_b(16.0D, 3.0D, 16.0D), this.villagerObj);
         if (entity == null) {
            return false;
         } else {
            this.mate = (EntityPlayer)entity;
            return true;
         }
      } else {
         this.forceExecute = false;
         return true;
      }
   }

   public void func_75249_e() {
      this.matingTimeout = 1000;
      this.villagerObj.func_70947_e(true);
   }

   public void func_75251_c() {
      this.mate = null;
      this.villagerObj.func_70947_e(false);
   }

   public boolean func_75253_b() {
      return this.matingTimeout >= 0 && this.villagerObj.func_70874_b() == 0;
   }

   public void func_75246_d() {
      if (this.matingTimeout > 0) {
         --this.matingTimeout;
      }

      this.villagerObj.func_70671_ap().func_75651_a(this.mate, 10.0F, 30.0F);
      if (this.villagerObj.func_70068_e(this.mate) > 2.25D) {
         this.villagerObj.func_70661_as().func_75497_a(this.mate, 0.3D);
      } else if (this.matingTimeout > 0 && this.villagerObj.func_70068_e(this.mate) <= 2.25D) {
         this.matingTimeout = 0;
         this.giveBirth();
      }

      if (this.villagerObj.func_70681_au().nextInt(20) == 0) {
         this.worldObj.func_72960_a(this.villagerObj, (byte)12);
      }

   }

   private void giveBirth() {
      EntityVillager entityvillager = this.villagerObj.func_90011_a(this.villagerObj);
      this.villagerObj.func_70873_a(6000);
      entityvillager.func_70873_a(-24000);
      entityvillager.func_70012_b(this.villagerObj.field_70165_t, this.villagerObj.field_70163_u, this.villagerObj.field_70161_v, 0.0F, 0.0F);
      this.worldObj.func_72838_d(entityvillager);
      this.worldObj.func_72960_a(entityvillager, (byte)12);
   }
}
