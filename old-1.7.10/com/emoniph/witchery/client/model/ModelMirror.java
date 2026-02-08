package com.emoniph.witchery.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMirror extends ModelBase {
   public ModelRenderer backMiddle;
   public ModelRenderer backRight;
   public ModelRenderer backLeft;
   public ModelRenderer frameOuterRight;
   public ModelRenderer frameOuterCurveLowerRight;
   public ModelRenderer frameOuterCurveMidRight;
   public ModelRenderer frameOuterCurveUpperRight;
   public ModelRenderer frameOuterTop;
   public ModelRenderer frameOuterCurveUpperLeft;
   public ModelRenderer frameOuterCurveMidLeft;
   public ModelRenderer frameOuterCurveLowerLeft;
   public ModelRenderer frameOuterLeft;
   public ModelRenderer frameInnerRight;
   public ModelRenderer frameInnerCurveLowerRight;
   public ModelRenderer frameInnerCurveUpperRight;
   public ModelRenderer frameInnerTop;
   public ModelRenderer frameInnerCurveUpperRight_1;
   public ModelRenderer frameInnerCurveLowerLeft;
   public ModelRenderer frameInnerLeft;

   public ModelMirror() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.frameInnerRight = new ModelRenderer(this, 5, 5);
      this.frameInnerRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameInnerRight.func_78790_a(-6.0F, -2.0F, -1.0F, 1, 10, 1, 0.0F);
      this.frameOuterCurveLowerLeft = new ModelRenderer(this, 5, 0);
      this.frameOuterCurveLowerLeft.field_78809_i = true;
      this.frameOuterCurveLowerLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterCurveLowerLeft.func_78790_a(7.0F, -5.0F, -1.0F, 1, 2, 1, 0.0F);
      this.frameOuterLeft = new ModelRenderer(this, 0, 0);
      this.frameOuterLeft.field_78809_i = true;
      this.frameOuterLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterLeft.func_78790_a(6.5F, -3.0F, -1.0F, 1, 11, 1, 0.0F);
      this.backRight = new ModelRenderer(this, 0, 17);
      this.backRight.func_78793_a(0.0F, 0.0F, 1.0F);
      this.backRight.func_78790_a(-7.0F, -6.0F, -1.0F, 2, 14, 1, 0.0F);
      this.backLeft = new ModelRenderer(this, 0, 17);
      this.backLeft.field_78809_i = true;
      this.backLeft.func_78793_a(0.0F, 0.0F, 1.0F);
      this.backLeft.func_78790_a(5.0F, -6.0F, -1.0F, 2, 14, 1, 0.0F);
      this.frameOuterTop = new ModelRenderer(this, 4, 3);
      this.frameOuterTop.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterTop.func_78790_a(-5.0F, -7.5F, -1.0F, 10, 1, 1, 0.0F);
      this.frameInnerLeft = new ModelRenderer(this, 5, 5);
      this.frameInnerLeft.field_78809_i = true;
      this.frameInnerLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameInnerLeft.func_78790_a(5.0F, -2.0F, -1.0F, 1, 10, 1, 0.0F);
      this.frameInnerCurveLowerLeft = new ModelRenderer(this, 10, 6);
      this.frameInnerCurveLowerLeft.field_78809_i = true;
      this.frameInnerCurveLowerLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameInnerCurveLowerLeft.func_78790_a(4.0F, -3.0F, -1.0F, 1, 2, 1, 0.0F);
      this.frameInnerCurveLowerRight = new ModelRenderer(this, 10, 6);
      this.frameInnerCurveLowerRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameInnerCurveLowerRight.func_78790_a(-5.0F, -3.0F, -1.0F, 1, 2, 1, 0.0F);
      this.frameOuterRight = new ModelRenderer(this, 0, 0);
      this.frameOuterRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterRight.func_78790_a(-7.5F, -3.0F, -1.0F, 1, 11, 1, 0.0F);
      this.frameOuterCurveUpperLeft = new ModelRenderer(this, 17, 0);
      this.frameOuterCurveUpperLeft.field_78809_i = true;
      this.frameOuterCurveUpperLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterCurveUpperLeft.func_78790_a(5.0F, -7.0F, -1.0F, 1, 1, 1, 0.0F);
      this.frameInnerCurveUpperRight_1 = new ModelRenderer(this, 15, 6);
      this.frameInnerCurveUpperRight_1.field_78809_i = true;
      this.frameInnerCurveUpperRight_1.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameInnerCurveUpperRight_1.func_78790_a(3.0F, -4.0F, -1.0F, 1, 2, 1, 0.0F);
      this.frameOuterCurveLowerRight = new ModelRenderer(this, 5, 0);
      this.frameOuterCurveLowerRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterCurveLowerRight.func_78790_a(-8.0F, -5.0F, -1.0F, 1, 2, 1, 0.0F);
      this.frameInnerTop = new ModelRenderer(this, 10, 10);
      this.frameInnerTop.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameInnerTop.func_78790_a(-3.0F, -4.0F, -1.0F, 6, 1, 1, 0.0F);
      this.frameInnerCurveUpperRight = new ModelRenderer(this, 15, 6);
      this.frameInnerCurveUpperRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameInnerCurveUpperRight.func_78790_a(-4.0F, -4.0F, -1.0F, 1, 2, 1, 0.0F);
      this.frameOuterCurveMidRight = new ModelRenderer(this, 10, 0);
      this.frameOuterCurveMidRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterCurveMidRight.func_78790_a(-7.5F, -6.0F, -1.0F, 2, 1, 1, 0.0F);
      this.frameOuterCurveUpperRight = new ModelRenderer(this, 17, 0);
      this.frameOuterCurveUpperRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterCurveUpperRight.func_78790_a(-6.0F, -7.0F, -1.0F, 1, 1, 1, 0.0F);
      this.frameOuterCurveMidLeft = new ModelRenderer(this, 10, 0);
      this.frameOuterCurveMidLeft.field_78809_i = true;
      this.frameOuterCurveMidLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.frameOuterCurveMidLeft.func_78790_a(5.5F, -6.0F, -1.0F, 2, 1, 1, 0.0F);
      this.backMiddle = new ModelRenderer(this, 7, 16);
      this.backMiddle.func_78793_a(0.0F, 0.0F, 1.0F);
      this.backMiddle.func_78790_a(-5.0F, -7.0F, -1.0F, 10, 15, 1, 0.0F);
   }

   private void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.frameInnerRight.func_78785_a(f5);
      this.frameOuterCurveLowerLeft.func_78785_a(f5);
      this.frameOuterLeft.func_78785_a(f5);
      this.backRight.func_78785_a(f5);
      this.backLeft.func_78785_a(f5);
      this.frameOuterTop.func_78785_a(f5);
      this.frameInnerLeft.func_78785_a(f5);
      this.frameInnerCurveLowerLeft.func_78785_a(f5);
      this.frameInnerCurveLowerRight.func_78785_a(f5);
      this.frameOuterRight.func_78785_a(f5);
      this.frameOuterCurveUpperLeft.func_78785_a(f5);
      this.frameInnerCurveUpperRight_1.func_78785_a(f5);
      this.frameOuterCurveLowerRight.func_78785_a(f5);
      this.frameInnerTop.func_78785_a(f5);
      this.frameInnerCurveUpperRight.func_78785_a(f5);
      this.frameOuterCurveMidRight.func_78785_a(f5);
      this.frameOuterCurveUpperRight.func_78785_a(f5);
      this.frameOuterCurveMidLeft.func_78785_a(f5);
      this.backMiddle.func_78785_a(f5);
   }
}
