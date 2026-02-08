package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelMindrake extends ModelBase {
   ModelRenderer leaves;
   ModelRenderer bodyTop;
   ModelRenderer bodyBottom;
   ModelRenderer legLeft;
   ModelRenderer legRight;

   public ModelMindrake() {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.leaves = new ModelRenderer(this, 0, 0);
      this.leaves.func_78789_a(-3.0F, 0.0F, 0.0F, 6, 6, 0);
      this.leaves.func_78793_a(0.0F, 13.0F, 0.0F);
      this.leaves.func_78787_b(64, 32);
      this.leaves.field_78809_i = true;
      this.setRotation(this.leaves, 0.0F, 0.0F, 0.0F);
      this.bodyTop = new ModelRenderer(this, 0, 7);
      this.bodyTop.func_78789_a(-1.5F, 0.0F, -1.5F, 3, 1, 3);
      this.bodyTop.func_78793_a(0.0F, 19.0F, 0.0F);
      this.bodyTop.func_78787_b(64, 32);
      this.bodyTop.field_78809_i = true;
      this.setRotation(this.bodyTop, 0.0F, 0.0F, 0.0F);
      this.bodyBottom = new ModelRenderer(this, 0, 12);
      this.bodyBottom.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 3, 4);
      this.bodyBottom.func_78793_a(0.0F, 20.0F, 0.0F);
      this.bodyBottom.func_78787_b(64, 32);
      this.bodyBottom.field_78809_i = true;
      this.setRotation(this.bodyBottom, 0.0F, 0.0F, 0.0F);
      this.legLeft = new ModelRenderer(this, 0, 20);
      this.legLeft.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 1, 1);
      this.legLeft.func_78793_a(1.0F, 23.0F, 0.0F);
      this.legLeft.func_78787_b(64, 32);
      this.legLeft.field_78809_i = true;
      this.setRotation(this.legLeft, 0.0F, 0.0F, 0.0F);
      this.legRight = new ModelRenderer(this, 0, 20);
      this.legRight.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 1, 1);
      this.legRight.func_78793_a(-1.0F, 23.0F, 0.0F);
      this.legRight.func_78787_b(64, 32);
      this.legRight.field_78809_i = true;
      this.setRotation(this.legRight, 0.0F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.leaves.func_78785_a(f5);
      this.bodyTop.func_78785_a(f5);
      this.bodyBottom.func_78785_a(f5);
      this.legLeft.func_78785_a(f5);
      this.legRight.func_78785_a(f5);
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
