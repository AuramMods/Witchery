package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityLilith;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelLilith extends ModelBase {
   public ModelRenderer legRight;
   public ModelRenderer legLeft;
   public ModelRenderer bodyChest;
   public ModelRenderer bodyWaist;
   public ModelRenderer skirt1;
   public ModelRenderer skirt2;
   public ModelRenderer bodyShoulders;
   public ModelRenderer armRight;
   public ModelRenderer armLeft;
   public ModelRenderer neck;
   public ModelRenderer head;
   public ModelRenderer legRightLower;
   public ModelRenderer legLeftLower;
   public ModelRenderer armRightLower;
   public ModelRenderer armRightWing;
   public ModelRenderer armLeftLower;
   public ModelRenderer armLeftWing;
   public ModelRenderer head2;
   public ModelRenderer hornRight;
   public ModelRenderer hornLeft;
   public ModelRenderer nose;
   public ModelRenderer toothRight;
   public ModelRenderer toothLeft;
   public ModelRenderer head3;

   public ModelLilith() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.neck = new ModelRenderer(this, 24, 0);
      this.neck.func_78793_a(0.0F, -13.0F, 0.0F);
      this.neck.func_78790_a(-1.5F, -1.5F, -1.5F, 3, 2, 3, 0.0F);
      this.legLeftLower = new ModelRenderer(this, 48, 47);
      this.legLeftLower.field_78809_i = true;
      this.legLeftLower.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legLeftLower.func_78790_a(-2.0F, 8.0F, 2.0F, 4, 13, 4, 0.0F);
      this.hornLeft = new ModelRenderer(this, 52, 30);
      this.hornLeft.field_78809_i = true;
      this.hornLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.hornLeft.func_78790_a(1.0F, -12.3F, 0.0F, 6, 10, 0, 0.0F);
      this.setRotateAngle(this.hornLeft, -0.18203785F, 0.0F, 0.0F);
      this.bodyChest = new ModelRenderer(this, 17, 17);
      this.bodyChest.func_78793_a(0.0F, -9.8F, -1.9F);
      this.bodyChest.func_78790_a(-4.0F, -1.5F, -1.5F, 8, 3, 3, 0.0F);
      this.setRotateAngle(this.bodyChest, 0.7853982F, 0.0F, 0.0F);
      this.nose = new ModelRenderer(this, 41, 0);
      this.nose.func_78793_a(0.0F, 0.0F, 0.0F);
      this.nose.func_78790_a(-0.5F, -3.6F, -4.0F, 1, 2, 1, 0.0F);
      this.armLeftLower = new ModelRenderer(this, 8, 25);
      this.armLeftLower.field_78809_i = true;
      this.armLeftLower.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armLeftLower.func_78790_a(-0.5F, 9.8F, 0.8F, 3, 13, 3, 0.0F);
      this.setRotateAngle(this.armLeftLower, -0.22759093F, 0.0F, 0.0F);
      this.skirt1 = new ModelRenderer(this, 0, 49);
      this.skirt1.func_78793_a(0.0F, -0.9F, 0.0F);
      this.skirt1.func_78790_a(-4.5F, 0.0F, -2.5F, 9, 10, 5, 0.0F);
      this.skirt2 = new ModelRenderer(this, 0, 49);
      this.skirt2.func_78793_a(0.0F, -0.9F, 0.0F);
      this.skirt2.func_78790_a(-4.5F, 0.0F, -2.5F, 9, 10, 5, 0.0F);
      this.armLeftWing = new ModelRenderer(this, 0, 13);
      this.armLeftWing.field_78809_i = true;
      this.armLeftWing.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armLeftWing.func_78790_a(1.0F, -19.6F, -12.7F, 0, 30, 4, 0.0F);
      this.setRotateAngle(this.armLeftWing, 2.5497515F, 0.17453292F, 0.0F);
      this.legRightLower = new ModelRenderer(this, 48, 47);
      this.legRightLower.func_78793_a(0.0F, 0.0F, 0.0F);
      this.legRightLower.func_78790_a(-2.0F, 8.0F, 2.0F, 4, 13, 4, 0.0F);
      this.armLeft = new ModelRenderer(this, 0, 0);
      this.armLeft.field_78809_i = true;
      this.armLeft.func_78793_a(4.4F, -11.5F, 0.0F);
      this.armLeft.func_78790_a(-0.5F, -1.5F, -1.5F, 3, 13, 3, 0.0F);
      this.hornRight = new ModelRenderer(this, 52, 30);
      this.hornRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.hornRight.func_78790_a(-7.0F, -12.3F, 0.0F, 6, 10, 0, 0.0F);
      this.setRotateAngle(this.hornRight, -0.18203785F, 0.0F, 0.0F);
      this.legLeft = new ModelRenderer(this, 36, 30);
      this.legLeft.field_78809_i = true;
      this.legLeft.func_78793_a(2.1F, 2.5F, 0.0F);
      this.legLeft.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 13, 4, 0.0F);
      this.setRotateAngle(this.legLeft, -0.27314404F, 0.0F, 0.0F);
      this.bodyShoulders = new ModelRenderer(this, 15, 6);
      this.bodyShoulders.func_78793_a(0.0F, -12.7F, 0.0F);
      this.bodyShoulders.func_78790_a(-4.0F, 0.0F, -2.0F, 8, 6, 4, 0.0F);
      this.armRightLower = new ModelRenderer(this, 8, 25);
      this.armRightLower.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armRightLower.func_78790_a(-2.5F, 9.8F, 0.8F, 3, 13, 3, 0.0F);
      this.setRotateAngle(this.armRightLower, -0.22759093F, 0.0F, 0.0F);
      this.head3 = new ModelRenderer(this, 44, 22);
      this.head3.func_78793_a(0.0F, 0.0F, 0.0F);
      this.head3.func_78790_a(-2.0F, -4.7F, 5.6F, 4, 4, 4, 0.0F);
      this.head2 = new ModelRenderer(this, 42, 12);
      this.head2.func_78793_a(0.0F, 0.0F, 0.0F);
      this.head2.func_78790_a(-2.5F, -5.5F, 1.0F, 5, 5, 5, 0.0F);
      this.setRotateAngle(this.head2, -0.18203785F, 0.0F, 0.0F);
      this.armRightWing = new ModelRenderer(this, 0, 13);
      this.armRightWing.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armRightWing.func_78790_a(-1.0F, -19.6F, -12.7F, 0, 30, 4, 0.0F);
      this.setRotateAngle(this.armRightWing, 2.5497515F, -0.17453292F, 0.0F);
      this.legRight = new ModelRenderer(this, 36, 30);
      this.legRight.func_78793_a(-2.1F, 2.5F, 0.0F);
      this.legRight.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 13, 4, 0.0F);
      this.setRotateAngle(this.legRight, -0.27314404F, 0.0F, 0.0F);
      this.armRight = new ModelRenderer(this, 0, 0);
      this.armRight.func_78793_a(-4.5F, -11.5F, 0.0F);
      this.armRight.func_78790_a(-2.5F, -1.5F, -1.5F, 3, 13, 3, 0.0F);
      this.toothLeft = new ModelRenderer(this, 20, 0);
      this.toothLeft.field_78809_i = true;
      this.toothLeft.func_78793_a(0.0F, 0.0F, 0.0F);
      this.toothLeft.func_78790_a(0.5F, -1.6F, -3.6F, 1, 3, 1, -0.35F);
      this.toothRight = new ModelRenderer(this, 20, 0);
      this.toothRight.func_78793_a(0.0F, 0.0F, 0.0F);
      this.toothRight.func_78790_a(-1.5F, -1.6F, -3.6F, 1, 3, 1, -0.35F);
      this.bodyWaist = new ModelRenderer(this, 20, 24);
      this.bodyWaist.func_78793_a(0.0F, -7.5F, 0.0F);
      this.bodyWaist.func_78790_a(-3.0F, 0.0F, -1.0F, 6, 10, 2, 0.0F);
      this.head = new ModelRenderer(this, 40, 0);
      this.head.func_78793_a(0.0F, -13.5F, 0.0F);
      this.head.func_78790_a(-3.0F, -6.0F, -3.0F, 6, 6, 6, 0.0F);
      this.legLeft.func_78792_a(this.legLeftLower);
      this.head.func_78792_a(this.hornLeft);
      this.head.func_78792_a(this.nose);
      this.armLeft.func_78792_a(this.armLeftLower);
      this.armLeft.func_78792_a(this.armLeftWing);
      this.legRight.func_78792_a(this.legRightLower);
      this.head.func_78792_a(this.hornRight);
      this.armRight.func_78792_a(this.armRightLower);
      this.head2.func_78792_a(this.head3);
      this.head.func_78792_a(this.head2);
      this.armRight.func_78792_a(this.armRightWing);
      this.head.func_78792_a(this.toothLeft);
      this.head.func_78792_a(this.toothRight);
   }

   private void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.neck.func_78785_a(f5);
      this.bodyChest.func_78785_a(f5);
      this.skirt1.func_78785_a(f5);
      this.skirt2.func_78785_a(f5);
      this.armLeft.func_78785_a(f5);
      this.legLeft.func_78785_a(f5);
      this.bodyShoulders.func_78785_a(f5);
      this.legRight.func_78785_a(f5);
      this.armRight.func_78785_a(f5);
      this.bodyWaist.func_78785_a(f5);
      this.head.func_78785_a(f5);
   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
      this.head.field_78796_g = par4 / 57.295776F;
      this.head.field_78795_f = par5 / 57.295776F;
      this.armRight.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 2.0F * par2 * 0.5F;
      this.armLeft.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
      this.armRight.field_78808_h = 0.0F;
      this.armLeft.field_78808_h = 0.0F;
      this.legRight.field_78795_f = Math.max(MathHelper.func_76134_b(par1 * 0.6662F) * 1.4F * par2 - 0.27314404F, -0.8F);
      this.legLeft.field_78795_f = Math.max(MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 1.4F * par2 - 0.27314404F, -0.8F);
      this.legRight.field_78796_g = 0.0F;
      this.legLeft.field_78796_g = 0.0F;
      this.skirt1.field_78795_f = Math.min(this.legRight.field_78795_f, this.legLeft.field_78795_f);
      this.skirt2.field_78795_f = Math.max(Math.max(this.legRight.field_78795_f, this.legLeft.field_78795_f), 0.2F);
      ModelRenderer var10000;
      if (this.field_78093_q) {
         var10000 = this.armRight;
         var10000.field_78795_f += -0.62831855F;
         var10000 = this.armLeft;
         var10000.field_78795_f += -0.62831855F;
         this.legRight.field_78795_f = -1.2566371F;
         this.legLeft.field_78795_f = -1.2566371F;
         this.legRight.field_78796_g = 0.31415927F;
         this.legLeft.field_78796_g = -0.31415927F;
      }

      this.armRight.field_78796_g = 0.0F;
      this.armLeft.field_78796_g = 0.0F;
      var10000 = this.armRight;
      var10000.field_78808_h += MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.armLeft;
      var10000.field_78808_h -= MathHelper.func_76134_b(par3 * 0.09F) * 0.05F + 0.05F;
      var10000 = this.armRight;
      var10000.field_78795_f += MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      var10000 = this.armLeft;
      var10000.field_78795_f -= MathHelper.func_76126_a(par3 * 0.067F) * 0.05F;
      EntityLilith entityDemon = (EntityLilith)entity;
      int i = entityDemon.getAttackTimer();
      if (i > 0) {
         float di = 10.0F;
         this.armRight.field_78795_f = -2.0F + 1.5F * (Math.abs(((float)i - par4) % 10.0F - di * 0.5F) - di * 0.25F) / (di * 0.25F);
      }

   }
}
