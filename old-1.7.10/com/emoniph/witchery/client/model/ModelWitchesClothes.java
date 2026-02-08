package com.emoniph.witchery.client.model;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.item.ItemWitchesClothes;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelWitchesClothes extends ModelBiped {
   ModelRenderer hat;
   ModelRenderer torso;
   ModelRenderer bottomBack;
   ModelRenderer bottomRight;
   ModelRenderer bottomLeft;
   ModelRenderer Shape1;
   ModelRenderer Shape2;
   ModelRenderer Shape3;
   ModelRenderer headRight1;
   ModelRenderer headLeft1;
   ModelRenderer legRightF;
   ModelRenderer legLeftF;
   ModelRenderer legRightB;
   ModelRenderer legLeftB;
   ModelRenderer bodyF;
   ModelRenderer bodyB;
   ModelRenderer armRightF;
   ModelRenderer armLeftF;
   ModelRenderer armRightB;
   ModelRenderer armLeftB;
   ModelRenderer armLeftOut;
   ModelRenderer armRightOut;
   ModelRenderer spikeLowerRight;
   ModelRenderer spikeLowerLeft;
   ModelRenderer spikeUpperLeft;
   ModelRenderer spikeUpperRight;
   ModelRenderer shoulderRight;
   ModelRenderer shoulderLeft;
   private ModelRenderer babasHat;

   public ModelWitchesClothes(float scale, boolean shoulders) {
      super(scale, 0.0F, 128, 64);
      this.func_78085_a("hat.hatBrim", 0, 49);
      this.func_78085_a("hat.hatCollar", 0, 36);
      this.func_78085_a("hat.hatBody", 31, 34);
      this.func_78085_a("hat.hatPoint", 50, 34);
      this.hat = new ModelRenderer(this, "hat");
      this.hat.func_78793_a(0.0F, 0.0F, 0.0F);
      this.setRotation(this.hat, 0.0F, 0.0F, 0.0F);
      this.hat.field_78809_i = true;
      this.hat.func_78786_a("hatBrim", -7.0F, -7.0F, -7.0F, 14, 1, 14);
      this.hat.func_78786_a("hatCollar", -5.0F, -9.0F, -5.0F, 10, 2, 10);
      this.hat.func_78786_a("hatBody", -3.0F, -14.0F, -3.0F, 6, 5, 6);
      this.hat.func_78786_a("hatPoint", -1.0F, -17.0F, -1.0F, 2, 3, 2);
      this.field_78116_c.func_78792_a(this.hat);
      this.babasHat = new ModelRenderer(this, 72, 48);
      this.babasHat.func_78793_a(-7.0F, -8.0F, -7.0F);
      this.babasHat.func_78790_a(0.0F, 0.0F, 0.0F, 14, 2, 14, 0.52F);
      this.setRotation(this.babasHat, 0.0F, 0.0F, 0.0F);
      this.babasHat.field_78809_i = true;
      this.field_78116_c.func_78792_a(this.babasHat);
      ModelRenderer modelrenderer = new ModelRenderer(this, 83, 29);
      modelrenderer.func_78793_a(3.75F, -4.0F, 4.0F);
      modelrenderer.func_78790_a(0.0F, 0.0F, 0.0F, 7, 4, 7, 0.4F);
      modelrenderer.field_78795_f = -0.05235988F;
      modelrenderer.field_78808_h = 0.02617994F;
      this.babasHat.func_78792_a(modelrenderer);
      ModelRenderer modelrenderer1 = new ModelRenderer(this, 83, 40);
      modelrenderer1.func_78793_a(1.75F, -4.0F, 2.0F);
      modelrenderer1.func_78789_a(0.0F, 0.0F, 0.0F, 4, 4, 4);
      modelrenderer1.field_78795_f = -0.10471976F;
      modelrenderer1.field_78808_h = 0.05235988F;
      modelrenderer.func_78792_a(modelrenderer1);
      ModelRenderer modelrenderer2 = new ModelRenderer(this, 81, 48);
      modelrenderer2.func_78793_a(1.75F, -2.0F, 2.0F);
      modelrenderer2.func_78790_a(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
      modelrenderer2.field_78795_f = -0.20943952F;
      modelrenderer2.field_78808_h = 0.10471976F;
      modelrenderer1.func_78792_a(modelrenderer2);
      this.torso = new ModelRenderer(this, 43, 46);
      this.torso.func_78790_a(-4.0F, 0.0F, -2.0F, 8, 6, 4, scale);
      this.torso.func_78793_a(0.0F, 12.0F, 0.0F);
      this.torso.field_78809_i = true;
      this.setRotation(this.torso, 0.0F, 0.0F, 0.0F);
      this.field_78115_e.func_78792_a(this.torso);
      if (shoulders) {
         this.Shape1 = new ModelRenderer(this, 61, 32);
         this.Shape1.func_78790_a(0.0F, 0.0F, 0.0F, 5, 1, 6, scale + 0.1F);
         this.Shape1.func_78793_a(-9.0F, 0.0F, -3.0F);
         this.Shape1.field_78809_i = true;
         this.Shape2 = new ModelRenderer(this, 61, 39);
         this.Shape2.func_78790_a(0.0F, 0.0F, 0.0F, 5, 1, 6, scale + 0.1F);
         this.Shape2.func_78793_a(4.0F, 0.0F, -3.0F);
         this.Shape2.field_78809_i = true;
         this.Shape2.func_78793_a(0.0F, -2.0F, -3.0F);
         this.Shape1.func_78793_a(-4.0F, -2.0F, -3.0F);
         this.field_78112_f.func_78792_a(this.Shape1);
         this.field_78113_g.func_78792_a(this.Shape2);
      }

      this.headRight1 = new ModelRenderer(this, 124, 0);
      this.headRight1.func_78789_a(-0.5F, -5.0F, -0.5F, 1, 5, 1);
      this.headRight1.func_78793_a(-4.0F, 0.0F, 0.0F);
      this.headRight1.func_78787_b(64, 128);
      this.headRight1.field_78809_i = true;
      this.setRotation(this.headRight1, -0.1487144F, 0.0F, -0.4089647F);
      this.headLeft1 = new ModelRenderer(this, 124, 0);
      this.headLeft1.func_78789_a(-0.5F, -5.0F, -0.5F, 1, 5, 1);
      this.headLeft1.func_78793_a(4.0F, 0.0F, 0.0F);
      this.headLeft1.func_78787_b(64, 128);
      this.headLeft1.field_78809_i = true;
      this.setRotation(this.headLeft1, -0.1487144F, 0.0F, 0.4089647F);
      this.legRightF = new ModelRenderer(this, 95, 0);
      this.legRightF.func_78789_a(0.0F, 0.0F, 0.0F, 2, 7, 1);
      this.legRightF.func_78793_a(-4.0F, 13.0F, -3.0F);
      this.legRightF.func_78787_b(64, 128);
      this.legRightF.field_78809_i = true;
      this.setRotation(this.legRightF, 0.0F, 0.0F, -0.2230717F);
      this.legLeftF = new ModelRenderer(this, 95, 0);
      this.legLeftF.func_78789_a(0.0F, 0.0F, 0.0F, 2, 7, 1);
      this.legLeftF.func_78793_a(1.0F, 13.0F, -3.0F);
      this.legLeftF.func_78787_b(64, 128);
      this.legLeftF.field_78809_i = true;
      this.setRotation(this.legLeftF, 0.0F, 0.0F, 0.1230717F);
      this.legRightB = new ModelRenderer(this, 95, 0);
      this.legRightB.func_78789_a(0.0F, 0.0F, 0.0F, 2, 7, 1);
      this.legRightB.func_78793_a(-4.0F, 13.0F, 2.0F);
      this.legRightB.func_78787_b(64, 128);
      this.legRightB.field_78809_i = true;
      this.setRotation(this.legRightB, 0.0F, 0.0F, -0.2230717F);
      this.legLeftB = new ModelRenderer(this, 95, 0);
      this.legLeftB.func_78789_a(0.0F, 0.0F, 0.0F, 2, 7, 1);
      this.legLeftB.func_78793_a(1.0F, 13.0F, 2.0F);
      this.legLeftB.func_78787_b(64, 128);
      this.legLeftB.field_78809_i = true;
      this.setRotation(this.legLeftB, 0.0F, 0.0F, 0.1230717F);
      this.bodyF = new ModelRenderer(this, 111, 0);
      this.bodyF.func_78789_a(0.0F, 0.0F, 0.0F, 6, 9, 1);
      this.bodyF.func_78793_a(-2.5F, 1.0F, -3.1F);
      this.bodyF.func_78787_b(64, 128);
      this.bodyF.field_78809_i = true;
      this.setRotation(this.bodyF, 0.0F, 0.0F, 0.1487144F);
      this.bodyB = new ModelRenderer(this, 111, 0);
      this.bodyB.func_78789_a(0.0F, 0.0F, 0.0F, 6, 9, 1);
      this.bodyB.func_78793_a(-2.5F, 1.0F, 2.1F);
      this.bodyB.func_78787_b(64, 128);
      this.bodyB.field_78809_i = true;
      this.setRotation(this.bodyB, 0.0F, 0.0F, 0.0887144F);
      this.armRightF = new ModelRenderer(this, 102, 0);
      this.armRightF.func_78789_a(0.0F, 0.0F, 0.0F, 3, 7, 1);
      this.armRightF.func_78793_a(-8.0F, 3.0F, -3.0F);
      this.armRightF.func_78787_b(64, 128);
      this.armRightF.field_78809_i = true;
      this.setRotation(this.armRightF, 0.0F, 0.0F, -0.1487144F);
      this.armLeftF = new ModelRenderer(this, 102, 0);
      this.armLeftF.func_78789_a(0.0F, 0.0F, 0.0F, 3, 6, 1);
      this.armLeftF.func_78793_a(5.0F, 2.0F, -3.0F);
      this.armLeftF.func_78787_b(64, 128);
      this.armLeftF.field_78809_i = true;
      this.setRotation(this.armLeftF, 0.0F, 0.0F, 0.0687144F);
      this.armRightB = new ModelRenderer(this, 102, 0);
      this.armRightB.func_78789_a(0.0F, 0.0F, 0.0F, 3, 7, 1);
      this.armRightB.func_78793_a(-8.0F, 3.0F, 2.0F);
      this.armRightB.func_78787_b(64, 128);
      this.armRightB.field_78809_i = true;
      this.setRotation(this.armRightB, 0.0F, 0.0F, -0.1487144F);
      this.armLeftB = new ModelRenderer(this, 102, 0);
      this.armLeftB.func_78789_a(0.0F, 0.0F, 0.0F, 3, 6, 1);
      this.armLeftB.func_78793_a(5.0F, 2.0F, 2.0F);
      this.armLeftB.func_78787_b(64, 128);
      this.armLeftB.field_78809_i = true;
      this.setRotation(this.armLeftB, 0.0F, 0.0F, 0.0687144F);
      this.armLeftOut = new ModelRenderer(this, 120, 0);
      this.armLeftOut.func_78789_a(0.0F, 0.0F, 0.0F, 1, 7, 3);
      this.armLeftOut.func_78793_a(8.0F, 2.0F, -1.5F);
      this.armLeftOut.func_78787_b(128, 64);
      this.armLeftOut.field_78809_i = true;
      this.setRotation(this.armLeftOut, 0.0371786F, 0.0F, 0.0F);
      this.armRightOut = new ModelRenderer(this, 120, 0);
      this.armRightOut.func_78789_a(0.0F, 0.0F, 0.0F, 1, 6, 3);
      this.armRightOut.func_78793_a(-9.0F, 2.0F, -1.0F);
      this.armRightOut.func_78787_b(128, 64);
      this.armRightOut.field_78809_i = true;
      this.setRotation(this.armRightOut, -0.1858931F, 0.0F, 0.0F);
      this.spikeLowerRight = new ModelRenderer(this, 120, 0);
      this.spikeLowerRight.func_78789_a(-0.5F, -6.0F, -0.5F, 1, 6, 1);
      this.spikeLowerRight.func_78793_a(-1.0F, 7.0F, 2.0F);
      this.spikeLowerRight.func_78787_b(128, 64);
      this.spikeLowerRight.field_78809_i = true;
      this.setRotation(this.spikeLowerRight, -0.7807508F, -0.1858931F, 0.0F);
      this.spikeLowerLeft = new ModelRenderer(this, 120, 0);
      this.spikeLowerLeft.func_78789_a(-0.5F, -6.0F, -0.5F, 1, 6, 1);
      this.spikeLowerLeft.func_78793_a(1.0F, 7.0F, 2.0F);
      this.spikeLowerLeft.func_78787_b(128, 64);
      this.spikeLowerLeft.field_78809_i = true;
      this.setRotation(this.spikeLowerLeft, -0.7807508F, 0.1858931F, 0.0F);
      this.spikeUpperLeft = new ModelRenderer(this, 120, 0);
      this.spikeUpperLeft.func_78789_a(-0.5F, -6.0F, -0.5F, 1, 6, 1);
      this.spikeUpperLeft.func_78793_a(2.0F, 3.0F, 2.0F);
      this.spikeUpperLeft.func_78787_b(128, 64);
      this.spikeUpperLeft.field_78809_i = true;
      this.setRotation(this.spikeUpperLeft, -0.7807508F, 0.1858931F, 0.0F);
      this.spikeUpperRight = new ModelRenderer(this, 120, 0);
      this.spikeUpperRight.func_78789_a(-0.5F, -6.0F, -0.5F, 1, 6, 1);
      this.spikeUpperRight.func_78793_a(-2.0F, 3.0F, 2.0F);
      this.spikeUpperRight.func_78787_b(128, 64);
      this.spikeUpperRight.field_78809_i = true;
      this.setRotation(this.spikeUpperRight, -0.7807508F, -0.1858931F, 0.0F);
      this.shoulderRight = new ModelRenderer(this, 108, 0);
      this.shoulderRight.func_78789_a(0.0F, 0.0F, 0.0F, 5, 1, 5);
      this.shoulderRight.func_78793_a(-9.0F, -1.5F, -2.5F);
      this.shoulderRight.func_78787_b(128, 64);
      this.shoulderRight.field_78809_i = true;
      this.setRotation(this.shoulderRight, 0.0371786F, -0.1115358F, -0.1230717F);
      this.shoulderLeft = new ModelRenderer(this, 108, 0);
      this.shoulderLeft.func_78789_a(0.0F, 0.0F, 0.0F, 5, 1, 5);
      this.shoulderLeft.func_78793_a(4.0F, -2.5F, -1.5F);
      this.shoulderLeft.func_78787_b(128, 64);
      this.shoulderLeft.field_78809_i = true;
      this.setRotation(this.shoulderLeft, 0.0F, 0.2974289F, 0.1830717F);
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      EntityLivingBase living;
      ItemStack belt;
      if (entity != null && entity instanceof EntityLivingBase) {
         living = (EntityLivingBase)entity;
         belt = living.func_71124_b(4);
         if (belt != null && this.field_78116_c.field_78806_j) {
            boolean baba = belt.func_77973_b() == Witchery.Items.BABAS_HAT;
            this.hat.field_78806_j = !baba;
            this.babasHat.field_78806_j = baba;
         }
      }

      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      if (entity != null && entity instanceof EntityLivingBase) {
         living = (EntityLivingBase)entity;
         belt = living.func_71124_b(2);
         if (belt != null && belt.func_77973_b() == Witchery.Items.BARK_BELT && this.field_78115_e.field_78806_j) {
            int charge = Math.min(Witchery.Items.BARK_BELT.getChargeLevel(belt), Witchery.Items.BARK_BELT.getMaxChargeLevel(living));
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            this.renderBark(f5, this.field_78115_e, this.bodyF, charge >= 1);
            this.renderBark(f5, this.field_78115_e, this.bodyB, charge >= 1);
            --charge;
            this.renderBark(f5, this.field_78115_e, this.spikeLowerLeft, charge >= 1);
            this.renderBark(f5, this.field_78115_e, this.spikeLowerRight, charge >= 1);
            this.renderBark(f5, this.field_78115_e, this.spikeUpperLeft, charge >= 1);
            this.renderBark(f5, this.field_78115_e, this.spikeUpperRight, charge >= 1);
            --charge;
            ItemStack shoes = living.func_71124_b(1);
            if (shoes != null && shoes.func_77973_b() instanceof ItemWitchesClothes) {
               this.renderBark(f5, this.field_78123_h, this.legRightF, charge >= 1 && this.field_78115_e.field_78806_j, true);
               this.renderBark(f5, this.field_78123_h, this.legRightB, charge >= 1 && this.field_78115_e.field_78806_j, true);
               --charge;
               this.renderBark(f5, this.field_78124_i, this.legLeftF, charge >= 1 && this.field_78115_e.field_78806_j, true);
               this.renderBark(f5, this.field_78124_i, this.legLeftB, charge >= 1 && this.field_78115_e.field_78806_j, true);
               --charge;
            }

            ItemStack robes = living.func_71124_b(3);
            if (robes != null && robes.func_77973_b() instanceof ItemWitchesClothes) {
               this.renderBark(f5, this.field_78112_f, this.armRightF, charge >= 1);
               this.renderBark(f5, this.field_78112_f, this.armRightOut, charge >= 1);
               this.renderBark(f5, this.field_78112_f, this.armRightB, charge >= 1);
               --charge;
               this.renderBark(f5, this.field_78113_g, this.armLeftB, charge >= 1);
               this.renderBark(f5, this.field_78113_g, this.armLeftF, charge >= 1);
               this.renderBark(f5, this.field_78113_g, this.armLeftOut, charge >= 1);
               --charge;
            }

            ItemStack hat = living.func_71124_b(4);
            if (hat != null && hat.func_77973_b() instanceof ItemWitchesClothes) {
               this.renderBark(f5, this.field_78112_f, this.headRight1, charge >= 1);
               this.renderBark(f5, this.field_78112_f, this.shoulderRight, charge >= 1);
               --charge;
               this.renderBark(f5, this.field_78113_g, this.headLeft1, charge >= 1);
               this.renderBark(f5, this.field_78113_g, this.shoulderLeft, charge >= 1);
            }
         }
      }

   }

   private void renderBark(float f5, ModelRenderer bodyPart, ModelRenderer barkPiece, boolean visible) {
      this.renderBark(f5, bodyPart, barkPiece, visible, false);
   }

   private void renderBark(float f5, ModelRenderer bodyPart, ModelRenderer barkPiece, boolean visible, boolean leg) {
      if (visible) {
         GL11.glTranslatef(bodyPart.field_82906_o, bodyPart.field_82908_p, bodyPart.field_82907_q);
         if (bodyPart.field_78795_f == 0.0F && bodyPart.field_78796_g == 0.0F && bodyPart.field_78808_h == 0.0F && !leg) {
            if (bodyPart.field_78800_c == 0.0F && bodyPart.field_78797_d == 0.0F && bodyPart.field_78798_e == 0.0F) {
               barkPiece.func_78785_a(f5);
            } else {
               GL11.glTranslatef(bodyPart.field_78800_c * f5, bodyPart.field_78797_d * f5, bodyPart.field_78798_e * f5);
               barkPiece.func_78785_a(f5);
               GL11.glTranslatef(-bodyPart.field_78800_c * f5, -bodyPart.field_78797_d * f5, -bodyPart.field_78798_e * f5);
            }
         } else {
            GL11.glPushMatrix();
            GL11.glTranslatef(bodyPart.field_78800_c * f5, bodyPart.field_78797_d * f5, bodyPart.field_78798_e * f5);
            if (bodyPart.field_78808_h != 0.0F) {
               GL11.glRotatef(bodyPart.field_78808_h * 57.295776F, 0.0F, 0.0F, 1.0F);
            }

            if (bodyPart.field_78796_g != 0.0F) {
               GL11.glRotatef(bodyPart.field_78796_g * 57.295776F, 0.0F, 1.0F, 0.0F);
            }

            if (bodyPart.field_78795_f != 0.0F) {
               GL11.glRotatef(bodyPart.field_78795_f * 57.295776F, 1.0F, 0.0F, 0.0F);
            }

            GL11.glTranslatef(-bodyPart.field_78800_c * f5, -bodyPart.field_78797_d * f5, -bodyPart.field_78798_e * f5);
            if (this.field_78117_n && leg) {
               GL11.glTranslatef(0.0F, -3.0F * f5, 4.0F * f5);
               barkPiece.func_78785_a(f5);
            } else {
               barkPiece.func_78785_a(f5);
            }

            GL11.glPopMatrix();
         }

         GL11.glTranslatef(-bodyPart.field_82906_o, -bodyPart.field_82908_p, -bodyPart.field_82907_q);
      }

   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }
}
