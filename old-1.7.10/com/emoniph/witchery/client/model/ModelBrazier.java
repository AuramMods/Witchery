package com.emoniph.witchery.client.model;

import com.emoniph.witchery.blocks.BlockBrazier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelBrazier extends ModelBase {
   ModelRenderer leg1;
   ModelRenderer leg2;
   ModelRenderer leg3;
   ModelRenderer leg4;
   ModelRenderer foot3;
   ModelRenderer foot2;
   ModelRenderer foot1;
   ModelRenderer foot4;
   ModelRenderer ash;
   ModelRenderer panSide1;
   ModelRenderer panSide2;
   ModelRenderer panSide3;
   ModelRenderer panSide4;
   ModelRenderer footBase;
   ModelRenderer panBase;

   public ModelBrazier() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.leg1 = new ModelRenderer(this, 0, 0);
      this.leg1.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 11, 1);
      this.leg1.func_78793_a(0.7F, 10.0F, -0.74F);
      this.leg1.func_78787_b(64, 64);
      this.leg1.field_78809_i = true;
      this.setRotation(this.leg1, 0.0F, 0.0F, 0.0F);
      this.leg2 = new ModelRenderer(this, 0, 0);
      this.leg2.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 11, 1);
      this.leg2.func_78793_a(-0.7F, 10.0F, -0.7F);
      this.leg2.func_78787_b(64, 64);
      this.leg2.field_78809_i = true;
      this.setRotation(this.leg2, 0.0F, 0.0F, 0.0F);
      this.leg3 = new ModelRenderer(this, 0, 0);
      this.leg3.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 11, 1);
      this.leg3.func_78793_a(-0.7F, 10.0F, 0.7F);
      this.leg3.func_78787_b(64, 64);
      this.leg3.field_78809_i = true;
      this.setRotation(this.leg3, 0.0F, 0.0F, 0.0F);
      this.leg4 = new ModelRenderer(this, 0, 0);
      this.leg4.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 11, 1);
      this.leg4.func_78793_a(0.7F, 10.0F, 0.7F);
      this.leg4.func_78787_b(64, 64);
      this.leg4.field_78809_i = true;
      this.setRotation(this.leg4, 0.0F, 0.0F, 0.0F);
      this.foot3 = new ModelRenderer(this, 0, 13);
      this.foot3.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 5, 1);
      this.foot3.func_78793_a(-0.7F, 21.0F, 0.7F);
      this.foot3.func_78787_b(64, 64);
      this.foot3.field_78809_i = true;
      this.setRotation(this.foot3, 0.7853982F, 0.0F, 0.7853982F);
      this.foot2 = new ModelRenderer(this, 0, 13);
      this.foot2.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 5, 1);
      this.foot2.func_78793_a(-0.7F, 21.0F, -0.7F);
      this.foot2.func_78787_b(64, 64);
      this.foot2.field_78809_i = true;
      this.setRotation(this.foot2, -0.7853982F, 0.0F, 0.7853982F);
      this.foot1 = new ModelRenderer(this, 0, 13);
      this.foot1.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 5, 1);
      this.foot1.func_78793_a(0.7F, 21.0F, -0.7F);
      this.foot1.func_78787_b(64, 64);
      this.foot1.field_78809_i = true;
      this.setRotation(this.foot1, -0.7853982F, 0.0F, -0.7853982F);
      this.foot4 = new ModelRenderer(this, 0, 13);
      this.foot4.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 5, 1);
      this.foot4.func_78793_a(0.7F, 21.0F, 0.7F);
      this.foot4.func_78787_b(64, 64);
      this.foot4.field_78809_i = true;
      this.setRotation(this.foot4, 0.7853982F, 0.0F, -0.7853982F);
      this.ash = new ModelRenderer(this, 0, 20);
      this.ash.func_78789_a(-2.5F, 0.0F, -2.5F, 5, 0, 5);
      this.ash.func_78793_a(0.0F, 9.7F, 0.0F);
      this.ash.func_78787_b(64, 64);
      this.ash.field_78809_i = true;
      this.setRotation(this.ash, 0.0F, 0.0F, 0.0F);
      this.panSide1 = new ModelRenderer(this, 5, 12);
      this.panSide1.func_78789_a(-0.5F, -0.5F, -3.0F, 1, 1, 6);
      this.panSide1.func_78793_a(3.0F, 9.5F, 0.0F);
      this.panSide1.func_78787_b(64, 64);
      this.panSide1.field_78809_i = true;
      this.setRotation(this.panSide1, 0.0F, 0.0F, 0.0F);
      this.panSide2 = new ModelRenderer(this, 4, 26);
      this.panSide2.func_78789_a(-3.0F, -0.5F, -0.5F, 6, 1, 1);
      this.panSide2.func_78793_a(0.0F, 9.5F, 3.0F);
      this.panSide2.func_78787_b(64, 64);
      this.panSide2.field_78809_i = true;
      this.setRotation(this.panSide2, 0.0F, 0.0F, 0.0F);
      this.panSide4 = new ModelRenderer(this, 4, 26);
      this.panSide4.func_78789_a(-3.0F, -0.5F, -0.5F, 6, 1, 1);
      this.panSide4.func_78793_a(0.0F, 9.5F, -3.0F);
      this.panSide4.func_78787_b(64, 64);
      this.panSide4.field_78809_i = true;
      this.setRotation(this.panSide4, 0.0F, 0.0F, 0.0F);
      this.panSide3 = new ModelRenderer(this, 5, 12);
      this.panSide3.func_78789_a(-0.5F, -0.5F, -3.0F, 1, 1, 6);
      this.panSide3.func_78793_a(-3.0F, 9.5F, 0.0F);
      this.panSide3.func_78787_b(64, 64);
      this.panSide3.field_78809_i = true;
      this.setRotation(this.panSide3, 0.0F, 0.0F, 0.0F);
      this.footBase = new ModelRenderer(this, 6, 0);
      this.footBase.func_78789_a(-1.5F, -0.5F, -1.5F, 3, 1, 3);
      this.footBase.func_78793_a(0.0F, 21.0F, 0.0F);
      this.footBase.func_78787_b(64, 64);
      this.footBase.field_78809_i = true;
      this.setRotation(this.footBase, 0.0F, 0.0F, 0.0F);
      this.panBase = new ModelRenderer(this, 6, 5);
      this.panBase.func_78789_a(-3.0F, 0.0F, -3.0F, 6, 1, 6);
      this.panBase.func_78793_a(0.0F, 9.95F, 0.0F);
      this.panBase.func_78787_b(64, 64);
      this.panBase.field_78809_i = true;
      this.setRotation(this.panBase, 0.0F, 0.0F, 0.0F);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, BlockBrazier.TileEntityBrazier tile) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.leg1.func_78785_a(f5);
      this.leg2.func_78785_a(f5);
      this.leg3.func_78785_a(f5);
      this.leg4.func_78785_a(f5);
      this.foot3.func_78785_a(f5);
      this.foot2.func_78785_a(f5);
      this.foot1.func_78785_a(f5);
      this.foot4.func_78785_a(f5);
      this.panSide1.func_78785_a(f5);
      this.panSide2.func_78785_a(f5);
      this.panSide3.func_78785_a(f5);
      this.panSide4.func_78785_a(f5);
      this.footBase.func_78785_a(f5);
      this.panBase.func_78785_a(f5);
      this.panSide4.field_78795_f = 0.0F;
      this.panSide2.field_78795_f = 0.0F;
      this.panSide1.field_78808_h = 0.0F;
      this.panSide3.field_78808_h = 0.0F;
      if (tile != null) {
         int ingredientCount = tile.getIngredientCount();
         if (ingredientCount > 0) {
            this.ash.field_78797_d = 9.7F - (float)(ingredientCount - 1) * 0.1F;
            this.ash.func_78785_a(f5);
         }
      }

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
