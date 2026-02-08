package com.emoniph.witchery.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelGrassper extends ModelBase {
   ModelRenderer stalkTop;
   ModelRenderer leafRight;
   ModelRenderer leafFront;
   ModelRenderer leafback;
   ModelRenderer leafLeft;
   ModelRenderer petalBackRight;
   ModelRenderer stalkBottom;
   ModelRenderer petalFrontRight;
   ModelRenderer petalBackLeft;
   ModelRenderer petalFrontLeft;

   public ModelGrassper() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.stalkTop = new ModelRenderer(this, 0, 4);
      this.stalkTop.func_78789_a(-1.0F, -4.0F, -1.0F, 2, 4, 2);
      this.stalkTop.func_78793_a(2.0F, 21.0F, 0.0F);
      this.stalkTop.func_78787_b(64, 64);
      this.stalkTop.field_78809_i = true;
      this.setRotation(this.stalkTop, 0.0F, 0.0F, -0.5235988F);
      this.leafRight = new ModelRenderer(this, 0, 8);
      this.leafRight.func_78789_a(0.0F, 0.0F, -4.0F, 8, 0, 8);
      this.leafRight.func_78793_a(0.0F, 24.0F, 0.0F);
      this.leafRight.func_78787_b(64, 64);
      this.leafRight.field_78809_i = true;
      this.leafFront = new ModelRenderer(this, 0, 0);
      this.leafFront.func_78789_a(-4.0F, 0.0F, -8.0F, 8, 0, 8);
      this.leafFront.func_78793_a(0.0F, 24.0F, 0.0F);
      this.leafFront.func_78787_b(64, 64);
      this.leafFront.field_78809_i = true;
      this.leafback = new ModelRenderer(this, 0, 0);
      this.leafback.func_78789_a(-4.0F, 0.0F, -8.0F, 8, 0, 8);
      this.leafback.func_78793_a(0.0F, 24.0F, 0.0F);
      this.leafback.func_78787_b(64, 64);
      this.leafback.field_78809_i = true;
      this.leafLeft = new ModelRenderer(this, 0, 8);
      this.leafLeft.func_78789_a(0.0F, 0.0F, -4.0F, 8, 0, 8);
      this.leafLeft.func_78793_a(0.0F, 24.0F, 0.0F);
      this.leafLeft.func_78787_b(64, 64);
      this.leafLeft.field_78809_i = true;
      this.petalBackRight = new ModelRenderer(this, 0, 0);
      this.petalBackRight.func_78789_a(-1.0F, -2.0F, 0.0F, 1, 2, 1);
      this.petalBackRight.func_78793_a(0.0F, 18.0F, 0.0F);
      this.petalBackRight.func_78787_b(64, 64);
      this.petalBackRight.field_78809_i = true;
      this.setRotation(this.petalBackRight, -0.5235988F, 0.0F, -0.7853982F);
      this.stalkBottom = new ModelRenderer(this, 0, 10);
      this.stalkBottom.func_78789_a(-1.0F, -4.0F, -1.0F, 2, 4, 2);
      this.stalkBottom.func_78793_a(0.0F, 24.0F, 0.0F);
      this.stalkBottom.func_78787_b(64, 64);
      this.stalkBottom.field_78809_i = true;
      this.setRotation(this.stalkBottom, 0.0F, 0.0F, 0.5235988F);
      this.petalFrontRight = new ModelRenderer(this, 0, 0);
      this.petalFrontRight.func_78789_a(-1.0F, -2.0F, -1.0F, 1, 2, 1);
      this.petalFrontRight.func_78793_a(0.0F, 18.0F, 0.0F);
      this.petalFrontRight.func_78787_b(64, 64);
      this.petalFrontRight.field_78809_i = true;
      this.setRotation(this.petalFrontRight, 0.5235988F, 0.0F, -0.7853982F);
      this.petalBackLeft = new ModelRenderer(this, 0, 0);
      this.petalBackLeft.func_78789_a(0.0F, -2.0F, 0.0F, 1, 2, 1);
      this.petalBackLeft.func_78793_a(0.0F, 18.0F, 0.0F);
      this.petalBackLeft.func_78787_b(64, 64);
      this.petalBackLeft.field_78809_i = true;
      this.setRotation(this.petalBackLeft, -0.3490659F, 0.0F, 0.2617994F);
      this.petalFrontLeft = new ModelRenderer(this, 0, 0);
      this.petalFrontLeft.func_78789_a(0.0F, -2.0F, -1.0F, 1, 2, 1);
      this.petalFrontLeft.func_78793_a(0.0F, 18.0F, 0.0F);
      this.petalFrontLeft.func_78787_b(64, 64);
      this.petalFrontLeft.field_78809_i = true;
      this.setRotation(this.petalFrontLeft, 0.3490659F, 0.0F, 0.2617994F);
      this.setRotation(this.leafRight, 0.0F, 3.141593F, 0.5235988F);
      this.setRotation(this.leafLeft, 0.0F, 0.0F, -0.5235988F);
      this.setRotation(this.leafFront, -0.5235988F, 0.0F, 0.0F);
      this.setRotation(this.leafback, -0.5235988F, -3.141593F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.stalkTop.func_78785_a(f5);
      this.leafRight.func_78785_a(f5);
      this.leafFront.func_78785_a(f5);
      this.leafback.func_78785_a(f5);
      this.leafLeft.func_78785_a(f5);
      this.petalBackRight.func_78785_a(f5);
      this.stalkBottom.func_78785_a(f5);
      this.petalFrontRight.func_78785_a(f5);
      this.petalBackLeft.func_78785_a(f5);
      this.petalFrontLeft.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
   }
}
