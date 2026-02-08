package com.emoniph.witchery.client.model;

import com.emoniph.witchery.blocks.BlockSilverVat;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelSilverVat extends ModelBase {
   public ModelRenderer base;
   public ModelRenderer sideBack;
   public ModelRenderer sideFront;
   public ModelRenderer sideLeft;
   public ModelRenderer sideRight;
   public ModelRenderer spoutLowerLeft;
   public ModelRenderer spoutUpperLeft;
   public ModelRenderer spoutUpperFront;
   public ModelRenderer spoutLowerFront;
   public ModelRenderer spoutUpperRight;
   public ModelRenderer spoutUpperBack;
   public ModelRenderer spoutLowerRight;
   public ModelRenderer silver1;
   public ModelRenderer spoutLowerBack;
   public ModelRenderer silver2;
   public ModelRenderer silver3;
   public ModelRenderer silver4;
   public ModelRenderer silver5;
   public ModelRenderer silver6;
   public ModelRenderer silver7;
   public ModelRenderer silver8;
   private final ModelRenderer[] silver;
   int capacity;

   public ModelSilverVat() {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.base = new ModelRenderer(this, 0, 19);
      this.base.func_78793_a(-6.0F, 23.0F, -6.0F);
      this.base.func_78790_a(0.0F, 0.0F, 0.0F, 12, 1, 12, 0.0F);
      this.spoutLowerRight = new ModelRenderer(this, 15, 0);
      this.spoutLowerRight.func_78793_a(-5.2F, 16.0F, -0.5F);
      this.spoutLowerRight.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.spoutLowerFront = new ModelRenderer(this, 15, 0);
      this.spoutLowerFront.func_78793_a(-0.5F, 16.0F, -5.2F);
      this.spoutLowerFront.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.silver2 = new ModelRenderer(this, 0, 6);
      this.silver2.func_78793_a(1.6F, 19.0F, -2.1F);
      this.silver2.func_78790_a(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
      this.sideRight = new ModelRenderer(this, 38, 10);
      this.sideRight.func_78793_a(-7.0F, 16.0F, -6.0F);
      this.sideRight.func_78790_a(0.0F, 0.0F, 0.0F, 1, 8, 12, 0.0F);
      this.spoutUpperLeft = new ModelRenderer(this, 15, 3);
      this.spoutUpperLeft.func_78793_a(4.0F, 14.0F, -1.5F);
      this.spoutUpperLeft.func_78790_a(0.0F, 0.0F, 0.0F, 4, 2, 3, 0.0F);
      this.sideFront = new ModelRenderer(this, 34, 0);
      this.sideFront.func_78793_a(-7.0F, 16.0F, -7.0F);
      this.sideFront.func_78790_a(0.0F, 0.0F, 0.0F, 14, 8, 1, 0.0F);
      this.silver7 = new ModelRenderer(this, 0, 22);
      this.silver7.func_78793_a(-0.5F, 19.0F, 2.0F);
      this.silver7.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.spoutUpperRight = new ModelRenderer(this, 15, 3);
      this.spoutUpperRight.func_78793_a(-8.0F, 14.0F, -1.5F);
      this.spoutUpperRight.func_78790_a(0.0F, 0.0F, 0.0F, 4, 2, 3, 0.0F);
      this.sideBack = new ModelRenderer(this, 34, 0);
      this.sideBack.func_78793_a(-7.0F, 16.0F, 6.0F);
      this.sideBack.func_78790_a(0.0F, 0.0F, 0.0F, 14, 8, 1, 0.0F);
      this.spoutUpperFront = new ModelRenderer(this, 15, 9);
      this.spoutUpperFront.func_78793_a(-1.5F, 14.0F, -8.0F);
      this.spoutUpperFront.func_78790_a(0.0F, 0.0F, 0.0F, 3, 2, 4, 0.0F);
      this.spoutLowerLeft = new ModelRenderer(this, 15, 0);
      this.spoutLowerLeft.func_78793_a(4.2F, 16.0F, -0.5F);
      this.spoutLowerLeft.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.silver8 = new ModelRenderer(this, 0, 25);
      this.silver8.func_78793_a(-1.2F, 19.0F, -0.3F);
      this.silver8.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.silver3 = new ModelRenderer(this, 0, 9);
      this.silver3.func_78793_a(-3.8F, 19.1F, 3.1F);
      this.silver3.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
      this.silver6 = new ModelRenderer(this, 0, 19);
      this.silver6.func_78793_a(-4.6F, 19.1F, -1.6F);
      this.silver6.func_78790_a(0.0F, 0.0F, 0.0F, 2, 1, 1, 0.0F);
      this.spoutLowerBack = new ModelRenderer(this, 15, 0);
      this.spoutLowerBack.func_78793_a(-0.5F, 16.0F, 4.2F);
      this.spoutLowerBack.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.silver1 = new ModelRenderer(this, 0, 3);
      this.silver1.func_78793_a(-2.2F, 19.3F, -3.9F);
      this.silver1.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.silver4 = new ModelRenderer(this, 0, 13);
      this.silver4.func_78793_a(-3.4F, 18.8F, 0.9F);
      this.silver4.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.spoutUpperBack = new ModelRenderer(this, 15, 9);
      this.spoutUpperBack.func_78793_a(-1.5F, 14.0F, 4.0F);
      this.spoutUpperBack.func_78790_a(0.0F, 0.0F, 0.0F, 3, 2, 4, 0.0F);
      this.sideLeft = new ModelRenderer(this, 38, 10);
      this.sideLeft.func_78793_a(6.0F, 16.0F, -6.0F);
      this.sideLeft.func_78790_a(0.0F, 0.0F, 0.0F, 1, 8, 12, 0.0F);
      this.silver5 = new ModelRenderer(this, 0, 16);
      this.silver5.func_78793_a(1.6F, 19.0F, -0.1F);
      this.silver5.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
      this.silver = new ModelRenderer[]{this.silver1, this.silver2, this.silver3, this.silver4, this.silver5, this.silver6, this.silver7, this.silver8};
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, BlockSilverVat.TileEntitySilverVat te) {
      this.base.func_78785_a(f5);
      this.sideRight.func_78785_a(f5);
      this.sideFront.func_78785_a(f5);
      this.sideBack.func_78785_a(f5);
      this.sideLeft.func_78785_a(f5);
      boolean isWorld = te != null && te.func_145831_w() != null;
      if (!isWorld || te.func_145831_w().func_147439_a(te.field_145851_c - 1, te.field_145848_d, te.field_145849_e).func_149716_u()) {
         this.spoutUpperLeft.func_78785_a(f5);
         this.spoutLowerLeft.func_78785_a(f5);
      }

      if (!isWorld || te.func_145831_w().func_147439_a(te.field_145851_c + 1, te.field_145848_d, te.field_145849_e).func_149716_u()) {
         this.spoutUpperRight.func_78785_a(f5);
         this.spoutLowerRight.func_78785_a(f5);
      }

      if (!isWorld || te.func_145831_w().func_147439_a(te.field_145851_c, te.field_145848_d, te.field_145849_e - 1).func_149716_u()) {
         this.spoutUpperFront.func_78785_a(f5);
         this.spoutLowerFront.func_78785_a(f5);
      }

      if (!isWorld || te.func_145831_w().func_147439_a(te.field_145851_c, te.field_145848_d, te.field_145849_e + 1).func_149716_u()) {
         this.spoutUpperBack.func_78785_a(f5);
         this.spoutLowerBack.func_78785_a(f5);
      }

      int capacity = isWorld ? (te.func_70301_a(0) != null && te.func_70301_a(0).field_77994_a > 0 ? Math.max(te.func_70301_a(0).field_77994_a / 8, 1) : 0) : 0;

      for(int i = 0; i < capacity; ++i) {
         this.silver[i].func_78785_a(f5);
      }

   }

   public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void setCapactiy(int i) {
      this.capacity = i;
   }
}
