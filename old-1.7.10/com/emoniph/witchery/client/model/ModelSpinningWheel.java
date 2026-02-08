package com.emoniph.witchery.client.model;

import com.emoniph.witchery.blocks.BlockSpinningWheel;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelSpinningWheel extends ModelBase {
   private ModelRenderer seat;
   private ModelRenderer legBackRight;
   private ModelRenderer legBackLeft;
   private ModelRenderer legFrontRight;
   private ModelRenderer legFrontLeft;
   private ModelRenderer thread;
   private ModelRenderer threadPole;
   private ModelRenderer armRight;
   private ModelRenderer armLeft;
   private ModelRenderer wheel;

   public ModelSpinningWheel() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.func_78085_a("wheel.spokes", 0, -6);
      this.func_78085_a("wheel.top", 0, 7);
      this.func_78085_a("wheel.bottom", 0, 7);
      this.func_78085_a("wheel.back", 23, 5);
      this.func_78085_a("wheel.front", 23, 5);
      this.seat = new ModelRenderer(this, 0, 0);
      this.seat.func_78789_a(-2.0F, -1.0F, -7.0F, 4, 1, 14);
      this.seat.func_78793_a(0.0F, 18.0F, 0.0F);
      this.seat.func_78787_b(64, 64);
      this.seat.field_78809_i = true;
      this.setRotation(this.seat, 0.2602503F, 0.0F, 0.0F);
      this.legBackRight = new ModelRenderer(this, 32, 0);
      this.legBackRight.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 9, 1);
      this.legBackRight.func_78793_a(-1.0F, 16.0F, 5.0F);
      this.legBackRight.func_78787_b(64, 64);
      this.legBackRight.field_78809_i = true;
      this.setRotation(this.legBackRight, 0.1745329F, 0.0F, 0.1745329F);
      this.legBackLeft = new ModelRenderer(this, 32, 0);
      this.legBackLeft.func_78789_a(0.0F, 0.0F, 0.0F, 1, 9, 1);
      this.legBackLeft.func_78793_a(1.0F, 16.0F, 5.0F);
      this.legBackLeft.func_78787_b(64, 64);
      this.legBackLeft.field_78809_i = true;
      this.setRotation(this.legBackLeft, 0.1745329F, 0.0F, -0.1745329F);
      this.legFrontRight = new ModelRenderer(this, 0, 6);
      this.legFrontRight.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 6, 1);
      this.legFrontRight.func_78793_a(-1.0F, 19.0F, -6.0F);
      this.legFrontRight.func_78787_b(64, 64);
      this.legFrontRight.field_78809_i = true;
      this.setRotation(this.legFrontRight, -0.1745329F, 0.0F, 0.1745329F);
      this.legFrontLeft = new ModelRenderer(this, 0, 6);
      this.legFrontLeft.func_78789_a(0.0F, 0.0F, 0.0F, 1, 6, 1);
      this.legFrontLeft.func_78793_a(1.0F, 19.0F, -6.0F);
      this.legFrontLeft.func_78787_b(64, 64);
      this.legFrontLeft.field_78809_i = true;
      this.setRotation(this.legFrontLeft, -0.1745329F, 0.0F, -0.1745329F);
      this.thread = new ModelRenderer(this, 23, 0);
      this.thread.func_78789_a(-1.0F, -3.0F, -1.0F, 2, 3, 2);
      this.thread.func_78793_a(0.0F, 12.0F, 5.0F);
      this.thread.func_78787_b(64, 64);
      this.thread.field_78809_i = true;
      this.setRotation(this.thread, 0.0F, 0.0F, 0.0F);
      this.threadPole = new ModelRenderer(this, 9, 7);
      this.threadPole.func_78789_a(-0.5F, 0.0F, -0.5F, 1, 4, 1);
      this.threadPole.func_78793_a(0.0F, 12.0F, 5.0F);
      this.threadPole.func_78787_b(64, 64);
      this.threadPole.field_78809_i = true;
      this.setRotation(this.threadPole, 0.0F, 0.0F, 0.0F);
      this.armRight = new ModelRenderer(this, 28, 6);
      this.armRight.func_78789_a(-0.5F, -7.0F, -0.5F, 1, 7, 1);
      this.armRight.func_78793_a(-1.0F, 18.0F, -2.0F);
      this.armRight.func_78787_b(64, 64);
      this.armRight.field_78809_i = true;
      this.setRotation(this.armRight, 0.2268928F, 0.0F, 0.0F);
      this.armLeft = new ModelRenderer(this, 28, 6);
      this.armLeft.func_78789_a(-0.5F, -7.0F, -0.5F, 1, 7, 1);
      this.armLeft.func_78793_a(1.0F, 18.0F, -2.0F);
      this.armLeft.func_78787_b(64, 64);
      this.armLeft.field_78809_i = true;
      this.setRotation(this.armLeft, 0.2268928F, 0.0F, 0.0F);
      this.wheel = new ModelRenderer(this, "wheel");
      this.wheel.func_78793_a(0.0F, 12.0F, -3.5F);
      this.setRotation(this.wheel, 0.0F, 0.0F, 0.0F);
      this.wheel.field_78809_i = true;
      this.wheel.func_78786_a("spokes", 0.0F, -3.0F, -3.0F, 0, 6, 6);
      this.wheel.func_78786_a("top", -0.5F, -4.0F, -3.0F, 1, 1, 6);
      this.wheel.func_78786_a("bottom", -0.5F, 3.0F, -3.0F, 1, 1, 6);
      this.wheel.func_78786_a("back", -0.5F, -4.0F, 3.0F, 1, 8, 1);
      this.wheel.func_78786_a("front", -0.5F, -4.0F, -4.0F, 1, 8, 1);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.render(entity, f, f1, f2, f3, f4, f5, (BlockSpinningWheel.TileEntitySpinningWheel)null);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, BlockSpinningWheel.TileEntitySpinningWheel spinningWheel) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.setRotationAngles(f, f1, f2, f3, f4, f5, entity, spinningWheel);
      this.seat.func_78785_a(f5);
      this.legBackRight.func_78785_a(f5);
      this.legBackLeft.func_78785_a(f5);
      this.legFrontRight.func_78785_a(f5);
      this.legFrontLeft.func_78785_a(f5);
      this.thread.func_78785_a(f5);
      this.threadPole.func_78785_a(f5);
      this.armRight.func_78785_a(f5);
      this.armLeft.func_78785_a(f5);
      this.wheel.func_78785_a(f5);
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      this.setRotationAngles(f, f1, f2, f3, f4, f5, entity, (BlockSpinningWheel.TileEntitySpinningWheel)null);
   }

   private void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity, BlockSpinningWheel.TileEntitySpinningWheel spinningWheel) {
      super.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      if (spinningWheel != null) {
         boolean spinning = spinningWheel.getCookTime() > 0 && spinningWheel.getCookTime() < spinningWheel.getTotalCookTime() && spinningWheel.powerLevel > 0;
         Minecraft.func_71410_x();
         long ticks = Minecraft.func_71386_F() / 25L;
         this.wheel.field_78795_f = spinning ? (float)(-(ticks / 3L) % 360L) : 0.0F;
         this.thread.field_78796_g = spinning ? (float)(ticks / 2L % 360L) : 0.0F;
      }

   }
}
