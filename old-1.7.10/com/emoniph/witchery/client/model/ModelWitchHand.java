package com.emoniph.witchery.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelWitchHand extends ModelBase {
   ModelRenderer wrist;
   ModelRenderer palmUpper;
   ModelRenderer palmLower;
   ModelRenderer finger1Upper;
   ModelRenderer finger2Upper;
   ModelRenderer finger3Upper;
   ModelRenderer finger1Lower;
   ModelRenderer finger2Lower;
   ModelRenderer finger3Lower;
   ModelRenderer rightPalm;
   ModelRenderer rightFingerUpper;
   ModelRenderer rightFingerLower;
   ModelRenderer rightThumbUpper;
   ModelRenderer rightThumbLower;
   ModelRenderer leftPalm;
   ModelRenderer leftFingerUpper;
   ModelRenderer leftFingerLower;
   ModelRenderer leftThumbUpper;
   ModelRenderer leftThumbLower;
   ModelRenderer scythe;

   public ModelWitchHand(float scale) {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.wrist = new ModelRenderer(this, 0, 0);
      this.wrist.func_78789_a(0.0F, 0.0F, 0.0F, 5, 5, 2);
      this.wrist.func_78793_a(-3.0F, 0.0F, 0.0F);
      this.wrist.func_78787_b(64, 64);
      this.wrist.field_78809_i = true;
      this.setRotation(this.wrist, 0.0F, 0.0F, 0.0F);
      this.palmUpper = new ModelRenderer(this, 0, 7);
      this.palmUpper.func_78790_a(0.0F, 0.0F, 0.0F, 5, 1, 5, scale);
      this.palmUpper.func_78793_a(-3.0F, 0.0F, -5.0F);
      this.palmUpper.func_78787_b(64, 64);
      this.palmUpper.field_78809_i = true;
      this.setRotation(this.palmUpper, 0.0F, 0.0F, 0.0F);
      this.palmLower = new ModelRenderer(this, 0, 13);
      this.palmLower.func_78790_a(0.0F, 0.0F, 0.0F, 5, 2, 3, scale);
      this.palmLower.func_78793_a(-3.0F, 1.0F, -3.0F);
      this.palmLower.func_78787_b(64, 64);
      this.palmLower.field_78809_i = true;
      this.setRotation(this.palmLower, 0.0F, 0.0F, 0.0F);
      this.finger1Upper = new ModelRenderer(this, 0, 18);
      this.finger1Upper.func_78790_a(0.0F, 0.0F, -2.0F, 1, 1, 4, scale);
      this.finger1Upper.func_78793_a(-3.0F, 1.0F, -7.0F);
      this.finger1Upper.func_78787_b(64, 64);
      this.finger1Upper.field_78809_i = true;
      this.setRotation(this.finger1Upper, 0.4833219F, 0.0F, 0.0F);
      this.finger2Upper = new ModelRenderer(this, 6, 19);
      this.finger2Upper.func_78790_a(0.0F, 0.0F, -2.0F, 1, 1, 4, scale);
      this.finger2Upper.func_78793_a(-1.0F, 1.0F, -7.0F);
      this.finger2Upper.func_78787_b(64, 64);
      this.finger2Upper.field_78809_i = true;
      this.setRotation(this.finger2Upper, 0.4833219F, 0.0F, 0.0F);
      this.finger3Upper = new ModelRenderer(this, 12, 18);
      this.finger3Upper.func_78790_a(0.0F, 0.0F, -2.0F, 1, 1, 4, scale);
      this.finger3Upper.func_78793_a(1.0F, 1.0F, -7.0F);
      this.finger3Upper.func_78787_b(64, 64);
      this.finger3Upper.field_78809_i = true;
      this.setRotation(this.finger3Upper, 0.4833219F, 0.0F, 0.0F);
      this.finger1Lower = new ModelRenderer(this, 0, 23);
      this.finger1Lower.func_78790_a(0.0F, 0.0F, -4.0F, 1, 1, 4, scale);
      this.finger1Lower.func_78793_a(-3.0F, 2.0F, -9.0F);
      this.finger1Lower.func_78787_b(64, 64);
      this.finger1Lower.field_78809_i = true;
      this.setRotation(this.finger1Lower, 2.044824F, 0.0F, 0.0F);
      this.finger2Lower = new ModelRenderer(this, 6, 24);
      this.finger2Lower.func_78790_a(0.0F, 0.0F, -4.0F, 1, 1, 4, scale);
      this.finger2Lower.func_78793_a(-1.0F, 2.0F, -9.0F);
      this.finger2Lower.func_78787_b(64, 64);
      this.finger2Lower.field_78809_i = true;
      this.setRotation(this.finger2Lower, 2.044824F, 0.0F, 0.0F);
      this.finger3Lower = new ModelRenderer(this, 12, 23);
      this.finger3Lower.func_78790_a(0.0F, 0.0F, -4.0F, 1, 1, 4, scale);
      this.finger3Lower.func_78793_a(1.0F, 2.0F, -9.0F);
      this.finger3Lower.func_78787_b(64, 64);
      this.finger3Lower.field_78809_i = true;
      this.setRotation(this.finger3Lower, 2.044824F, 0.0F, 0.0F);
      this.rightPalm = new ModelRenderer(this, 16, 0);
      this.rightPalm.func_78790_a(0.0F, 0.0F, 0.0F, 2, 1, 6, scale);
      this.rightPalm.func_78793_a(2.0F, 0.0F, -5.0F);
      this.rightPalm.func_78787_b(64, 64);
      this.rightPalm.field_78809_i = true;
      this.setRotation(this.rightPalm, 0.0F, 0.0F, 0.0F);
      this.rightFingerUpper = new ModelRenderer(this, 20, 7);
      this.rightFingerUpper.func_78790_a(0.0F, 0.0F, -4.0F, 1, 1, 4, scale);
      this.rightFingerUpper.func_78793_a(3.0F, 0.0F, -5.0F);
      this.rightFingerUpper.func_78787_b(64, 64);
      this.rightFingerUpper.field_78809_i = true;
      this.setRotation(this.rightFingerUpper, -0.5205006F, 0.0F, 0.0F);
      this.rightFingerLower = new ModelRenderer(this, 20, 12);
      this.rightFingerLower.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 4, scale);
      this.rightFingerLower.func_78793_a(3.0F, -1.0F, -8.0F);
      this.rightFingerLower.func_78787_b(64, 64);
      this.rightFingerLower.field_78809_i = true;
      this.setRotation(this.rightFingerLower, -2.732628F, 0.0F, 0.0F);
      this.rightThumbUpper = new ModelRenderer(this, 22, 17);
      this.rightThumbUpper.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 3, scale);
      this.rightThumbUpper.func_78793_a(3.5F, 1.0F, 0.0F);
      this.rightThumbUpper.func_78787_b(64, 64);
      this.rightThumbUpper.field_78809_i = true;
      this.setRotation(this.rightThumbUpper, -2.7F, -0.7F, -0.669215F);
      this.rightThumbLower = new ModelRenderer(this, 22, 21);
      this.rightThumbLower.func_78790_a(0.0F, -1.0F, -1.0F, 1, 1, 3, scale);
      this.rightThumbLower.func_78793_a(5.0F, 3.0F, -2.0F);
      this.rightThumbLower.func_78787_b(64, 64);
      this.rightThumbLower.field_78809_i = true;
      this.setRotation(this.rightThumbLower, -1.896109F, 0.2602503F, 0.3717861F);
      this.leftPalm = new ModelRenderer(this, 16, 0);
      this.leftPalm.func_78790_a(0.0F, 0.0F, 0.0F, 2, 1, 6, scale);
      this.leftPalm.func_78793_a(-5.0F, 0.0F, -5.0F);
      this.leftPalm.func_78787_b(64, 64);
      this.leftPalm.field_78809_i = true;
      this.setRotation(this.leftPalm, 0.0F, 0.0F, 0.0F);
      this.leftFingerUpper = new ModelRenderer(this, 20, 7);
      this.leftFingerUpper.func_78790_a(0.0F, 0.0F, -4.0F, 1, 1, 4, scale);
      this.leftFingerUpper.func_78793_a(-5.0F, 0.0F, -5.0F);
      this.leftFingerUpper.func_78787_b(64, 64);
      this.leftFingerUpper.field_78809_i = true;
      this.setRotation(this.leftFingerUpper, -0.5205006F, 0.0F, 0.0F);
      this.leftFingerLower = new ModelRenderer(this, 20, 12);
      this.leftFingerLower.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 4, scale);
      this.leftFingerLower.func_78793_a(-5.0F, -1.0F, -8.0F);
      this.leftFingerLower.func_78787_b(64, 64);
      this.leftFingerLower.field_78809_i = true;
      this.setRotation(this.leftFingerLower, -2.732628F, 0.0F, 0.0F);
      this.leftThumbUpper = new ModelRenderer(this, 22, 17);
      this.leftThumbUpper.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 3, scale);
      this.leftThumbUpper.func_78793_a(-5.0F, 1.0F, 0.0F);
      this.leftThumbUpper.func_78787_b(64, 64);
      this.leftThumbUpper.field_78809_i = true;
      this.setRotation(this.leftThumbUpper, -1.7F, 0.8F, 0.148711F);
      this.leftThumbLower = new ModelRenderer(this, 22, 21);
      this.leftThumbLower.func_78790_a(0.0F, 0.0F, 0.0F, 1, 1, 3, scale);
      this.leftThumbLower.func_78793_a(-6.0F, 4.0F, -1.0F);
      this.leftThumbLower.func_78787_b(64, 64);
      this.leftThumbLower.field_78809_i = true;
      this.setRotation(this.leftThumbLower, -2.082002F, 0.0371828F, -0.6320403F);
      this.func_78085_a("scythe.shaft", 58, 5);
      this.func_78085_a("scythe.blade", 36, 0);
      this.scythe = new ModelRenderer(this, "scythe");
      this.scythe.func_78793_a(-6.0F, 10.0F, 0.0F);
      this.setRotation(this.scythe, 0.0F, 0.0F, 0.0F);
      this.scythe.field_78809_i = true;
      this.scythe.func_78786_a("shaft", -0.5F, -16.0F, -0.5F, 1, 35, 1);
      this.scythe.func_78786_a("blade", 0.0F, -16.0F, 0.0F, 13, 4, 0);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, boolean firstPerson, boolean deployed) {
      this.rightFingerUpper.func_78793_a(3.0F, 0.0F, -5.0F);
      this.rightFingerLower.func_78793_a(3.0F, -1.0F, -8.0F);
      this.leftFingerUpper.func_78793_a(-5.0F, 0.0F, -5.0F);
      this.leftFingerLower.func_78793_a(-5.0F, -1.0F, -8.0F);
      if (deployed) {
         this.rightFingerUpper.func_78793_a(3.0F, 4.0F, -4.0F);
         this.rightFingerLower.func_78793_a(3.0F, 1.0F, -4.0F);
         this.leftFingerUpper.func_78793_a(-5.0F, 4.0F, -4.0F);
         this.leftFingerLower.func_78793_a(-5.0F, 1.0F, -4.0F);
      }

      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.wrist.func_78791_b(f5);
      this.palmUpper.func_78791_b(f5);
      this.palmLower.func_78791_b(f5);
      this.finger1Upper.func_78791_b(f5);
      this.finger2Upper.func_78791_b(f5);
      this.finger3Upper.func_78791_b(f5);
      this.finger1Lower.func_78791_b(f5);
      this.finger2Lower.func_78791_b(f5);
      this.finger3Lower.func_78791_b(f5);
      if (firstPerson) {
         this.rightPalm.func_78791_b(f5);
         this.rightFingerUpper.func_78791_b(f5);
         this.rightFingerLower.func_78791_b(f5);
         this.rightThumbUpper.func_78791_b(f5);
         this.rightThumbLower.func_78791_b(f5);
      } else {
         this.leftPalm.func_78791_b(f5);
         this.leftFingerUpper.func_78791_b(f5);
         this.leftFingerLower.func_78791_b(f5);
         this.leftThumbUpper.func_78791_b(f5);
         this.leftThumbLower.func_78791_b(f5);
      }

      if (deployed) {
         GL11.glScalef(1.2F, 1.2F, 1.2F);
         this.scythe.field_78808_h = -1.5707964F;
         this.scythe.field_78800_c = -5.0F;
         this.scythe.field_78798_e = -3.0F;
         this.scythe.field_78797_d = 2.0F;
         this.scythe.field_78795_f = 3.1415927F;
         this.scythe.field_78796_g = 0.0F;
         if (firstPerson) {
            this.scythe.field_78796_g = -3.1415927F;
            this.scythe.field_78800_c = 6.0F;
         }

         this.scythe.func_78791_b(f5);
      }

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
