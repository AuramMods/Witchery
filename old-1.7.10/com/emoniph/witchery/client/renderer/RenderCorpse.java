package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.entity.EntityCorpse;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderCorpse extends RenderLiving {
   public RenderCorpse() {
      super(new ModelBiped() {
         public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
         }
      }, 0.0F);
   }

   public void doRenderCorpse(EntityCorpse entity, double par2, double par4, double par6, float par8, float par9) {
      GL11.glPushMatrix();
      super.func_76986_a(entity, par2, par4, par6, par8, par9);
      GL11.glPopMatrix();
   }

   protected ResourceLocation getEntityTexture(EntityCorpse entity) {
      return entity.getLocationSkin();
   }

   protected void func_77043_a(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
      GL11.glTranslatef(0.9F, 0.25F, 0.0F);
      GL11.glRotatef(this.func_77037_a(par1EntityLivingBase), 0.0F, 0.0F, 1.0F);
      GL11.glRotatef(this.func_77037_a(par1EntityLivingBase), 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(180.0F - par3, 0.0F, 1.0F, 0.0F);
      if (par1EntityLivingBase.field_70725_aQ > 0) {
         float f3 = ((float)par1EntityLivingBase.field_70725_aQ + par4 - 1.0F) / 20.0F * 1.6F;
         f3 = MathHelper.func_76129_c(f3);
         if (f3 > 1.0F) {
            f3 = 1.0F;
         }

         GL11.glRotatef(f3 * this.func_77037_a(par1EntityLivingBase), 0.0F, 1.0F, 0.0F);
      } else {
         String s = EnumChatFormatting.func_110646_a(par1EntityLivingBase.func_70005_c_());
         if ((s.equals("Dinnerbone") || s.equals("Grumm")) && (!(par1EntityLivingBase instanceof EntityPlayer) || !((EntityPlayer)par1EntityLivingBase).func_82238_cc())) {
            GL11.glTranslatef(0.0F, par1EntityLivingBase.field_70131_O + 0.1F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
         }
      }

   }

   protected float func_77040_d(EntityLivingBase par1EntityLivingBase, float par2) {
      return 0.0F;
   }

   public void func_76986_a(EntityLiving entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderCorpse((EntityCorpse)entity, par2, par4, par6, par8, par9);
   }

   public void func_76986_a(EntityLivingBase par1, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderCorpse((EntityCorpse)par1, par2, par4, par6, par8, par9);
   }

   public void func_76986_a(Entity entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderCorpse((EntityCorpse)entity, par2, par4, par6, par8, par9);
   }

   protected ResourceLocation func_110775_a(Entity par1Entity) {
      return this.getEntityTexture((EntityCorpse)par1Entity);
   }
}
