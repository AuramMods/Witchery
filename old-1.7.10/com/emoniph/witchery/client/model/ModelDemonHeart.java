package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelDemonHeart extends ModelBase {
   ModelRenderer Shape1;
   ModelRenderer Shape2;
   ModelRenderer Shape3;
   ModelRenderer Shape4;
   ModelRenderer bigTube1;
   ModelRenderer tube1;
   ModelRenderer tube2;
   ModelRenderer tube3;
   ModelRenderer tube4;
   ModelRenderer tube5;

   public ModelDemonHeart() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.Shape1 = new ModelRenderer(this, 14, 20);
      this.Shape1.func_78789_a(0.0F, 0.0F, 0.0F, 5, 8, 4);
      this.Shape1.func_78793_a(-3.0F, 14.0F, 0.0F);
      this.Shape1.func_78787_b(32, 32);
      this.Shape1.field_78809_i = true;
      this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
      this.Shape2 = new ModelRenderer(this, 0, 7);
      this.Shape2.func_78789_a(0.0F, 0.0F, 0.0F, 3, 8, 6);
      this.Shape2.func_78793_a(-4.0F, 15.0F, -1.0F);
      this.Shape2.func_78787_b(32, 32);
      this.Shape2.field_78809_i = true;
      this.setRotation(this.Shape2, 0.0F, 0.0F, 0.0F);
      this.Shape3 = new ModelRenderer(this, 13, 0);
      this.Shape3.func_78789_a(0.0F, 0.0F, 0.0F, 1, 6, 4);
      this.Shape3.func_78793_a(-5.0F, 16.0F, 0.0F);
      this.Shape3.func_78787_b(32, 32);
      this.Shape3.field_78809_i = true;
      this.setRotation(this.Shape3, 0.0F, 0.0F, 0.0F);
      this.Shape4 = new ModelRenderer(this, 0, 0);
      this.Shape4.func_78789_a(0.0F, 0.0F, 0.0F, 2, 1, 2);
      this.Shape4.func_78793_a(-3.0F, 13.0F, 1.0F);
      this.Shape4.func_78787_b(32, 32);
      this.Shape4.field_78809_i = true;
      this.setRotation(this.Shape4, 0.0F, 0.0F, 0.0F);
      this.bigTube1 = new ModelRenderer(this, 3, 3);
      this.bigTube1.func_78789_a(0.0F, 0.0F, 0.0F, 3, 2, 2);
      this.bigTube1.func_78793_a(-2.0F, 15.0F, -1.0F);
      this.bigTube1.func_78787_b(32, 32);
      this.bigTube1.field_78809_i = true;
      this.setRotation(this.bigTube1, 0.0F, 0.3717861F, 0.2230717F);
      this.tube1 = new ModelRenderer(this, 19, 11);
      this.tube1.func_78789_a(0.0F, -3.0F, 1.0F, 1, 3, 1);
      this.tube1.func_78793_a(-3.0F, 14.0F, 1.0F);
      this.tube1.func_78787_b(32, 32);
      this.tube1.field_78809_i = true;
      this.setRotation(this.tube1, 0.4089647F, 0.6320364F, 0.0F);
      this.tube2 = new ModelRenderer(this, 19, 11);
      this.tube2.func_78789_a(0.0F, -3.0F, 1.0F, 1, 3, 1);
      this.tube2.func_78793_a(-2.0F, 14.0F, 1.0F);
      this.tube2.func_78787_b(32, 32);
      this.tube2.field_78809_i = true;
      this.setRotation(this.tube2, -0.2974289F, -0.2230717F, -0.3346075F);
      this.tube3 = new ModelRenderer(this, 19, 11);
      this.tube3.func_78789_a(0.0F, -3.0F, 1.0F, 1, 3, 1);
      this.tube3.func_78793_a(1.0F, 13.0F, -0.8F);
      this.tube3.func_78787_b(32, 32);
      this.tube3.field_78809_i = true;
      this.setRotation(this.tube3, -0.0743572F, 0.1487144F, -0.2602503F);
      this.tube4 = new ModelRenderer(this, 19, 11);
      this.tube4.func_78789_a(0.0F, -3.0F, 1.0F, 1, 3, 1);
      this.tube4.func_78793_a(0.0F, 15.0F, 0.0F);
      this.tube4.func_78787_b(32, 32);
      this.tube4.field_78809_i = true;
      this.setRotation(this.tube4, 0.2602503F, 0.0F, 0.4089647F);
      this.tube5 = new ModelRenderer(this, 19, 11);
      this.tube5.func_78789_a(0.0F, -3.0F, 1.0F, 1, 3, 1);
      this.tube5.func_78793_a(0.0F, 14.0F, 1.0F);
      this.tube5.func_78787_b(32, 32);
      this.tube5.field_78809_i = true;
      this.setRotation(this.tube5, -0.2602503F, 0.0F, 0.0F);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, long ticks) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.tube1.func_78785_a(f5);
      this.tube2.func_78785_a(f5);
      this.tube3.func_78785_a(f5);
      this.tube4.func_78785_a(f5);
      this.tube5.func_78785_a(f5);
      this.Shape4.func_78785_a(f5);
      GL11.glTranslatef(0.0F, 1.0F, 0.0F);
      double size = 0.165D * (7.0D + 0.25D * Math.sin(0.25132741228718347D * (double)ticks));
      GL11.glScaled(size, size, size);
      GL11.glTranslatef(0.0F, -1.0F, 0.0F);
      this.bigTube1.func_78785_a(f5);
      this.Shape1.func_78785_a(f5);
      this.Shape2.func_78785_a(f5);
      this.Shape3.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      super.func_78087_a(f, f1, f2, f3, f4, f5, entity);
   }
}
