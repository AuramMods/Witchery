package com.emoniph.witchery.client.model;

import com.emoniph.witchery.blocks.BlockFetish;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelFetishScarecrow extends ModelBase {
   ModelRenderer poleVertical;
   ModelRenderer poleHorizontal;
   ModelRenderer head;
   ModelRenderer headInner;
   ModelRenderer body;
   ModelRenderer armLeft;
   ModelRenderer armRight;
   ModelRenderer armLeftInner;
   ModelRenderer armRightInner;

   public ModelFetishScarecrow() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.poleVertical = new ModelRenderer(this, 0, 2);
      this.poleVertical.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 15, 2);
      this.poleVertical.func_78793_a(0.0F, 9.0F, 0.0F);
      this.poleVertical.func_78787_b(64, 64);
      this.poleVertical.field_78809_i = true;
      this.setRotation(this.poleVertical, 0.0F, 0.0F, 0.0F);
      this.poleHorizontal = new ModelRenderer(this, 0, 0);
      this.poleHorizontal.func_78789_a(-8.0F, 0.0F, -0.5F, 16, 1, 1);
      this.poleHorizontal.func_78793_a(0.0F, 13.0F, 0.0F);
      this.poleHorizontal.func_78787_b(64, 64);
      this.poleHorizontal.field_78809_i = true;
      this.setRotation(this.poleHorizontal, 0.0F, 0.0F, 0.0F);
      this.head = new ModelRenderer(this, 12, 21);
      this.head.func_78789_a(-2.0F, -4.0F, -2.0F, 4, 5, 4);
      this.head.func_78793_a(0.0F, 12.0F, 0.0F);
      this.head.func_78787_b(64, 64);
      this.head.field_78809_i = true;
      this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
      this.headInner = new ModelRenderer(this, 29, 25);
      this.headInner.func_78789_a(-2.0F, -4.0F, -1.9F, 4, 5, 0);
      this.headInner.func_78793_a(0.0F, 12.0F, 0.0F);
      this.headInner.func_78787_b(64, 64);
      this.headInner.field_78809_i = true;
      this.setRotation(this.headInner, 0.0F, 0.0F, 0.0F);
      this.body = new ModelRenderer(this, 8, 2);
      this.body.func_78789_a(-3.0F, 0.0F, -1.5F, 6, 9, 3);
      this.body.func_78793_a(0.0F, 12.5F, 0.0F);
      this.body.func_78787_b(64, 64);
      this.body.field_78809_i = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.armLeft = new ModelRenderer(this, 0, 23);
      this.armLeft.func_78789_a(0.0F, -0.5F, -1.5F, 3, 4, 3);
      this.armLeft.func_78793_a(3.0F, 13.0F, 0.0F);
      this.armLeft.func_78787_b(64, 64);
      this.armLeft.field_78809_i = true;
      this.setRotation(this.armLeft, 0.0F, 0.0F, 0.0F);
      this.armLeftInner = new ModelRenderer(this, 29, 25);
      this.armLeftInner.func_78789_a(2.9F, -0.5F, -1.5F, 0, 4, 3);
      this.armLeftInner.func_78793_a(3.0F, 13.0F, 0.0F);
      this.armLeftInner.func_78787_b(64, 64);
      this.armLeftInner.field_78809_i = true;
      this.setRotation(this.armLeftInner, 0.0F, 0.0F, 0.0F);
      this.armRight = new ModelRenderer(this, 0, 23);
      this.armRight.func_78789_a(-3.0F, -0.5F, -1.5F, 3, 4, 3);
      this.armRight.func_78793_a(-3.0F, 13.0F, 0.0F);
      this.armRight.func_78787_b(64, 64);
      this.armRight.field_78809_i = true;
      this.setRotation(this.armRight, 0.0F, 0.0F, 0.0F);
      this.armRightInner = new ModelRenderer(this, 29, 25);
      this.armRightInner.func_78789_a(-2.9F, -0.5F, -1.5F, 0, 4, 3);
      this.armRightInner.func_78793_a(-3.0F, 13.0F, 0.0F);
      this.armRightInner.func_78787_b(64, 64);
      this.armRightInner.field_78809_i = true;
      this.setRotation(this.armRightInner, 0.0F, 0.0F, 0.0F);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, BlockFetish.TileEntityFetish tile) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.poleVertical.func_78785_a(f5);
      this.poleHorizontal.func_78785_a(f5);
      this.headInner.func_78785_a(f5);
      this.armLeftInner.func_78785_a(f5);
      this.armRightInner.func_78785_a(f5);
      int colorIndex = 9;
      float alpha = 1.0F;
      if (tile != null) {
         int color = tile.getColor();
         if (color >= 0 && color <= 15) {
            colorIndex = color;
         }

         if (tile.isSpectral()) {
            alpha = 0.7F;
         }
      }

      GL11.glColor4f(ModelBroom.fleeceColorTable[colorIndex][0], ModelBroom.fleeceColorTable[colorIndex][1], ModelBroom.fleeceColorTable[colorIndex][2], alpha);
      this.head.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.armLeft.func_78785_a(f5);
      this.armRight.func_78785_a(f5);
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      super.func_78087_a(f, f1, f2, f3, f4, f5, entity);
   }
}
