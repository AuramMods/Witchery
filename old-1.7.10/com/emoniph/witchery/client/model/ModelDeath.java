package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelDeath extends ModelBase {
   ModelRenderer bipedHead;
   ModelRenderer bipedBody;
   ModelRenderer bipedRightArm;
   ModelRenderer bipedLeftArm;
   ModelRenderer bipedRightLeg;
   ModelRenderer bipedLeftLeg;
   ModelRenderer robe;
   ModelRenderer scythe;

   public ModelDeath() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.func_78085_a("scythe.shaft", 58, 5);
      this.func_78085_a("scythe.blade", 36, 0);
      this.bipedHead = new ModelRenderer(this, 27, 43);
      this.bipedHead.func_78789_a(-4.0F, -8.0F, -4.0F, 8, 10, 8);
      this.bipedHead.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bipedHead.func_78787_b(64, 64);
      this.bipedHead.field_78809_i = true;
      this.setRotation(this.bipedHead, 0.0F, 0.0F, 0.0F);
      this.bipedBody = new ModelRenderer(this, 16, 16);
      this.bipedBody.func_78789_a(-4.0F, 0.0F, -2.0F, 8, 12, 4);
      this.bipedBody.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bipedBody.func_78787_b(64, 64);
      this.bipedBody.field_78809_i = true;
      this.setRotation(this.bipedBody, 0.0F, 0.0F, 0.0F);
      this.bipedRightArm = new ModelRenderer(this, 40, 16);
      this.bipedRightArm.func_78789_a(-3.0F, -2.0F, -2.0F, 4, 12, 4);
      this.bipedRightArm.func_78793_a(-5.0F, 2.0F, 0.0F);
      this.bipedRightArm.func_78787_b(64, 64);
      this.bipedRightArm.field_78809_i = true;
      this.setRotation(this.bipedRightArm, 0.0F, 0.0F, 0.0F);
      this.bipedLeftArm = new ModelRenderer(this, 40, 16);
      this.bipedLeftArm.func_78789_a(-1.0F, -2.0F, -2.0F, 4, 12, 4);
      this.bipedLeftArm.func_78793_a(5.0F, 2.0F, 0.0F);
      this.bipedLeftArm.func_78787_b(64, 64);
      this.bipedLeftArm.field_78809_i = true;
      this.setRotation(this.bipedLeftArm, 0.0F, 0.0F, 0.0F);
      this.bipedRightLeg = new ModelRenderer(this, 0, 16);
      this.bipedRightLeg.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 12, 2);
      this.bipedRightLeg.func_78793_a(-2.0F, 12.0F, 0.0F);
      this.bipedRightLeg.func_78787_b(64, 64);
      this.bipedRightLeg.field_78809_i = true;
      this.setRotation(this.bipedRightLeg, 0.0F, 0.0F, 0.0F);
      this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
      this.bipedLeftLeg.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 12, 2);
      this.bipedLeftLeg.func_78793_a(2.0F, 12.0F, 0.0F);
      this.bipedLeftLeg.func_78787_b(64, 64);
      this.bipedLeftLeg.field_78809_i = true;
      this.setRotation(this.bipedLeftLeg, 0.0F, 0.0F, 0.0F);
      this.robe = new ModelRenderer(this, 0, 33);
      this.robe.func_78789_a(-4.0F, 0.0F, -2.5F, 8, 23, 5);
      this.robe.func_78793_a(0.0F, 0.0F, 0.0F);
      this.robe.func_78787_b(64, 64);
      this.robe.field_78809_i = true;
      this.setRotation(this.robe, 0.0F, 0.0F, 0.0F);
      this.scythe = new ModelRenderer(this, "scythe");
      this.scythe.func_78793_a(-6.0F, 10.0F, 0.0F);
      this.setRotation(this.scythe, 0.0F, 0.0F, 0.0F);
      this.scythe.field_78809_i = true;
      this.scythe.func_78786_a("shaft", -0.5F, -16.0F, -0.5F, 1, 35, 1);
      this.scythe.func_78786_a("blade", 0.0F, -16.0F, 0.0F, 13, 4, 0);
      this.bipedRightArm.func_78792_a(this.scythe);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.bipedHead.func_78785_a(f5);
      this.bipedBody.func_78785_a(f5);
      this.bipedRightArm.func_78785_a(f5);
      this.bipedLeftArm.func_78785_a(f5);
      this.bipedRightLeg.func_78785_a(f5);
      this.bipedLeftLeg.func_78785_a(f5);
      GL11.glScalef(1.05F, 1.0F, 1.05F);
      this.robe.func_78785_a(f5);
   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
      this.scythe.field_78800_c = -0.8F;
      this.scythe.field_78798_e = 0.0F;
      this.scythe.field_78797_d = 8.1F;
      this.scythe.field_78795_f = 1.5707964F;
      this.bipedHead.field_78796_g = par4 / 57.295776F;
      this.bipedHead.field_78795_f = par5 / 57.295776F;
      this.bipedRightArm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 2.0F * par2 * 0.5F - 1.5707964F;
      this.bipedLeftArm.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
      this.bipedRightArm.field_78808_h = 0.0F;
      this.bipedLeftArm.field_78808_h = 0.0F;
      this.bipedRightLeg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 1.4F * par2;
      this.bipedLeftLeg.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 1.4F * par2;
      this.bipedRightLeg.field_78796_g = 0.0F;
      this.bipedLeftLeg.field_78796_g = 0.0F;
      this.bipedRightArm.field_78796_g = 0.0F;
      this.bipedLeftArm.field_78796_g = 0.0F;
      ModelRenderer var10000;
      if (this.field_78095_p > -9990.0F) {
         float f6 = this.field_78095_p;
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
         float f7 = MathHelper.func_76126_a(f6 * 3.1415927F);
         float f8 = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -(this.bipedHead.field_78795_f - 0.7F) * 0.75F;
         this.bipedRightArm.field_78795_f = (float)((double)this.bipedRightArm.field_78795_f - ((double)f7 * 1.2D + (double)f8));
         var10000 = this.bipedRightArm;
         var10000.field_78796_g += this.bipedBody.field_78796_g * 2.0F;
         this.bipedRightArm.field_78808_h = MathHelper.func_76126_a(this.field_78095_p * 3.1415927F) * -0.4F;
      }

      this.bipedBody.field_78795_f = 0.0F;
      this.bipedRightLeg.field_78798_e = 0.1F;
      this.bipedLeftLeg.field_78798_e = 0.1F;
      this.bipedRightLeg.field_78797_d = 12.0F;
      this.bipedLeftLeg.field_78797_d = 12.0F;
      this.bipedHead.field_78797_d = 0.0F;
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
