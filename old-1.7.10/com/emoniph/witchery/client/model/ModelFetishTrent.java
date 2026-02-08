package com.emoniph.witchery.client.model;

import com.emoniph.witchery.blocks.BlockFetish;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelFetishTrent extends ModelBase {
   ModelRenderer body;
   ModelRenderer armLeft;
   ModelRenderer armRight;
   ModelRenderer legLeftUpper;
   ModelRenderer legLeftLower;
   ModelRenderer legRightUpper;
   ModelRenderer legRightLower;
   ModelRenderer headdress1;
   ModelRenderer headdress2;
   ModelRenderer headdress3;
   ModelRenderer face;

   public ModelFetishTrent() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.body = new ModelRenderer(this, 0, 14);
      this.body.func_78789_a(-3.0F, 0.0F, -3.0F, 6, 9, 6);
      this.body.func_78793_a(0.0F, 12.0F, 0.0F);
      this.body.func_78787_b(64, 64);
      this.body.field_78809_i = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.face = new ModelRenderer(this, 18, 1);
      this.face.func_78789_a(-3.0F, 1.0F, -2.9F, 6, 7, 0);
      this.face.func_78793_a(0.0F, 12.0F, 0.0F);
      this.face.func_78787_b(64, 64);
      this.face.field_78809_i = true;
      this.setRotation(this.face, 0.0F, 0.0F, 0.0F);
      this.armLeft = new ModelRenderer(this, 0, 0);
      this.armLeft.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 5, 2);
      this.armLeft.func_78793_a(2.0F, 13.0F, 0.0F);
      this.armLeft.func_78787_b(64, 64);
      this.armLeft.field_78809_i = true;
      this.setRotation(this.armLeft, -0.1858931F, 0.0F, -0.7435722F);
      this.armRight = new ModelRenderer(this, 0, 0);
      this.armRight.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 5, 2);
      this.armRight.func_78793_a(-2.0F, 13.0F, 0.0F);
      this.armRight.func_78787_b(64, 64);
      this.armRight.field_78809_i = true;
      this.setRotation(this.armRight, -0.1858931F, 0.0F, 0.8551081F);
      this.legLeftUpper = new ModelRenderer(this, 9, 0);
      this.legLeftUpper.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 5, 2);
      this.legLeftUpper.func_78793_a(2.0F, 18.0F, 0.0F);
      this.legLeftUpper.func_78787_b(64, 64);
      this.legLeftUpper.field_78809_i = true;
      this.setRotation(this.legLeftUpper, -0.1487144F, 0.0F, -0.2602503F);
      this.legLeftLower = new ModelRenderer(this, 11, 8);
      this.legLeftLower.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 3, 1);
      this.legLeftLower.func_78793_a(3.0F, 21.0F, -0.5F);
      this.legLeftLower.func_78787_b(64, 64);
      this.legLeftLower.field_78809_i = true;
      this.setRotation(this.legLeftLower, 0.0743572F, 0.0F, -0.1115358F);
      this.legRightUpper = new ModelRenderer(this, 9, 0);
      this.legRightUpper.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 5, 2);
      this.legRightUpper.func_78793_a(-2.0F, 18.0F, 0.0F);
      this.legRightUpper.func_78787_b(64, 64);
      this.legRightUpper.field_78809_i = true;
      this.setRotation(this.legRightUpper, 0.1858931F, 0.0F, 0.3346075F);
      this.legRightLower = new ModelRenderer(this, 11, 8);
      this.legRightLower.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 3, 1);
      this.legRightLower.func_78793_a(-3.0F, 21.0F, 0.5F);
      this.legRightLower.func_78787_b(64, 64);
      this.legRightLower.field_78809_i = true;
      this.setRotation(this.legRightLower, 0.1858931F, 0.0F, 0.2230717F);
      this.headdress1 = new ModelRenderer(this, 0, 30);
      this.headdress1.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 5, 2);
      this.headdress1.func_78793_a(0.0F, 13.0F, 1.0F);
      this.headdress1.func_78787_b(64, 64);
      this.headdress1.field_78809_i = true;
      this.setRotation(this.headdress1, 0.1115358F, 0.0F, -2.862753F);
      this.headdress2 = new ModelRenderer(this, 0, 30);
      this.headdress2.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 5, 2);
      this.headdress2.func_78793_a(-1.0F, 13.0F, 0.0F);
      this.headdress2.func_78787_b(64, 64);
      this.headdress2.field_78809_i = true;
      this.setRotation(this.headdress2, 0.3717861F, 0.0F, 2.639681F);
      this.headdress3 = new ModelRenderer(this, 0, 30);
      this.headdress3.func_78789_a(-1.0F, 0.0F, -1.0F, 2, 5, 2);
      this.headdress3.func_78793_a(-1.0F, 13.0F, 0.0F);
      this.headdress3.func_78787_b(64, 64);
      this.headdress3.field_78809_i = true;
      this.setRotation(this.headdress3, -0.4461433F, 0.0F, 2.862753F);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, BlockFetish.TileEntityFetish tile) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.body.func_78785_a(f5);
      this.armLeft.func_78785_a(f5);
      this.armRight.func_78785_a(f5);
      this.legLeftUpper.func_78785_a(f5);
      this.legLeftLower.func_78785_a(f5);
      this.legRightUpper.func_78785_a(f5);
      this.legRightLower.func_78785_a(f5);
      this.headdress1.func_78785_a(f5);
      this.headdress2.func_78785_a(f5);
      this.headdress3.func_78785_a(f5);
      int colorIndex = 9;
      if (tile != null) {
         int color = tile.getColor();
         if (color >= 0 && color <= 15) {
            colorIndex = color;
         }
      }

      GL11.glColor4f(ModelBroom.fleeceColorTable[colorIndex][0], ModelBroom.fleeceColorTable[colorIndex][1], ModelBroom.fleeceColorTable[colorIndex][2], 1.0F);
      this.face.func_78785_a(f5);
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
