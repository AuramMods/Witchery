package com.emoniph.witchery.item;

import net.minecraft.client.renderer.texture.TextureCompass;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class ItemEntityLocatorTexture extends TextureCompass {
   public ItemEntityLocatorTexture() {
      super("witchery:entitylocator");
   }

   public void func_94241_a(World world, double playerX, double playerY, double playerZ, boolean p_94241_8_, boolean p_94241_9_) {
      if (!this.field_110976_a.isEmpty()) {
         double d3 = 0.0D;
         if (world != null && !p_94241_8_) {
            ChunkCoordinates chunkcoordinates = world.func_72861_E();
            double d4 = (double)chunkcoordinates.field_71574_a - playerX;
            double d5 = (double)chunkcoordinates.field_71573_c - playerY;
            playerZ %= 360.0D;
            d3 = -((playerZ - 90.0D) * 3.141592653589793D / 180.0D - Math.atan2(d5, d4));
            if (!world.field_73011_w.func_76569_d()) {
               d3 = Math.random() * 3.141592653589793D * 2.0D;
            }
         }

         if (p_94241_9_) {
            this.field_94244_i = d3;
         } else {
            double d6;
            for(d6 = d3 - this.field_94244_i; d6 < -3.141592653589793D; d6 += 6.283185307179586D) {
            }

            while(d6 >= 3.141592653589793D) {
               d6 -= 6.283185307179586D;
            }

            if (d6 < -1.0D) {
               d6 = -1.0D;
            }

            if (d6 > 1.0D) {
               d6 = 1.0D;
            }

            this.field_94242_j += d6 * 0.1D;
            this.field_94242_j *= 0.8D;
            this.field_94244_i += this.field_94242_j;
         }

         int i;
         for(i = (int)((this.field_94244_i / 6.283185307179586D + 1.0D) * (double)this.field_110976_a.size()) % this.field_110976_a.size(); i < 0; i = (i + this.field_110976_a.size()) % this.field_110976_a.size()) {
         }

         if (i != this.field_110973_g) {
            this.field_110973_g = i;
            TextureUtil.func_147955_a((int[][])((int[][])this.field_110976_a.get(this.field_110973_g)), this.field_130223_c, this.field_130224_d, this.field_110975_c, this.field_110974_d, false, false);
         }
      }

   }
}
