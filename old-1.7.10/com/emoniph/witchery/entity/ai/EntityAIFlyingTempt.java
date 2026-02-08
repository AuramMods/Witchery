package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class EntityAIFlyingTempt extends EntityAIBase {
   private EntityCreature temptedEntity;
   private double field_75282_b;
   private double targetX;
   private double targetY;
   private double targetZ;
   private double field_75278_f;
   private double field_75279_g;
   private EntityPlayer temptingPlayer;
   private int delayTemptCounter;
   private boolean isRunning;
   private ItemStack[] breedingFood;
   private boolean scaredByPlayerMovement;
   private boolean field_75286_m;

   public EntityAIFlyingTempt(EntityCreature par1EntityCreature, double par2, ItemStack[] par4, boolean par5) {
      this.temptedEntity = par1EntityCreature;
      this.field_75282_b = par2;
      this.breedingFood = par4;
      this.scaredByPlayerMovement = par5;
      this.func_75248_a(1);
   }

   public boolean func_75250_a() {
      boolean isTame = this.temptedEntity instanceof EntityTameable && ((EntityTameable)this.temptedEntity).func_70909_n();
      if (isTame) {
         return false;
      } else if (this.delayTemptCounter > 0) {
         --this.delayTemptCounter;
         return false;
      } else {
         this.temptingPlayer = this.temptedEntity.field_70170_p.func_72890_a(this.temptedEntity, 10.0D);
         if (this.temptingPlayer == null) {
            return false;
         } else {
            ItemStack itemstack = this.temptingPlayer.func_71045_bC();
            return itemstack == null ? false : this.isBreedingFood(itemstack);
         }
      }
   }

   private boolean isBreedingFood(ItemStack stack) {
      ItemStack[] arr$ = this.breedingFood;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ItemStack possibleFoods = arr$[i$];
         if (possibleFoods.func_77969_a(stack)) {
            return true;
         }
      }

      return false;
   }

   public boolean func_75253_b() {
      if (this.scaredByPlayerMovement) {
         if (this.temptedEntity.func_70068_e(this.temptingPlayer) < 36.0D) {
            if (this.temptingPlayer.func_70092_e(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002D) {
               return false;
            }
         } else {
            this.targetX = this.temptingPlayer.field_70165_t;
            this.targetY = this.temptingPlayer.field_70163_u;
            this.targetZ = this.temptingPlayer.field_70161_v;
         }

         this.field_75278_f = (double)this.temptingPlayer.field_70125_A;
         this.field_75279_g = (double)this.temptingPlayer.field_70177_z;
      }

      return this.func_75250_a();
   }

   public void func_75249_e() {
      this.isRunning = true;
   }

   public void func_75251_c() {
      this.temptingPlayer = null;
      this.delayTemptCounter = 100;
      this.isRunning = false;
   }

   public void func_75246_d() {
      if (this.temptedEntity.func_70068_e(this.temptingPlayer) >= 3.0D) {
         double d0 = this.targetX - this.temptedEntity.field_70165_t;
         double d1 = this.targetY - this.temptedEntity.field_70163_u;
         double d2 = this.targetZ - this.temptedEntity.field_70161_v;
         double d3 = d0 * d0 + d1 * d1 + d2 * d2;
         d3 = (double)MathHelper.func_76133_a(d3);
         if (this.isCourseTraversable(this.targetX, this.targetY, this.targetZ, d3)) {
            EntityCreature var10000 = this.temptedEntity;
            var10000.field_70159_w += d0 / d3 * 0.05D;
            if (this.temptedEntity.field_70163_u < this.targetY + 1.0D) {
               var10000 = this.temptedEntity;
               var10000.field_70181_x += d1 / d3 * 0.05D + 0.025D;
            } else {
               var10000 = this.temptedEntity;
               var10000.field_70181_x += d1 / d3 * 0.05D;
            }

            var10000 = this.temptedEntity;
            var10000.field_70179_y += d2 / d3 * 0.05D;
         }

         this.temptedEntity.field_70761_aq = this.temptedEntity.field_70177_z = -((float)Math.atan2(this.temptedEntity.field_70159_w, this.temptedEntity.field_70179_y)) * 180.0F / 3.1415927F;
      }

   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.temptedEntity.field_70165_t) / par7;
      double d5 = (par3 - this.temptedEntity.field_70163_u) / par7;
      double d6 = (par5 - this.temptedEntity.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.temptedEntity.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.temptedEntity.field_70170_p.func_72945_a(this.temptedEntity, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public boolean isRunning() {
      return this.isRunning;
   }
}
