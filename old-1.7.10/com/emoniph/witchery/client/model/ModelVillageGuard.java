package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelVillageGuard extends ModelBiped {
   public ModelRenderer bipedHeadNose;
   public ModelRenderer bipedBodyRobe;

   public ModelVillageGuard() {
      super(0.0F, 0.0F, 64, 64);
      this.field_78116_c = new ModelRenderer(this, 28, 46);
      this.field_78116_c.func_78793_a(0.0F, 0.0F, 0.0F);
      this.field_78116_c.func_78790_a(-4.0F, -10.0F, -4.0F, 8, 10, 8, 0.0F);
      this.bipedHeadNose = new ModelRenderer(this, 52, 46);
      this.bipedHeadNose.func_78793_a(0.0F, -2.0F, 0.0F);
      this.bipedHeadNose.func_78790_a(-1.0F, -1.0F, -6.0F, 2, 4, 2, 0.0F);
      this.bipedBodyRobe = new ModelRenderer(this, 0, 38);
      this.bipedBodyRobe.func_78793_a(0.0F, 0.0F, 0.0F);
      this.bipedBodyRobe.func_78790_a(-4.0F, 0.0F, -3.0F, 8, 18, 6, 0.5F);
      this.field_78116_c.func_78792_a(this.bipedHeadNose);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.bipedBodyRobe.func_78785_a(f5);
   }

   public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void func_78087_a(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
      super.func_78087_a(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, p_78087_7_);
      ModelRenderer var10000 = this.field_78114_d;
      var10000.field_78797_d += 0.3F;
      this.field_78123_h.field_78795_f = MathHelper.func_76134_b(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_ * 0.5F;
      this.field_78124_i.field_78795_f = MathHelper.func_76134_b(p_78087_1_ * 0.6662F + 3.1415927F) * 1.4F * p_78087_2_ * 0.5F;
      this.field_78123_h.field_78796_g = 0.0F;
      this.field_78124_i.field_78796_g = 0.0F;
   }
}
