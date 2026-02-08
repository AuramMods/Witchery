package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityDemon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@SideOnly(Side.CLIENT)
public class ModelDemon extends ModelBase {
   ModelRenderer head;
   ModelRenderer body;
   ModelRenderer rightarm;
   ModelRenderer leftarm;
   ModelRenderer rightleg;
   ModelRenderer leftleg;
   ModelRenderer wingRight;
   ModelRenderer wingLeft;

   public ModelDemon() {
      this.field_78090_t = 128;
      this.field_78089_u = 32;
      this.func_78085_a("head.face", 0, 0);
      this.func_78085_a("head.leftHorn", 0, 16);
      this.func_78085_a("head.rightHorn", 0, 16);
      this.func_78085_a("head.leftTusk", 4, 16);
      this.func_78085_a("head.rightTusk", 4, 16);
      this.func_78085_a("head.snout", 20, 16);
      this.func_78085_a("head.bottomLip", 8, 16);
      this.head = new ModelRenderer(this, "head");
      this.head.func_78784_a(0, 0);
      this.head.func_78786_a("face", -4.0F, -8.0F, -4.0F, 8, 8, 8);
      this.head.func_78786_a("leftHorn", 4.0F, -12.0F, -0.5F, 1, 8, 1);
      this.head.func_78786_a("rightHorn", -5.0F, -12.0F, -0.5F, 1, 8, 1);
      this.head.func_78786_a("leftTusk", 1.0F, -4.0F, -5.0F, 1, 2, 1);
      this.head.func_78786_a("bottomLip", -2.0F, -2.0F, -6.0F, 4, 1, 2);
      this.head.func_78786_a("snout", -1.0F, -6.0F, -6.0F, 2, 3, 2);
      this.head.func_78786_a("rightTusk", -2.0F, -4.0F, -5.0F, 1, 2, 1);
      this.head.func_78793_a(0.0F, -9.0F, 0.0F);
      this.head.func_78787_b(128, 32);
      this.head.field_78809_i = true;
      this.body = new ModelRenderer(this, 64, 0);
      this.body.func_78789_a(-4.0F, 0.0F, -3.0F, 8, 14, 6);
      this.body.func_78793_a(0.0F, -9.0F, 0.0F);
      this.body.func_78787_b(128, 32);
      this.body.field_78809_i = true;
      this.rightarm = new ModelRenderer(this, 48, 0);
      this.rightarm.func_78789_a(-3.0F, -2.0F, -2.0F, 4, 20, 4);
      this.rightarm.func_78793_a(-5.0F, -7.0F, 0.0F);
      this.rightarm.func_78787_b(128, 32);
      this.rightarm.field_78809_i = true;
      this.leftarm = new ModelRenderer(this, 48, 0);
      this.leftarm.func_78789_a(-1.0F, -2.0F, -2.0F, 4, 20, 4);
      this.leftarm.func_78793_a(5.0F, -7.0F, 0.0F);
      this.leftarm.func_78787_b(128, 32);
      this.leftarm.field_78809_i = true;
      this.rightleg = new ModelRenderer(this, 32, 0);
      this.rightleg.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 20, 4);
      this.rightleg.func_78793_a(-2.0F, 4.0F, 0.0F);
      this.rightleg.func_78787_b(128, 32);
      this.rightleg.field_78809_i = true;
      this.leftleg = new ModelRenderer(this, 32, 0);
      this.leftleg.func_78789_a(-2.0F, 0.0F, -2.0F, 4, 20, 4);
      this.leftleg.func_78793_a(2.0F, 4.0F, 0.0F);
      this.leftleg.func_78787_b(128, 32);
      this.leftleg.field_78809_i = false;
      this.wingRight = new ModelRenderer(this, 93, 0);
      this.wingRight.func_78789_a(0.0F, 0.0F, 0.0F, 14, 21, 0);
      this.wingRight.func_78793_a(1.0F, -8.0F, 3.0F);
      this.wingRight.func_78787_b(128, 32);
      this.wingRight.field_78809_i = true;
      this.setRotation(this.wingRight, 0.3047198F, -0.6698132F, -0.6283185F);
      this.wingLeft = new ModelRenderer(this, 93, 0);
      this.wingLeft.func_78789_a(0.0F, 0.0F, 0.0F, 14, 21, 0);
      this.wingLeft.func_78793_a(-1.0F, -8.0F, 3.0F);
      this.wingLeft.func_78787_b(128, 32);
      this.wingLeft.field_78809_i = true;
      this.setRotation(this.wingLeft, -0.3047198F, 3.811406F, 0.6283185F);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.head.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.rightarm.func_78785_a(f5);
      this.leftarm.func_78785_a(f5);
      this.rightleg.func_78785_a(f5);
      this.leftleg.func_78785_a(f5);
      this.wingLeft.func_78785_a(f5);
      this.wingRight.func_78785_a(f5);
   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
      this.head.field_78796_g = par4 / 57.295776F;
      this.head.field_78795_f = par5 / 57.295776F;
      this.leftleg.field_78795_f = -1.5F * this.func_78172_a(par1, 13.0F) * par2;
      this.rightleg.field_78795_f = 1.5F * this.func_78172_a(par1, 13.0F) * par2;
      this.leftleg.field_78796_g = 0.0F;
      this.rightleg.field_78796_g = 0.0F;
   }

   public void func_78086_a(EntityLivingBase par1EntityLiving, float par2, float par3, float par4) {
      EntityDemon entityDemon = (EntityDemon)par1EntityLiving;
      int i = entityDemon.getAttackTimer();
      if (i > 0) {
         this.rightarm.field_78795_f = -2.0F + 1.5F * this.func_78172_a((float)i - par4, 10.0F);
      } else {
         this.rightarm.field_78795_f = (-0.2F + 1.5F * this.func_78172_a(par2, 13.0F)) * par3;
         this.leftarm.field_78795_f = (-0.2F - 1.5F * this.func_78172_a(par2, 13.0F)) * par3;
      }

   }

   private float func_78172_a(float par1, float par2) {
      return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
   }
}
