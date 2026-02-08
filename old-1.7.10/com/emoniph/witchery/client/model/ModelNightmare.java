package com.emoniph.witchery.client.model;

import com.emoniph.witchery.entity.EntityNightmare;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelNightmare extends ModelBase {
   ModelRenderer head;
   ModelRenderer body;
   ModelRenderer leftarm;
   ModelRenderer leg7;
   ModelRenderer leg6;
   ModelRenderer leg5;
   ModelRenderer leg4;
   ModelRenderer leg3;
   ModelRenderer leg2;
   ModelRenderer leg1;
   ModelRenderer rightfingerlittle;
   ModelRenderer rightfingerindex;
   ModelRenderer rightfingerthumb;
   ModelRenderer rightarm;
   ModelRenderer leftfingerlittle;
   ModelRenderer leftfingerindex;
   ModelRenderer leftfingerthumb;

   public ModelNightmare() {
      this.field_78090_t = 64;
      this.field_78089_u = 64;
      this.head = new ModelRenderer(this, 0, 0);
      this.head.func_78789_a(-4.0F, -8.0F, -3.0F, 8, 8, 6);
      this.head.func_78793_a(0.0F, -6.0F, 0.0F);
      this.head.func_78787_b(64, 64);
      this.head.field_78809_i = true;
      this.setRotation(this.head, 0.0F, 0.0F, 0.0F);
      this.body = new ModelRenderer(this, 16, 16);
      this.body.func_78789_a(-4.0F, 0.0F, -2.0F, 8, 12, 4);
      this.body.func_78793_a(0.0F, -6.0F, 0.0F);
      this.body.func_78787_b(64, 64);
      this.body.field_78809_i = true;
      this.setRotation(this.body, 0.0F, 0.0F, 0.0F);
      this.leftarm = new ModelRenderer(this, 40, 16);
      this.leftarm.func_78789_a(-1.0F, -2.0F, -1.0F, 2, 16, 2);
      this.leftarm.func_78793_a(5.0F, -4.0F, 0.0F);
      this.leftarm.func_78787_b(64, 64);
      this.leftarm.field_78809_i = true;
      this.setRotation(this.leftarm, 0.0F, 0.0F, 0.0F);
      this.leg7 = new ModelRenderer(this, 12, 16);
      this.leg7.func_78789_a(0.0F, 0.0F, 0.0F, 1, 16, 1);
      this.leg7.func_78793_a(-1.0F, 6.0F, 0.0F);
      this.leg7.func_78787_b(64, 64);
      this.leg7.field_78809_i = true;
      this.setRotation(this.leg7, 0.0F, 0.0F, 0.0F);
      this.leg6 = new ModelRenderer(this, 8, 16);
      this.leg6.func_78789_a(0.0F, 0.0F, 0.0F, 1, 17, 1);
      this.leg6.func_78793_a(1.0F, 6.0F, -1.0F);
      this.leg6.func_78787_b(64, 64);
      this.leg6.field_78809_i = true;
      this.setRotation(this.leg6, 0.0F, 0.0F, 0.0F);
      this.leg5 = new ModelRenderer(this, 4, 16);
      this.leg5.func_78789_a(0.0F, 0.0F, 0.0F, 1, 12, 1);
      this.leg5.func_78793_a(-3.0F, 6.0F, 0.0F);
      this.leg5.func_78787_b(64, 64);
      this.leg5.field_78809_i = true;
      this.setRotation(this.leg5, 0.0F, 0.0F, 0.0F);
      this.leg4 = new ModelRenderer(this, 8, 16);
      this.leg4.func_78789_a(0.0F, 0.0F, 0.0F, 1, 14, 1);
      this.leg4.func_78793_a(-2.0F, 6.0F, -1.0F);
      this.leg4.func_78787_b(64, 64);
      this.leg4.field_78809_i = true;
      this.setRotation(this.leg4, 0.0F, 0.0F, 0.0F);
      this.leg3 = new ModelRenderer(this, 12, 16);
      this.leg3.func_78789_a(0.0F, 0.0F, 0.0F, 1, 11, 1);
      this.leg3.func_78793_a(0.0F, 6.0F, -1.0F);
      this.leg3.func_78787_b(64, 64);
      this.leg3.field_78809_i = true;
      this.setRotation(this.leg3, 0.0F, 0.0F, 0.0F);
      this.leg2 = new ModelRenderer(this, 0, 16);
      this.leg2.func_78789_a(0.0F, 0.0F, 0.0F, 1, 14, 1);
      this.leg2.func_78793_a(2.0F, 6.0F, 0.0F);
      this.leg2.func_78787_b(64, 64);
      this.leg2.field_78809_i = true;
      this.setRotation(this.leg2, 0.0F, 0.0F, 0.0F);
      this.leg1 = new ModelRenderer(this, 0, 16);
      this.leg1.func_78789_a(0.0F, 0.0F, 0.0F, 1, 3, 1);
      this.leg1.func_78793_a(-3.0F, 6.0F, -1.0F);
      this.leg1.func_78787_b(64, 64);
      this.leg1.field_78809_i = true;
      this.setRotation(this.leg1, 0.0F, 0.0F, 0.0F);
      this.rightfingerlittle = new ModelRenderer(this, 0, 46);
      this.rightfingerlittle.func_78789_a(-1.0F, 0.0F, -1.0F, 1, 6, 1);
      this.rightfingerlittle.func_78793_a(-5.0F, 8.0F, 0.0F);
      this.rightfingerlittle.func_78787_b(64, 64);
      this.rightfingerlittle.field_78809_i = true;
      this.setRotation(this.rightfingerlittle, -0.148353F, 0.0F, 0.1134464F);
      this.rightfingerindex = new ModelRenderer(this, 4, 46);
      this.rightfingerindex.func_78789_a(0.0F, 0.0F, -1.0F, 1, 6, 1);
      this.rightfingerindex.func_78793_a(-5.0F, 8.0F, 0.0F);
      this.rightfingerindex.func_78787_b(64, 64);
      this.rightfingerindex.field_78809_i = true;
      this.setRotation(this.rightfingerindex, -0.148353F, 0.0F, -0.1134464F);
      this.rightfingerthumb = new ModelRenderer(this, 8, 46);
      this.rightfingerthumb.func_78789_a(-0.5F, 0.0F, 0.0F, 1, 6, 1);
      this.rightfingerthumb.func_78793_a(-5.0F, 8.0F, 0.0F);
      this.rightfingerthumb.func_78787_b(64, 64);
      this.rightfingerthumb.field_78809_i = true;
      this.setRotation(this.rightfingerthumb, 0.1396263F, 0.0F, 0.0F);
      this.rightarm = new ModelRenderer(this, 40, 16);
      this.rightarm.func_78789_a(-1.0F, -2.0F, -1.0F, 2, 16, 2);
      this.rightarm.func_78793_a(-5.0F, -4.0F, 0.0F);
      this.rightarm.func_78787_b(64, 64);
      this.rightarm.field_78809_i = true;
      this.setRotation(this.rightarm, 0.0F, 0.0F, 0.0F);
      this.leftfingerlittle = new ModelRenderer(this, 8, 53);
      this.leftfingerlittle.func_78789_a(0.0F, 0.0F, -1.0F, 1, 6, 1);
      this.leftfingerlittle.func_78793_a(5.0F, 8.0F, 0.0F);
      this.leftfingerlittle.func_78787_b(64, 64);
      this.leftfingerlittle.field_78809_i = true;
      this.setRotation(this.leftfingerlittle, -0.148353F, 0.0F, -0.1134464F);
      this.leftfingerindex = new ModelRenderer(this, 4, 53);
      this.leftfingerindex.func_78789_a(-1.0F, 0.0F, -1.0F, 1, 6, 1);
      this.leftfingerindex.func_78793_a(5.0F, 8.0F, 0.0F);
      this.leftfingerindex.func_78787_b(64, 64);
      this.leftfingerindex.field_78809_i = true;
      this.setRotation(this.leftfingerindex, -0.148353F, 0.0F, 0.1134464F);
      this.leftfingerthumb = new ModelRenderer(this, 0, 53);
      this.leftfingerthumb.func_78789_a(-0.5F, 0.0F, 0.0F, 1, 6, 1);
      this.leftfingerthumb.func_78793_a(5.0F, 8.0F, 0.0F);
      this.leftfingerthumb.func_78787_b(64, 64);
      this.leftfingerthumb.field_78809_i = true;
      this.setRotation(this.leftfingerthumb, 0.1396263F, 0.0F, 0.0F);
      this.leftarm.func_78792_a(this.leftfingerindex);
      this.leftarm.func_78792_a(this.leftfingerlittle);
      this.leftarm.func_78792_a(this.leftfingerthumb);
      this.rightarm.func_78792_a(this.rightfingerindex);
      this.rightarm.func_78792_a(this.rightfingerlittle);
      this.rightarm.func_78792_a(this.rightfingerthumb);
      this.leftfingerlittle.func_78793_a(0.0F, 12.0F, 0.0F);
      this.leftfingerindex.func_78793_a(0.0F, 12.0F, 0.0F);
      this.leftfingerthumb.func_78793_a(0.0F, 12.0F, 0.0F);
      this.rightfingerlittle.func_78793_a(0.0F, 12.0F, 0.0F);
      this.rightfingerindex.func_78793_a(0.0F, 12.0F, 0.0F);
      this.rightfingerthumb.func_78793_a(0.0F, 12.0F, 0.0F);
      this.leg1.field_78800_c = -2.0F;
      this.leg2.field_78800_c = 2.0F;
      this.leg3.field_78800_c = 1.0F;
      this.leg4.field_78800_c = -1.0F;
      this.leg5.field_78800_c = -2.0F;
      this.leg6.field_78800_c = 3.0F;
      this.leg7.field_78800_c = 0.0F;
   }

   public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.head.func_78785_a(f5);
      this.body.func_78785_a(f5);
      this.leftarm.func_78785_a(f5);
      this.leg7.func_78785_a(f5);
      this.leg6.func_78785_a(f5);
      this.leg5.func_78785_a(f5);
      this.leg4.func_78785_a(f5);
      this.leg3.func_78785_a(f5);
      this.leg2.func_78785_a(f5);
      this.leg1.func_78785_a(f5);
      this.rightarm.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78086_a(EntityLivingBase par1EntityLiving, float par2, float par3, float par4) {
      EntityNightmare entityDemon = (EntityNightmare)par1EntityLiving;
      int i = entityDemon.getAttackTimer();
      if (i > 0) {
         this.rightarm.field_78795_f = -1.5F + 0.8F * this.func_78172_a((float)i - par4, 15.0F);
         this.leftarm.field_78795_f = -1.5F + 0.8F * this.func_78172_a((float)i - par4, 15.0F);
         this.rightarm.field_78808_h = -(-1.5F + 1.5F * this.func_78172_a((float)i - par4, 15.0F));
         this.leftarm.field_78808_h = -1.5F + 1.5F * this.func_78172_a((float)i - par4, 15.0F);
      } else {
         this.leftarm.field_78795_f = -0.1F;
         this.rightarm.field_78795_f = -0.1F;
         this.leftarm.field_78808_h = 0.0F;
         this.rightarm.field_78808_h = 0.0F;
      }

   }

   public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
      super.func_78087_a(par1, par2, par3, par4, par5, par6, entity);
      this.head.field_78796_g = par4 / 57.295776F;
      this.head.field_78795_f = par5 / 57.295776F;
      float f6 = 0.01F * (float)(entity.func_145782_y() % 10);
      this.leg7.field_78795_f = -1.8F + MathHelper.func_76126_a((float)entity.field_70173_aa * f6) * 4.5F * 3.1415927F / 180.0F;
      this.leg7.field_78796_g = -1.7F;
      this.leg7.field_78808_h = 1.839205F + MathHelper.func_76134_b((float)entity.field_70173_aa * f6) * 2.5F * 3.1415927F / 180.0F;
      f6 = 0.01F * (float)(entity.func_145782_y() % 10);
      this.leg6.field_78795_f = -1.8F + MathHelper.func_76126_a((float)entity.field_70173_aa * f6) * 4.5F * 3.1415927F / 180.0F;
      this.leg6.field_78796_g = -1.7F;
      this.leg6.field_78808_h = 1.839205F + MathHelper.func_76134_b((float)entity.field_70173_aa * f6) * 2.5F * 3.1415927F / 180.0F;
      f6 = 0.01F * (float)(entity.func_145782_y() % 12);
      this.leg5.field_78795_f = -1.8F + MathHelper.func_76126_a((float)entity.field_70173_aa * f6) * 4.5F * 3.1415927F / 180.0F;
      this.leg5.field_78796_g = -1.7F;
      this.leg5.field_78808_h = 1.839205F + MathHelper.func_76134_b((float)entity.field_70173_aa * f6) * 2.5F * 3.1415927F / 180.0F;
      f6 = 0.01F * (float)(entity.func_145782_y() % 10);
      this.leg4.field_78795_f = -1.8F + MathHelper.func_76126_a((float)entity.field_70173_aa * f6) * 4.5F * 3.1415927F / 180.0F;
      this.leg4.field_78796_g = -1.7F;
      this.leg4.field_78808_h = 1.839205F + MathHelper.func_76134_b((float)entity.field_70173_aa * f6) * 2.5F * 3.1415927F / 180.0F;
      f6 = 0.01F * (float)(entity.func_145782_y() % 13);
      this.leg3.field_78795_f = -1.8F + MathHelper.func_76126_a((float)entity.field_70173_aa * f6) * 4.5F * 3.1415927F / 180.0F;
      this.leg3.field_78796_g = -1.7F;
      this.leg3.field_78808_h = 1.839205F + MathHelper.func_76134_b((float)entity.field_70173_aa * f6) * 2.5F * 3.1415927F / 180.0F;
      f6 = 0.01F * (float)(entity.func_145782_y() % 12);
      this.leg2.field_78795_f = -1.8F + MathHelper.func_76126_a((float)entity.field_70173_aa * f6) * 4.5F * 3.1415927F / 180.0F;
      this.leg2.field_78796_g = -1.7F;
      this.leg2.field_78808_h = 1.839205F + MathHelper.func_76134_b((float)entity.field_70173_aa * f6) * 2.5F * 3.1415927F / 180.0F;
      f6 = 0.01F * (float)(entity.func_145782_y() % 12);
      this.leg1.field_78795_f = -1.8F + MathHelper.func_76126_a((float)entity.field_70173_aa * f6) * 4.5F * 3.1415927F / 180.0F;
      this.leg1.field_78796_g = -1.7F;
      this.leg1.field_78808_h = 1.839205F + MathHelper.func_76134_b((float)entity.field_70173_aa * f6) * 2.5F * 3.1415927F / 180.0F;
   }

   private float func_78172_a(float par1, float par2) {
      return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
   }
}
