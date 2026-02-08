package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.client.model.ModelOwl;
import com.emoniph.witchery.entity.EntityOwl;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderOwl extends RenderLiving {
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/entities/owl.png");
   public static final float[][] fleeceColorTable = new float[][]{{1.0F, 1.0F, 1.0F}, {0.85F, 0.5F, 0.2F}, {0.7F, 0.3F, 0.85F}, {0.4F, 0.6F, 0.85F}, {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.5F, 0.65F}, {0.3F, 0.3F, 0.3F}, {0.6F, 0.6F, 0.6F}, {0.3F, 0.5F, 0.6F}, {0.5F, 0.25F, 0.7F}, {0.2F, 0.3F, 0.7F}, {0.4F, 0.3F, 0.2F}, {0.4F, 0.5F, 0.2F}, {0.6F, 0.2F, 0.2F}, {0.1F, 0.1F, 0.1F}};

   public RenderOwl(ModelBase par1ModelBase, float par2) {
      super(par1ModelBase, par2);
   }

   public void doRenderOwl(EntityOwl entity, double par2, double par4, double par6, float par8, float par9) {
      float f1 = 1.0F;
      int j = entity.getFeatherColor();
      if (j == 0) {
         GL11.glColor3f(f1 * fleeceColorTable[j][0], f1 * fleeceColorTable[j][1], f1 * fleeceColorTable[j][2]);
      } else {
         float alpha = 0.84313726F;
         float bR = 0.41568628F;
         float bG = 0.3137255F;
         float bB = 0.24313726F;
         GL11.glColor3f(f1 * fleeceColorTable[j][0] * 0.15686274F + 0.41568628F, f1 * fleeceColorTable[j][1] * 0.15686274F + 0.3137255F, f1 * fleeceColorTable[j][2] * 0.15686274F + 0.24313726F);
      }

      super.func_76986_a(entity, par2, par4, par6, par8, par9);
   }

   protected void rotateOwlCorpse(EntityOwl entity, float par2, float par3, float par4) {
      super.func_77043_a(entity, par2, par3, par4);
   }

   public void func_76986_a(EntityLiving entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderOwl((EntityOwl)entity, par2, par4, par6, par8, par9);
   }

   protected void func_77043_a(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
      this.rotateOwlCorpse((EntityOwl)par1EntityLivingBase, par2, par3, par4);
   }

   public void func_76986_a(EntityLivingBase par1, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderOwl((EntityOwl)par1, par2, par4, par6, par8, par9);
   }

   public void func_76986_a(Entity entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderOwl((EntityOwl)entity, par2, par4, par6, par8, par9);
   }

   protected ResourceLocation func_110775_a(Entity par1Entity) {
      return TEXTURE_URL;
   }

   protected void func_77029_c(EntityLivingBase par1EntityLiving, float par2) {
      GL11.glColor3f(1.0F, 1.0F, 1.0F);
      super.func_77029_c(par1EntityLiving, par2);
      ItemStack itemstack = par1EntityLiving.func_70694_bm();
      if (itemstack != null && itemstack.func_77973_b() != null) {
         Item item = itemstack.func_77973_b();
         GL11.glPushMatrix();
         float f1;
         if (this.field_77045_g.field_78091_s) {
            f1 = 0.5F;
            GL11.glTranslatef(0.0F, 0.625F, 0.0F);
            GL11.glRotatef(-20.0F, -1.0F, 0.0F, 0.0F);
            GL11.glScalef(f1, f1, f1);
         }

         if (par1EntityLiving != null && par1EntityLiving instanceof EntityOwl && ModelOwl.isLanded(par1EntityLiving)) {
            GL11.glTranslatef(-0.0625F, 1.1375F, 0.0625F);
         } else {
            GL11.glTranslatef(-0.0625F, 1.375F, 0.3F);
         }

         IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.EQUIPPED);
         boolean is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(ItemRenderType.EQUIPPED, itemstack, ItemRendererHelper.BLOCK_3D);
         if (!(item instanceof ItemBlock) || !is3D && !RenderBlocks.func_147739_a(Block.func_149634_a(item).func_149645_b())) {
            f1 = 0.375F;
            GL11.glTranslatef(0.25F, 0.1875F, -0.3F);
            GL11.glScalef(f1, f1, f1);
            GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-120.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
         } else {
            f1 = 0.5F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            f1 *= 0.75F;
            GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-f1, -f1, f1);
         }

         float f2;
         float f3;
         int i;
         if (itemstack.func_77973_b().func_77623_v()) {
            for(i = 0; i < itemstack.func_77973_b().getRenderPasses(itemstack.func_77960_j()); ++i) {
               int j = itemstack.func_77973_b().func_82790_a(itemstack, i);
               f2 = (float)(j >> 16 & 255) / 255.0F;
               f3 = (float)(j >> 8 & 255) / 255.0F;
               float f4 = (float)(j & 255) / 255.0F;
               GL11.glColor4f(f2, f3, f4, 1.0F);
               this.field_76990_c.field_78721_f.func_78443_a(par1EntityLiving, itemstack, i);
            }
         } else {
            i = itemstack.func_77973_b().func_82790_a(itemstack, 0);
            float f5 = (float)(i >> 16 & 255) / 255.0F;
            f2 = (float)(i >> 8 & 255) / 255.0F;
            f3 = (float)(i & 255) / 255.0F;
            GL11.glColor4f(f5, f2, f3, 1.0F);
            this.field_76990_c.field_78721_f.func_78443_a(par1EntityLiving, itemstack, 0);
         }

         GL11.glPopMatrix();
      }

   }

   protected void func_82422_c() {
      GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
   }
}
