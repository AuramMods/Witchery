package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityImp;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelImp extends ModelBase {
   ModelRenderer head;
   ModelRenderer body;
   ModelRenderer rightarm;
   ModelRenderer leftarm;
   ModelRenderer rightleg;
   ModelRenderer leftleg;
   ModelRenderer chest;
   ModelRenderer hornLeft;
   ModelRenderer hornRight;
   ModelRenderer nose;
   ModelRenderer wingRight;
   ModelRenderer wingLeft;

   public ModelImp() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.head = new ModelRenderer(this, 0, 0);
      this.head.func_78789_a(-5.0F, -8.0F, -4.0F, 10, 8, 10);
      this.head.func_78793_a(0.0F, 8.0F, 0.0F);
      this.head.func_78787_b(64, 64);
      this.head.field_78809_i = true;
      this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
      this.body = new ModelRenderer(this, 0, 48);
      this.body.func_78789_a(-4.0F, 0.0F, -4.0F, 8, 9, 7);
      this.body.func_78793_a(0.0F, 9.0F, 0.0F);
      this.body.func_78787_b(64, 64);
      this.body.field_78809_i = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.rightarm = new ModelRenderer(this, 41, 0);
      this.rightarm.func_78789_a(-2.0F, -2.0F, -1.5F, 3, 13, 3);
      this.rightarm.func_78793_a(-5.0F, 11.0F, 0.0F);
      this.rightarm.func_78787_b(64, 64);
      this.rightarm.field_78809_i = true;
      this.setRotation(this.rightarm, 0.0F, 0.0F, 0.0F);
      this.leftarm = new ModelRenderer(this, 41, 0);
      this.leftarm.func_78789_a(-1.0F, -2.0F, -1.5F, 3, 13, 3);
      this.leftarm.func_78793_a(5.0F, 11.0F, 0.0F);
      this.leftarm.func_78787_b(64, 64);
      this.leftarm.field_78809_i = true;
      this.setRotation(this.leftarm, 0.0F, 0.0F, 0.0F);
      this.rightleg = new ModelRenderer(this, 33, 48);
      this.rightleg.func_78789_a(-1.5F, 0.0F, -1.5F, 3, 6, 3);
      this.rightleg.func_78793_a(-1.5F, 18.0F, 0.0F);
      this.rightleg.func_78787_b(64, 64);
      this.rightleg.field_78809_i = true;
      this.setRotation(this.rightleg, 0.0F, 0.0F, 0.0F);
      this.leftleg = new ModelRenderer(this, 33, 48);
      this.leftleg.func_78789_a(-1.5F, 0.0F, -1.5F, 3, 6, 3);
      this.leftleg.func_78793_a(1.5F, 18.0F, 0.0F);
      this.leftleg.func_78787_b(64, 64);
      this.leftleg.field_78809_i = true;
      this.setRotation(this.leftleg, 0.0F, 0.0F, 0.0F);
      this.chest = new ModelRenderer(this, 4, 41);
      this.chest.func_78789_a(-4.0F, 0.0F, -2.0F, 6, 2, 4);
      this.chest.func_78793_a(1.0F, 8.0F, 0.0F);
      this.chest.func_78787_b(64, 64);
      this.chest.field_78809_i = true;
      this.setRotation(this.chest, 0.0F, 0.0F, 0.0F);
      this.hornLeft = new ModelRenderer(this, 0, 21);
      this.hornLeft.func_78789_a(-1.0F, -5.0F, -1.0F, 2, 5, 2);
      this.hornLeft.func_78793_a(4.0F, 2.0F, 0.0F);
      this.hornLeft.func_78787_b(64, 64);
      this.hornLeft.field_78809_i = true;
      this.setRotation(this.hornLeft, 0.4089647F, 0.0F, 0.7435722F);
      this.head.func_78792_a(this.hornLeft);
      this.hornRight = new ModelRenderer(this, 0, 21);
      this.hornRight.func_78789_a(-1.0F, -5.0F, -1.0F, 2, 5, 2);
      this.hornRight.func_78793_a(-4.0F, 2.0F, 0.0F);
      this.hornRight.func_78787_b(64, 64);
      this.hornRight.field_78809_i = true;
      this.setRotation(this.hornRight, 0.4089647F, 0.0F, -0.7435722F);
      this.head.func_78792_a(this.hornRight);
      this.nose = new ModelRenderer(this, 9, 21);
      this.nose.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 4, 2);
      this.nose.func_78793_a(0.0F, 3.0F, -3.0F);
      this.nose.func_78787_b(64, 64);
      this.nose.field_78809_i = true;
      this.setRotation(this.nose, -0.9666439F, 0.0F, 0.0F);
      this.head.func_78792_a(this.nose);
      this.wingRight = new ModelRenderer(this, 23, 21);
      this.wingRight.func_78789_a(0.0F, 0.0F, 0.0F, 14, 21, 0);
      this.wingRight.func_78793_a(1.0F, -8.0F, 3.0F);
      this.wingRight.func_78787_b(128, 32);
      this.wingRight.field_78809_i = true;
      this.setRotation(this.wingRight, 0.3047198F, -0.6698132F, -0.6283185F);
      this.wingLeft = new ModelRenderer(this, 23, 21);
      this.wingLeft.func_78789_a(0.0F, 0.0F, 0.0F, 14, 21, 0);
      this.wingLeft.func_78793_a(-1.0F, -8.0F, 3.0F);
      this.wingLeft.func_78787_b(128, 32);
      this.wingLeft.field_78809_i = true;
      this.setRotation(this.wingLeft, -0.3047198F, 3.811406F, 0.6283185F);
      this.wingRight.func_78793_a(-2.0F, 10.0F, -1.0F);
      this.wingLeft.func_78793_a(2.0F, 10.0F, -1.0F);
      this.leftleg.func_78793_a(1.5F, 18.0F, 0.0F);
      this.rightleg.func_78793_a(-1.5F, 18.0F, 0.0F);
      this.chest.func_78793_a(1.0F, 8.0F, 0.0F);
      this.head.func_78793_a(0.0F, 8.0F, 0.0F);
      this.hornRight.func_78793_a(-4.0F, -5.0F, 0.0F);
      this.hornLeft.func_78793_a(4.0F, -5.0F, 0.0F);
      this.nose.func_78793_a(0.0F, -4.0F, -3.0F);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      boolean scaled = false;
      if (entity != null && entity instanceof EntityImp) {
         EntityImp imp = (EntityImp)entity;
         if (imp.isPowered()) {
            scaled = true;
            GL11.glScalef(1.5F, 1.0F, 1.5F);
         }
      }

      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.leftleg.func_78793_a(1.5F, 18.0F, 0.0F);
      this.rightleg.func_78793_a(-1.5F, 18.0F, 0.0F);
      this.chest.func_78793_a(1.0F, 8.0F, 0.0F);
      this.head.func_78793_a(0.0F, 8.0F, 0.0F);
      this.hornRight.func_78793_a(-4.0F, -5.0F, 0.0F);
      this.hornLeft.func_78793_a(4.0F, -5.0F, 0.0F);
      this.nose.func_78793_a(0.0F, -4.0F, -3.0F);
      this.head.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.rightarm.func_78785_a(f5);
      this.leftarm.func_78785_a(f5);
      this.rightleg.func_78785_a(f5);
      this.leftleg.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.chest.func_78785_a(f5);
      this.wingLeft.func_78785_a(f5);
      this.wingRight.func_78785_a(f5);
      if (scaled) {
         GL11.glScalef(1.0F, 1.0F, 1.0F);
      }

   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
      this.head.field_78796_g = par4 / 57.295776F;
      this.head.field_78795_f = par5 / 57.295776F;
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
      if (this.field_78095_p > -9990.0F) {
         float f6 = this.field_78095_p;
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
         float f7 = MathHelper.func_76126_a(f6 * 3.1415927F);
         float f8 = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -(this.head.field_78795_f - 0.7F) * 0.75F;
         this.rightarm.field_78795_f = (float)((double)this.rightarm.field_78795_f - ((double)f7 * 1.2D + (double)f8));
         var10000 = this.rightarm;
         var10000.field_78796_g += this.body.field_78796_g * 2.0F;
         this.rightarm.field_78808_h = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -0.4F;
      }

      this.body.field_78795_f = 0.0F;
      this.rightleg.field_78798_e = 0.1F;
      this.leftleg.field_78798_e = 0.1F;
      this.rightleg.field_78797_d = 12.0F;
      this.leftleg.field_78797_d = 12.0F;
      this.head.field_78797_d = 0.0F;
      var10000 = this.rightarm;
      var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.leftarm;
      var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.rightarm;
      var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      var10000 = this.leftarm;
      var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
   }
}
