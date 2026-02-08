package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityBroom;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelBroom extends ModelBase {
   ModelRenderer handle;
   ModelRenderer bristle1;
   ModelRenderer bristle2;
   ModelRenderer bristle3;
   ModelRenderer bristle4;
   ModelRenderer bristle5;
   ModelRenderer bristle6;
   ModelRenderer bristle7;
   ModelRenderer bristle8;
   ModelRenderer bristle9;
   public static final float[][] fleeceColorTable = new float[][]{{1.0F, 1.0F, 1.0F}, {0.85F, 0.5F, 0.2F}, {0.7F, 0.3F, 0.85F}, {0.4F, 0.6F, 0.85F}, {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.5F, 0.65F}, {0.3F, 0.3F, 0.3F}, {0.6F, 0.6F, 0.6F}, {0.3F, 0.5F, 0.6F}, {0.5F, 0.25F, 0.7F}, {0.2F, 0.3F, 0.7F}, {0.4F, 0.3F, 0.2F}, {0.4F, 0.5F, 0.2F}, {0.6F, 0.2F, 0.2F}, {0.1F, 0.1F, 0.1F}};

   public ModelBroom() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.handle = new ModelRenderer(this, 24, 0);
      this.handle.func_78789_a(-1.0F, -10.0F, -1.0F, 2, 24, 2);
      this.handle.func_78793_a(0.0F, 11.0F, -5.0F);
      this.handle.func_78787_b(32, 32);
      this.handle.field_78809_i = true;
      this.setRotation(this.handle, 1.570796F, 0.0F, 0.0F);
      this.bristle1 = new ModelRenderer(this, 0, 0);
      this.bristle1.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 10);
      this.bristle1.func_78793_a(-1.0F, 10.0F, 9.0F);
      this.bristle1.func_78787_b(32, 32);
      this.bristle1.field_78809_i = true;
      this.setRotation(this.bristle1, 0.1858931F, -0.1487144F, 0.0F);
      this.bristle2 = new ModelRenderer(this, 0, 0);
      this.bristle2.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 10);
      this.bristle2.func_78793_a(1.0F, 12.0F, 9.0F);
      this.bristle2.func_78787_b(32, 32);
      this.bristle2.field_78809_i = true;
      this.setRotation(this.bristle2, -0.1487144F, 0.1858931F, 0.0F);
      this.bristle3 = new ModelRenderer(this, 0, 12);
      this.bristle3.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 9);
      this.bristle3.func_78793_a(1.0F, 10.0F, 9.0F);
      this.bristle3.func_78787_b(32, 32);
      this.bristle3.field_78809_i = true;
      this.setRotation(this.bristle3, 0.2230717F, 0.1858931F, 0.0F);
      this.bristle4 = new ModelRenderer(this, 0, 0);
      this.bristle4.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 10);
      this.bristle4.func_78793_a(0.0F, 10.0F, 9.0F);
      this.bristle4.func_78787_b(32, 32);
      this.bristle4.field_78809_i = true;
      this.setRotation(this.bristle4, 0.2230717F, 0.0743572F, 0.0F);
      this.bristle5 = new ModelRenderer(this, 0, 0);
      this.bristle5.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 10);
      this.bristle5.func_78793_a(-1.0F, 12.0F, 9.0F);
      this.bristle5.func_78787_b(32, 32);
      this.bristle5.field_78809_i = true;
      this.setRotation(this.bristle5, -0.2230717F, -0.1487144F, 0.0F);
      this.bristle6 = new ModelRenderer(this, 0, 0);
      this.bristle6.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 10);
      this.bristle6.func_78793_a(0.0F, 11.0F, 9.0F);
      this.bristle6.func_78787_b(32, 32);
      this.bristle6.field_78809_i = true;
      this.setRotation(this.bristle6, -0.0371786F, 0.0743572F, 0.0F);
      this.bristle7 = new ModelRenderer(this, 0, 0);
      this.bristle7.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 10);
      this.bristle7.func_78793_a(1.0F, 11.0F, 9.0F);
      this.bristle7.func_78787_b(32, 32);
      this.bristle7.field_78809_i = true;
      this.setRotation(this.bristle7, -0.0371786F, 0.2230717F, 0.0F);
      this.bristle8 = new ModelRenderer(this, 0, 12);
      this.bristle8.func_78789_a(-0.5F, -0.5F, 0.0F, 1, 1, 9);
      this.bristle8.func_78793_a(-1.0F, 11.0F, 9.0F);
      this.bristle8.func_78787_b(32, 32);
      this.bristle8.field_78809_i = true;
      this.setRotation(this.bristle8, -0.0743572F, -0.1487144F, 0.0F);
      this.bristle9 = new ModelRenderer(this, 0, 12);
      this.bristle9.func_78789_a(-0.5333334F, -0.5F, 0.0F, 1, 1, 9);
      this.bristle9.func_78793_a(0.0F, 12.0F, 9.0F);
      this.bristle9.func_78787_b(32, 32);
      this.bristle9.field_78809_i = true;
      this.setRotation(this.bristle9, -0.1858931F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.handle.func_78785_a(f5);
      if (entity != null && entity instanceof EntityBroom) {
         int j = ((EntityBroom)entity).getBrushColor();
         if (j < 0 || j > 15) {
            j = 12;
         }

         GL11.glColor3f(fleeceColorTable[j][0], fleeceColorTable[j][1], fleeceColorTable[j][2]);
      }

      this.bristle1.func_78785_a(f5);
      this.bristle2.func_78785_a(f5);
      this.bristle3.func_78785_a(f5);
      this.bristle4.func_78785_a(f5);
      this.bristle5.func_78785_a(f5);
      this.bristle6.func_78785_a(f5);
      this.bristle7.func_78785_a(f5);
      this.bristle8.func_78785_a(f5);
      this.bristle9.func_78785_a(f5);
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
