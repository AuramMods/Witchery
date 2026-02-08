package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.client.model.ModelImp;
import com.emoniph.witchery.entity.EntityImp;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderImp extends RenderLiving {
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/entities/imp.png");

   public RenderImp() {
      super(new ModelImp(), 0.5F);
   }

   public void doRenderDemon(EntityImp entity, double par2, double par4, double par6, float par8, float par9) {
      super.func_76986_a(entity, par2, par4, par6, par8, par9);
   }

   protected void rotateDemonCorpse(EntityImp entity, float par2, float par3, float par4) {
      super.func_77043_a(entity, par2, par3, par4);
   }

   public void func_76986_a(EntityLiving entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderDemon((EntityImp)entity, par2, par4, par6, par8, par9);
   }

   protected void func_77043_a(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
      this.rotateDemonCorpse((EntityImp)par1EntityLivingBase, par2, par3, par4);
   }

   public void func_76986_a(EntityLivingBase par1, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderDemon((EntityImp)par1, par2, par4, par6, par8, par9);
   }

   public void func_76986_a(Entity entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderDemon((EntityImp)entity, par2, par4, par6, par8, par9);
   }

   protected ResourceLocation func_110775_a(Entity par1Entity) {
      return this.func_110832_a((EntityImp)par1Entity);
   }

   protected ResourceLocation func_110832_a(EntityImp par1Entity) {
      return TEXTURE_URL;
   }
}
