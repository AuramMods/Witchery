package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityToad;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelToad extends ModelBase {
   ModelRenderer head;
   ModelRenderer body;
   ModelRenderer armRight;
   ModelRenderer armLeft;
   ModelRenderer legRight;
   ModelRenderer legLeft;

   public ModelToad() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.func_78085_a("head.nose", 0, 5);
      this.func_78085_a("head.eyeRight", 0, 0);
      this.func_78085_a("head.eyeLeft", 8, 0);
      this.func_78085_a("legRight.thighRight", 0, 20);
      this.func_78085_a("legRight.footRight", 0, 26);
      this.func_78085_a("legLeft.thighLeft", 11, 20);
      this.func_78085_a("legLeft.footLeft", 0, 26);
      this.head = new ModelRenderer(this, "head");
      this.head.func_78793_a(0.0F, 20.0F, -1.0F);
      this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
      this.head.field_78809_i = true;
      this.head.func_78786_a("nose", -2.0F, -1.0F, -4.0F, 4, 2, 4);
      this.head.func_78786_a("eyeRight", -2.5F, -3.0F, -3.0F, 2, 2, 2);
      this.head.func_78786_a("eyeLeft", 0.5F, -3.0F, -3.0F, 2, 2, 2);
      this.body = new ModelRenderer(this, 0, 12);
      this.body.func_78789_a(-2.0F, -1.0F, 0.0F, 4, 2, 5);
      this.body.func_78793_a(0.0F, 20.0F, -1.0F);
      this.body.func_78787_b(32, 32);
      this.body.field_78809_i = true;
      this.setRotation(this.body, -0.4833219F, 0.0F, 0.0F);
      this.armRight = new ModelRenderer(this, 13, 26);
      this.armRight.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 4, 1);
      this.armRight.func_78793_a(-2.0F, 20.0F, -1.0F);
      this.armRight.func_78787_b(32, 32);
      this.armRight.field_78809_i = true;
      this.setRotation(this.armRight, -0.3346075F, 0.0F, 0.0F);
      this.armLeft = new ModelRenderer(this, 18, 26);
      this.armLeft.func_78789_a(0.0F, 0.0F, 0.0F, 1, 4, 1);
      this.armLeft.func_78793_a(2.0F, 20.0F, -1.0F);
      this.armLeft.func_78787_b(32, 32);
      this.armLeft.field_78809_i = true;
      this.setRotation(this.armLeft, -0.3346075F, 0.0F, 0.0F);
      this.legRight = new ModelRenderer(this, "legRight");
      this.legRight.func_78793_a(-2.0F, 23.0F, 3.0F);
      this.setRotation(this.legRight, 0.0F, 0.0F, 0.0F);
      this.legRight.field_78809_i = true;
      this.legRight.func_78786_a("thighRight", -2.0F, -1.0F, -2.0F, 2, 2, 3);
      this.legRight.func_78786_a("footRight", -3.0F, 1.0F, -4.0F, 3, 0, 3);
      this.legLeft = new ModelRenderer(this, "legLeft");
      this.legLeft.func_78793_a(2.0F, 23.0F, 3.0F);
      this.setRotation(this.legLeft, 0.0F, 0.0F, 0.0F);
      this.legLeft.field_78809_i = true;
      this.legLeft.func_78786_a("thighLeft", 0.0F, -1.0F, -2.0F, 2, 2, 3);
      this.legLeft.func_78786_a("footLeft", 0.0F, 1.0F, -4.0F, 3, 0, 3);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.head.field_78796_g = f3 / 57.295776F;
      this.head.field_78795_f = f4 / 57.295776F;
      if (this.field_78091_s) {
         float p6 = 2.0F;
         GL11.glPushMatrix();
         GL11.glScalef(1.5F / p6, 1.5F / p6, 1.5F / p6);
         GL11.glTranslatef(0.0F, 10.0F * f5, 0.0F);
         this.head.func_78785_a(f5);
         GL11.glPopMatrix();
         GL11.glPushMatrix();
         GL11.glScalef(1.0F / p6, 1.0F / p6, 1.0F / p6);
         GL11.glTranslatef(0.0F, 24.0F * f5, 0.0F);
         this.body.func_78785_a(f5);
         this.armRight.func_78785_a(f5);
         this.armLeft.func_78785_a(f5);
         this.legRight.func_78785_a(f5);
         this.legLeft.func_78785_a(f5);
         GL11.glPopMatrix();
      } else {
         this.head.func_78785_a(f5);
         this.body.func_78785_a(f5);
         this.armRight.func_78785_a(f5);
         this.armLeft.func_78785_a(f5);
         this.legRight.func_78785_a(f5);
         this.legLeft.func_78785_a(f5);
      }

   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      super.func_78087_a(f, f1, f2, f3, f4, f5, entity);
   }

   public void func_78086_a(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
      EntityToad toad = (EntityToad)par1EntityLivingBase;
      if (toad.func_70906_o()) {
         this.legLeft.field_78795_f = -0.3926991F;
         this.legRight.field_78795_f = this.legLeft.field_78795_f;
      } else {
         this.legLeft.field_78795_f = MathHelper.func_76134_b(par2 * 0.6662F) * 1.4F * par3 + (!((double)par3 > 0.1D) && !((double)par3 < -0.1D) ? 0.0F : 0.5F);
         this.legRight.field_78795_f = this.legLeft.field_78795_f;
         this.armLeft.field_78795_f = MathHelper.func_76134_b(par2 * 0.6662F + 3.1415927F) * 1.4F * par3;
         this.armRight.field_78795_f = MathHelper.func_76134_b(par2 * 0.6662F) * 1.4F * par3;
      }

   }
}
