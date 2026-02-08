package com.emoniph.witchery.client.model;

import com.emoniph.witchery.util.RenderUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelMirrorFace extends ModelBase {
   public ModelRenderer bipedHead;

   public ModelMirrorFace() {
      this(0.0F);
   }

   public ModelMirrorFace(float p_i1148_1_) {
      this(p_i1148_1_, 0.0F, 64, 32);
   }

   public ModelMirrorFace(float p_i1149_1_, float p_i1149_2_, int p_i1149_3_, int p_i1149_4_) {
      this.field_78090_t = p_i1149_3_;
      this.field_78089_u = p_i1149_4_;
      this.bipedHead = new ModelRenderer(this, 0, 0);
      this.bipedHead.func_78790_a(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i1149_1_);
      this.bipedHead.func_78793_a(0.0F, 24.0F + p_i1149_2_, 0.0F);
   }

   public void func_78088_a(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
      this.func_78087_a(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, p_78088_1_);
      this.bipedHead.func_78793_a(0.0F, 24.0F, 0.0F);
      float scale = 0.75F;
      GL11.glScalef(scale, scale, scale);
      GL11.glTranslatef(0.0F, 0.4F, 0.0F);
      RenderUtil.blend(true);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
      this.bipedHead.func_78785_a(p_78088_7_);
      RenderUtil.blend(false);
   }

   public void func_78087_a(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
      this.bipedHead.field_78796_g = p_78087_4_ / 57.295776F;
      this.bipedHead.field_78795_f = p_78087_5_ / 57.295776F;
   }
}
