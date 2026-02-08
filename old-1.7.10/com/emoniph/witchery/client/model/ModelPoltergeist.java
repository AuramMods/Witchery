package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityPoltergeist;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelPoltergeist extends ModelBase {
   ModelRenderer bipedHead;
   ModelRenderer bipedBody;
   ModelRenderer bipedRightArm;
   ModelRenderer bipedRightArm2;
   ModelRenderer bipedLeftArm;
   ModelRenderer bipedLeftArm2;
   ModelRenderer bipedRightLeg;
   ModelRenderer bipedLeftLeg;

   public ModelPoltergeist() {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.bipedHead = new ModelRenderer(this, 0, 0);
      this.bipedHead.func_78789_a(-4.0F, -8.0F, -3.0F, 8, 8, 6);
      this.bipedHead.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bipedHead.func_78787_b(64, 32);
      this.bipedHead.field_78809_i = true;
      this.setRotation(this.bipedHead, 0.0F, 0.0F, 0.0F);
      this.bipedBody = new ModelRenderer(this, 16, 16);
      this.bipedBody.func_78789_a(-4.0F, 0.0F, -1.0F, 8, 11, 2);
      this.bipedBody.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bipedBody.func_78787_b(64, 32);
      this.bipedBody.field_78809_i = true;
      this.setRotation(this.bipedBody, 0.0F, 0.0F, 0.0F);
      this.bipedRightArm = new ModelRenderer(this, 40, 0);
      this.bipedRightArm.func_78789_a(-1.0F, -2.0F, -1.0F, 2, 18, 2);
      this.bipedRightArm.func_78793_a(-5.0F, 2.0F, 0.0F);
      this.bipedRightArm.func_78787_b(64, 32);
      this.bipedRightArm.field_78809_i = true;
      this.setRotation(this.bipedRightArm, 0.0F, 0.0F, 0.0F);
      this.bipedRightArm2 = new ModelRenderer(this, 40, 0);
      this.bipedRightArm2.func_78789_a(-1.0F, -2.0F, -1.0F, 2, 18, 2);
      this.bipedRightArm2.func_78793_a(-5.0F, 2.0F, 0.0F);
      this.bipedRightArm2.func_78787_b(64, 32);
      this.bipedRightArm2.field_78809_i = true;
      this.setRotation(this.bipedRightArm2, 0.0F, 0.0F, 0.0F);
      this.bipedLeftArm = new ModelRenderer(this, 40, 0);
      this.bipedLeftArm.func_78789_a(-1.0F, -2.0F, -1.0F, 2, 18, 2);
      this.bipedLeftArm.func_78793_a(5.0F, 2.0F, 0.0F);
      this.bipedLeftArm.func_78787_b(64, 32);
      this.bipedLeftArm.field_78809_i = true;
      this.setRotation(this.bipedLeftArm, 0.0F, 0.0F, 0.0F);
      this.bipedLeftArm2 = new ModelRenderer(this, 40, 0);
      this.bipedLeftArm2.func_78789_a(-1.0F, -2.0F, -1.0F, 2, 18, 2);
      this.bipedLeftArm2.func_78793_a(5.0F, 2.0F, 0.0F);
      this.bipedLeftArm2.func_78787_b(64, 32);
      this.bipedLeftArm2.field_78809_i = true;
      this.setRotation(this.bipedLeftArm2, 0.0F, 0.0F, 0.0F);
      this.bipedRightLeg = new ModelRenderer(this, 0, 16);
      this.bipedRightLeg.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 13, 2);
      this.bipedRightLeg.func_78793_a(-2.0F, 11.0F, 0.0F);
      this.bipedRightLeg.func_78787_b(64, 32);
      this.bipedRightLeg.field_78809_i = true;
      this.setRotation(this.bipedRightLeg, 0.0F, 0.0F, 0.0F);
      this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
      this.bipedLeftLeg.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 13, 2);
      this.bipedLeftLeg.func_78793_a(2.0F, 11.0F, 0.0F);
      this.bipedLeftLeg.func_78787_b(64, 32);
      this.bipedLeftLeg.field_78809_i = true;
      this.setRotation(this.bipedLeftLeg, 0.0F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.bipedHead.func_78785_a(f5);
      this.bipedBody.func_78785_a(f5);
      this.bipedRightArm.func_78785_a(f5);
      this.bipedRightArm2.func_78785_a(f5);
      this.bipedLeftArm.func_78785_a(f5);
      this.bipedLeftArm2.func_78785_a(f5);
      this.bipedRightLeg.func_78785_a(f5);
      this.bipedLeftLeg.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
      super.func_78087_a(par1, par2, par3, par4, par5, par6, entity);
      this.bipedHead.field_78796_g = par4 / 57.295776F;
      this.bipedHead.field_78795_f = par5 / 57.295776F;
      this.bipedRightArm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 2.0F * par2 * 0.5F;
      this.bipedRightArm2.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 2.0F * par2 * 0.25F;
      this.bipedLeftArm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
      this.bipedLeftArm2.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 2.0F * par2 * 0.25F;
      this.bipedRightArm.field_78796_g = 0.0F;
      this.bipedRightArm2.field_78796_g = 0.0F;
      this.bipedLeftArm.field_78796_g = 0.0F;
      this.bipedLeftArm2.field_78796_g = 0.0F;
      this.bipedRightArm.field_78808_h = 0.0F;
      this.bipedRightArm2.field_78808_h = 0.0F;
      this.bipedLeftArm.field_78808_h = 0.0F;
      this.bipedLeftArm2.field_78808_h = 0.0F;
      this.bipedRightLeg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 1.4F * par2;
      this.bipedLeftLeg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 1.4F * par2;
      this.bipedRightLeg.field_78796_g = 0.0F;
      this.bipedLeftLeg.field_78796_g = 0.0F;
      this.bipedBody.field_78795_f = 0.0F;
      this.bipedRightLeg.field_78798_e = 0.1F;
      this.bipedLeftLeg.field_78798_e = 0.1F;
      this.bipedRightLeg.field_78797_d = 12.0F;
      this.bipedLeftLeg.field_78797_d = 12.0F;
      this.bipedHead.field_78797_d = 0.0F;
      ModelRenderer var10000 = this.bipedRightArm;
      var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.bipedRightArm2;
      var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.bipedLeftArm;
      var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.bipedLeftArm2;
      var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.bipedRightArm;
      var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      var10000 = this.bipedRightArm2;
      var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      var10000 = this.bipedLeftArm;
      var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      var10000 = this.bipedLeftArm2;
      var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      EntityPoltergeist entityDemon = (EntityPoltergeist)entity;
      int i = entityDemon.getAttackTimer();
      if (i > 0) {
         this.bipedRightArm.field_78795_f = -1.5F + 0.8F * this.func_78172_a((float)i - par4, 15.0F);
         this.bipedLeftArm.field_78795_f = -1.5F + 0.8F * this.func_78172_a((float)i - par4, 15.0F);
         this.bipedRightArm2.field_78795_f = -1.5F + 0.8F * this.func_78172_a((float)i - par4, 15.0F);
         this.bipedLeftArm2.field_78795_f = -1.5F + 0.8F * this.func_78172_a((float)i - par4, 15.0F);
         this.bipedRightArm.field_78808_h = -(-1.5F + 1.5F * this.func_78172_a((float)i - par4, 15.0F));
         this.bipedLeftArm.field_78808_h = -1.5F + 1.5F * this.func_78172_a((float)i - par4, 15.0F);
      }

   }

   public void func_78086_a(EntityLivingBase par1EntityLiving, float par2, float par3, float par4) {
   }

   private float func_78172_a(float par1, float par2) {
      return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
   }
}
