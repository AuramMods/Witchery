package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityLeonard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelLeonard extends ModelBase {
   private final ModelRenderer head;
   private final ModelRenderer snout;
   private final ModelRenderer beard;
   private final ModelRenderer earLeft;
   private final ModelRenderer earRight;
   private final ModelRenderer hornLeft;
   private final ModelRenderer hornMiddle;
   private final ModelRenderer hornRight;
   private final ModelRenderer neck;
   private final ModelRenderer body;
   private final ModelRenderer gownLowerRight;
   private final ModelRenderer rightarm;
   private final ModelRenderer leftarm;
   private final ModelRenderer rightleg;
   private final ModelRenderer leftleg;
   private final ModelRenderer gownLowerLeft;

   public ModelLeonard() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.neck = new ModelRenderer(this, 48, 0);
      this.neck.func_78789_a(-2.0F, -1.0F, -2.0F, 4, 2, 4);
      this.neck.func_78793_a(0.0F, 0.0F, 0.0F);
      this.neck.func_78787_b(64, 64);
      this.neck.field_78809_i = true;
      this.setRotation(this.neck, 0.1745329F, 0.0F, 0.0F);
      this.head = new ModelRenderer(this, 0, 0);
      this.head.func_78789_a(-3.0F, -5.0F, -1.0F, 6, 4, 4);
      this.head.func_78793_a(0.0F, 0.0F, 0.0F);
      this.head.func_78787_b(64, 64);
      this.head.field_78809_i = true;
      this.neck.func_78792_a(this.head);
      this.setRotation(this.head, 0.1745329F, 0.0F, 0.0F);
      this.snout = new ModelRenderer(this, 16, 2);
      this.snout.func_78789_a(-2.0F, -5.0F, -7.0F, 4, 4, 7);
      this.snout.func_78793_a(0.0F, 0.0F, 0.0F);
      this.snout.func_78787_b(64, 64);
      this.snout.field_78809_i = true;
      this.setRotation(this.snout, 0.1745329F, 0.0F, 0.0F);
      this.head.func_78792_a(this.snout);
      this.beard = new ModelRenderer(this, 0, 10);
      this.beard.func_78789_a(-2.0F, -0.2F, -7.0F, 4, 2, 2);
      this.beard.func_78793_a(0.0F, 0.0F, 0.0F);
      this.beard.func_78787_b(64, 64);
      this.beard.field_78809_i = true;
      this.setRotation(this.beard, -0.0113601F, 0.0F, 0.0F);
      this.head.func_78792_a(this.beard);
      this.earLeft = new ModelRenderer(this, 38, 0);
      this.earLeft.func_78789_a(3.5F, 1.0F, -0.5F, 1, 3, 1);
      this.earLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.earLeft.func_78787_b(64, 64);
      this.earLeft.field_78809_i = true;
      this.setRotation(this.earLeft, -0.5129616F, -0.2617994F, -1.180008F);
      this.head.func_78792_a(this.earLeft);
      this.earRight = new ModelRenderer(this, 38, 0);
      this.earRight.func_78789_a(-4.5F, 1.0F, 0.5F, 1, 3, 1);
      this.earRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.earRight.func_78787_b(64, 64);
      this.earRight.field_78809_i = true;
      this.setRotation(this.earRight, -0.3346075F, 0.0371786F, 1.226894F);
      this.head.func_78792_a(this.earRight);
      this.hornLeft = new ModelRenderer(this, 43, 0);
      this.hornLeft.func_78789_a(-0.5F, -12.0F, -0.5F, 1, 8, 1);
      this.hornLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.hornLeft.func_78787_b(64, 64);
      this.hornLeft.field_78809_i = true;
      this.setRotation(this.hornLeft, -0.2268928F, 0.0F, 0.3665191F);
      this.head.func_78792_a(this.hornLeft);
      this.hornMiddle = new ModelRenderer(this, 43, 0);
      this.hornMiddle.func_78789_a(-0.5F, -10.0F, -0.5F, 1, 6, 1);
      this.hornMiddle.func_78793_a(0.0F, 0.0F, 0.0F);
      this.hornMiddle.func_78787_b(64, 64);
      this.hornMiddle.field_78809_i = true;
      this.setRotation(this.hornMiddle, -0.2974289F, 0.0F, 0.0F);
      this.head.func_78792_a(this.hornMiddle);
      this.hornRight = new ModelRenderer(this, 43, 0);
      this.hornRight.func_78789_a(-0.5F, -12.0F, -0.5F, 1, 8, 1);
      this.hornRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.hornRight.func_78787_b(64, 64);
      this.hornRight.field_78809_i = true;
      this.setRotation(this.hornRight, -0.2268928F, 0.0F, -0.3665191F);
      this.head.func_78792_a(this.hornRight);
      this.body = new ModelRenderer(this, 16, 16);
      this.body.func_78789_a(-4.0F, 0.0F, -2.0F, 8, 12, 4);
      this.body.func_78793_a(0.0F, 0.0F, 0.0F);
      this.body.func_78787_b(64, 64);
      this.body.field_78809_i = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.gownLowerRight = new ModelRenderer(this, 0, 33);
      this.gownLowerRight.func_78789_a(-5.0F, 12.0F, -2.5F, 5, 11, 5);
      this.gownLowerRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.gownLowerRight.func_78787_b(64, 64);
      this.gownLowerRight.field_78809_i = true;
      this.setRotation(this.gownLowerRight, 0.0F, 0.0F, 0.0F);
      this.rightarm = new ModelRenderer(this, 40, 16);
      this.rightarm.func_78789_a(-3.0F, -2.0F, -2.0F, 4, 12, 4);
      this.rightarm.func_78793_a(-5.0F, 2.0F, 0.0F);
      this.rightarm.func_78787_b(64, 64);
      this.rightarm.field_78809_i = true;
      this.setRotation(this.rightarm, 0.0F, 0.0F, 0.0F);
      this.leftarm = new ModelRenderer(this, 40, 16);
      this.leftarm.func_78789_a(-1.0F, -2.0F, -2.0F, 4, 12, 4);
      this.leftarm.func_78793_a(5.0F, 2.0F, 0.0F);
      this.leftarm.func_78787_b(64, 64);
      this.leftarm.field_78809_i = true;
      this.setRotation(this.leftarm, 0.0F, 0.0F, 0.0F);
      this.rightleg = new ModelRenderer(this, 0, 16);
      this.rightleg.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 12, 4);
      this.rightleg.func_78793_a(-2.0F, 12.0F, 0.0F);
      this.rightleg.func_78787_b(64, 64);
      this.rightleg.field_78809_i = true;
      this.setRotation(this.rightleg, 0.0F, 0.0F, 0.0F);
      this.rightleg.func_78792_a(this.gownLowerRight);
      this.leftleg = new ModelRenderer(this, 0, 16);
      this.leftleg.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 12, 4);
      this.leftleg.func_78793_a(2.0F, 12.0F, 0.0F);
      this.leftleg.func_78787_b(64, 64);
      this.leftleg.field_78809_i = true;
      this.setRotation(this.leftleg, 0.0F, 0.0F, 0.0F);
      this.gownLowerLeft = new ModelRenderer(this, 21, 33);
      this.gownLowerLeft.func_78789_a(0.0F, 12.0F, -2.5F, 5, 11, 5);
      this.gownLowerLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.gownLowerLeft.func_78787_b(64, 64);
      this.gownLowerLeft.field_78809_i = true;
      this.setRotation(this.gownLowerLeft, 0.0F, 0.0F, 0.0F);
      this.leftleg.func_78792_a(this.gownLowerLeft);
      this.neck.field_78795_f = 0.1745329F;
      this.head.field_78795_f = 0.1745329F;
      this.setRotation(this.earRight, -0.3346075F, 0.0371786F, 1.226894F);
      this.gownLowerLeft.func_78793_a(-2.0F, -12.0F, 0.0F);
      this.gownLowerRight.func_78793_a(2.0F, -12.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.neck.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.rightarm.func_78785_a(f5);
      this.leftarm.func_78785_a(f5);
      this.rightleg.func_78785_a(f5);
      this.leftleg.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
      this.neck.field_78796_g = par4 / 57.295776F;
      this.neck.field_78795_f = par5 / 57.295776F;
      this.rightarm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 2.0F * par2 * 0.5F;
      this.leftarm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
      this.rightarm.field_78808_h = 0.0F;
      this.leftarm.field_78808_h = 0.0F;
      this.rightleg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 1.4F * par2;
      this.leftleg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 1.4F * par2;
      this.rightleg.field_78796_g = 0.0F;
      this.leftleg.field_78796_g = 0.0F;
      ModelRenderer var10000;
      if (this.field_78093_q) {
         var10000 = this.rightarm;
         var10000.field_78795_f += -0.62831855F;
         var10000 = this.leftarm;
         var10000.field_78795_f += -0.62831855F;
         this.rightleg.field_78795_f = -1.2566371F;
         this.leftleg.field_78795_f = -1.2566371F;
         this.rightleg.field_78796_g = 0.31415927F;
         this.leftleg.field_78796_g = -0.31415927F;
      }

      this.rightarm.field_78796_g = 0.0F;
      this.leftarm.field_78796_g = 0.0F;
      float f6;
      float f7;
      if (this.field_78095_p > -9990.0F) {
         f6 = this.field_78095_p;
         this.body.field_78796_g = MathHelper.func_76126_a(MathHelper.func_76129_c(f6) * 3.1415927F * 2.0F) * 0.2F;
         this.rightarm.field_78798_e = MathHelper.func_76126_a(this.body.field_78796_g) * 5.0F;
         this.rightarm.field_78800_c = -MathHelper.func_76134_b(this.body.field_78796_g) * 5.0F;
         this.leftarm.field_78798_e = -MathHelper.func_76126_a(this.body.field_78796_g) * 5.0F;
         this.leftarm.field_78800_c = MathHelper.func_76134_b(this.body.field_78796_g) * 5.0F;
         var10000 = this.rightarm;
         var10000.field_78796_g += this.body.field_78796_g;
         var10000 = this.leftarm;
         var10000.field_78796_g += this.body.field_78796_g;
         var10000 = this.leftarm;
         var10000.field_78795_f += this.body.field_78796_g;
         f6 = 1.0F - this.field_78095_p;
         f6 *= f6;
         f6 *= f6;
         f6 = 1.0F - f6;
         f7 = MathHelper.func_76126_a(f6 * 3.1415927F);
         float f8 = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -(this.neck.field_78795_f - 0.7F) * 0.75F;
         this.rightarm.field_78795_f = (float)((double)this.rightarm.field_78795_f - ((double)f7 * 1.2D + (double)f8));
         var10000 = this.rightarm;
         var10000.field_78796_g += this.body.field_78796_g * 2.0F;
         this.rightarm.field_78808_h = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -0.4F;
      }

      boolean sneaking = false;
      if (sneaking) {
         this.body.field_78795_f = 0.5F;
         var10000 = this.rightarm;
         var10000.field_78795_f += 0.4F;
         var10000 = this.leftarm;
         var10000.field_78795_f += 0.4F;
         this.rightleg.field_78798_e = 4.0F;
         this.leftleg.field_78798_e = 4.0F;
         this.rightleg.field_78797_d = 9.0F;
         this.leftleg.field_78797_d = 9.0F;
         this.neck.field_78797_d = 1.0F;
      } else {
         this.body.field_78795_f = 0.0F;
         this.rightleg.field_78798_e = 0.1F;
         this.leftleg.field_78798_e = 0.1F;
         this.rightleg.field_78797_d = 12.0F;
         this.leftleg.field_78797_d = 12.0F;
         this.neck.field_78797_d = 0.0F;
      }

      var10000 = this.rightarm;
      var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.leftarm;
      var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.rightarm;
      var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      var10000 = this.leftarm;
      var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      boolean shootingBow = false;
      if (shootingBow) {
         f6 = 0.0F;
         f7 = 0.0F;
         this.rightarm.field_78808_h = 0.0F;
         this.leftarm.field_78808_h = 0.0F;
         this.rightarm.field_78796_g = -(0.1F - f6 * 0.6F) + this.neck.field_78796_g;
         this.leftarm.field_78796_g = 0.1F - f6 * 0.6F + this.neck.field_78796_g + 0.4F;
         this.rightarm.field_78795_f = -1.5707964F + this.neck.field_78795_f;
         this.leftarm.field_78795_f = -1.5707964F + this.neck.field_78795_f;
         var10000 = this.rightarm;
         var10000.field_78795_f -= f6 * 1.2F - f7 * 0.4F;
         var10000 = this.leftarm;
         var10000.field_78795_f -= f6 * 1.2F - f7 * 0.4F;
         var10000 = this.rightarm;
         var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.leftarm;
         var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.rightarm;
         var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
         var10000 = this.leftarm;
         var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      }

      EntityLeonard entityDemon = (EntityLeonard)entity;
      int i = entityDemon.getAttackTimer();
      if (i > 0) {
         float di = 10.0F;
         this.rightarm.field_78795_f = -2.0F + 1.5F * (Math.abs(((float)i - par4) % 10.0F - di * 0.5F) - di * 0.25F) / (di * 0.25F);
      }

   }
}
