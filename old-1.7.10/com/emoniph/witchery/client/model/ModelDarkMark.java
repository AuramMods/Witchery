package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelDarkMark extends ModelBase {
   private ModelRenderer skull;

   public ModelDarkMark() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.func_78085_a("skull.top", 0, 0);
      this.func_78085_a("skull.bottom", 0, 29);
      this.skull = new ModelRenderer(this, "skull");
      this.skull.func_78793_a(0.0F, 20.0F, 0.0F);
      this.setRotation(this.skull, 0.0F, 0.0F, 0.0F);
      this.skull.field_78809_i = true;
      this.skull.func_78786_a("top", -8.0F, -12.0F, -8.0F, 16, 12, 16);
      this.skull.func_78786_a("bottom", -5.0F, 0.0F, -8.0F, 10, 4, 12);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      GL11.glTranslatef(0.0F, -1.0F, 0.0F);
      this.skull.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
      this.skull.field_78796_g = par4 / 57.295776F;
      this.skull.field_78795_f = par5 / 57.295776F;
   }
}
