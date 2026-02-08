package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelClothesDeath extends ModelBiped {
   ModelRenderer head;
   ModelRenderer body;
   ModelRenderer rightarm;
   ModelRenderer leftarm;
   ModelRenderer rightleg;
   ModelRenderer leftleg;
   ModelRenderer cowl;
   ModelRenderer robe;
   ModelRenderer rightsleave;
   ModelRenderer leftsleave;

   public ModelClothesDeath(float scale) {
      super(0.0F, 0.0F, 128, 64);
      int off = 56;
      this.head = new ModelRenderer(this, 0 + off, 0);
      this.head.func_78790_a(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
      this.head.func_78793_a(0.0F, 0.0F, 0.0F);
      this.head.func_78787_b(128, 64);
      this.head.field_78809_i = true;
      this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
      this.field_78116_c.func_78792_a(this.head);
      this.cowl = new ModelRenderer(this, 0, 45);
      this.cowl.func_78790_a(-4.5F, -8.5F, -4.5F, 9, 10, 9, 0.4F);
      this.cowl.func_78793_a(0.0F, 0.0F, 0.0F);
      this.cowl.func_78787_b(128, 64);
      this.cowl.field_78809_i = true;
      this.setRotation(this.cowl, 0.0F, 0.0F, 0.0F);
      this.field_78116_c.func_78792_a(this.cowl);
      this.body = new ModelRenderer(this, 16 + off, 16);
      this.body.func_78790_a(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F);
      this.body.func_78793_a(0.0F, 0.0F, 0.0F);
      this.body.func_78787_b(128, 64);
      this.body.field_78809_i = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.field_78115_e.func_78792_a(this.body);
      this.rightarm = new ModelRenderer(this, 40 + off, 16);
      this.rightarm.func_78790_a(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.05F);
      this.rightarm.func_78787_b(128, 64);
      this.rightarm.field_78809_i = true;
      this.setRotation(this.rightarm, 0.0F, 0.0F, 0.0F);
      this.field_78112_f.func_78792_a(this.rightarm);
      this.leftarm = new ModelRenderer(this, 40 + off, 16);
      this.leftarm.func_78790_a(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.05F);
      this.leftarm.func_78787_b(128, 64);
      this.leftarm.field_78809_i = true;
      this.setRotation(this.leftarm, 0.0F, 0.0F, 0.0F);
      this.field_78113_g.func_78792_a(this.leftarm);
      this.rightleg = new ModelRenderer(this, 0 + off, 16);
      this.rightleg.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.05F);
      this.rightleg.func_78787_b(128, 64);
      this.rightleg.field_78809_i = true;
      this.setRotation(this.rightleg, 0.0F, 0.0F, 0.0F);
      this.field_78123_h.func_78792_a(this.rightleg);
      this.leftleg = new ModelRenderer(this, 0 + off, 16);
      this.leftleg.func_78790_a(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.05F);
      this.leftleg.func_78787_b(128, 64);
      this.leftleg.field_78809_i = true;
      this.setRotation(this.leftleg, 0.0F, 0.0F, 0.0F);
      this.field_78124_i.func_78792_a(this.leftleg);
      this.robe = new ModelRenderer(this, 36, 37);
      this.robe.func_78790_a(-4.5F, 0.0F, -2.5F, 9, 22, 5, 0.1F);
      this.robe.func_78793_a(0.0F, 0.0F, 0.0F);
      this.robe.func_78787_b(128, 64);
      this.robe.field_78809_i = true;
      this.setRotation(this.robe, 0.0F, 0.0F, 0.0F);
      this.field_78115_e.func_78792_a(this.robe);
      this.rightsleave = new ModelRenderer(this, 64, 50);
      this.rightsleave.func_78790_a(-3.0F, -2.0F, -2.0F, 4, 10, 4, 0.1F);
      this.rightsleave.func_78787_b(128, 64);
      this.rightsleave.field_78809_i = true;
      this.setRotation(this.rightsleave, 0.0F, 0.0F, 0.0F);
      this.field_78112_f.func_78792_a(this.rightsleave);
      this.leftsleave = new ModelRenderer(this, 64, 50);
      this.leftsleave.func_78790_a(-1.0F, -2.0F, -2.0F, 4, 10, 4, 0.1F);
      this.leftsleave.func_78787_b(128, 64);
      this.leftsleave.field_78809_i = true;
      this.setRotation(this.leftsleave, 0.0F, 0.0F, 0.0F);
      this.field_78113_g.func_78792_a(this.leftsleave);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      super.func_78087_a(f, f1, f2, f3, f4, f5, entity);
   }
}
