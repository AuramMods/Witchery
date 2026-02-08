package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelCoffin extends ModelBase {
   public ModelRenderer sideLeft;
   public ModelRenderer sideRight;
   public ModelRenderer sideEnd;
   public ModelRenderer base;
   public ModelRenderer baseLower;
   public ModelRenderer lid;
   public ModelRenderer lidMid;
   public ModelRenderer lidTop;

   public ModelCoffin() {
      this.field_78090_t = 128;
      this.field_78089_u = 64;
      this.lid = new ModelRenderer(this, 60, 0);
      this.lid.func_78793_a(-7.0F, -5.0F, 0.0F);
      this.lid.func_78790_a(-1.0F, 0.0F, -8.0F, 16, 1, 16, 0.0F);
      this.lidTop = new ModelRenderer(this, 67, 35);
      this.lidTop.func_78793_a(0.0F, 0.0F, 0.0F);
      this.lidTop.func_78790_a(1.0F, -2.0F, -8.0F, 12, 1, 14, 0.0F);
      this.lidMid = new ModelRenderer(this, 64, 18);
      this.lidMid.func_78793_a(0.0F, 0.0F, 0.0F);
      this.lidMid.func_78790_a(0.0F, -1.0F, -8.0F, 14, 1, 15, 0.0F);
      this.lid.func_78792_a(this.lidTop);
      this.lid.func_78792_a(this.lidMid);
      this.sideEnd = new ModelRenderer(this, 33, 51);
      this.sideEnd.func_78793_a(0.0F, -4.0F, 0.0F);
      this.sideEnd.func_78790_a(-6.0F, 0.0F, 6.0F, 12, 7, 1, 0.0F);
      this.sideRight = new ModelRenderer(this, 0, 37);
      this.sideRight.func_78793_a(0.0F, -4.0F, 0.0F);
      this.sideRight.func_78790_a(-7.0F, 0.0F, -8.0F, 1, 7, 15, 0.0F);
      this.sideLeft = new ModelRenderer(this, 0, 37);
      this.sideLeft.field_78809_i = true;
      this.sideLeft.func_78793_a(0.0F, -4.0F, 0.0F);
      this.sideLeft.func_78790_a(6.0F, 0.0F, -8.0F, 1, 7, 15, 0.0F);
      this.baseLower = new ModelRenderer(this, 0, 20);
      this.baseLower.func_78793_a(0.0F, 8.0F, 0.0F);
      this.baseLower.func_78790_a(-8.0F, 0.0F, -8.0F, 16, 1, 16, 0.0F);
      this.base = new ModelRenderer(this, 0, 0);
      this.base.func_78793_a(0.0F, 3.0F, 0.0F);
      this.base.func_78790_a(-7.0F, 0.0F, -8.0F, 14, 5, 15, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.sideLeft.func_78785_a(f5);
      this.baseLower.func_78785_a(f5);
      this.sideEnd.func_78785_a(f5);
      this.base.func_78785_a(f5);
      this.sideRight.func_78785_a(f5);
      this.lid.func_78785_a(f5);
   }

   public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }
}
