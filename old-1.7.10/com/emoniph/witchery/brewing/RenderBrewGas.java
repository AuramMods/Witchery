package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

public class RenderBrewGas implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      Tessellator tessellator = Tessellator.field_78398_a;
      int l = block.func_149720_d(world, x, y, z);
      float red = (float)(l >> 16 & 255) / 255.0F;
      float green = (float)(l >> 8 & 255) / 255.0F;
      float blue = (float)(l & 255) / 255.0F;
      boolean flag = block.func_149646_a(world, x, y + 1, z, 1);
      boolean flag1 = block.func_149646_a(world, x, y - 1, z, 0);
      boolean[] aboolean = new boolean[]{block.func_149646_a(world, x, y, z - 1, 2), block.func_149646_a(world, x, y, z + 1, 3), block.func_149646_a(world, x - 1, y, z, 4), block.func_149646_a(world, x + 1, y, z, 5)};
      float opacityInner = 0.2F;
      float opacityOuter = 0.4F;
      renderer.field_147837_f = true;
      if (!renderer.field_147837_f && !flag && !flag1 && !aboolean[0] && !aboolean[1] && !aboolean[2] && !aboolean[3]) {
         return false;
      } else {
         boolean flag2 = false;
         float f3 = 0.5F;
         float f4 = 1.0F;
         float f5 = 0.8F;
         float f6 = 0.6F;
         double d0 = 0.0D;
         double d1 = 1.0D;
         Material material = block.func_149688_o();
         int i1 = world.func_72805_g(x, y, z);
         double d2 = 1.0D;
         double d3 = 1.0D;
         double d4 = 1.0D;
         double d5 = 1.0D;
         double d6 = 0.0010000000474974513D;
         float f9;
         float f10;
         float f11;
         double d9;
         double d11;
         double d13;
         double d15;
         double d17;
         double d19;
         if (renderer.field_147837_f || flag) {
            flag2 = true;
            IIcon iicon = renderer.func_147787_a(block, 1, i1);
            float f7 = 0.0F;
            if (f7 > -999.0F) {
               iicon = renderer.func_147787_a(block, 2, i1);
            }

            d2 -= d6;
            d3 -= d6;
            d4 -= d6;
            d5 -= d6;
            double d7;
            double d20;
            if (f7 < -999.0F) {
               d7 = (double)iicon.func_94214_a(0.0D);
               d15 = (double)iicon.func_94207_b(0.0D);
               d9 = d7;
               d17 = (double)iicon.func_94207_b(16.0D);
               d11 = (double)iicon.func_94214_a(16.0D);
               d19 = d17;
               d13 = d11;
               d20 = d15;
            } else {
               f9 = MathHelper.func_76126_a(f7) * 0.25F;
               f10 = MathHelper.func_76134_b(f7) * 0.25F;
               f11 = 8.0F;
               d7 = (double)iicon.func_94214_a((double)(8.0F + (-f10 - f9) * 16.0F));
               d15 = (double)iicon.func_94207_b((double)(8.0F + (-f10 + f9) * 16.0F));
               d9 = (double)iicon.func_94214_a((double)(8.0F + (-f10 + f9) * 16.0F));
               d17 = (double)iicon.func_94207_b((double)(8.0F + (f10 + f9) * 16.0F));
               d11 = (double)iicon.func_94214_a((double)(8.0F + (f10 + f9) * 16.0F));
               d19 = (double)iicon.func_94207_b((double)(8.0F + (f10 - f9) * 16.0F));
               d13 = (double)iicon.func_94214_a((double)(8.0F + (f10 - f9) * 16.0F));
               d20 = (double)iicon.func_94207_b((double)(8.0F + (-f10 - f9) * 16.0F));
            }

            tessellator.func_78380_c(block.func_149677_c(world, x, y, z));
            tessellator.func_78369_a(f4 * red, f4 * green, f4 * blue, flag ? opacityOuter : opacityInner);
            tessellator.func_78374_a((double)(x + 0), (double)y + d2, (double)(z + 0), d7, d15);
            tessellator.func_78374_a((double)(x + 0), (double)y + d3, (double)(z + 1), d9, d17);
            tessellator.func_78374_a((double)(x + 1), (double)y + d4, (double)(z + 1), d11, d19);
            tessellator.func_78374_a((double)(x + 1), (double)y + d5, (double)(z + 0), d13, d20);
            tessellator.func_78374_a((double)(x + 0), (double)y + d2, (double)(z + 0), d7, d15);
            tessellator.func_78374_a((double)(x + 1), (double)y + d5, (double)(z + 0), d13, d20);
            tessellator.func_78374_a((double)(x + 1), (double)y + d4, (double)(z + 1), d11, d19);
            tessellator.func_78374_a((double)(x + 0), (double)y + d3, (double)(z + 1), d9, d17);
         }

         if (renderer.field_147837_f || flag1) {
            tessellator.func_78380_c(block.func_149677_c(world, x, y - 1, z));
            tessellator.func_78369_a(f4 * red, f4 * green, f4 * blue, flag1 ? opacityOuter : opacityInner);
            renderer.func_147768_a(block, (double)x, (double)y + d6, (double)z, renderer.func_147777_a(block, 0));
            flag2 = true;
         }

         for(int k1 = 0; k1 < 4; ++k1) {
            int l1 = x;
            int j1 = z;
            if (k1 == 0) {
               j1 = z - 1;
            }

            if (k1 == 1) {
               ++j1;
            }

            if (k1 == 2) {
               l1 = x - 1;
            }

            if (k1 == 3) {
               ++l1;
            }

            IIcon iicon1 = renderer.func_147787_a(block, k1 + 2, i1);
            if (renderer.field_147837_f || aboolean[k1]) {
               if (k1 == 0) {
                  d9 = d2;
                  d11 = d5;
                  d13 = (double)x;
                  d17 = (double)(x + 1);
                  d15 = (double)z + d6;
                  d19 = (double)z + d6;
               } else if (k1 == 1) {
                  d9 = d4;
                  d11 = d3;
                  d13 = (double)(x + 1);
                  d17 = (double)x;
                  d15 = (double)(z + 1) - d6;
                  d19 = (double)(z + 1) - d6;
               } else if (k1 == 2) {
                  d9 = d3;
                  d11 = d2;
                  d13 = (double)x + d6;
                  d17 = (double)x + d6;
                  d15 = (double)(z + 1);
                  d19 = (double)z;
               } else {
                  d9 = d5;
                  d11 = d4;
                  d13 = (double)(x + 1) - d6;
                  d17 = (double)(x + 1) - d6;
                  d15 = (double)z;
                  d19 = (double)(z + 1);
               }

               flag2 = true;
               float f8 = iicon1.func_94214_a(0.0D);
               f9 = iicon1.func_94214_a(8.0D);
               f10 = iicon1.func_94207_b((1.0D - d9) * 16.0D * 0.5D);
               f11 = iicon1.func_94207_b((1.0D - d11) * 16.0D * 0.5D);
               float f12 = iicon1.func_94207_b(8.0D);
               tessellator.func_78380_c(block.func_149677_c(world, l1, y, j1));
               float f13 = 1.0F;
               float var10000 = f13 * (k1 < 2 ? f5 : f6);
               tessellator.func_78369_a(f4 * red, f4 * green, f4 * blue, aboolean[k1] ? opacityOuter : opacityInner);
               tessellator.func_78374_a(d13, (double)y + d9, d15, (double)f8, (double)f10);
               tessellator.func_78374_a(d17, (double)y + d11, d19, (double)f9, (double)f11);
               tessellator.func_78374_a(d17, (double)(y + 0), d19, (double)f9, (double)f12);
               tessellator.func_78374_a(d13, (double)(y + 0), d15, (double)f8, (double)f12);
               tessellator.func_78374_a(d13, (double)(y + 0), d15, (double)f8, (double)f12);
               tessellator.func_78374_a(d17, (double)(y + 0), d19, (double)f9, (double)f12);
               tessellator.func_78374_a(d17, (double)y + d11, d19, (double)f9, (double)f11);
               tessellator.func_78374_a(d13, (double)y + d9, d15, (double)f8, (double)f10);
            }
         }

         renderer.field_147855_j = d0;
         renderer.field_147857_k = d1;
         return flag2;
      }
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return false;
   }

   public int getRenderId() {
      return Witchery.proxy.getGasRenderId();
   }
}
