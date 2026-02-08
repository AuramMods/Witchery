package com.emoniph.witchery.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.EntityRenderer;

public class RenderEntityViewer extends EntityRenderer {
   private final Minecraft mc;
   private float offsetY;

   public RenderEntityViewer(Minecraft mc) {
      super(mc, mc.func_110442_L());
      this.mc = mc;
   }

   public void setOffset(float offset) {
      this.offsetY = offset;
   }

   public float getOffset() {
      return this.offsetY;
   }

   private boolean canShiftView() {
      return this.mc.field_71439_g != null && !this.mc.field_71439_g.func_70608_bn() && !this.mc.field_71439_g.func_70115_ae();
   }

   public void func_78480_b(float partialTicks) {
      if (this.canShiftView()) {
         EntityClientPlayerMP var10000 = this.mc.field_71439_g;
         var10000.field_70163_u += (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70137_T += (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70167_r += (double)(-this.offsetY);
         float savedHeight = this.mc.field_71439_g.eyeHeight;
         this.mc.field_71439_g.eyeHeight = this.mc.field_71439_g.getDefaultEyeHeight();
         super.func_78480_b(partialTicks);
         this.mc.field_71439_g.eyeHeight = savedHeight;
         var10000 = this.mc.field_71439_g;
         var10000.field_70163_u -= (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70137_T -= (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70167_r -= (double)(-this.offsetY);
      } else {
         super.func_78480_b(partialTicks);
      }

   }

   public void func_78473_a(float partialTicks) {
      if (this.canShiftView()) {
         EntityClientPlayerMP var10000 = this.mc.field_71439_g;
         var10000.field_70163_u += (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70167_r += (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70137_T += (double)(-this.offsetY);
         float savedHeight = this.mc.field_71439_g.eyeHeight;
         this.mc.field_71439_g.eyeHeight = this.mc.field_71439_g.getDefaultEyeHeight();
         super.func_78473_a(partialTicks);
         this.mc.field_71439_g.eyeHeight = savedHeight;
         var10000 = this.mc.field_71439_g;
         var10000.field_70163_u -= (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70167_r -= (double)(-this.offsetY);
         var10000 = this.mc.field_71439_g;
         var10000.field_70137_T -= (double)(-this.offsetY);
      } else {
         super.func_78473_a(partialTicks);
      }

   }
}
