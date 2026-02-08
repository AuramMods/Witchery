package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelEarmuffs extends ModelBiped {
   private ModelRenderer earRight;
   private ModelRenderer earLeft;
   private ModelRenderer bandLeft;
   private ModelRenderer bandTop;
   private ModelRenderer bandRight;

   public ModelEarmuffs() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.bandTop = new ModelRenderer(this, 46, 38);
      this.bandTop.func_78789_a(-4.0F, -10.0F, -0.5F, 8, 1, 1);
      this.bandTop.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bandTop.func_78787_b(64, 64);
      this.bandTop.field_78809_i = true;
      this.setRotation(this.bandTop, 0.0F, 0.0F, 0.0F);
      this.earRight = new ModelRenderer(this, 33, 32);
      this.earRight.func_78789_a(-6.0F, -6.0F, -2.0F, 2, 4, 4);
      this.earRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.earRight.func_78787_b(64, 64);
      this.earRight.field_78809_i = true;
      this.setRotation(this.earRight, 0.0F, 0.0F, 0.0F);
      this.earLeft = new ModelRenderer(this, 33, 32);
      this.earLeft.func_78789_a(4.0F, -6.0F, -2.0F, 2, 4, 4);
      this.earLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.earLeft.func_78787_b(64, 64);
      this.earLeft.field_78809_i = true;
      this.setRotation(this.earLeft, 0.0F, 0.0F, 0.0F);
      this.bandLeft = new ModelRenderer(this, 46, 32);
      this.bandLeft.func_78789_a(4.0F, -10.0F, -0.5F, 1, 4, 1);
      this.bandLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bandLeft.func_78787_b(64, 64);
      this.bandLeft.field_78809_i = true;
      this.setRotation(this.bandLeft, 0.0F, 0.0F, 0.0F);
      this.bandRight = new ModelRenderer(this, 46, 32);
      this.bandRight.func_78789_a(-5.0F, -10.0F, -0.5F, 1, 4, 1);
      this.bandRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bandRight.func_78787_b(64, 64);
      this.bandRight.field_78809_i = true;
      this.setRotation(this.bandRight, 0.0F, 0.0F, 0.0F);
      this.field_78116_c.func_78792_a(this.earRight);
      this.field_78116_c.func_78792_a(this.earLeft);
      this.field_78116_c.func_78792_a(this.bandLeft);
      this.field_78116_c.func_78792_a(this.bandRight);
      this.field_78116_c.func_78792_a(this.bandTop);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
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
