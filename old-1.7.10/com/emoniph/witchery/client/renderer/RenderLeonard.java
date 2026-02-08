package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.entity.EntityLeonard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderLeonard extends RenderLiving {
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/entities/leonard.png");

   public RenderLeonard(ModelBase model, float shadowSize) {
      super(model, shadowSize);
   }

   public void doRenderLeonard(EntityLeonard entity, double x, double y, double z, float par8, float par9) {
      BossStatus.func_82824_a(entity, true);
      super.func_76986_a(entity, x, y, z, par8, par9);
   }

   protected void rotateLeonardCorpse(EntityLeonard entity, float x, float y, float z) {
      super.func_77043_a(entity, x, y, z);
   }

   public void func_76986_a(EntityLiving entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderLeonard((EntityLeonard)entity, par2, par4, par6, par8, par9);
   }

   protected void func_77043_a(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
      this.rotateLeonardCorpse((EntityLeonard)par1EntityLivingBase, par2, par3, par4);
   }

   public void func_76986_a(EntityLivingBase entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderLeonard((EntityLeonard)entity, par2, par4, par6, par8, par9);
   }

   public void func_76986_a(Entity entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderLeonard((EntityLeonard)entity, par2, par4, par6, par8, par9);
   }

   protected ResourceLocation func_110775_a(Entity entity) {
      return TEXTURE_URL;
   }
}
