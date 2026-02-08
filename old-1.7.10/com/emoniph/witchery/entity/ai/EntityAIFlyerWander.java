package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFlyerWander extends EntityAIBase {
   private double xPosition;
   private double yPosition;
   private double zPosition;
   private double speed;
   World worldObj;
   public int courseChangeCooldown;
   public double waypointX;
   public double waypointY;
   public double waypointZ;
   public double fleeDistance;
   EntityLiving living;

   public EntityAIFlyerWander(EntityLiving par1EntityCreature, double par2, double fleeDistance) {
      this.living = par1EntityCreature;
      this.worldObj = this.living.field_70170_p;
      this.speed = par2;
      this.fleeDistance = fleeDistance;
      this.func_75248_a(1);
   }

   public boolean func_75250_a() {
      boolean isTame = this.living instanceof EntityTameable && ((EntityTameable)this.living).func_70909_n();
      if (!isTame && this.living.field_70170_p.func_72890_a(this.living, this.fleeDistance) != null) {
         return true;
      } else if (this.living.func_70654_ax() >= 100) {
         return false;
      } else {
         return this.living.func_70681_au().nextInt(this.living.field_70170_p.field_73011_w.isDaytime() ? 300 : 100) == 0 && (!(this.living instanceof EntityTameable) || !((EntityTameable)this.living).func_70906_o());
      }
   }

   public boolean func_75253_b() {
      return this.living instanceof EntityTameable && !((EntityTameable)this.living).func_70906_o() || this.living.func_70681_au().nextInt(40) != 0;
   }

   public void func_75249_e() {
   }

   public void func_75246_d() {
      double d0 = this.waypointX - this.living.field_70165_t;
      double d1 = this.waypointY - this.living.field_70163_u;
      double d2 = this.waypointZ - this.living.field_70161_v;
      double d3 = d0 * d0 + d1 * d1 + d2 * d2;
      if (d3 < 1.0D || d3 > 3600.0D) {
         float distance = this.living instanceof EntityTameable && ((EntityTameable)this.living).func_70909_n() ? 2.0F : 6.0F;
         this.waypointX = this.living.field_70165_t + (double)((this.worldObj.field_73012_v.nextFloat() * 8.0F - 4.0F) * distance);
         this.waypointY = this.living.field_70163_u + (double)((this.worldObj.field_73012_v.nextFloat() * 2.0F - 1.0F) * distance);
         this.waypointZ = this.living.field_70161_v + (double)((this.worldObj.field_73012_v.nextFloat() * 8.0F - 4.0F) * distance);
      }

      if (this.courseChangeCooldown-- <= 0) {
         this.courseChangeCooldown += this.worldObj.field_73012_v.nextInt(2) + 2;
         d3 = (double)MathHelper.func_76133_a(d3);
         if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, d3)) {
            EntityLiving var10000 = this.living;
            var10000.field_70159_w += d0 / d3 * 0.1D;
            var10000 = this.living;
            var10000.field_70181_x += d1 / d3 * 0.1D;
            var10000 = this.living;
            var10000.field_70179_y += d2 / d3 * 0.1D;
         } else {
            this.waypointX = this.living.field_70165_t;
            this.waypointY = this.living.field_70163_u;
            this.waypointZ = this.living.field_70161_v;
         }
      }

      this.living.field_70761_aq = this.living.field_70177_z = -((float)Math.atan2(this.living.field_70159_w, this.living.field_70179_y)) * 180.0F / 3.1415927F;
   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.living.field_70165_t) / par7;
      double d5 = (par3 - this.living.field_70163_u) / par7;
      double d6 = (par5 - this.living.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.living.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.living.field_70170_p.func_72945_a(this.living, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
