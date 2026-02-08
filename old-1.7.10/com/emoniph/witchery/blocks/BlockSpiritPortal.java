package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.dimension.WorldProviderDreamWorld;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSpiritPortal extends BlockBreakable {
   private final Block portalFrameBlock;

   public BlockSpiritPortal(Block portalFrameBlock) {
      super("portal", Material.field_151567_E, false);
      this.func_149675_a(true);
      this.portalFrameBlock = portalFrameBlock;
      this.func_149711_c(-1.0F);
      this.func_149672_a(field_149778_k);
      this.func_149715_a(0.75F);
      this.func_149647_a((CreativeTabs)null);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, blockName);
      return super.func_149663_c(blockName);
   }

   public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
      super.func_149674_a(par1World, par2, par3, par4, par5Random);
   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      return null;
   }

   public void func_149719_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
      float f;
      float f1;
      if (par1IBlockAccess.func_147439_a(par2 - 1, par3, par4) != this && par1IBlockAccess.func_147439_a(par2 + 1, par3, par4) != this) {
         f = 0.125F;
         f1 = 0.5F;
         this.func_149676_a(0.5F - f, 0.0F, 0.5F - f1, 0.5F + f, 1.0F, 0.5F + f1);
      } else {
         f = 0.5F;
         f1 = 0.125F;
         this.func_149676_a(0.5F - f, 0.0F, 0.5F - f1, 0.5F + f, 1.0F, 0.5F + f1);
      }

   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      byte b0 = 0;
      byte b1 = 1;
      if (par1World.func_147439_a(par2 - 1, par3, par4) == this || par1World.func_147439_a(par2 + 1, par3, par4) == this) {
         b0 = 1;
         b1 = 0;
      }

      int i1;
      for(i1 = par3; par1World.func_147439_a(par2, i1 - 1, par4) == this; --i1) {
      }

      if (par1World.func_147439_a(par2, i1 - 1, par4) != this.portalFrameBlock) {
         par1World.func_147468_f(par2, par3, par4);
      } else {
         int j1;
         for(j1 = 1; j1 < 3 && par1World.func_147439_a(par2, i1 + j1, par4) == this; ++j1) {
         }

         if (j1 == 2 && par1World.func_147439_a(par2, i1 + j1, par4) == this.portalFrameBlock) {
            boolean flag = par1World.func_147439_a(par2 - 1, par3, par4) == this || par1World.func_147439_a(par2 + 1, par3, par4) == this;
            boolean flag1 = par1World.func_147439_a(par2, par3, par4 - 1) == this || par1World.func_147439_a(par2, par3, par4 + 1) == this;
            if (flag && flag1) {
               par1World.func_147468_f(par2, par3, par4);
            } else if (par1World.func_147439_a(par2 + b0, par3, par4 + b1) != this.portalFrameBlock && par1World.func_147439_a(par2 - b0, par3, par4 - b1) != this.portalFrameBlock) {
               par1World.func_147468_f(par2, par3, par4);
            }
         } else {
            par1World.func_147468_f(par2, par3, par4);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      if (par1IBlockAccess.func_147439_a(par2, par3, par4) == this) {
         return false;
      } else {
         boolean flag = par1IBlockAccess.func_147439_a(par2 - 1, par3, par4) == this && par1IBlockAccess.func_147439_a(par2 - 2, par3, par4) != this;
         boolean flag1 = par1IBlockAccess.func_147439_a(par2 + 1, par3, par4) == this && par1IBlockAccess.func_147439_a(par2 + 2, par3, par4) != this;
         boolean flag2 = par1IBlockAccess.func_147439_a(par2, par3, par4 - 1) == this && par1IBlockAccess.func_147439_a(par2, par3, par4 - 2) != this;
         boolean flag3 = par1IBlockAccess.func_147439_a(par2, par3, par4 + 1) == this && par1IBlockAccess.func_147439_a(par2, par3, par4 + 2) != this;
         boolean flag4 = flag || flag1;
         boolean flag5 = flag2 || flag3;
         return flag4 && par5 == 4 ? true : (flag4 && par5 == 5 ? true : (flag5 && par5 == 2 ? true : flag5 && par5 == 3));
      }
   }

   public int func_149745_a(Random par1Random) {
      return 0;
   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && entity instanceof EntityPlayer && entity.field_71093_bK == Config.instance().dimensionDreamID && entity.field_70154_o == null && entity.field_70153_n == null && WorldProviderDreamWorld.canPlayerManifest((EntityPlayer)entity)) {
         WorldProviderDreamWorld.manifestPlayerInOverworldAsGhost((EntityPlayer)entity);
      }

   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return 1;
   }

   public int func_149720_d(IBlockAccess iblockaccess, int x, int y, int z) {
      return 65382;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
      for(int l = 0; l < 2; ++l) {
         double d0 = (double)((float)par2 + par5Random.nextFloat());
         double d1 = (double)((float)par3 + par5Random.nextFloat());
         double d2 = (double)((float)par4 + par5Random.nextFloat());
         double d3 = 0.0D;
         double d4 = 0.0D;
         double d5 = 0.0D;
         int i1 = par5Random.nextInt(2) * 2 - 1;
         d3 = ((double)par5Random.nextFloat() - 0.5D) * 0.5D;
         d4 = ((double)par5Random.nextFloat() - 0.5D) * 0.5D;
         d5 = ((double)par5Random.nextFloat() - 0.5D) * 0.5D;
         if (par1World.func_147439_a(par2 - 1, par3, par4) != this && par1World.func_147439_a(par2 + 1, par3, par4) != this) {
            d0 = (double)par2 + 0.5D + 0.25D * (double)i1;
            d3 = (double)(par5Random.nextFloat() * 2.0F * (float)i1);
         } else {
            d2 = (double)par4 + 0.5D + 0.25D * (double)i1;
            d5 = (double)(par5Random.nextFloat() * 2.0F * (float)i1);
         }

         par1World.func_72869_a(ParticleEffect.DRIP_WATER.toString(), d0, d1, d2, d3, d4, d5);
      }

   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }

   public boolean tryToCreatePortal(World par1World, int par2, int par3, int par4) {
      byte b0 = 0;
      byte b1 = 0;
      if (par1World.func_147439_a(par2 - 1, par3, par4) == this.portalFrameBlock || par1World.func_147439_a(par2 + 1, par3, par4) == this.portalFrameBlock) {
         b0 = 1;
      }

      if (par1World.func_147439_a(par2, par3, par4 - 1) == this.portalFrameBlock || par1World.func_147439_a(par2, par3, par4 + 1) == this.portalFrameBlock) {
         b1 = 1;
      }

      if (b0 == b1) {
         return false;
      } else {
         if (par1World.func_147437_c(par2 - b0, par3, par4 - b1)) {
            par2 -= b0;
            par4 -= b1;
         }

         int WIDTH = true;
         int HEIGHT = true;

         int l;
         int i1;
         for(l = -1; l <= 2; ++l) {
            for(i1 = -1; i1 <= 2; ++i1) {
               boolean flag = l == -1 || l == 2 || i1 == -1 || i1 == 2;
               if (l != -1 && l != 2 || i1 != -1 && i1 != 2) {
                  Block j1 = par1World.func_147439_a(par2 + b0 * l, par3 + i1, par4 + b1 * l);
                  boolean isAirBlock = par1World.func_147437_c(par2 + b0 * l, par3 + i1, par4 + b1 * l);
                  if (flag) {
                     if (j1 != this.portalFrameBlock) {
                        return false;
                     }
                  } else if (!isAirBlock && j1 != Witchery.Blocks.FLOWING_SPIRIT) {
                     return false;
                  }
               }
            }
         }

         for(l = 0; l < 2; ++l) {
            for(i1 = 0; i1 < 2; ++i1) {
               par1World.func_147465_d(par2 + b0 * l, par3 + i1, par4 + b1 * l, this, 0, 2);
            }
         }

         return true;
      }
   }
}
