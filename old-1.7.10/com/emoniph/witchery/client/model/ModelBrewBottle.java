package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelBrewBottle extends ModelBase {
   private ModelRenderer Bottle;
   private ModelRenderer Stopper;

   public ModelBrewBottle() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.func_78085_a("Bottle.BodyOuter", 0, 14);
      this.func_78085_a("Bottle.BodyInner", 2, 8);
      this.func_78085_a("Bottle.Neck", 4, 4);
      this.func_78085_a("Bottle.Stopper", 2, 0);
      this.Bottle = new ModelRenderer(this, "Bottle");
      this.Bottle.func_78793_a(0.0F, 0.0F, 0.0F);
      this.setRotation(this.Bottle, 0.0F, 0.0F, 0.0F);
      this.Bottle.field_78809_i = true;
      this.Bottle.func_78786_a("BodyOuter", -1.5F, -2.0F, -1.5F, 3, 2, 3);
      this.Bottle.func_78786_a("BodyInner", -1.0F, -2.5F, -1.0F, 2, 3, 2);
      this.Bottle.func_78786_a("Neck", -0.5F, -4.0F, -0.5F, 1, 2, 1);
      this.Stopper = new ModelRenderer(this, 2, 0);
      this.Stopper.func_78789_a(0.0F, 0.0F, 0.0F, 2, 1, 2);
      this.Stopper.func_78787_b(32, 32);
      this.Stopper.func_78793_a(-1.0F, -4.5F, -1.0F);
      this.setRotation(this.Stopper, 0.0F, 0.0F, 0.0F);
      this.Stopper.field_78809_i = true;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.Bottle.func_78785_a(f5);
      GL11.glColor3f(1.0F, 1.0F, 1.0F);
      this.Stopper.func_78785_a(f5);
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
