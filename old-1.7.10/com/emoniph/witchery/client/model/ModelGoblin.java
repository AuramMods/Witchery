package com.emoniph.witchery.client.model;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityGoblin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelGoblin extends ModelBase {
   public ModelRenderer bipedHead;
   public ModelRenderer bipedBody;
   public ModelRenderer bipedRightArm;
   public ModelRenderer bipedLeftArm;
   public ModelRenderer bipedRightLeg;
   public ModelRenderer bipedLeftLeg;
   public int heldItemLeft;
   public int heldItemRight;
   public boolean isSneak;
   public boolean aimedBow;

   public ModelGoblin() {
      this(0.0F);
   }

   public ModelGoblin(float scale) {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.func_78085_a("head.face", 0, 0);
      this.func_78085_a("head.nose1", 34, 3);
      this.func_78085_a("head.nose2", 34, 0);
      this.func_78085_a("head.nose3", 33, 9);
      this.func_78085_a("head.earTipLeft", 46, 0);
      this.func_78085_a("head.earInnerLeft", 39, 0);
      this.func_78085_a("head.earInnerRight", 39, 0);
      this.func_78085_a("head.earTipRight", 46, 0);
      this.bipedHead = new ModelRenderer(this, "head");
      this.bipedHead.func_78793_a(0.0F, 11.0F, 0.0F);
      this.setRotation(this.bipedHead, 0.0F, 0.0F, 0.0F);
      this.bipedHead.field_78809_i = true;
      this.bipedHead.func_78786_a("face", -4.0F, -8.0F, -4.0F, 8, 8, 8);
      this.bipedHead.func_78786_a("nose1", -0.5F, -6.0F, -5.0F, 1, 3, 1);
      this.bipedHead.func_78786_a("nose2", -0.5F, -5.0F, -6.0F, 1, 1, 1);
      this.bipedHead.func_78786_a("nose3", -0.5F, -4.0F, -7.0F, 1, 2, 2);
      this.bipedHead.func_78786_a("earTipLeft", 6.0F, -7.0F, 0.0F, 2, 2, 1);
      this.bipedHead.func_78786_a("earInnerLeft", 4.0F, -7.0F, 0.0F, 2, 3, 1);
      this.bipedHead.func_78786_a("earInnerRight", -6.0F, -7.0F, 0.0F, 2, 3, 1);
      this.bipedHead.func_78786_a("earTipRight", -8.0F, -7.0F, 0.0F, 2, 2, 1);
      this.bipedBody = new ModelRenderer(this, 16, 16);
      this.bipedBody.func_78790_a(-4.0F, 0.0F, -2.0F, 8, 7, 4, scale);
      this.bipedBody.func_78793_a(0.0F, 11.0F, 0.0F);
      this.bipedBody.func_78787_b(64, 32);
      this.bipedBody.field_78809_i = true;
      this.setRotation(this.bipedBody, 0.0F, 0.0F, 0.0F);
      this.bipedRightArm = new ModelRenderer(this, 40, 16);
      this.bipedRightArm.func_78790_a(-3.0F, -3.0F, -2.0F, 4, 12, 4, scale);
      this.bipedRightArm.func_78793_a(-5.0F, 12.0F, 0.0F);
      this.bipedRightArm.func_78787_b(64, 32);
      this.bipedRightArm.field_78809_i = true;
      this.setRotation(this.bipedRightArm, 0.0F, 0.0F, 0.0F);
      this.bipedLeftArm = new ModelRenderer(this, 40, 16);
      this.bipedLeftArm.func_78790_a(-1.0F, -3.0F, -2.0F, 4, 12, 4, scale);
      this.bipedLeftArm.func_78793_a(5.0F, 12.0F, 0.0F);
      this.bipedLeftArm.func_78787_b(64, 32);
      this.bipedLeftArm.field_78809_i = true;
      this.setRotation(this.bipedLeftArm, 0.0F, 0.0F, 0.0F);
      this.bipedRightLeg = new ModelRenderer(this, 0, 16);
      this.bipedRightLeg.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
      this.bipedRightLeg.func_78793_a(-2.0F, 18.0F, 0.0F);
      this.bipedRightLeg.func_78787_b(64, 32);
      this.bipedRightLeg.field_78809_i = true;
      this.setRotation(this.bipedRightLeg, 0.0F, 0.0F, 0.0F);
      this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
      this.bipedLeftLeg.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 6, 4, scale);
      this.bipedLeftLeg.func_78793_a(2.0F, 18.0F, 0.0F);
      this.bipedLeftLeg.func_78787_b(64, 32);
      this.bipedLeftLeg.field_78809_i = true;
      this.setRotation(this.bipedLeftLeg, 0.0F, 0.0F, 0.0F);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78088_a(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
      this.func_78087_a(par2, par3, par4, par5, par6, par7, par1Entity);
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
         this.bipedRightArm.func_78785_a(par7);
         this.bipedLeftArm.func_78785_a(par7);
         this.bipedRightLeg.func_78785_a(par7);
         this.bipedLeftLeg.func_78785_a(par7);
         GL11.glPopMatrix();
      } else {
         this.bipedHead.func_78785_a(par7);
         this.bipedBody.func_78785_a(par7);
         this.bipedRightArm.func_78785_a(par7);
         this.bipedLeftArm.func_78785_a(par7);
         this.bipedRightLeg.func_78785_a(par7);
         this.bipedLeftLeg.func_78785_a(par7);
      }

   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
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
         if (par7Entity != null && par7Entity instanceof EntityGoblin) {
            EntityGoblin goblin = (EntityGoblin)par7Entity;
            if (goblin.isWorking()) {
               if (goblin.func_70694_bm() != null && goblin.func_70694_bm().func_77973_b() == Witchery.Items.KOBOLDITE_PICKAXE) {
                  var10000 = this.bipedRightArm;
                  var10000.field_78795_f = (float)((double)var10000.field_78795_f - (double)(par7Entity.field_70173_aa % 6) * 0.3D);
               } else {
                  var10000 = this.bipedRightArm;
                  var10000.field_78795_f = (float)((double)var10000.field_78795_f - (double)(par7Entity.field_70173_aa % 20) * 0.1D);
               }
            }
         }
      }

      this.bipedRightArm.field_78796_g = 0.0F;
      this.bipedLeftArm.field_78796_g = 0.0F;
      float f7;
      float f6;
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

      boolean isWorshipping = par7Entity != null && par7Entity instanceof EntityGoblin && ((EntityGoblin)par7Entity).isWorshipping();
      if (!this.isSneak && !isWorshipping) {
         this.bipedBody.field_78795_f = 0.0F;
         this.bipedRightLeg.field_78798_e = 0.1F;
         this.bipedLeftLeg.field_78798_e = 0.1F;
         this.bipedRightLeg.field_78797_d = 18.0F;
         this.bipedLeftLeg.field_78797_d = 18.0F;
         this.bipedHead.field_78797_d = 11.0F;
         this.bipedBody.field_78797_d = 11.0F;
      } else {
         this.bipedBody.field_78795_f = 0.5F;
         var10000 = this.bipedRightArm;
         var10000.field_78795_f -= 2.2F;
         var10000 = this.bipedLeftArm;
         var10000.field_78795_f -= 2.2F;
         this.bipedRightLeg.field_78798_e = 3.0F;
         this.bipedLeftLeg.field_78798_e = 3.0F;
         this.bipedHead.field_78795_f = 0.5F;
         this.bipedRightLeg.field_78797_d = 18.0F;
         this.bipedLeftLeg.field_78797_d = 18.0F;
         this.bipedHead.field_78797_d = 13.0F;
         this.bipedBody.field_78797_d = 13.0F;
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

   }
}
