package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelVillagerBed extends ModelBase {
   public ModelRenderer base;
   public ModelRenderer head;
   public ModelRenderer legFL;
   public ModelRenderer legFR;
   public ModelRenderer legBL;
   public ModelRenderer legBR;

   public ModelVillagerBed() {
      this.field_78090_t = 128;
      this.field_78089_u = 32;
      this.legBR = new ModelRenderer(this, 0, 0);
      this.legBR.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legBR.func_78790_a(-5.0F, 1.0F, 14.0F, 1, 3, 1, 0.0F);
      this.base = new ModelRenderer(this, 0, 0);
      this.base.func_78793_a(0.0F, 0.0F, 0.0F);
      this.base.func_78790_a(-5.0F, 0.0F, -15.0F, 10, 1, 30, 0.0F);
      this.head = new ModelRenderer(this, 0, 4);
      this.head.func_78793_a(0.0F, 0.0F, 0.0F);
      this.head.func_78790_a(-5.0F, -4.0F, 14.0F, 10, 4, 1, 0.0F);
      this.legFL = new ModelRenderer(this, 0, 0);
      this.legFL.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legFL.func_78790_a(4.0F, 1.0F, -15.0F, 1, 3, 1, 0.0F);
      this.legBL = new ModelRenderer(this, 0, 0);
      this.legBL.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legBL.func_78790_a(4.0F, 1.0F, 14.0F, 1, 3, 1, 0.0F);
      this.legFR = new ModelRenderer(this, 0, 0);
      this.legFR.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legFR.func_78790_a(-5.0F, 1.0F, -15.0F, 1, 3, 1, 0.0F);
      this.base.func_78792_a(this.legBR);
      this.base.func_78792_a(this.head);
      this.base.func_78792_a(this.legFL);
      this.base.func_78792_a(this.legBL);
      this.base.func_78792_a(this.legFR);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.base.func_78785_a(f5);
   }

   public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }
}
