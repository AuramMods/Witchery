package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelHunterClothes extends ModelBiped {
   ModelRenderer coat;
   ModelRenderer hatBrim;
   ModelRenderer hatTop;
   ModelRenderer hatMid;

   public ModelHunterClothes(float scale, boolean shoulders) {
      super(scale, 0.0F, 128, 64);
      float hatScale = 0.52F;
      this.hatBrim = new ModelRenderer(this, 0, 50);
      this.hatBrim.func_78790_a(-6.5F, 0.0F, -6.5F, 13, 1, 13, hatScale - 0.2F);
      this.hatBrim.func_78793_a(0.0F, -6.0F, 0.0F);
      this.hatBrim.func_78787_b(128, 64);
      this.hatBrim.field_78809_i = true;
      this.setRotation(this.hatBrim, 0.0F, 0.0F, 0.0F);
      this.field_78116_c.func_78792_a(this.hatBrim);
      this.hatMid = new ModelRenderer(this, 40, 52);
      this.hatMid.func_78790_a(-4.0F, 0.0F, -4.0F, 8, 2, 8, hatScale);
      this.hatMid.func_78793_a(0.0F, -2.0F, 0.0F);
      this.hatMid.func_78787_b(128, 64);
      this.hatMid.field_78809_i = true;
      this.setRotation(this.hatMid, 0.0F, 0.0F, 0.0F);
      this.hatBrim.func_78792_a(this.hatMid);
      this.hatTop = new ModelRenderer(this, 12, 41);
      this.hatTop.func_78790_a(-3.5F, 0.0F, -3.5F, 7, 2, 7, hatScale);
      this.hatTop.func_78793_a(0.0F, -2.0F, 0.0F);
      this.hatTop.func_78787_b(128, 64);
      this.hatTop.field_78809_i = true;
      this.setRotation(this.hatTop, 0.0F, 0.0F, 0.0F);
      this.hatMid.func_78792_a(this.hatTop);
      this.coat = new ModelRenderer(this, 41, 33);
      this.coat.func_78790_a(-5.5F, 0.0F, -3.0F, 11, 10, 6, -0.3F);
      this.coat.func_78793_a(0.0F, 12.0F, 0.0F);
      this.coat.func_78787_b(128, 64);
      this.coat.field_78809_i = true;
      this.setRotation(this.coat, 0.0F, 0.0F, 0.0F);
      this.field_78115_e.func_78792_a(this.coat);
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
}
