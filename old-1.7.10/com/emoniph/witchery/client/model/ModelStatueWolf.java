package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelStatueWolf extends ModelBase {
   ModelRenderer WolfHead;
   ModelRenderer Body;
   ModelRenderer Mane;
   ModelRenderer Leg1;
   ModelRenderer Leg2;
   ModelRenderer Leg3;
   ModelRenderer Leg4;
   ModelRenderer Tail;
   ModelRenderer Ear1;
   ModelRenderer Ear2;
   ModelRenderer Nose;
   ModelRenderer Shape1;

   public ModelStatueWolf() {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.WolfHead = new ModelRenderer(this, 0, 0);
      this.WolfHead.func_78789_a(-3.0F, -3.0F, -2.0F, 6, 6, 4);
      this.WolfHead.func_78793_a(-0.5F, 13.5F, -7.0F);
      this.WolfHead.func_78787_b(64, 32);
      this.WolfHead.field_78809_i = true;
      this.setRotation(this.WolfHead, 0.0F, 0.0F, 0.0F);
      this.Body = new ModelRenderer(this, 18, 14);
      this.Body.func_78789_a(-4.0F, -2.0F, -3.0F, 6, 9, 6);
      this.Body.func_78793_a(0.5F, 14.0F, 2.0F);
      this.Body.func_78787_b(64, 32);
      this.Body.field_78809_i = true;
      this.setRotation(this.Body, 1.570796F, 0.0F, 0.0F);
      this.Mane = new ModelRenderer(this, 21, 0);
      this.Mane.func_78789_a(-4.0F, -3.0F, -3.0F, 8, 6, 7);
      this.Mane.func_78793_a(-0.5F, 14.0F, -3.0F);
      this.Mane.func_78787_b(64, 32);
      this.Mane.field_78809_i = true;
      this.setRotation(this.Mane, 1.570796F, 0.0F, 0.0F);
      this.Leg1 = new ModelRenderer(this, 0, 18);
      this.Leg1.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 7, 2);
      this.Leg1.func_78793_a(-2.0F, 16.0F, 7.0F);
      this.Leg1.func_78787_b(64, 32);
      this.Leg1.field_78809_i = true;
      this.setRotation(this.Leg1, 0.0F, 0.0F, 0.0F);
      this.Leg2 = new ModelRenderer(this, 0, 18);
      this.Leg2.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 7, 2);
      this.Leg2.func_78793_a(1.0F, 16.0F, 7.0F);
      this.Leg2.func_78787_b(64, 32);
      this.Leg2.field_78809_i = true;
      this.setRotation(this.Leg2, 0.0F, 0.0F, 0.0F);
      this.Leg3 = new ModelRenderer(this, 0, 18);
      this.Leg3.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 7, 2);
      this.Leg3.func_78793_a(-2.0F, 16.0F, -4.0F);
      this.Leg3.func_78787_b(64, 32);
      this.Leg3.field_78809_i = true;
      this.setRotation(this.Leg3, 0.0F, 0.0F, 0.0F);
      this.Leg4 = new ModelRenderer(this, 0, 18);
      this.Leg4.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 7, 2);
      this.Leg4.func_78793_a(1.0F, 16.0F, -4.0F);
      this.Leg4.func_78787_b(64, 32);
      this.Leg4.field_78809_i = true;
      this.setRotation(this.Leg4, 0.0F, 0.0F, 0.0F);
      this.Tail = new ModelRenderer(this, 9, 18);
      this.Tail.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 8, 2);
      this.Tail.func_78793_a(-0.5F, 12.0F, 8.0F);
      this.Tail.func_78787_b(64, 32);
      this.Tail.field_78809_i = true;
      this.setRotation(this.Tail, 1.130069F, 0.0F, 0.0F);
      this.Ear1 = new ModelRenderer(this, 16, 14);
      this.Ear1.func_78789_a(-3.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Ear1.func_78793_a(-0.5F, 13.5F, -7.0F);
      this.Ear1.func_78787_b(64, 32);
      this.Ear1.field_78809_i = true;
      this.setRotation(this.Ear1, 0.0F, 0.0F, 0.0F);
      this.Ear2 = new ModelRenderer(this, 16, 14);
      this.Ear2.func_78789_a(1.0F, -5.0F, 0.0F, 2, 2, 1);
      this.Ear2.func_78793_a(-0.5F, 13.5F, -7.0F);
      this.Ear2.func_78787_b(64, 32);
      this.Ear2.field_78809_i = true;
      this.setRotation(this.Ear2, 0.0F, 0.0F, 0.0F);
      this.Nose = new ModelRenderer(this, 0, 10);
      this.Nose.func_78789_a(-2.0F, 0.0F, -5.0F, 3, 3, 4);
      this.Nose.func_78793_a(0.0F, 13.5F, -7.0F);
      this.Nose.func_78787_b(64, 32);
      this.Nose.field_78809_i = true;
      this.setRotation(this.Nose, 0.0F, 0.0F, 0.0F);
      this.Shape1 = new ModelRenderer(this, 22, 18);
      this.Shape1.func_78789_a(0.0F, 0.0F, 0.0F, 8, 1, 13);
      this.Shape1.func_78793_a(-4.5F, 23.0F, -5.0F);
      this.Shape1.func_78787_b(64, 32);
      this.Shape1.field_78809_i = true;
      this.setRotation(this.Shape1, 0.0F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.WolfHead.func_78785_a(f5);
      this.Body.func_78785_a(f5);
      this.Mane.func_78785_a(f5);
      this.Leg1.func_78785_a(f5);
      this.Leg2.func_78785_a(f5);
      this.Leg3.func_78785_a(f5);
      this.Leg4.func_78785_a(f5);
      this.Tail.func_78785_a(f5);
      this.Ear1.func_78785_a(f5);
      this.Ear2.func_78785_a(f5);
      this.Nose.func_78785_a(f5);
      this.Shape1.func_78785_a(f5);
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
