package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.item.ItemGeneral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityAIFlyerFollowOwner extends EntityAIBase {
   private EntityTameable thePet;
   private EntityLivingBase theOwner;
   World theWorld;
   private double field_75336_f;
   private int field_75343_h;
   float maxDist;
   float minDist;
   private boolean field_75344_i;

   public EntityAIFlyerFollowOwner(EntityTameable par1EntityTameable, double par2, float par4, float par5) {
      this.thePet = par1EntityTameable;
      this.theWorld = par1EntityTameable.field_70170_p;
      this.field_75336_f = par2;
      this.minDist = par4;
      this.maxDist = par5;
      this.func_75248_a(1);
   }

   public boolean func_75250_a() {
      EntityLivingBase entitylivingbase = this.thePet.func_70902_q();
      if (entitylivingbase == null && Familiar.couldBeFamiliar(this.thePet)) {
         entitylivingbase = Familiar.getOwnerForFamiliar(this.thePet).getCurrentOwner();
      }

      if (entitylivingbase == null) {
         return false;
      } else if (this.thePet.func_70906_o()) {
         return false;
      } else if (this.thePet.field_71093_bK == ((EntityLivingBase)entitylivingbase).field_71093_bK && !(this.thePet.func_70068_e((Entity)entitylivingbase) < (double)(this.minDist * this.minDist))) {
         this.theOwner = (EntityLivingBase)entitylivingbase;
         return true;
      } else {
         return false;
      }
   }

   public boolean func_75253_b() {
      return this.thePet.func_70068_e(this.theOwner) > (double)(this.maxDist * this.maxDist) && !this.thePet.func_70906_o();
   }

   public void func_75249_e() {
      this.field_75343_h = 0;
   }

   public void func_75251_c() {
      this.theOwner = null;
   }

   public void func_75246_d() {
      if (!this.thePet.func_70906_o() && --this.field_75343_h <= 0) {
         this.field_75343_h = 10;
         if (this.thePet.field_71093_bK == this.theOwner.field_71093_bK && !(this.thePet.func_70068_e(this.theOwner) >= 256.0D)) {
            double d0 = this.theOwner.field_70165_t - this.thePet.field_70165_t;
            double d1 = this.theOwner.field_70163_u - this.thePet.field_70163_u;
            double d2 = this.theOwner.field_70161_v - this.thePet.field_70161_v;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            d3 = (double)MathHelper.func_76133_a(d3);
            EntityTameable var20;
            if (this.isCourseTraversable(this.theOwner.field_70165_t, this.theOwner.field_70163_u, this.theOwner.field_70161_v, d3)) {
               var20 = this.thePet;
               var20.field_70159_w += d0 / d3 * 0.1D;
               if (this.thePet.field_70163_u < this.theOwner.field_70163_u + 2.0D) {
                  var20 = this.thePet;
                  var20.field_70181_x += d1 / d3 * 0.1D + 0.1D;
               } else {
                  var20 = this.thePet;
                  var20.field_70181_x += d1 / d3 * 0.1D;
               }

               var20 = this.thePet;
               var20.field_70179_y += d2 / d3 * 0.1D;
            } else {
               double newX = this.thePet.field_70165_t + (double)((this.thePet.field_70170_p.field_73012_v.nextFloat() * 8.0F - 4.0F) * 6.0F);
               double newY = this.thePet.field_70163_u + (double)((this.thePet.field_70170_p.field_73012_v.nextFloat() * 2.0F - 1.0F) * 6.0F);
               double newZ = this.thePet.field_70161_v + (double)((this.thePet.field_70170_p.field_73012_v.nextFloat() * 8.0F - 4.0F) * 6.0F);
               d0 = newX - this.thePet.field_70165_t;
               d1 = newY - this.thePet.field_70163_u;
               d2 = newZ - this.thePet.field_70161_v;
               d3 = d0 * d0 + d1 * d1 + d2 * d2;
               d3 = (double)MathHelper.func_76133_a(d3);
               var20 = this.thePet;
               var20.field_70159_w += d0 / d3 * 0.1D;
               var20 = this.thePet;
               var20.field_70181_x += d1 / d3 * 0.1D + 0.1D;
               var20 = this.thePet;
               var20.field_70179_y += d2 / d3 * 0.1D;
            }
         } else {
            int x = MathHelper.func_76128_c(this.theOwner.field_70165_t) - 2;
            int z = MathHelper.func_76128_c(this.theOwner.field_70161_v) - 2;
            int y = MathHelper.func_76128_c(this.theOwner.field_70121_D.field_72338_b) - 2;

            for(int dx = 0; dx <= 4; ++dx) {
               for(int dz = 0; dz <= 4; ++dz) {
                  for(int dy = 0; dy <= 4; ++dy) {
                     int newX = x + dz;
                     int newY = y + dy;
                     int newZ = z + dz;
                     if (this.theOwner.field_70170_p.func_147439_a(newX, newY - 1, newZ).isSideSolid(this.theOwner.field_70170_p, newX, newY - 1, newZ, ForgeDirection.UP) && !this.theOwner.field_70170_p.func_147439_a(newX, newY, newZ).func_149721_r() && !this.theOwner.field_70170_p.func_147439_a(newX, newY + 1, newZ).func_149721_r()) {
                        ItemGeneral var10000 = Witchery.Items.GENERIC;
                        ItemGeneral.teleportToLocation(this.theWorld, 0.5D + (double)newX, 0.01D + (double)newY, 0.5D + (double)newZ, this.theOwner.field_71093_bK, this.thePet, true);
                        return;
                     }
                  }
               }
            }
         }

         this.thePet.field_70761_aq = this.thePet.field_70177_z = -((float)Math.atan2(this.thePet.field_70159_w, this.thePet.field_70179_y)) * 180.0F / 3.1415927F;
      }

   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.thePet.field_70165_t) / par7;
      double d5 = (par3 - this.thePet.field_70163_u) / par7;
      double d6 = (par5 - this.thePet.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.thePet.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.thePet.field_70170_p.func_72945_a(this.thePet, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
