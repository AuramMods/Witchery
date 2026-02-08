package com.emoniph.witchery.blocks;

import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGlowGlobe extends BlockBase {
   public BlockGlowGlobe() {
      super(Material.field_151592_s);
      super.registerWithCreateTab = false;
      this.func_149711_c(0.0F);
      this.func_149715_a(0.9375F);
      this.func_149649_H();
      float f = 0.1F;
      this.func_149676_a(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F);
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      return null;
   }

   public int func_149645_b() {
      return 1;
   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return 1;
   }

   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      if (rand.nextInt(3) != 0) {
         double d0 = (double)((float)x + 0.45F + (float)rand.nextInt(3) * 0.05F);
         double d1 = (double)((float)y + 0.4F + (float)rand.nextInt(4) * 0.1F);
         double d2 = (double)((float)z + 0.45F + (float)rand.nextInt(3) * 0.05F);
         world.func_72869_a(ParticleEffect.FLAME.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   public int func_149745_a(Random par1Random) {
      return 0;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }
}
