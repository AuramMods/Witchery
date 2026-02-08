package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityWingedMonkey;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelMonkey extends ModelBase {
   private ModelRenderer tail;
   private ModelRenderer armLeft;
   private ModelRenderer legRight;
   private ModelRenderer bodyShoulder;
   private ModelRenderer body;
   private ModelRenderer legLeft;
   private ModelRenderer head;
   private ModelRenderer armRight;
   private ModelRenderer wingRight;
   private ModelRenderer wingLeft;
   private ModelRenderer headFace;
   private ModelRenderer headNose;
   private ModelRenderer headEarLeft;
   private ModelRenderer headEarRight;

   public ModelMonkey() {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.headEarRight = new ModelRenderer(this, 18, 14);
      this.headEarRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.headEarRight.func_78790_a(-4.5F, -2.5F, -1.5F, 2, 3, 1, 0.0F);
      this.tail = new ModelRenderer(this, 18, 23);
      this.tail.func_78790_a(-0.5F, -7.8F, -0.5F, 1, 8, 1, 0.0F);
      this.tail.func_78793_a(0.0F, 18.5F, 5.3F);
      this.setRotateAngle(this.tail, -0.63739425F, 0.0F, 0.0F);
      this.armRight = new ModelRenderer(this, 0, 19);
      this.armRight.func_78793_a(-3.5F, 14.0F, 0.0F);
      this.armRight.func_78790_a(-2.0F, -1.1F, -1.0F, 2, 11, 2, 0.0F);
      this.setRotateAngle(this.armRight, -0.18203785F, 0.0F, 0.0F);
      this.legRight = new ModelRenderer(this, 9, 25);
      this.legRight.func_78793_a(-1.4F, 19.0F, 4.7F);
      this.legRight.func_78790_a(-1.0F, 0.0F, -1.0F, 2, 5, 2, 0.0F);
      this.bodyShoulder = new ModelRenderer(this, 32, 0);
      this.bodyShoulder.func_78793_a(0.0F, 16.0F, 1.0F);
      this.bodyShoulder.func_78790_a(-3.5F, -3.0F, -3.0F, 7, 5, 7, 0.0F);
      this.setRotateAngle(this.bodyShoulder, 1.5707964F, 0.0F, 0.0F);
      this.wingRight = new ModelRenderer(this, 28, 25);
      this.wingRight.func_78793_a(-1.0F, 14.0F, 2.5F);
      this.wingRight.func_78790_a(-12.0F, -0.5F, -3.0F, 12, 1, 6, 0.0F);
      this.setRotateAngle(this.wingRight, -0.68294734F, 0.3642502F, 0.5462881F);
      this.headEarLeft = new ModelRenderer(this, 18, 14);
      this.headEarLeft.field_78809_i = true;
      this.headEarLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.headEarLeft.func_78790_a(2.5F, -2.5F, -1.5F, 2, 3, 1, 0.0F);
      this.headFace = new ModelRenderer(this, 5, 12);
      this.headFace.func_78793_a(0.0F, 0.0F, 0.0F);
      this.headFace.func_78790_a(-2.5F, -3.5F, -3.5F, 5, 5, 1, 0.0F);
      this.body = new ModelRenderer(this, 36, 12);
      this.body.func_78793_a(0.0F, 15.8F, 2.0F);
      this.body.func_78790_a(-2.5F, -2.0F, -3.0F, 5, 7, 5, 0.0F);
      this.setRotateAngle(this.body, 0.59184116F, 0.0F, 0.0F);
      this.legLeft = new ModelRenderer(this, 9, 25);
      this.legLeft.field_78809_i = true;
      this.legLeft.func_78793_a(1.4F, 19.0F, 4.7F);
      this.legLeft.func_78790_a(-1.0F, 0.0F, -1.0F, 2, 5, 2, 0.0F);
      this.armLeft = new ModelRenderer(this, 0, 19);
      this.armLeft.field_78809_i = true;
      this.armLeft.func_78793_a(4.0F, 14.0F, 0.0F);
      this.armLeft.func_78790_a(-0.5F, -1.0F, -1.0F, 2, 11, 2, 0.0F);
      this.setRotateAngle(this.armLeft, -0.18203785F, 0.0F, 0.0F);
      this.wingLeft = new ModelRenderer(this, 28, 25);
      this.wingLeft.field_78809_i = true;
      this.wingLeft.func_78793_a(1.0F, 14.0F, 2.5F);
      this.wingLeft.func_78790_a(0.0F, -0.5F, -3.0F, 12, 1, 6, 0.0F);
      this.setRotateAngle(this.wingLeft, -0.68294734F, -0.3642502F, -0.5462881F);
      this.head = new ModelRenderer(this, 0, 0);
      this.head.func_78793_a(0.0F, 12.0F, -1.5F);
      this.head.func_78790_a(-3.0F, -4.0F, -3.0F, 6, 6, 5, 0.0F);
      this.headNose = new ModelRenderer(this, 9, 19);
      this.headNose.func_78793_a(0.0F, 0.0F, 0.0F);
      this.headNose.func_78790_a(-2.0F, -1.5F, -4.5F, 4, 3, 1, 0.0F);
      this.head.func_78792_a(this.headEarRight);
      this.head.func_78792_a(this.headEarLeft);
      this.head.func_78792_a(this.headFace);
      this.head.func_78792_a(this.headNose);
   }

   private void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      EntityWingedMonkey entitybat = (EntityWingedMonkey)entity;
      this.armRight.field_78808_h = 0.0F;
      this.armLeft.field_78808_h = 0.0F;
      this.legRight.field_78808_h = 0.0F;
      this.legLeft.field_78808_h = 0.0F;
      boolean landed = entity.field_70181_x == 0.0D && entity.field_70159_w == 0.0D && entity.field_70179_y == 0.0D && ModelOwl.isLanded(entity);
      ModelRenderer var10000;
      if (landed) {
         this.setRotateAngle(this.wingLeft, -0.68294734F, -0.3642502F, -0.5462881F);
         this.setRotateAngle(this.wingRight, -0.68294734F, 0.3642502F, 0.5462881F);
         this.armLeft.field_78795_f = -0.18203785F;
         this.armRight.field_78795_f = -0.18203785F;
         this.legLeft.field_78795_f = 0.0F;
         this.legRight.field_78795_f = 0.0F;
         this.wingLeft.field_78797_d = 14.0F;
         this.wingRight.field_78797_d = 14.0F;
      } else {
         this.wingRight.field_78808_h = MathHelper.func_76134_b(f2 * 0.5F) * 3.1415927F * 0.2F * 2.0F + 0.2F;
         this.wingRight.field_78795_f = 0.0F;
         this.wingRight.field_78797_d = 12.0F;
         this.wingLeft.field_78808_h = -this.wingRight.field_78808_h;
         this.wingLeft.field_78795_f = 0.0F;
         this.wingLeft.field_78797_d = 12.0F;
         this.armLeft.field_78795_f = 0.2F;
         this.armRight.field_78795_f = 0.2F;
         this.legLeft.field_78795_f = 0.1F;
         this.legRight.field_78795_f = 0.1F;
         var10000 = this.armRight;
         var10000.field_78808_h += MathHelper.func_76134_b(f2 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.armLeft;
         var10000.field_78808_h -= MathHelper.func_76134_b(f2 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.armRight;
         var10000.field_78795_f += MathHelper.func_76126_a(f2 * 0.067F) * 0.05F;
         var10000 = this.armLeft;
         var10000.field_78795_f -= MathHelper.func_76126_a(f2 * 0.067F) * 0.05F;
         var10000 = this.legRight;
         var10000.field_78795_f += MathHelper.func_76126_a(f2 * 0.067F) * 0.05F;
         var10000 = this.legLeft;
         var10000.field_78795_f -= MathHelper.func_76126_a(f2 * 0.067F) * 0.05F;
      }

      if (entitybat.func_70906_o()) {
         this.legRight.field_78795_f = -1.3F;
         this.legRight.field_78798_e = 2.0F;
         this.legRight.field_78797_d = 21.0F;
         this.legLeft.field_78795_f = -1.3F;
         this.legLeft.field_78798_e = 2.0F;
         this.legLeft.field_78797_d = 21.0F;
         this.body.field_78795_f = 0.1F;
         this.body.field_78797_d = 17.0F;
         this.tail.func_78793_a(0.0F, 18.5F, 5.3F);
         this.tail.field_78798_e = 4.0F;
         this.tail.field_78797_d = 20.0F;
         this.setRotateAngle(this.tail, -0.9F, 0.0F, 0.0F);
         var10000 = this.armRight;
         var10000.field_78808_h += MathHelper.func_76134_b(f2 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.armLeft;
         var10000.field_78808_h -= MathHelper.func_76134_b(f2 * 0.09F) * 0.05F + 0.05F;
         var10000 = this.armRight;
         var10000.field_78795_f += MathHelper.func_76126_a(f2 * 0.067F) * 0.05F;
         var10000 = this.armLeft;
         var10000.field_78795_f -= MathHelper.func_76126_a(f2 * 0.067F) * 0.05F;
         if (!landed) {
            this.legRight.field_78795_f = 0.0F;
            this.legLeft.field_78795_f = 0.0F;
         }
      } else {
         this.body.field_78795_f = 0.59184116F;
         this.body.func_78793_a(0.0F, 15.8F, 2.0F);
         this.legRight.func_78793_a(-1.4F, 19.0F, 4.7F);
         this.legLeft.func_78793_a(1.4F, 19.0F, 4.7F);
         this.legRight.field_78795_f = 0.0F;
         this.legLeft.field_78795_f = 0.0F;
         this.tail.func_78793_a(0.0F, 18.5F, 5.3F);
         this.setRotateAngle(this.tail, -0.63739425F, 0.0F, 0.0F);
      }

      if ((double)f1 > 0.1D) {
         var10000 = this.tail;
         var10000.field_78795_f = (float)((double)var10000.field_78795_f + ((double)(-f1) - 0.1D));
         var10000 = this.tail;
         var10000.field_78808_h += 5.0F * MathHelper.func_76134_b(f2 * 0.09F) * 0.05F + 0.05F;
      } else {
         var10000 = this.tail;
         var10000.field_78808_h += 3.0F * MathHelper.func_76134_b(f2 * 0.09F) * 0.05F + 0.05F;
      }

      this.head.field_78796_g = f3 / 57.295776F;
      this.head.field_78795_f = f4 / 57.295776F;
      int i = entitybat.getAttackTimer();
      if (i > 0) {
         float di = 10.0F;
         this.armRight.field_78795_f = -2.0F + 1.5F * (Math.abs(((float)i - f3) % 10.0F - di * 0.5F) - di * 0.25F) / (di * 0.25F);
      }

      this.tail.func_78785_a(f5);
      this.armRight.func_78785_a(f5);
      this.legRight.func_78785_a(f5);
      this.bodyShoulder.func_78785_a(f5);
      this.wingRight.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.legLeft.func_78785_a(f5);
      this.armLeft.func_78785_a(f5);
      this.wingLeft.func_78785_a(f5);
      this.head.func_78785_a(f5);
   }
}
