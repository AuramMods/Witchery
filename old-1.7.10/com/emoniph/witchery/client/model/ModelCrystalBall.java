package com.emoniph.witchery.client.model;

import com.emoniph.witchery.util.RenderUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelCrystalBall extends ModelBase {
   ModelRenderer baseBottom;
   ModelRenderer baseMiddle;
   ModelRenderer baseTop;
   ModelRenderer globeInner;
   ModelRenderer globeMiddle;
   ModelRenderer globeOuter;

   public ModelCrystalBall() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.baseBottom = new ModelRenderer(this, 0, 25);
      this.baseBottom.func_78789_a(0.0F, 0.0F, 0.0F, 6, 1, 6);
      this.baseBottom.func_78793_a(-3.0F, 23.0F, -3.0F);
      this.baseBottom.func_78787_b(32, 32);
      this.baseBottom.field_78809_i = true;
      this.setRotation(this.baseBottom, 0.0F, 0.0F, 0.0F);
      this.baseMiddle = new ModelRenderer(this, 0, 20);
      this.baseMiddle.func_78789_a(0.0F, 0.0F, 0.0F, 4, 1, 4);
      this.baseMiddle.func_78793_a(-2.0F, 22.0F, -2.0F);
      this.baseMiddle.func_78787_b(32, 32);
      this.baseMiddle.field_78809_i = true;
      this.setRotation(this.baseMiddle, 0.0F, 0.0F, 0.0F);
      this.baseTop = new ModelRenderer(this, 0, 17);
      this.baseTop.func_78789_a(0.0F, 0.0F, 0.0F, 2, 1, 2);
      this.baseTop.func_78793_a(-1.0F, 21.0F, -1.0F);
      this.baseTop.func_78787_b(32, 32);
      this.baseTop.field_78809_i = true;
      this.setRotation(this.baseTop, 0.0F, 0.0F, 0.0F);
      this.globeInner = new ModelRenderer(this, 4, 0);
      this.globeInner.func_78789_a(0.0F, 0.0F, 0.0F, 2, 2, 2);
      this.globeInner.func_78793_a(-1.0F, 17.0F, -1.0F);
      this.globeInner.func_78787_b(32, 32);
      this.globeInner.field_78809_i = true;
      this.setRotation(this.globeInner, 0.0F, 0.0F, 0.0F);
      this.globeMiddle = new ModelRenderer(this, 12, 0);
      this.globeMiddle.func_78789_a(0.0F, 0.0F, 0.0F, 4, 4, 4);
      this.globeMiddle.func_78793_a(-2.0F, 16.0F, -2.0F);
      this.globeMiddle.func_78787_b(32, 32);
      this.globeMiddle.field_78809_i = true;
      this.setRotation(this.globeMiddle, 0.0F, 0.0F, 0.0F);
      this.globeOuter = new ModelRenderer(this, 8, 8);
      this.globeOuter.func_78789_a(0.0F, 0.0F, 0.0F, 6, 6, 6);
      this.globeOuter.func_78793_a(-3.0F, 15.0F, -3.0F);
      this.globeOuter.func_78787_b(32, 32);
      this.globeOuter.field_78809_i = true;
      this.setRotation(this.globeOuter, 0.0F, 0.0F, 0.0F);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, TileEntity tile) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.baseBottom.func_78785_a(f5);
      this.baseMiddle.func_78785_a(f5);
      this.baseTop.func_78785_a(f5);
      RenderUtil.blend(true);
      if (tile != null && tile.func_145831_w() != null) {
         long time = tile.func_145831_w().func_72820_D();
         long scale = 100L - Math.abs(time % 160L - 80L);
         GL11.glColor3f(0.01F * (float)scale, 0.01F * (float)scale, 0.01F * (float)scale);
      }

      this.globeInner.func_78785_a(f5);
      GL11.glColor3f(0.8F, 0.8F, 1.0F);
      this.globeMiddle.func_78785_a(f5);
      this.globeOuter.func_78785_a(f5);
      RenderUtil.blend(false);
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      super.func_78087_a(f, f1, f2, f3, f4, f5, entity);
   }
}
