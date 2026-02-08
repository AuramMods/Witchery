package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelPoppetChest extends ModelBase {
   ModelRenderer table;

   public ModelPoppetChest() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.table = new ModelRenderer(this, 0, 0);
      this.table.func_78789_a(0.0F, 0.0F, 0.0F, 16, 8, 16);
      this.table.func_78793_a(0.0F, 0.0F, 0.0F);
      this.table.func_78787_b(64, 64);
      this.table.field_78809_i = true;
      this.setRotation(this.table, 0.0F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.table.func_78785_a(f5);
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
