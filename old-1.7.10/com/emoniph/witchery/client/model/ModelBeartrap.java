package com.emoniph.witchery.client.model;

import com.emoniph.witchery.blocks.BlockBeartrap;
import com.emoniph.witchery.util.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelBeartrap extends ModelBase {
   public ModelRenderer plate;
   public ModelRenderer base;
   public ModelRenderer armFront;
   public ModelRenderer armBack;
   public ModelRenderer diskLeft;
   public ModelRenderer diskRight;
   public ModelRenderer armRightFront;
   public ModelRenderer armLeftFront;
   public ModelRenderer armTooth1Front;
   public ModelRenderer armTooth2Front;
   public ModelRenderer armTooth3Front;
   public ModelRenderer armTooth4Front;
   public ModelRenderer armTooth5Front;
   public ModelRenderer armRightBack;
   public ModelRenderer armLeftBack;
   public ModelRenderer armTooth1Back;
   public ModelRenderer armTooth2Back;
   public ModelRenderer armTooth3Back;
   public ModelRenderer armTooth4Back;
   public ModelRenderer armTooth5Back;

   public ModelBeartrap() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.armTooth4Back = new ModelRenderer(this, 0, 0);
      this.armTooth4Back.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth4Back.func_78790_a(1.5F, -2.0F, 6.0F, 1, 1, 1, 0.0F);
      this.plate = new ModelRenderer(this, 1, 0);
      this.plate.func_78793_a(0.0F, 24.0F, 0.0F);
      this.plate.func_78790_a(-2.0F, -1.5F, -2.0F, 4, 1, 4, 0.0F);
      this.armFront = new ModelRenderer(this, 0, 9);
      this.armFront.func_78793_a(0.0F, 23.99F, 0.0F);
      this.armFront.func_78790_a(-4.5F, -1.0F, -7.0F, 9, 1, 1, 0.0F);
      this.armTooth2Front = new ModelRenderer(this, 0, 0);
      this.armTooth2Front.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth2Front.func_78790_a(-2.5F, -2.0F, -7.0F, 1, 1, 1, 0.0F);
      this.armLeftFront = new ModelRenderer(this, 0, 12);
      this.armLeftFront.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armLeftFront.func_78790_a(3.5F, -1.0F, -6.0F, 1, 1, 6, 0.0F);
      this.armTooth4Front = new ModelRenderer(this, 0, 0);
      this.armTooth4Front.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth4Front.func_78790_a(1.5F, -2.0F, -7.0F, 1, 1, 1, 0.0F);
      this.armLeftBack = new ModelRenderer(this, 0, 12);
      this.armLeftBack.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armLeftBack.func_78790_a(3.5F, -1.0F, 0.0F, 1, 1, 6, 0.0F);
      this.armTooth3Front = new ModelRenderer(this, 0, 0);
      this.armTooth3Front.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth3Front.func_78790_a(-0.5F, -2.0F, -7.0F, 1, 1, 1, 0.0F);
      this.armTooth3Back = new ModelRenderer(this, 0, 0);
      this.armTooth3Back.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth3Back.func_78790_a(-0.5F, -2.0F, 6.0F, 1, 1, 1, 0.0F);
      this.armTooth5Front = new ModelRenderer(this, 0, 0);
      this.armTooth5Front.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth5Front.func_78790_a(3.5F, -2.0F, -7.0F, 1, 1, 1, 0.0F);
      this.base = new ModelRenderer(this, 0, 20);
      this.base.func_78793_a(0.0F, 23.99F, 0.0F);
      this.base.func_78790_a(-5.0F, -1.0F, -0.5F, 10, 1, 1, 0.0F);
      this.armBack = new ModelRenderer(this, 0, 9);
      this.armBack.func_78793_a(0.0F, 23.99F, 0.0F);
      this.armBack.func_78790_a(-4.5F, -1.0F, 6.0F, 9, 1, 1, 0.0F);
      this.diskLeft = new ModelRenderer(this, 19, 3);
      this.diskLeft.func_78793_a(0.0F, 24.0F, 0.0F);
      this.diskLeft.func_78790_a(3.7F, -2.0F, -1.0F, 1, 2, 2, 0.0F);
      this.armTooth2Back = new ModelRenderer(this, 0, 0);
      this.armTooth2Back.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth2Back.func_78790_a(-2.5F, -2.0F, 6.0F, 1, 1, 1, 0.0F);
      this.armTooth1Back = new ModelRenderer(this, 0, 0);
      this.armTooth1Back.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth1Back.func_78790_a(-4.5F, -2.0F, 6.0F, 1, 1, 1, 0.0F);
      this.armTooth5Back = new ModelRenderer(this, 0, 0);
      this.armTooth5Back.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth5Back.func_78790_a(3.5F, -2.0F, 6.0F, 1, 1, 1, 0.0F);
      this.armRightBack = new ModelRenderer(this, 0, 12);
      this.armRightBack.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armRightBack.func_78790_a(-4.5F, -1.0F, 0.0F, 1, 1, 6, 0.0F);
      this.armTooth1Front = new ModelRenderer(this, 0, 0);
      this.armTooth1Front.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armTooth1Front.func_78790_a(-4.5F, -2.0F, -7.0F, 1, 1, 1, 0.0F);
      this.armRightFront = new ModelRenderer(this, 0, 12);
      this.armRightFront.func_78793_a(0.0F, 0.0F, 0.0F);
      this.armRightFront.func_78790_a(-4.5F, -1.0F, -6.0F, 1, 1, 6, 0.0F);
      this.diskRight = new ModelRenderer(this, 19, 3);
      this.diskRight.func_78793_a(0.0F, 24.0F, 0.0F);
      this.diskRight.func_78790_a(-4.7F, -2.0F, -1.0F, 1, 2, 2, 0.0F);
      this.armBack.func_78792_a(this.armTooth4Back);
      this.armFront.func_78792_a(this.armTooth2Front);
      this.armFront.func_78792_a(this.armLeftFront);
      this.armFront.func_78792_a(this.armTooth4Front);
      this.armBack.func_78792_a(this.armLeftBack);
      this.armFront.func_78792_a(this.armTooth3Front);
      this.armBack.func_78792_a(this.armTooth3Back);
      this.armFront.func_78792_a(this.armTooth5Front);
      this.armBack.func_78792_a(this.armTooth2Back);
      this.armBack.func_78792_a(this.armTooth1Back);
      this.armBack.func_78792_a(this.armTooth5Back);
      this.armBack.func_78792_a(this.armRightBack);
      this.armFront.func_78792_a(this.armTooth1Front);
      this.armFront.func_78792_a(this.armRightFront);
   }

   private void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.field_78795_f = x;
      modelRenderer.field_78796_g = y;
      modelRenderer.field_78808_h = z;
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, BlockBeartrap.TileEntityBeartrap tile) {
      boolean inWorld = tile != null && tile.func_145831_w() != null;
      if (inWorld && !tile.isVisibleTo(Minecraft.func_71410_x().field_71439_g)) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, Config.instance().mantrapAlpha);
      }

      this.base.func_78785_a(f5);
      this.diskLeft.func_78785_a(f5);
      this.diskRight.func_78785_a(f5);
      if (inWorld && tile.isSprung()) {
         this.plate.field_78797_d = 23.8F;
      } else {
         this.plate.field_78797_d = 23.2F;
      }

      this.plate.func_78785_a(f5);
      if (inWorld && tile.isSprung()) {
         this.armFront.field_78795_f = -1.2F;
      } else {
         this.armFront.field_78795_f = 0.0F;
      }

      this.armFront.func_78785_a(f5);
      if (inWorld && tile.isSprung()) {
         this.armBack.field_78795_f = 1.2F;
      } else {
         this.armBack.field_78795_f = 0.0F;
      }

      this.armBack.func_78785_a(f5);
   }
}
