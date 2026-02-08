package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityReflection;
import com.emoniph.witchery.entity.EntityWolfman;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelWolfman extends ModelBase {
   public ModelRenderer headMain;
   public ModelRenderer bodyUpper;
   public ModelRenderer legRightUpper;
   public ModelRenderer legLeftUpper;
   public ModelRenderer armLeft;
   public ModelRenderer armRight;
   public ModelRenderer tail;
   public ModelRenderer bodyLower;
   public ModelRenderer legRightLower;
   public ModelRenderer legLeftLower;
   public int heldItemLeft;
   public int heldItemRight;
   public boolean isSneak;
   public boolean aimedBow;

   public ModelWolfman() {
      this(0.0F);
   }

   public ModelWolfman(float scale) {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      float headScale = 0.05F;
      this.headMain = new ModelRenderer(this, 0, 0);
      this.headMain.func_78790_a(-3.0F, -6.0F, -2.0F, 6, 6, 4, 0.05F);
      this.headMain.func_78793_a(0.0F, 0.0F, -2.0F);
      float f = 0.0F;
      this.headMain.func_78784_a(16, 14).func_78790_a(-3.0F, -8.0F, 1.0F, 2, 2, 1, 0.0F);
      this.headMain.func_78784_a(16, 14).func_78790_a(1.0F, -8.0F, 1.0F, 2, 2, 1, 0.0F);
      this.headMain.func_78784_a(0, 10).func_78790_a(-1.5F, -3.1F, -5.0F, 3, 3, 4, 0.0F);
      this.bodyUpper = new ModelRenderer(this, 0, 35);
      this.bodyUpper.func_78793_a(0.0F, -0.1F, -2.0F);
      this.bodyUpper.func_78790_a(-5.0F, 0.0F, -3.9F, 10, 7, 8, scale);
      this.setRotateAngle(this.bodyUpper, 0.4098033F, 0.0F, 0.0F);
      this.bodyLower = new ModelRenderer(this, 3, 50);
      this.bodyLower.func_78793_a(0.0F, 5.0F, -1.5F);
      this.bodyLower.func_78790_a(-4.0F, 2.0F, -2.3F, 8, 7, 5, scale);
      this.bodyUpper.func_78792_a(this.bodyLower);
      this.tail = new ModelRenderer(this, 55, 52);
      this.tail.func_78793_a(0.0F, 11.9F, 3.6F);
      this.tail.func_78790_a(-1.0F, 0.0F, -1.0F, 2, 10, 2, scale);
      this.setRotateAngle(this.tail, 0.59184116F, 0.0F, 0.0F);
      this.legLeftUpper = new ModelRenderer(this, 38, 0);
      this.legLeftUpper.field_78809_i = true;
      this.legLeftUpper.func_78793_a(2.0F, 12.0F, 0.0F);
      this.legLeftUpper.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 7, 4, scale);
      this.setRotateAngle(this.legLeftUpper, -0.4098033F, 0.0F, 0.0F);
      this.legLeftLower = new ModelRenderer(this, 38, 13);
      this.legLeftLower.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legLeftLower.func_78790_a(-2.0F, 3.5F, 2.0F, 4, 8, 4, scale);
      this.legLeftUpper.func_78792_a(this.legLeftLower);
      this.legRightUpper = new ModelRenderer(this, 38, 0);
      this.legRightUpper.func_78793_a(-2.0F, 12.0F, 0.0F);
      this.legRightUpper.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 7, 4, scale);
      this.setRotateAngle(this.legRightUpper, -0.4098033F, 0.0F, 0.0F);
      this.legRightLower = new ModelRenderer(this, 38, 13);
      this.legRightLower.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legRightLower.func_78790_a(-2.0F, 3.5F, 2.0F, 4, 8, 4, scale);
      this.legRightUpper.func_78792_a(this.legRightLower);
      this.armLeft = new ModelRenderer(this, 38, 46);
      this.armLeft.field_78809_i = true;
      this.armLeft.func_78793_a(6.0F, 2.0F, 0.0F);
      this.armLeft.func_78790_a(-1.0F, -2.0F, -2.0F, 4, 14, 4, scale);
      this.armRight = new ModelRenderer(this, 38, 46);
      this.armRight.func_78793_a(-5.8F, 2.0F, 0.0F);
      this.armRight.func_78790_a(-3.0F, -2.0F, -2.0F, 4, 14, 4, scale);
   }

   public void func_78088_a(Entity entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
      this.func_78087_a(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, entity);
      this.headMain.func_78785_a(p_78088_7_);
      this.bodyUpper.func_78785_a(p_78088_7_);
      this.armRight.func_78785_a(p_78088_7_);
      this.legLeftUpper.func_78785_a(p_78088_7_);
      this.tail.func_78785_a(p_78088_7_);
      this.armLeft.func_78785_a(p_78088_7_);
      this.legRightUpper.func_78785_a(p_78088_7_);
   }

   public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void func_78086_a(EntityLivingBase entity, float par2, float par3, float par4) {
      float i = 0.0F;
      if (entity instanceof EntityWolfman) {
         EntityWolfman wolfman = (EntityWolfman)entity;
         i = (float)wolfman.getAttackTimer();
         this.field_78093_q = wolfman.isSitting();
      } else if (entity instanceof EntityReflection) {
         EntityReflection wolfman = (EntityReflection)entity;
         i = (float)wolfman.getAttackTimer();
      }

      if (i > 0.0F) {
         this.armRight.field_78795_f = -2.0F + 1.5F * this.interpolateRotation(i - par4, 10.0F);
         this.armLeft.field_78795_f = -1.0F + 0.9F * this.interpolateRotation(i - par4, 10.0F);
      } else {
         this.armRight.field_78795_f = MathHelper.func_76134_b(par2 * 0.6662F + 3.1415927F) * 2.0F * par3 * 0.5F;
         this.armLeft.field_78795_f = MathHelper.func_76134_b(par2 * 0.6662F) * 2.0F * par3 * 0.5F;
      }

   }

   private float interpolateRotation(float par1, float par2) {
      return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
   }

   public void func_78087_a(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
      this.headMain.field_78796_g = p_78087_4_ / 57.295776F;
      this.headMain.field_78795_f = p_78087_5_ / 57.295776F;
      this.armRight.field_78808_h = 0.0F;
      this.armLeft.field_78808_h = 0.0F;
      this.legRightUpper.field_78795_f = Math.max(-0.4098033F + MathHelper.func_76134_b(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_, -0.8F);
      this.legLeftUpper.field_78795_f = Math.max(-0.4098033F + MathHelper.func_76134_b(p_78087_1_ * 0.6662F + 3.1415927F) * 1.4F * p_78087_2_, -0.8F);
      this.legRightUpper.field_78796_g = 0.0F;
      this.legLeftUpper.field_78796_g = 0.0F;
      ModelRenderer var10000;
      if (this.field_78093_q) {
         var10000 = this.armRight;
         var10000.field_78795_f += -0.62831855F;
         var10000 = this.armLeft;
         var10000.field_78795_f += -0.62831855F;
         this.legRightUpper.field_78795_f = -1.2566371F;
         this.legLeftUpper.field_78795_f = -1.2566371F;
         this.legRightUpper.field_78796_g = 0.31415927F;
         this.legLeftUpper.field_78796_g = -0.31415927F;
      }

      if (this.heldItemLeft != 0) {
         this.armLeft.field_78795_f = this.armLeft.field_78795_f * 0.5F - 0.31415927F * (float)this.heldItemLeft;
      }

      if (this.heldItemRight != 0) {
         this.armRight.field_78795_f = this.armRight.field_78795_f * 0.5F - 0.31415927F * (float)this.heldItemRight;
      }

      this.armRight.field_78796_g = 0.0F;
      this.armLeft.field_78796_g = 0.0F;
      float f6;
      float f7;
      if (this.field_78095_p > -9990.0F) {
         f6 = this.field_78095_p;
         this.bodyUpper.field_78796_g = MathHelper.func_76126_a(MathHelper.func_76129_c(f6) * 3.1415927F * 2.0F) * 0.2F;
         this.armRight.field_78798_e = MathHelper.func_76126_a(this.bodyUpper.field_78796_g) * 5.0F;
         this.armRight.field_78800_c = -MathHelper.func_76134_b(this.bodyUpper.field_78796_g) * 5.0F;
         this.armLeft.field_78798_e = -MathHelper.func_76126_a(this.bodyUpper.field_78796_g) * 5.0F;
         this.armLeft.field_78800_c = MathHelper.func_76134_b(this.bodyUpper.field_78796_g) * 5.0F;
         var10000 = this.armRight;
         var10000.field_78796_g += this.bodyUpper.field_78796_g;
         var10000 = this.armLeft;
         var10000.field_78796_g += this.bodyUpper.field_78796_g;
         var10000 = this.armLeft;
         var10000.field_78795_f += this.bodyUpper.field_78796_g;
         f6 = 1.0F - this.field_78095_p;
         f6 *= f6;
         f6 *= f6;
         f6 = 1.0F - f6;
         f7 = MathHelper.func_76126_a(f6 * 3.1415927F);
         float f8 = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -(this.headMain.field_78795_f - 0.7F) * 0.75F;
         this.armRight.field_78795_f = (float)((double)this.armRight.field_78795_f - ((double)f7 * 1.2D + (double)f8));
         var10000 = this.armRight;
         var10000.field_78796_g += this.bodyUpper.field_78796_g * 2.0F;
         this.armRight.field_78808_h = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -0.4F;
      }

      if (this.isSneak) {
         this.bodyUpper.field_78795_f = 0.5F;
         var10000 = this.armRight;
         var10000.field_78795_f += 0.4F;
         var10000 = this.armLeft;
         var10000.field_78795_f += 0.4F;
         this.legRightUpper.field_78798_e = 4.0F;
         this.legLeftUpper.field_78798_e = 4.0F;
         this.legRightUpper.field_78797_d = 9.0F;
         this.legLeftUpper.field_78797_d = 9.0F;
         this.headMain.field_78797_d = 0.0F;
      } else {
         this.setRotateAngle(this.bodyUpper, 0.4098033F, 0.0F, 0.0F);
         this.legRightUpper.field_78798_e = 0.1F;
         this.legLeftUpper.field_78798_e = 0.1F;
         this.legRightUpper.field_78797_d = 12.0F;
         this.legLeftUpper.field_78797_d = 12.0F;
         this.headMain.field_78797_d = 0.0F;
      }

      this.setRotateAngle(this.tail, 0.59184116F, 0.0F, 0.0F);
      if ((double)p_78087_2_ > 0.1D) {
         var10000 = this.tail;
         var10000.field_78795_f = (float)((double)var10000.field_78795_f + ((double)p_78087_2_ - 0.1D));
         var10000 = this.tail;
         var10000.field_78808_h += 5.0F * MathHelper.func_76134_b(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
      } else {
         var10000 = this.tail;
         var10000.field_78808_h += 3.0F * MathHelper.func_76134_b(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
      }

      var10000 = this.armRight;
      var10000.field_78808_h += MathHelper.func_76134_b(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
      var10000 = this.armLeft;
      var10000.field_78808_h -= MathHelper.func_76134_b(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
      var10000 = this.armRight;
      var10000.field_78795_f += MathHelper.func_76126_a(p_78087_3_ * 0.067F) * 0.05F;
      var10000 = this.armLeft;
      var10000.field_78795_f -= MathHelper.func_76126_a(p_78087_3_ * 0.067F) * 0.05F;
      if (this.aimedBow) {
         f6 = 0.0F;
         f7 = 0.0F;
         this.armRight.field_78808_h = 0.0F;
         this.armLeft.field_78808_h = 0.0F;
         this.armRight.field_78796_g = -(0.1F - f6 * 0.6F) + this.headMain.field_78796_g;
         this.armLeft.field_78796_g = 0.1F - f6 * 0.6F + this.headMain.field_78796_g + 0.4F;
         this.armRight.field_78795_f = -1.5707964F + this.headMain.field_78795_f;
         this.armLeft.field_78795_f = -1.5707964F + this.headMain.field_78795_f;
         var10000 = this.armRight;
         var10000.field_78795_f -= f6 * 1.2F - f7 * 0.4F;
         var10000 = this.armLeft;
         var10000.field_78795_f -= f6 * 1.2F - f7 * 0.4F;
         var10000 = this.armRight;
         var10000.field_78808_h += MathHelper.func_76134_b(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
         var10000 = this.armLeft;
         var10000.field_78808_h -= MathHelper.func_76134_b(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
         var10000 = this.armRight;
         var10000.field_78795_f += MathHelper.func_76126_a(p_78087_3_ * 0.067F) * 0.05F;
         var10000 = this.armLeft;
         var10000.field_78795_f -= MathHelper.func_76126_a(p_78087_3_ * 0.067F) * 0.05F;
      }

   }
}
