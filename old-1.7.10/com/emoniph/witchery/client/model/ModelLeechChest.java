package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

@SideOnly(Side.CLIENT)
public class ModelLeechChest extends ModelBase {
   public ModelRenderer chestBelow;
   public ModelRenderer chestLidBL;
   public ModelRenderer chestLidFR;
   public ModelRenderer chestLidBR;
   public ModelRenderer chestLidFL;
   public ModelRenderer sac1;
   public ModelRenderer sac2;
   public ModelRenderer sac3;

   public ModelLeechChest() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.chestBelow = new ModelRenderer(this, 0, 0);
      this.chestBelow.func_78789_a(0.0F, 0.0F, 0.0F, 14, 9, 14);
      this.chestBelow.func_78793_a(1.0F, 7.0F, 1.0F);
      this.chestBelow.func_78787_b(64, 64);
      this.chestBelow.field_78809_i = true;
      this.setRotation(this.chestBelow, 0.0F, 0.0F, 0.0F);
      this.chestLidBL = new ModelRenderer(this, 28, 24);
      this.chestLidBL.func_78789_a(-6.0F, -5.0F, -6.0F, 6, 5, 6);
      this.chestLidBL.func_78793_a(14.0F, 7.0F, 14.0F);
      this.chestLidBL.func_78787_b(64, 64);
      this.chestLidBL.field_78809_i = true;
      this.setRotation(this.chestLidBL, 0.0F, 0.0F, 0.0F);
      this.chestLidFR = new ModelRenderer(this, 0, 36);
      this.chestLidFR.func_78789_a(0.0F, -5.0F, 0.0F, 6, 5, 6);
      this.chestLidFR.func_78793_a(2.0F, 7.0F, 2.0F);
      this.chestLidFR.func_78787_b(64, 64);
      this.chestLidFR.field_78809_i = true;
      this.setRotation(this.chestLidFR, 0.0F, 0.0F, 0.0F);
      this.chestLidBR = new ModelRenderer(this, 0, 24);
      this.chestLidBR.func_78789_a(0.0F, -5.0F, -6.0F, 6, 5, 6);
      this.chestLidBR.func_78793_a(2.0F, 7.0F, 14.0F);
      this.chestLidBR.func_78787_b(64, 64);
      this.chestLidBR.field_78809_i = true;
      this.setRotation(this.chestLidBR, 0.0F, 0.0F, 0.0F);
      this.chestLidFL = new ModelRenderer(this, 28, 36);
      this.chestLidFL.func_78789_a(-6.0F, -5.0F, 0.0F, 6, 5, 6);
      this.chestLidFL.func_78793_a(14.0F, 7.0F, 2.0F);
      this.chestLidFL.func_78787_b(64, 64);
      this.chestLidFL.field_78809_i = true;
      this.setRotation(this.chestLidFL, 0.0F, 0.0F, 0.0F);
      this.sac1 = new ModelRenderer(this, 0, 8);
      this.sac1.func_78789_a(0.0F, 0.0F, 0.0F, 2, 3, 1);
      this.sac1.func_78793_a(3.0F, 8.0F, 0.0F);
      this.sac1.func_78787_b(64, 64);
      this.sac1.field_78809_i = true;
      this.setRotation(this.sac1, 0.0F, 0.0F, 0.0F);
      this.sac2 = new ModelRenderer(this, 0, 3);
      this.sac2.func_78789_a(0.0F, 0.0F, 0.0F, 3, 2, 1);
      this.sac2.func_78793_a(9.0F, 13.0F, 0.0F);
      this.sac2.func_78787_b(64, 64);
      this.sac2.field_78809_i = true;
      this.setRotation(this.sac2, 0.0F, 0.0F, 0.0F);
      this.sac3 = new ModelRenderer(this, 0, 0);
      this.sac3.func_78789_a(0.0F, 0.0F, 0.0F, 2, 1, 1);
      this.sac3.func_78793_a(9.0F, 9.0F, 0.0F);
      this.sac3.func_78787_b(64, 64);
      this.sac3.field_78809_i = true;
      this.setRotation(this.sac3, 0.0F, 0.0F, 0.0F);
   }

   public void renderAll(int count) {
      this.chestLidBL.func_78785_a(0.0625F);
      this.chestLidFL.func_78785_a(0.0625F);
      this.chestLidBR.func_78785_a(0.0625F);
      this.chestLidFR.func_78785_a(0.0625F);
      this.chestBelow.func_78785_a(0.0625F);
      if (count >= 1) {
         this.sac1.func_78785_a(0.0625F);
      }

      if (count >= 2) {
         this.sac2.func_78785_a(0.0625F);
      }

      if (count >= 3) {
         this.sac3.func_78785_a(0.0625F);
      }

   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }
}
