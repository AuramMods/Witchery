package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityEnt;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelEnt extends ModelBase {
   ModelRenderer ArmLeft;
   ModelRenderer ArmRight;
   ModelRenderer Body;
   ModelRenderer Face;
   ModelRenderer Leg8;
   ModelRenderer Leg6;
   ModelRenderer Leg4;
   ModelRenderer Leg2;
   ModelRenderer Leg7;
   ModelRenderer Leg5;
   ModelRenderer Leg3;
   ModelRenderer Leg1;
   ModelRenderer LeavesBase;
   ModelRenderer LeavesTop;
   ModelRenderer LeavesBaseInner;
   ModelRenderer LeavesTopInner;

   public ModelEnt() {
      this.field_78090_t = 256;
      this.field_78089_u = 256;
      this.ArmLeft = new ModelRenderer(this, 82, 0);
      this.ArmLeft.func_78789_a(0.0F, -22.0F, -3.0F, 6, 24, 6);
      this.ArmLeft.func_78793_a(8.0F, -4.0F, 0.0F);
      this.ArmLeft.func_78787_b(256, 256);
      this.ArmLeft.field_78809_i = true;
      this.setRotation(this.ArmLeft, 0.0F, 0.0F, 0.0F);
      this.ArmRight = new ModelRenderer(this, 82, 0);
      this.ArmRight.func_78789_a(-6.0F, -22.0F, -3.0F, 6, 24, 6);
      this.ArmRight.func_78793_a(-8.0F, -4.0F, 0.0F);
      this.ArmRight.func_78787_b(256, 256);
      this.ArmRight.field_78809_i = true;
      this.setRotation(this.ArmRight, 0.0F, 0.0F, 0.0F);
      this.Body = new ModelRenderer(this, 0, 50);
      this.Body.func_78789_a(-8.0F, -46.0F, -8.0F, 16, 48, 16);
      this.Body.func_78793_a(0.0F, 20.0F, 0.0F);
      this.Body.func_78787_b(256, 256);
      this.Body.field_78809_i = true;
      this.setRotation(this.Body, 0.0F, 0.0F, 0.0F);
      this.Face = new ModelRenderer(this, 0, 116);
      this.Face.func_78789_a(-8.0F, -46.0F, -9.0F, 16, 24, 16);
      this.Face.func_78793_a(0.0F, 20.0F, 0.0F);
      this.Face.func_78787_b(256, 256);
      this.Face.field_78809_i = true;
      this.setRotation(this.Face, 0.0F, 0.0F, 0.0F);
      this.Leg8 = new ModelRenderer(this, 18, 0);
      this.Leg8.func_78789_a(-3.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg8.func_78793_a(4.0F, 20.0F, -1.0F);
      this.Leg8.func_78787_b(256, 256);
      this.Leg8.field_78809_i = true;
      this.setRotation(this.Leg8, 0.0F, 0.5759587F, 0.1919862F);
      this.Leg6 = new ModelRenderer(this, 18, 0);
      this.Leg6.func_78789_a(-3.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg6.func_78793_a(4.0F, 20.0F, 0.0F);
      this.Leg6.func_78787_b(256, 256);
      this.Leg6.field_78809_i = true;
      this.setRotation(this.Leg6, 0.0F, 0.2792527F, 0.1919862F);
      this.Leg4 = new ModelRenderer(this, 18, 0);
      this.Leg4.func_78789_a(-3.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg4.func_78793_a(4.0F, 20.0F, 1.0F);
      this.Leg4.func_78787_b(256, 256);
      this.Leg4.field_78809_i = true;
      this.setRotation(this.Leg4, 0.0F, -0.2792527F, 0.1919862F);
      this.Leg2 = new ModelRenderer(this, 18, 0);
      this.Leg2.func_78789_a(-3.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg2.func_78793_a(4.0F, 20.0F, 2.0F);
      this.Leg2.func_78787_b(256, 256);
      this.Leg2.field_78809_i = true;
      this.setRotation(this.Leg2, 0.0F, -0.5759587F, 0.1919862F);
      this.Leg7 = new ModelRenderer(this, 18, 0);
      this.Leg7.func_78789_a(-13.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg7.func_78793_a(-4.0F, 20.0F, -1.0F);
      this.Leg7.func_78787_b(256, 256);
      this.Leg7.field_78809_i = true;
      this.setRotation(this.Leg7, 0.0F, -0.5759587F, -0.1919862F);
      this.Leg5 = new ModelRenderer(this, 18, 0);
      this.Leg5.func_78789_a(-13.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg5.func_78793_a(-4.0F, 20.0F, 0.0F);
      this.Leg5.func_78787_b(256, 256);
      this.Leg5.field_78809_i = true;
      this.setRotation(this.Leg5, 0.0F, -0.2792527F, -0.1919862F);
      this.Leg3 = new ModelRenderer(this, 18, 0);
      this.Leg3.func_78789_a(-13.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg3.func_78793_a(-4.0F, 20.0F, 1.0F);
      this.Leg3.func_78787_b(256, 256);
      this.Leg3.field_78809_i = true;
      this.setRotation(this.Leg3, 0.0F, 0.2792527F, -0.1919862F);
      this.Leg1 = new ModelRenderer(this, 18, 0);
      this.Leg1.func_78789_a(-13.0F, -1.0F, -1.0F, 16, 2, 2);
      this.Leg1.func_78793_a(-4.0F, 20.0F, 2.0F);
      this.Leg1.func_78787_b(256, 256);
      this.Leg1.field_78809_i = true;
      this.setRotation(this.Leg1, 0.0F, 0.5759587F, -0.1919862F);
      this.LeavesBase = new ModelRenderer(this, 0, 180);
      this.LeavesBase.func_78789_a(0.0F, 0.0F, 0.0F, 60, 16, 60);
      this.LeavesBase.func_78793_a(-30.0F, -42.0F, -30.0F);
      this.LeavesBase.func_78787_b(256, 256);
      this.LeavesBase.field_78809_i = true;
      this.setRotation(this.LeavesBase, 0.0F, 0.0F, 0.0F);
      this.LeavesTop = new ModelRenderer(this, 56, 130);
      this.LeavesTop.func_78789_a(0.0F, 0.0F, 0.0F, 32, 16, 32);
      this.LeavesTop.func_78793_a(-16.0F, -58.0F, -16.0F);
      this.LeavesTop.func_78787_b(256, 256);
      this.LeavesTop.field_78809_i = true;
      this.setRotation(this.LeavesTop, 0.0F, 0.0F, 0.0F);
      this.LeavesBaseInner = new ModelRenderer(this, 24, 59);
      this.LeavesBaseInner.func_78789_a(0.0F, 0.0F, 0.0F, 56, 14, 56);
      this.LeavesBaseInner.func_78793_a(-28.0F, -41.0F, -28.0F);
      this.LeavesBaseInner.func_78787_b(64, 32);
      this.LeavesBaseInner.field_78809_i = true;
      this.setRotation(this.LeavesBaseInner, 0.0F, 0.0F, 0.0F);
      this.LeavesTopInner = new ModelRenderer(this, 108, 14);
      this.LeavesTopInner.func_78789_a(0.0F, 0.0F, 0.0F, 28, 14, 28);
      this.LeavesTopInner.func_78793_a(-14.0F, -57.0F, -14.0F);
      this.LeavesTopInner.func_78787_b(64, 32);
      this.LeavesTopInner.field_78809_i = true;
      this.setRotation(this.LeavesTopInner, 0.0F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.ArmLeft.func_78785_a(f5);
      this.ArmRight.func_78785_a(f5);
      this.Body.func_78785_a(f5);
      this.Leg8.func_78785_a(f5);
      this.Leg6.func_78785_a(f5);
      this.Leg4.func_78785_a(f5);
      this.Leg2.func_78785_a(f5);
      this.Leg7.func_78785_a(f5);
      this.Leg5.func_78785_a(f5);
      this.Leg3.func_78785_a(f5);
      this.Leg1.func_78785_a(f5);
      this.LeavesBaseInner.func_78785_a(f5);
      this.LeavesTopInner.func_78785_a(f5);
      this.LeavesBase.func_78785_a(f5);
      this.LeavesTop.func_78785_a(f5);
      if (entity != null && entity instanceof EntityEnt && ((EntityEnt)entity).isScreaming()) {
         this.Face.func_78785_a(f5);
      }

   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78086_a(EntityLivingBase par1EntityLiving, float par2, float par3, float par4) {
      EntityEnt entity = (EntityEnt)par1EntityLiving;
      int i = entity.getAttackTimer();
      if (i > 0) {
         this.ArmRight.field_78795_f = 3.0F - 1.3F * this.func_78172_a((float)i - par4, 10.0F);
         this.ArmLeft.field_78795_f = 2.5F - 1.2F * this.func_78172_a((float)i - par4, 10.0F);
      } else {
         this.ArmRight.field_78795_f = 0.0F;
         this.ArmLeft.field_78795_f = 0.0F;
         this.ArmRight.field_78808_h = (-0.2F + 0.1F * this.func_78172_a(par2, 13.0F)) * par3 - 0.1F;
         this.ArmLeft.field_78808_h = (0.2F - 0.1F * this.func_78172_a(par2, 13.0F)) * par3 + 0.1F;
      }

   }

   private float func_78172_a(float par1, float par2) {
      return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
   }

   public void func_78087_a(float par1, float par2, float f2, float f3, float f4, float f5, Entity entity) {
      super.func_78087_a(par1, par2, f2, f3, f4, f5, entity);
      float f6 = 0.7853982F;
      this.Leg1.field_78808_h = -f6;
      this.Leg2.field_78808_h = f6;
      this.Leg3.field_78808_h = -f6 * 0.74F;
      this.Leg4.field_78808_h = f6 * 0.74F;
      this.Leg5.field_78808_h = -f6 * 0.74F;
      this.Leg6.field_78808_h = f6 * 0.74F;
      this.Leg7.field_78808_h = -f6;
      this.Leg8.field_78808_h = f6;
      float f7 = -0.0F;
      float f8 = 0.3926991F;
      this.Leg1.field_78796_g = f8 * 2.0F + f7;
      this.Leg2.field_78796_g = -f8 * 2.0F - f7;
      this.Leg3.field_78796_g = f8 * 1.0F + f7;
      this.Leg4.field_78796_g = -f8 * 1.0F - f7;
      this.Leg5.field_78796_g = -f8 * 1.0F + f7;
      this.Leg6.field_78796_g = f8 * 1.0F - f7;
      this.Leg7.field_78796_g = -f8 * 2.0F + f7;
      this.Leg8.field_78796_g = f8 * 2.0F - f7;
      float f9 = -(MathHelper.func_76134_b(par1 * 0.6662F * 2.0F + 0.0F) * 0.4F) * par2;
      float f10 = -(MathHelper.func_76134_b(par1 * 0.6662F * 2.0F + 3.1415927F) * 0.4F) * par2;
      float f11 = -(MathHelper.func_76134_b(par1 * 0.6662F * 2.0F + 1.5707964F) * 0.4F) * par2;
      float f12 = -(MathHelper.func_76134_b(par1 * 0.6662F * 2.0F + 4.712389F) * 0.4F) * par2;
      float f13 = Math.abs(MathHelper.func_76126_a(par1 * 0.6662F + 0.0F) * 0.4F) * par2;
      float f14 = Math.abs(MathHelper.func_76126_a(par1 * 0.6662F + 3.1415927F) * 0.4F) * par2;
      float f15 = Math.abs(MathHelper.func_76126_a(par1 * 0.6662F + 1.5707964F) * 0.4F) * par2;
      float f16 = Math.abs(MathHelper.func_76126_a(par1 * 0.6662F + 4.712389F) * 0.4F) * par2;
      ModelRenderer var10000 = this.Leg1;
      var10000.field_78796_g += f9;
      var10000 = this.Leg2;
      var10000.field_78796_g += -f9;
      var10000 = this.Leg3;
      var10000.field_78796_g += f10;
      var10000 = this.Leg4;
      var10000.field_78796_g += -f10;
      var10000 = this.Leg5;
      var10000.field_78796_g += f11;
      var10000 = this.Leg6;
      var10000.field_78796_g += -f11;
      var10000 = this.Leg7;
      var10000.field_78796_g += f12;
      var10000 = this.Leg8;
      var10000.field_78796_g += -f12;
      var10000 = this.Leg1;
      var10000.field_78808_h += f13;
      var10000 = this.Leg2;
      var10000.field_78808_h += -f13;
      var10000 = this.Leg3;
      var10000.field_78808_h += f14;
      var10000 = this.Leg4;
      var10000.field_78808_h += -f14;
      var10000 = this.Leg5;
      var10000.field_78808_h += f15;
      var10000 = this.Leg6;
      var10000.field_78808_h += -f15;
      var10000 = this.Leg7;
      var10000.field_78808_h += f16;
      var10000 = this.Leg8;
      var10000.field_78808_h += -f16;
   }
}
