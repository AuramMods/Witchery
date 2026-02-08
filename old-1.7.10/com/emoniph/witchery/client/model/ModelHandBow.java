package com.emoniph.witchery.client.model;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.item.ItemGeneral;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelHandBow extends ModelBase {
   ModelRenderer stockTop;
   ModelRenderer stockBottom;
   ModelRenderer stockCatch;
   ModelRenderer grip;
   ModelRenderer cross;
   ModelRenderer drawnCrossOuterR;
   ModelRenderer drawnCrossInnerR;
   ModelRenderer drawnCrossOuterL;
   ModelRenderer drawnCrossInnerL;
   ModelRenderer drawnStringInnerR;
   ModelRenderer drawnStringMidR;
   ModelRenderer drawnStringOuterR;
   ModelRenderer drawnStringInnerL;
   ModelRenderer drawnStringMidL;
   ModelRenderer drawnStringOuterL;
   ModelRenderer drawnStringCenter;
   ModelRenderer boltStake;
   ModelRenderer boltDraining;
   ModelRenderer boltHoly;
   ModelRenderer boltSplitting;
   ModelRenderer boltSplitting2;
   ModelRenderer boltSilver;

   public ModelHandBow() {
      this.field_78090_t = 64;
      this.field_78089_u = 32;
      this.stockTop = new ModelRenderer(this, 2, 2);
      this.stockTop.func_78789_a(-1.0F, 0.0F, -5.0F, 2, 1, 7);
      this.stockTop.func_78793_a(0.0F, 0.0F, 0.0F);
      this.stockTop.func_78787_b(64, 32);
      this.stockTop.field_78809_i = true;
      this.setRotation(this.stockTop, 0.0F, 0.0F, 0.0F);
      this.stockBottom = new ModelRenderer(this, 0, 10);
      this.stockBottom.func_78789_a(-0.5F, 0.0F, -6.0F, 1, 1, 8);
      this.stockBottom.func_78793_a(0.0F, 1.0F, 0.0F);
      this.stockBottom.func_78787_b(64, 32);
      this.stockBottom.field_78809_i = true;
      this.setRotation(this.stockBottom, 0.0F, 0.0F, 0.0F);
      this.stockCatch = new ModelRenderer(this, 1, 11);
      this.stockCatch.func_78789_a(-0.5F, 0.0F, 0.0F, 1, 1, 1);
      this.stockCatch.func_78793_a(0.0F, -1.0F, 1.0F);
      this.stockCatch.func_78787_b(64, 32);
      this.stockCatch.field_78809_i = true;
      this.setRotation(this.stockCatch, 0.0F, 0.0F, 0.0F);
      this.grip = new ModelRenderer(this, 0, 3);
      this.grip.func_78789_a(-0.5F, 0.0F, -1.0F, 1, 3, 2);
      this.grip.func_78793_a(0.0F, 2.0F, 0.0F);
      this.grip.func_78787_b(64, 32);
      this.grip.field_78809_i = true;
      this.setRotation(this.grip, 0.0F, 0.0F, 0.0F);
      this.cross = new ModelRenderer(this, 1, 19);
      this.cross.func_78789_a(-3.0F, 0.0F, 0.0F, 6, 1, 2);
      this.cross.func_78793_a(0.0F, 0.0F, -7.0F);
      this.cross.func_78787_b(64, 32);
      this.cross.field_78809_i = true;
      this.setRotation(this.cross, 0.0F, 0.0F, 0.0F);
      this.drawnCrossOuterR = new ModelRenderer(this, 0, 14);
      this.drawnCrossOuterR.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 1, 2);
      this.drawnCrossOuterR.func_78793_a(-4.0F, 0.0F, -4.0F);
      this.drawnCrossOuterR.func_78787_b(64, 32);
      this.drawnCrossOuterR.field_78809_i = true;
      this.setRotation(this.drawnCrossOuterR, 0.0F, 0.0F, 0.0F);
      this.drawnCrossInnerR = new ModelRenderer(this, 0, 14);
      this.drawnCrossInnerR.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 1, 2);
      this.drawnCrossInnerR.func_78793_a(-3.0F, 0.0F, -6.0F);
      this.drawnCrossInnerR.func_78787_b(64, 32);
      this.drawnCrossInnerR.field_78809_i = true;
      this.setRotation(this.drawnCrossInnerR, 0.0F, 0.0F, 0.0F);
      this.drawnCrossOuterL = new ModelRenderer(this, 0, 14);
      this.drawnCrossOuterL.func_78789_a(0.0F, 0.0F, 0.0F, 1, 1, 2);
      this.drawnCrossOuterL.func_78793_a(4.0F, 0.0F, -4.0F);
      this.drawnCrossOuterL.func_78787_b(64, 32);
      this.drawnCrossOuterL.field_78809_i = true;
      this.setRotation(this.drawnCrossOuterL, 0.0F, 0.0F, 0.0F);
      this.drawnCrossInnerL = new ModelRenderer(this, 0, 14);
      this.drawnCrossInnerL.func_78789_a(0.0F, 0.0F, 0.0F, 1, 1, 2);
      this.drawnCrossInnerL.func_78793_a(3.0F, 0.0F, -6.0F);
      this.drawnCrossInnerL.func_78787_b(64, 32);
      this.drawnCrossInnerL.field_78809_i = true;
      this.setRotation(this.drawnCrossInnerL, 0.0F, 0.0F, 0.0F);
      this.drawnStringInnerR = new ModelRenderer(this, 0, 0);
      this.drawnStringInnerR.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 1, 1);
      this.drawnStringInnerR.func_78793_a(-2.0F, 0.0F, -1.0F);
      this.drawnStringInnerR.func_78787_b(64, 32);
      this.drawnStringInnerR.field_78809_i = true;
      this.setRotation(this.drawnStringInnerR, 0.0F, 0.0F, 0.0F);
      this.drawnStringMidR = new ModelRenderer(this, 0, 0);
      this.drawnStringMidR.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 1, 1);
      this.drawnStringMidR.func_78793_a(-1.0F, 0.0F, 0.0F);
      this.drawnStringMidR.func_78787_b(64, 32);
      this.drawnStringMidR.field_78809_i = true;
      this.setRotation(this.drawnStringMidR, 0.0F, 0.0F, 0.0F);
      this.drawnStringOuterR = new ModelRenderer(this, 0, 0);
      this.drawnStringOuterR.func_78789_a(-1.0F, 0.0F, 0.0F, 1, 1, 1);
      this.drawnStringOuterR.func_78793_a(-3.0F, 0.0F, -2.0F);
      this.drawnStringOuterR.func_78787_b(64, 32);
      this.drawnStringOuterR.field_78809_i = true;
      this.setRotation(this.drawnStringOuterR, 0.0F, 0.0F, 0.0F);
      this.drawnStringInnerL = new ModelRenderer(this, 0, 0);
      this.drawnStringInnerL.func_78789_a(0.0F, 0.0F, 0.0F, 1, 1, 1);
      this.drawnStringInnerL.func_78793_a(2.0F, 0.0F, -1.0F);
      this.drawnStringInnerL.func_78787_b(64, 32);
      this.drawnStringInnerL.field_78809_i = true;
      this.setRotation(this.drawnStringInnerL, 0.0F, 0.0F, 0.0F);
      this.drawnStringMidL = new ModelRenderer(this, 0, 0);
      this.drawnStringMidL.func_78789_a(0.0F, 0.0F, 0.0F, 1, 1, 1);
      this.drawnStringMidL.func_78793_a(1.0F, 0.0F, 0.0F);
      this.drawnStringMidL.func_78787_b(64, 32);
      this.drawnStringMidL.field_78809_i = true;
      this.setRotation(this.drawnStringMidL, 0.0F, 0.0F, 0.0F);
      this.drawnStringOuterL = new ModelRenderer(this, 0, 0);
      this.drawnStringOuterL.func_78789_a(0.0F, 0.0F, 0.0F, 1, 1, 1);
      this.drawnStringOuterL.func_78793_a(3.0F, 0.0F, -2.0F);
      this.drawnStringOuterL.func_78787_b(64, 32);
      this.drawnStringOuterL.field_78809_i = true;
      this.setRotation(this.drawnStringOuterL, 0.0F, 0.0F, 0.0F);
      this.drawnStringCenter = new ModelRenderer(this, 4, 0);
      this.drawnStringCenter.func_78789_a(-1.5F, 0.0F, -0.5F, 3, 1, 1);
      this.drawnStringCenter.func_78793_a(0.0F, -0.1F, 1.0F);
      this.drawnStringCenter.func_78787_b(64, 32);
      this.drawnStringCenter.field_78809_i = true;
      this.setRotation(this.drawnStringCenter, 0.0F, 0.0174533F, 0.0F);
      this.boltStake = new ModelRenderer(this, 0, 22);
      this.boltStake.func_78789_a(-0.5F, 0.5F, -6.0F, 1, 1, 9);
      this.boltStake.func_78793_a(0.0F, -1.0F, -2.0F);
      this.boltStake.func_78787_b(64, 32);
      this.boltStake.field_78809_i = true;
      this.setRotation(this.boltStake, 0.0F, 0.0F, 0.0F);
      this.boltDraining = new ModelRenderer(this, 20, 22);
      this.boltDraining.func_78789_a(-0.5F, 0.5F, -6.0F, 1, 1, 9);
      this.boltDraining.func_78793_a(0.0F, -1.0F, -2.0F);
      this.boltDraining.func_78787_b(64, 32);
      this.boltDraining.field_78809_i = true;
      this.setRotation(this.boltDraining, 0.0F, 0.0F, 0.0F);
      this.boltHoly = new ModelRenderer(this, 40, 22);
      this.boltHoly.func_78789_a(-0.5F, 0.5F, -6.0F, 1, 1, 9);
      this.boltHoly.func_78793_a(0.0F, -1.0F, -2.0F);
      this.boltHoly.func_78787_b(64, 32);
      this.boltHoly.field_78809_i = true;
      this.setRotation(this.boltHoly, 0.0F, 0.0F, 0.0F);
      this.boltSplitting = new ModelRenderer(this, 20, 12);
      this.boltSplitting.func_78789_a(-0.5F, 0.5F, -6.0F, 1, 1, 9);
      this.boltSplitting.func_78793_a(0.0F, -1.0F, -2.0F);
      this.boltSplitting.func_78787_b(64, 32);
      this.boltSplitting.field_78809_i = true;
      this.setRotation(this.boltSplitting, 0.0F, 0.0F, 0.0F);
      this.boltSplitting2 = new ModelRenderer(this, 17, 11);
      this.boltSplitting2.func_78789_a(-0.5F, 0.5F, -6.0F, 2, 1, 4);
      this.boltSplitting2.func_78793_a(-0.5F, -1.5F, -1.0F);
      this.boltSplitting2.func_78787_b(64, 32);
      this.boltSplitting2.field_78809_i = true;
      this.setRotation(this.boltSplitting2, 0.0F, 0.0F, 0.0F);
      this.boltSilver = new ModelRenderer(this, 40, 12);
      this.boltSilver.func_78789_a(-0.5F, 0.5F, -6.0F, 1, 1, 9);
      this.boltSilver.func_78793_a(0.0F, -1.0F, -2.0F);
      this.boltSilver.func_78787_b(64, 32);
      this.boltSilver.field_78809_i = true;
      this.setRotation(this.boltSplitting, 0.0F, 0.0F, 0.0F);
      this.cross.field_78797_d = -0.3F;
      this.drawnCrossInnerL.field_78797_d = this.drawnCrossInnerR.field_78797_d = -0.15F;
      this.drawnStringMidL.field_78797_d = this.drawnStringMidR.field_78797_d = -0.1F;
      this.drawnStringInnerL.field_78797_d = this.drawnStringInnerR.field_78797_d = -0.05F;
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, ItemGeneral.BoltType boltType, int useCount) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.stockTop.func_78785_a(f5);
      this.stockBottom.func_78785_a(f5);
      this.stockCatch.func_78785_a(f5);
      this.grip.func_78785_a(f5);
      this.cross.func_78785_a(f5);
      if (useCount > 10) {
         this.drawnCrossInnerL.field_78798_e = this.drawnCrossInnerR.field_78798_e = -6.0F;
         this.drawnCrossOuterL.field_78798_e = this.drawnCrossOuterR.field_78798_e = -4.0F;
         this.drawnStringInnerL.field_78798_e = this.drawnStringInnerR.field_78798_e = -1.0F;
         this.drawnStringMidL.field_78798_e = this.drawnStringMidR.field_78798_e = 0.0F;
         this.drawnStringOuterL.field_78798_e = this.drawnStringOuterR.field_78798_e = -2.0F;
         this.drawnStringCenter.field_78798_e = 1.0F;
      } else if (useCount > 5) {
         this.drawnCrossInnerL.field_78798_e = this.drawnCrossInnerR.field_78798_e = -6.0F;
         this.drawnCrossOuterL.field_78798_e = this.drawnCrossOuterR.field_78798_e = -5.0F;
         this.drawnStringInnerL.field_78798_e = this.drawnStringInnerR.field_78798_e = -2.0F;
         this.drawnStringMidL.field_78798_e = this.drawnStringMidR.field_78798_e = -2.0F;
         this.drawnStringOuterL.field_78798_e = this.drawnStringOuterR.field_78798_e = -3.0F;
         this.drawnStringCenter.field_78798_e = -1.0F;
      } else if (useCount == 0) {
         this.drawnCrossInnerL.field_78798_e = this.drawnCrossInnerR.field_78798_e = -7.0F;
         this.drawnCrossOuterL.field_78798_e = this.drawnCrossOuterR.field_78798_e = -6.0F;
         this.drawnStringInnerL.field_78798_e = this.drawnStringInnerR.field_78798_e = -4.0F;
         this.drawnStringMidL.field_78798_e = this.drawnStringMidR.field_78798_e = -4.0F;
         this.drawnStringOuterL.field_78798_e = this.drawnStringOuterR.field_78798_e = -4.0F;
         this.drawnStringCenter.field_78798_e = -3.25F;
      }

      this.drawnCrossOuterR.func_78785_a(f5);
      this.drawnCrossOuterL.func_78785_a(f5);
      this.drawnCrossInnerR.func_78785_a(f5);
      this.drawnCrossInnerL.func_78785_a(f5);
      this.drawnStringInnerR.func_78785_a(f5);
      this.drawnStringMidR.func_78785_a(f5);
      this.drawnStringOuterR.func_78785_a(f5);
      this.drawnStringInnerL.func_78785_a(f5);
      this.drawnStringMidL.func_78785_a(f5);
      this.drawnStringOuterL.func_78785_a(f5);
      this.drawnStringCenter.func_78785_a(f5);
      if (boltType == Witchery.Items.GENERIC.itemBoltStake) {
         this.boltStake.func_78785_a(f5);
      } else if (boltType == Witchery.Items.GENERIC.itemBoltAntiMagic) {
         this.boltDraining.func_78785_a(f5);
      } else if (boltType == Witchery.Items.GENERIC.itemBoltHoly) {
         this.boltHoly.func_78785_a(f5);
      } else if (boltType == Witchery.Items.GENERIC.itemBoltSilver) {
         this.boltSilver.func_78785_a(f5);
      } else if (boltType == Witchery.Items.GENERIC.itemBoltSplitting) {
         this.boltSplitting.func_78785_a(f5);
         this.boltSplitting2.func_78785_a(f5);
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
