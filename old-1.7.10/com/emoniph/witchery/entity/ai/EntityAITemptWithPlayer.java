package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAITemptWithPlayer extends EntityAIBase {
   private EntityCreature temptedEntity;
   private double field_75282_b;
   private double field_75283_c;
   private double field_75280_d;
   private double field_75281_e;
   private double field_75278_f;
   private double field_75279_g;
   private EntityPlayer temptingPlayer;
   private int delayTemptCounter;
   private boolean field_75287_j;
   private boolean field_75286_m;

   public EntityAITemptWithPlayer(EntityCreature par1EntityCreature, double par2) {
      this.temptedEntity = par1EntityCreature;
      this.field_75282_b = par2;
      this.func_75248_a(3);
   }

   public boolean func_75250_a() {
      if (this.delayTemptCounter > 0) {
         --this.delayTemptCounter;
         return false;
      } else {
         this.temptingPlayer = this.temptedEntity.field_70170_p.func_72890_a(this.temptedEntity, 10.0D);
         return this.temptingPlayer != null;
      }
   }

   public boolean func_75253_b() {
      return this.func_75250_a();
   }

   public void func_75249_e() {
      this.field_75283_c = this.temptingPlayer.field_70165_t;
      this.field_75280_d = this.temptingPlayer.field_70163_u;
      this.field_75281_e = this.temptingPlayer.field_70161_v;
      this.field_75287_j = true;
      this.field_75286_m = this.temptedEntity.func_70661_as().func_75486_a();
      this.temptedEntity.func_70661_as().func_75491_a(false);
   }

   public void func_75251_c() {
      this.temptingPlayer = null;
      this.temptedEntity.func_70661_as().func_75499_g();
      this.delayTemptCounter = 100;
      this.field_75287_j = false;
      this.temptedEntity.func_70661_as().func_75491_a(this.field_75286_m);
   }

   public void func_75246_d() {
      this.temptedEntity.func_70671_ap().func_75651_a(this.temptingPlayer, 30.0F, (float)this.temptedEntity.func_70646_bf());
      if (this.temptedEntity.func_70068_e(this.temptingPlayer) < 6.25D) {
         this.temptedEntity.func_70661_as().func_75499_g();
      } else {
         this.temptedEntity.func_70661_as().func_75497_a(this.temptingPlayer, this.field_75282_b);
      }

   }

   public boolean func_75277_f() {
      return this.field_75287_j;
   }
}
