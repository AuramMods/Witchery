package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockStockade;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class RenderStockade implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
      this.drawPost(0.3D, 0.3D, 0.7D, 0.7D, renderer, block, metadata, false, false);
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      BlockStockade blockStockade = (BlockStockade)block;
      boolean flag = true;
      boolean drawLR = false;
      boolean drawUD = false;
      if (blockStockade.canConnectFenceTo(world, x - 1, y, z) || blockStockade.canConnectFenceTo(world, x + 1, y, z)) {
         drawLR = true;
      }

      if (blockStockade.canConnectFenceTo(world, x, y, z - 1) || blockStockade.canConnectFenceTo(world, x, y, z + 1)) {
         drawUD = true;
      }

      boolean oneAbove = blockStockade.canConnectFenceTo(world, x, y + 1, z);
      if (!drawLR && !drawUD) {
         this.drawPost(0.3D, 0.3D, 0.7D, 0.7D, x, y, z, renderer, blockStockade, oneAbove);
      }

      int diagonal = 0;
      if (drawLR && drawUD && diagonal > 1) {
         this.drawPost(0.05D, 0.05D, 0.45D, 0.45D, x, y, z, renderer, blockStockade, oneAbove);
         this.drawPost(0.55D, 0.05D, 0.95D, 0.45D, x, y, z, renderer, blockStockade, oneAbove);
         this.drawPost(0.05D, 0.55D, 0.45D, 0.95D, x, y, z, renderer, blockStockade, oneAbove);
         this.drawPost(0.55D, 0.55D, 0.95D, 0.95D, x, y, z, renderer, blockStockade, oneAbove);
         flag = true;
      } else if (drawLR && drawUD) {
         this.drawPost(0.05D, 0.3D, 0.45D, 0.7D, x, y, z, renderer, blockStockade, oneAbove, true);
         this.drawPost(0.55D, 0.3D, 0.95D, 0.7D, x, y, z, renderer, blockStockade, oneAbove, false);
         this.drawPost(0.3D, 0.05D, 0.7D, 0.45D, x, y, z, renderer, blockStockade, oneAbove, true);
         this.drawPost(0.3D, 0.55D, 0.7D, 0.95D, x, y, z, renderer, blockStockade, oneAbove, false);
         flag = true;
      } else {
         if (drawLR) {
            this.drawPost(0.05D, 0.3D, 0.45D, 0.7D, x, y, z, renderer, blockStockade, oneAbove);
            this.drawPost(0.55D, 0.3D, 0.95D, 0.7D, x, y, z, renderer, blockStockade, oneAbove);
            flag = true;
         }

         if (drawUD) {
            this.drawPost(0.3D, 0.05D, 0.7D, 0.45D, x, y, z, renderer, blockStockade, oneAbove);
            this.drawPost(0.3D, 0.55D, 0.7D, 0.95D, x, y, z, renderer, blockStockade, oneAbove);
            flag = true;
         }
      }

      blockStockade.func_149719_a(world, x, y, z);
      return flag;
   }

   private void drawPost(double xMin, double zMin, double xMax, double zMax, int x, int y, int z, RenderBlocks renderer, Block block, boolean oneAbove) {
      this.drawPost(xMin, zMin, xMax, zMax, x, y, z, renderer, block, oneAbove, false);
   }

   private void drawPost(double xMin, double zMin, double xMax, double zMax, int x, int y, int z, RenderBlocks renderer, Block block, boolean oneAbove, boolean extra) {
      if (!oneAbove) {
         double startY = extra ? 0.6D : 0.5D;
         renderer.func_147782_a(xMin, 0.0D, zMin, xMax, startY, zMax);
         renderer.func_147784_q(block, x, y, z);

         try {
            ((BlockStockade)block).setTipTexture(true);
            double dx = 0.04D;
            double dy = 0.084D;

            for(int i = 0; i < 4; ++i) {
               double reduce = (double)(i + 1) * dx;
               renderer.func_147782_a(xMin + reduce, startY + (double)i * dy, zMin + reduce, xMax - reduce, startY + (double)(i + 1) * dy, zMax - reduce);
               renderer.func_147784_q(block, x, y, z);
            }
         } finally {
            ((BlockStockade)block).setTipTexture(false);
         }
      } else {
         renderer.func_147782_a(xMin, 0.0D, zMin, xMax, 1.0D, zMax);
         renderer.func_147784_q(block, x, y, z);
      }

   }

   private void drawPost(double xMin, double zMin, double xMax, double zMax, RenderBlocks renderer, Block block, int meta, boolean oneAbove, boolean extra) {
      if (!oneAbove) {
         double startY = extra ? 0.6D : 0.5D;
         renderer.func_147782_a(xMin, 0.0D, zMin, xMax, startY, zMax);
         renderStandardInvBlock(renderer, block, meta);

         try {
            ((BlockStockade)block).setTipTexture(true);
            double dx = 0.04D;
            double dy = 0.084D;

            for(int i = 0; i < 4; ++i) {
               double reduce = (double)(i + 1) * dx;
               renderer.func_147782_a(xMin + reduce, startY + (double)i * dy, zMin + reduce, xMax - reduce, startY + (double)(i + 1) * dy, zMax - reduce);
               renderStandardInvBlock(renderer, block, meta);
            }
         } finally {
            ((BlockStockade)block).setTipTexture(false);
         }
      } else {
         renderer.func_147782_a(xMin, 0.0D, zMin, xMax, 1.0D, zMax);
         renderStandardInvBlock(renderer, block, meta);
      }

   }

   public static void renderStandardInvBlock(RenderBlocks renderblocks, Block block, int meta) {
      Tessellator tessellator = Tessellator.field_78398_a;
      GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, -1.0F, 0.0F);
      renderblocks.func_147768_a(block, 0.0D, 0.0D, 0.0D, getIcon(block.func_149691_a(0, meta)));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, 1.0F, 0.0F);
      renderblocks.func_147806_b(block, 0.0D, 0.0D, 0.0D, getIcon(block.func_149691_a(1, meta)));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, 0.0F, -1.0F);
      renderblocks.func_147761_c(block, 0.0D, 0.0D, 0.0D, getIcon(block.func_149691_a(2, meta)));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(0.0F, 0.0F, 1.0F);
      renderblocks.func_147734_d(block, 0.0D, 0.0D, 0.0D, getIcon(block.func_149691_a(3, meta)));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(-1.0F, 0.0F, 0.0F);
      renderblocks.func_147798_e(block, 0.0D, 0.0D, 0.0D, getIcon(block.func_149691_a(4, meta)));
      tessellator.func_78381_a();
      tessellator.func_78382_b();
      tessellator.func_78375_b(1.0F, 0.0F, 0.0F);
      renderblocks.func_147764_f(block, 0.0D, 0.0D, 0.0D, getIcon(block.func_149691_a(5, meta)));
      tessellator.func_78381_a();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
   }

   private static IIcon getIcon(IIcon icon) {
      return (IIcon)(icon != null ? icon : ((TextureMap)Minecraft.func_71410_x().func_110434_K().func_110581_b(TextureMap.field_110575_b)).func_110572_b("missingno"));
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return Witchery.proxy.getStockageRenderId();
   }
}
