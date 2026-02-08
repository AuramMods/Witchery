package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.dimension.WorldProviderTorment;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTormentPortal extends BlockBreakable {
   private static final double MORE_TORMENT_CHANCE = 0.05D;

   public BlockTormentPortal() {
      super("portal", Material.field_151567_E, false);
      this.func_149675_a(true);
      this.func_149722_s();
      this.func_149752_b(9999.0F);
      this.func_149672_a(field_149778_k);
      this.func_149715_a(0.75F);
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, blockName);
      return super.func_149663_c(blockName);
   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      return null;
   }

   public void func_149719_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
      float f;
      float f1;
      if (par1IBlockAccess.func_147439_a(par2 - 1, par3, par4) == Blocks.field_150350_a && par1IBlockAccess.func_147439_a(par2 + 1, par3, par4) == Blocks.field_150350_a) {
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
      if (!world.field_72995_K && entity instanceof EntityPlayer && entity.field_70154_o == null && entity.field_70153_n == null && entity instanceof EntityPlayer) {
         if (entity.field_71093_bK == Config.instance().dimensionTormentID && !(world.field_73012_v.nextDouble() < 0.05D)) {
            WorldProviderTorment.setPlayerMustTorment((EntityPlayer)((EntityPlayer)entity), 3, -2);
         } else {
            WorldProviderTorment.setPlayerMustTorment((EntityPlayer)((EntityPlayer)entity), 1, -1);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return 1;
   }

   public int func_149720_d(IBlockAccess iblockaccess, int x, int y, int z) {
      return 16711714;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
      if (par5Random.nextInt(100) == 0) {
         par1World.func_72980_b((double)par2 + 0.5D, (double)par3 + 0.5D, (double)par4 + 0.5D, "portal.portal", 0.5F, par5Random.nextFloat() * 0.4F + 0.8F, false);
      }

      for(int l = 0; l < 2; ++l) {
         double d0 = (double)((float)par2 + par5Random.nextFloat());
         double d1 = (double)((float)par3 + par5Random.nextFloat());
         double d2 = (double)((float)par4 + par5Random.nextFloat());
         par1World.func_72869_a(ParticleEffect.FLAME.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }
}
