package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelTreefyd extends ModelBase {
   ModelRenderer head;
   ModelRenderer body;
   ModelRenderer ShapeV;
   ModelRenderer leavesH;
   ModelRenderer base;
   ModelRenderer leg3;
   ModelRenderer leg4;
   ModelRenderer leg1;
   ModelRenderer leg2;

   public ModelTreefyd() {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.func_78085_a("head.face", 0, 24);
      this.func_78085_a("head.petals", 0, 0);
      this.func_78085_a("head.tongue", 25, 18);
      this.head = new ModelRenderer(this, "head");
      this.head.func_78793_a(0.0F, 3.0F, 0.0F);
      this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
      this.head.field_78809_i = true;
      this.head.func_78786_a("face", -2.0F, -4.0F, -2.0F, 4, 4, 4);
      this.head.func_78786_a("petals", -5.0F, -7.0F, 0.0F, 10, 10, 0);
      this.head.func_78786_a("tongue", 0.0F, -3.0F, -6.0F, 0, 10, 4);
      this.body = new ModelRenderer(this, 16, 14);
      this.body.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 16, 2);
      this.body.func_78793_a(0.0F, 3.0F, 0.0F);
      this.body.func_78787_b(64, 32);
      this.body.field_78809_i = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.ShapeV = new ModelRenderer(this, 40, 6);
      this.ShapeV.func_78789_a(0.0F, 0.0F, -6.0F, 0, 14, 12);
      this.ShapeV.func_78793_a(0.0F, 6.0F, 0.0F);
      this.ShapeV.func_78787_b(64, 32);
      this.ShapeV.field_78809_i = true;
      this.setRotation(this.ShapeV, 0.0F, 0.7853982F, 0.0F);
      this.leavesH = new ModelRenderer(this, 40, 0);
      this.leavesH.func_78789_a(-6.0F, 0.0F, 0.0F, 12, 14, 0);
      this.leavesH.func_78793_a(0.0F, 6.0F, 0.0F);
      this.leavesH.func_78787_b(64, 32);
      this.leavesH.field_78809_i = true;
      this.setRotation(this.leavesH, 0.0F, 0.7853982F, 0.0F);
      this.base = new ModelRenderer(this, 15, 6);
      this.base.func_78789_a(0.0F, 0.0F, 0.0F, 6, 1, 6);
      this.base.func_78793_a(-3.0F, 19.0F, -3.0F);
      this.base.func_78787_b(64, 32);
      this.base.field_78809_i = true;
      this.setRotation(this.base, 0.0F, 0.0F, 0.0F);
      this.leg3 = new ModelRenderer(this, 0, 16);
      this.leg3.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 4, 4);
      this.leg3.func_78793_a(-2.0F, 20.0F, -4.0F);
      this.leg3.func_78787_b(64, 32);
      this.leg3.field_78809_i = true;
      this.setRotation(this.leg3, 0.0F, 0.0F, 0.0F);
      this.leg4 = new ModelRenderer(this, 0, 16);
      this.leg4.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 4, 4);
      this.leg4.func_78793_a(2.0F, 20.0F, -4.0F);
      this.leg4.func_78787_b(64, 32);
      this.leg4.field_78809_i = true;
      this.setRotation(this.leg4, 0.0F, 0.0F, 0.0F);
      this.leg1 = new ModelRenderer(this, 0, 16);
      this.leg1.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 4, 4);
      this.leg1.func_78793_a(-2.0F, 20.0F, 4.0F);
      this.leg1.func_78787_b(64, 32);
      this.leg1.field_78809_i = true;
      this.setRotation(this.leg1, 0.0F, 0.0F, 0.0F);
      this.leg2 = new ModelRenderer(this, 0, 16);
      this.leg2.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 4, 4);
      this.leg2.func_78793_a(2.0F, 20.0F, 4.0F);
      this.leg2.func_78787_b(64, 32);
      this.leg2.field_78809_i = true;
      this.setRotation(this.leg2, 0.0F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.head.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.ShapeV.func_78785_a(f5);
      this.leavesH.func_78785_a(f5);
      this.base.func_78785_a(f5);
      this.leg3.func_78785_a(f5);
      this.leg4.func_78785_a(f5);
      this.leg1.func_78785_a(f5);
      this.leg2.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
      this.head.field_78796_g = par4 / 57.295776F;
      this.head.field_78795_f = par5 / 57.295776F;
      this.leg1.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 1.4F * par2;
      this.leg2.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 1.4F * par2;
      this.leg3.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F + 3.1415927F) * 1.4F * par2;
      this.leg4.field_78795_f = MathHelper.func_76134_b(par1 * 0.6662F) * 1.4F * par2;
   }
}
