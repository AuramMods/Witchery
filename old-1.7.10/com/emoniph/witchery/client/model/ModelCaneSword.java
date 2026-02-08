package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelCaneSword extends ModelBase {
   private ModelRenderer sheath;
   private ModelRenderer ball;
   private ModelRenderer blade1;
   private ModelRenderer blade2;
   private ModelRenderer blade3;

   public ModelCaneSword() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.blade3 = new ModelRenderer(this, 24, 0);
      this.blade3.func_78793_a(-5.8F, 11.0F, -1.6F);
      this.blade3.func_78790_a(-1.0F, -17.0F, -1.0F, 2, 2, 2, 0.0F);
      this.blade2 = new ModelRenderer(this, 24, 5);
      this.blade2.func_78793_a(-5.8F, 11.0F, -1.6F);
      this.blade2.func_78790_a(-1.0F, -15.0F, -1.0F, 2, 4, 2, 0.0F);
      this.ball = new ModelRenderer(this, 0, 0);
      this.ball.func_78793_a(-5.8F, 10.0F, -1.6F);
      this.ball.func_78790_a(-1.5F, -1.0F, -1.5F, 3, 2, 3, 0.0F);
      this.blade1 = new ModelRenderer(this, 24, 12);
      this.blade1.func_78793_a(-5.8F, 8.0F, -1.6F);
      this.blade1.func_78790_a(-1.0F, -8.0F, -1.0F, 2, 9, 2, 0.0F);
      this.sheath = new ModelRenderer(this, 0, 6);
      this.sheath.func_78793_a(-5.8F, 11.0F, -1.6F);
      this.sheath.func_78790_a(-1.0F, 0.0F, -1.0F, 2, 13, 2, 0.0F);
   }

   private void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, boolean firstPerson, boolean deployed) {
      if (deployed) {
         GL11.glPushMatrix();
         GL11.glTranslatef(this.blade3.field_82906_o, this.blade3.field_82908_p, this.blade3.field_82907_q);
         GL11.glTranslatef(this.blade3.field_78800_c * f5, this.blade3.field_78797_d * f5, this.blade3.field_78798_e * f5);
         GL11.glTranslatef(0.0F, 3.15F, 0.0F);
         GL11.glScaled(0.2D, 4.3D, 0.2D);
         GL11.glTranslatef(-this.blade3.field_82906_o, -this.blade3.field_82908_p, -this.blade3.field_82907_q);
         GL11.glTranslatef(-this.blade3.field_78800_c * f5, -this.blade3.field_78797_d * f5, -this.blade3.field_78798_e * f5);
         this.blade3.func_78785_a(f5);
         GL11.glPopMatrix();
         GL11.glPushMatrix();
         GL11.glTranslatef(this.blade2.field_82906_o, this.blade2.field_82908_p, this.blade2.field_82907_q);
         GL11.glTranslatef(this.blade2.field_78800_c * f5, this.blade2.field_78797_d * f5, this.blade2.field_78798_e * f5);
         GL11.glTranslatef(0.0F, 2.1F, 0.0F);
         GL11.glScaled(0.15D, 3.7D, 0.7D);
         GL11.glTranslatef(-this.blade2.field_82906_o, -this.blade2.field_82908_p, -this.blade2.field_82907_q);
         GL11.glTranslatef(-this.blade2.field_78800_c * f5, -this.blade2.field_78797_d * f5, -this.blade2.field_78798_e * f5);
         this.blade2.func_78785_a(f5);
         GL11.glPopMatrix();
         GL11.glPushMatrix();
         GL11.glTranslatef(this.blade1.field_82906_o, this.blade1.field_82908_p, this.blade1.field_82907_q);
         GL11.glTranslatef(this.blade1.field_78800_c * f5, this.blade1.field_78797_d * f5, this.blade1.field_78798_e * f5);
         GL11.glScaled(0.21D, 1.4D, 0.5D);
         GL11.glTranslatef(-this.blade1.field_82906_o, -this.blade1.field_82908_p, -this.blade1.field_82907_q);
         GL11.glTranslatef(-this.blade1.field_78800_c * f5, -this.blade1.field_78797_d * f5, -this.blade1.field_78798_e * f5);
         this.blade1.func_78785_a(f5);
         GL11.glPopMatrix();
      }

      GL11.glPushMatrix();
      GL11.glTranslatef(this.ball.field_82906_o, this.ball.field_82908_p, this.ball.field_82907_q);
      GL11.glTranslatef(this.ball.field_78800_c * f5, this.ball.field_78797_d * f5, this.ball.field_78798_e * f5);
      GL11.glScaled(0.8D, 1.1D, 0.8D);
      GL11.glTranslatef(-this.ball.field_82906_o, -this.ball.field_82908_p, -this.ball.field_82907_q);
      GL11.glTranslatef(-this.ball.field_78800_c * f5, -this.ball.field_78797_d * f5, -this.ball.field_78798_e * f5);
      this.ball.func_78785_a(f5);
      GL11.glPopMatrix();
      if (!deployed) {
         GL11.glPushMatrix();
         GL11.glTranslatef(this.sheath.field_82906_o, this.sheath.field_82908_p, this.sheath.field_82907_q);
         GL11.glTranslatef(this.sheath.field_78800_c * f5, this.sheath.field_78797_d * f5, this.sheath.field_78798_e * f5);
         GL11.glScaled(0.8D, 1.0D, 0.8D);
         GL11.glTranslatef(-this.sheath.field_82906_o, -this.sheath.field_82908_p, -this.sheath.field_82907_q);
         GL11.glTranslatef(-this.sheath.field_78800_c * f5, -this.sheath.field_78797_d * f5, -this.sheath.field_78798_e * f5);
         this.sheath.func_78785_a(f5);
         GL11.glPopMatrix();
      }

   }
}
