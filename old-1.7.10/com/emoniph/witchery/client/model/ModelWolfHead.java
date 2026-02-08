package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelWolfHead extends ModelBase {
   public ModelRenderer skeletonHead;

   public ModelWolfHead() {
      this(0, 35, 64, 32);
   }

   public ModelWolfHead(int textureOffsetX, int textureoffsetY, int textureWidth, int textureHeight) {
      this.field_78090_t = textureWidth;
      this.field_78089_u = textureHeight;
      this.skeletonHead = new ModelRenderer(this, 0, 0);
      this.skeletonHead.func_78790_a(-3.0F, -6.0F, 0.0F, 6, 6, 4, 0.0F);
      this.skeletonHead.func_78793_a(0.0F, 0.0F, 0.0F);
      float f = 0.0F;
      this.skeletonHead.func_78784_a(16, 14).func_78790_a(-3.0F, -8.0F, 3.0F, 2, 2, 1, f);
      this.skeletonHead.func_78784_a(16, 14).func_78790_a(1.0F, -8.0F, 3.0F, 2, 2, 1, f);
      this.skeletonHead.func_78784_a(0, 10).func_78790_a(-1.5F, -3.1F, -3.0F, 3, 3, 4, f);
   }

   public void func_78088_a(Entity entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
      this.func_78087_a(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, entity);
      this.skeletonHead.func_78785_a(p_78088_7_);
   }

   public void func_78087_a(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
      super.func_78087_a(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, p_78087_7_);
      this.skeletonHead.field_78796_g = p_78087_4_ / 57.295776F;
      this.skeletonHead.field_78795_f = p_78087_5_ / 57.295776F;
   }
}
