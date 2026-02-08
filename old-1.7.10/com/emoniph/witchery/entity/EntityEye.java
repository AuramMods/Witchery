package com.emoniph.witchery.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityEye extends EntityLiving {
   public EntityEye(World world) {
      super(world);
      this.func_70105_a(0.0F, 0.0F);
      this.func_70661_as().func_75495_e(true);
      this.field_70178_ae = true;
      this.func_82142_c(true);
      this.field_70145_X = true;
   }

   protected void func_70069_a(float par1) {
   }

   protected void func_70064_a(double par1, boolean par3) {
   }

   public boolean func_70617_f_() {
      return false;
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   protected boolean func_70085_c(EntityPlayer par1EntityPlayer) {
      return true;
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      return true;
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.eye.name");
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected void func_70088_a() {
      super.func_70088_a();
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
   }

   public void func_70636_d() {
      this.field_70181_x = 0.5D;
      super.func_70636_d();
      if (this.field_70173_aa > 200) {
         this.func_70106_y();
      }

   }

   public void func_70612_e(float par1, float par2) {
      if (this.func_70090_H()) {
         this.func_70060_a(par1, par2, 0.02F);
         this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
         this.field_70159_w *= 0.800000011920929D;
         this.field_70181_x *= 0.800000011920929D;
         this.field_70179_y *= 0.800000011920929D;
      } else if (this.func_70058_J()) {
         this.func_70060_a(par1, par2, 0.02F);
         this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
         this.field_70159_w *= 0.5D;
         this.field_70181_x *= 0.5D;
         this.field_70179_y *= 0.5D;
      } else {
         float f2 = 0.91F;
         if (this.field_70122_E) {
            f2 = 0.54600006F;
            Block i = this.field_70170_p.func_147439_a(MathHelper.func_76128_c(this.field_70165_t), MathHelper.func_76128_c(this.field_70121_D.field_72338_b) - 1, MathHelper.func_76128_c(this.field_70161_v));
            if (i != Blocks.field_150350_a) {
               f2 = i.field_149765_K * 0.91F;
            }
         }

         float f3 = 0.16277136F / (f2 * f2 * f2);
         this.func_70060_a(par1, par2, this.field_70122_E ? 0.1F * f3 : 0.02F);
         f2 = 0.91F;
         if (this.field_70122_E) {
            f2 = 0.54600006F;
            Block j = this.field_70170_p.func_147439_a(MathHelper.func_76128_c(this.field_70165_t), MathHelper.func_76128_c(this.field_70121_D.field_72338_b) - 1, MathHelper.func_76128_c(this.field_70161_v));
            if (j != Blocks.field_150350_a) {
               f2 = j.field_149765_K * 0.91F;
            }
         }

         this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
         this.field_70159_w *= (double)f2;
         this.field_70181_x *= (double)f2;
         this.field_70179_y *= (double)f2;
      }

      this.field_70722_aY = this.field_70721_aZ;
      double d0 = this.field_70165_t - this.field_70169_q;
      double d1 = this.field_70161_v - this.field_70166_s;
      float f4 = MathHelper.func_76133_a(d0 * d0 + d1 * d1) * 4.0F;
      if (f4 > 1.0F) {
         f4 = 1.0F;
      }

      this.field_70721_aZ += (f4 - this.field_70721_aZ) * 0.4F;
      this.field_70754_ba += this.field_70721_aZ;
   }
}
