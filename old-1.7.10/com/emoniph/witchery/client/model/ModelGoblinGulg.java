package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityGoblinGulg;
import com.emoniph.witchery.util.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ModelGoblinGulg extends ModelBase {
   public ModelRenderer bipedBody;
   public ModelRenderer bipedRightArm;
   public ModelRenderer bipedLeftArm;
   public ModelRenderer bipedRightLeg;
   public ModelRenderer bipedLeftLeg;
   public ModelRenderer bipedHead;
   public ModelRenderer bipedChest;
   public int heldItemLeft;
   public int heldItemRight;
   public boolean isSneak;
   public boolean aimedBow;
   private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

   public ModelGoblinGulg() {
      this(0.0F);
   }

   public ModelGoblinGulg(float f) {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.func_78085_a("bipedHead.face", 0, 0);
      this.func_78085_a("bipedHead.tuskright", 0, 4);
      this.func_78085_a("bipedHead.tuskleft", 0, 4);
      this.func_78085_a("bipedHead.nose", 25, 0);
      this.func_78085_a("bipedHead.lip", 34, 0);
      this.bipedBody = new ModelRenderer(this, 16, 16);
      this.bipedBody.func_78789_a(-4.0F, 0.0F, -2.0F, 8, 4, 4);
      this.bipedBody.func_78793_a(0.0F, 8.0F, 0.0F);
      this.bipedBody.func_78787_b(64, 64);
      this.bipedBody.field_78809_i = true;
      this.setRotation(this.bipedBody, 0.0F, 0.0F, 0.0F);
      this.bipedRightArm = new ModelRenderer(this, 40, 14);
      this.bipedRightArm.func_78789_a(-3.0F, -2.0F, -2.0F, 4, 16, 4);
      this.bipedRightArm.func_78793_a(-6.0F, 2.0F, 0.0F);
      this.bipedRightArm.func_78787_b(64, 64);
      this.bipedRightArm.field_78809_i = true;
      this.setRotation(this.bipedRightArm, 0.0F, 0.0F, 0.0F);
      this.bipedLeftArm = new ModelRenderer(this, 40, 14);
      this.bipedLeftArm.func_78789_a(0.0F, -2.0F, -2.0F, 4, 16, 4);
      this.bipedLeftArm.func_78793_a(5.0F, 2.0F, 0.0F);
      this.bipedLeftArm.func_78787_b(64, 64);
      this.bipedLeftArm.field_78809_i = true;
      this.setRotation(this.bipedLeftArm, 0.0F, 0.0F, 0.0F);
      this.bipedRightLeg = new ModelRenderer(this, 0, 16);
      this.bipedRightLeg.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 12, 4);
      this.bipedRightLeg.func_78793_a(-2.0F, 12.0F, 0.0F);
      this.bipedRightLeg.func_78787_b(64, 64);
      this.bipedRightLeg.field_78809_i = true;
      this.setRotation(this.bipedRightLeg, 0.0F, 0.0F, 0.0F);
      this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
      this.bipedLeftLeg.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 12, 4);
      this.bipedLeftLeg.func_78793_a(2.0F, 12.0F, 0.0F);
      this.bipedLeftLeg.func_78787_b(64, 64);
      this.bipedLeftLeg.field_78809_i = true;
      this.setRotation(this.bipedLeftLeg, 0.0F, 0.0F, 0.0F);
      this.bipedHead = new ModelRenderer(this, "bipedHead");
      this.bipedHead.func_78793_a(0.0F, 0.0F, 0.0F);
      this.setRotation(this.bipedHead, 0.0F, 0.0F, 0.0F);
      this.bipedHead.field_78809_i = true;
      this.bipedHead.func_78786_a("face", -4.0F, -8.0F, -4.0F, 8, 8, 8);
      this.bipedHead.func_78786_a("tuskright", -2.0F, -4.0F, -5.0F, 1, 2, 1);
      this.bipedHead.func_78786_a("tuskleft", 1.0F, -4.0F, -5.0F, 1, 2, 1);
      this.bipedHead.func_78786_a("nose", -1.0F, -6.0F, -6.0F, 2, 3, 2);
      this.bipedHead.func_78786_a("lip", -2.0F, -2.0F, -6.0F, 4, 1, 2);
      this.bipedChest = new ModelRenderer(this, 12, 35);
      this.bipedChest.func_78789_a(-5.0F, 0.0F, -3.0F, 10, 8, 6);
      this.bipedChest.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bipedChest.func_78787_b(64, 64);
      this.bipedChest.field_78809_i = true;
      this.setRotation(this.bipedChest, 0.0F, 0.0F, 0.0F);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float par2, float par3, float par4, float par5, float par6, float par7) {
      this.func_78087_a(par2, par3, par4, par5, par6, par7, entity);
      this.doRender(par7);
      Minecraft mc = Minecraft.func_71410_x();
      if (mc.field_71474_y.field_74347_j && Config.instance().renderHuntsmanGlintEffect) {
         float f9 = (float)entity.field_70173_aa;
         mc.field_71446_o.func_110577_a(RES_ITEM_GLINT);
         GL11.glEnable(3042);
         float f10 = 0.5F;
         GL11.glColor4f(f10, f10, f10, 1.0F);
         GL11.glDepthFunc(514);
         GL11.glDepthMask(false);

         for(int k = 0; k < 2; ++k) {
            GL11.glDisable(2896);
            float f11 = 0.76F;
            GL11.glColor4f(0.2F * f11, 0.7F * f11, 0.7F * f11, 1.0F);
            GL11.glBlendFunc(768, 1);
            GL11.glMatrixMode(5890);
            GL11.glLoadIdentity();
            float f12 = f9 * (0.001F + (float)k * 0.003F) * 20.0F;
            float f13 = 0.33333334F;
            GL11.glScalef(f13, f13, f13);
            GL11.glRotatef(30.0F - (float)k * 60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.0F, f12, 0.0F);
            GL11.glMatrixMode(5888);
            this.doRender(par7);
         }

         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glMatrixMode(5890);
         GL11.glDepthMask(true);
         GL11.glLoadIdentity();
         GL11.glMatrixMode(5888);
         GL11.glEnable(2896);
         GL11.glDisable(3042);
         GL11.glDepthFunc(515);
      }

   }

   private void doRender(float par7) {
      if (this.field_78091_s) {
         float f6 = 2.0F;
         GL11.glPushMatrix();
         GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
         GL11.glTranslatef(0.0F, 16.0F * par7, 0.0F);
         this.bipedHead.func_78785_a(par7);
         GL11.glPopMatrix();
         GL11.glPushMatrix();
         GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
         GL11.glTranslatef(0.0F, 24.0F * par7, 0.0F);
         this.bipedBody.func_78785_a(par7);
         this.bipedChest.func_78785_a(par7);
         this.bipedRightArm.func_78785_a(par7);
         this.bipedLeftArm.func_78785_a(par7);
         this.bipedRightLeg.func_78785_a(par7);
         this.bipedLeftLeg.func_78785_a(par7);
         GL11.glPopMatrix();
      } else {
         this.bipedHead.func_78785_a(par7);
         this.bipedChest.func_78785_a(par7);
         this.bipedBody.func_78785_a(par7);
         this.bipedRightArm.func_78785_a(par7);
         this.bipedLeftArm.func_78785_a(par7);
         this.bipedRightLeg.func_78785_a(par7);
         this.bipedLeftLeg.func_78785_a(par7);
      }

   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
      this.bipedHead.field_78796_g = par4 / 57.295776F;
      this.bipedHead.field_78795_f = par5 / 57.295776F;
      this.bipedRightArm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 2.0F * par2 * 0.5F;
      this.bipedLeftArm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
      this.bipedRightArm.field_78808_h = 0.0F;
      this.bipedLeftArm.field_78808_h = 0.0F;
      this.bipedRightLeg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 1.4F * par2;
      this.bipedLeftLeg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 1.4F * par2;
      this.bipedRightLeg.field_78796_g = 0.0F;
      this.bipedLeftLeg.field_78796_g = 0.0F;
      ModelRenderer var10000;
      if (this.field_78093_q) {
         var10000 = this.bipedRightArm;
         var10000.field_78795_f += -0.62831855F;
         var10000 = this.bipedLeftArm;
         var10000.field_78795_f += -0.62831855F;
         this.bipedRightLeg.field_78795_f = -1.2566371F;
         this.bipedLeftLeg.field_78795_f = -1.2566371F;
         this.bipedRightLeg.field_78796_g = 0.31415927F;
         this.bipedLeftLeg.field_78796_g = -0.31415927F;
      }

      if (this.heldItemLeft != 0) {
         this.bipedLeftArm.field_78795_f = this.bipedLeftArm.field_78795_f * 0.5F - 0.31415927F * (float)this.heldItemLeft;
      }

      if (this.heldItemRight != 0) {
         this.bipedRightArm.field_78795_f = this.bipedRightArm.field_78795_f * 0.5F - 0.31415927F * (float)this.heldItemRight;
      }

      this.bipedRightArm.field_78796_g = 0.0F;
      this.bipedLeftArm.field_78796_g = 0.0F;
      float f6;
      float f7;
      if (this.field_78095_p > -9990.0F) {
         f6 = this.field_78095_p;
         this.bipedBody.field_78796_g = MathHelper.func_76126_a(MathHelper.func_76129_c(f6) * 3.1415927F * 2.0F) * 0.2F;
         this.bipedRightArm.field_78798_e = MathHelper.func_76126_a(this.bipedBody.field_78796_g) * 5.0F;
         this.bipedRightArm.field_78800_c = -MathHelper.func_76134_b(this.bipedBody.field_78796_g) * 5.0F;
         this.bipedLeftArm.field_78798_e = -MathHelper.func_76126_a(this.bipedBody.field_78796_g) * 5.0F;
         this.bipedLeftArm.field_78800_c = MathHelper.func_76134_b(this.bipedBody.field_78796_g) * 5.0F;
         var10000 = this.bipedRightArm;
         var10000.field_78796_g += this.bipedBody.field_78796_g;
         var10000 = this.bipedLeftArm;
         var10000.field_78796_g += this.bipedBody.field_78796_g;
         var10000 = this.bipedLeftArm;
         var10000.field_78795_f += this.bipedBody.field_78796_g;
         f6 = 1.0F - this.field_78095_p;
         f6 *= f6;
         f6 *= f6;
         f6 = 1.0F - f6;
         f7 = MathHelper.func_76126_a(f6 * 3.1415927F);
         float f8 = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -(this.bipedHead.field_78795_f - 0.7F) * 0.75F;
         this.bipedRightArm.field_78795_f = (float)((double)this.bipedRightArm.field_78795_f - ((double)f7 * 1.2D + (double)f8));
         var10000 = this.bipedRightArm;
         var10000.field_78796_g += this.bipedBody.field_78796_g * 2.0F;
         this.bipedRightArm.field_78808_h = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -0.4F;
      }

      if (this.isSneak) {
         this.bipedBody.field_78795_f = 0.5F;
         var10000 = this.bipedRightArm;
         var10000.field_78795_f += 0.4F;
         var10000 = this.bipedLeftArm;
         var10000.field_78795_f += 0.4F;
         this.bipedRightLeg.field_78798_e = 4.0F;
         this.bipedLeftLeg.field_78798_e = 4.0F;
         this.bipedRightLeg.field_78797_d = 9.0F;
         this.bipedLeftLeg.field_78797_d = 9.0F;
         this.bipedHead.field_78797_d = 1.0F;
      } else {
         this.bipedBody.field_78795_f = 0.0F;
         this.bipedRightLeg.field_78798_e = 0.1F;
         this.bipedLeftLeg.field_78798_e = 0.1F;
         this.bipedRightLeg.field_78797_d = 12.0F;
         this.bipedLeftLeg.field_78797_d = 12.0F;
         this.bipedHead.field_78797_d = 0.0F;
      }

      var10000 = this.bipedRightArm;
      var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.bipedLeftArm;
      var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.bipedRightArm;
      var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      var10000 = this.bipedLeftArm;
      var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      if (this.aimedBow) {
         f6 = 0.0F;
         f7 = 0.0F;
         this.bipedRightArm.field_78808_h = 0.0F;
         this.bipedLeftArm.field_78808_h = 0.0F;
         this.bipedRightArm.field_78796_g = -(0.1F - f6 * 0.6F) + this.bipedHead.field_78796_g;
         this.bipedLeftArm.field_78796_g = 0.1F - f6 * 0.6F + this.bipedHead.field_78796_g + 0.4F;
         this.bipedRightArm.field_78795_f = -1.5707964F + this.bipedHead.field_78795_f;
         this.bipedLeftArm.field_78795_f = -1.5707964F + this.bipedHead.field_78795_f;
         var10000 = this.bipedRightArm;
         var10000.field_78795_f -= f6 * 1.2F - f7 * 0.4F;
         var10000 = this.bipedLeftArm;
         var10000.field_78795_f -= f6 * 1.2F - f7 * 0.4F;
         var10000 = this.bipedRightArm;
         var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.bipedLeftArm;
         var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.bipedRightArm;
         var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
         var10000 = this.bipedLeftArm;
         var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      }

      EntityGoblinGulg entityDemon = (EntityGoblinGulg)entity;
      int i = entityDemon.getAttackTimer();
      if (i > 0) {
         this.bipedRightArm.field_78795_f = -2.0F + 1.5F * this.func_78172_a((float)i - par4, 10.0F);
      }

   }

   private float func_78172_a(float par1, float par2) {
      return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
   }
}
