package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDemonHeart extends BlockBaseContainer {
   public BlockDemonHeart() {
      super(Material.field_151578_c, BlockDemonHeart.TileEntityDemonHeart.class);
      super.registerWithCreateTab = false;
      this.func_149715_a(0.2F);
      this.func_149711_c(1.0F);
      this.func_149672_a(field_149767_g);
      this.func_149676_a(0.25F, 0.0F, 0.25F, 0.75F, 0.8F, 0.75F);
   }

   public void func_149689_a(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
      int l = MathHelper.func_76128_c((double)(par5EntityLivingBase.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
      if (l == 0) {
         par1World.func_72921_c(par2, par3, par4, 2, 2);
      }

      if (l == 1) {
         par1World.func_72921_c(par2, par3, par4, 5, 2);
      }

      if (l == 2) {
         par1World.func_72921_c(par2, par3, par4, 3, 2);
      }

      if (l == 3) {
         par1World.func_72921_c(par2, par3, par4, 4, 2);
      }

   }

   public TileEntity func_149915_a(World world, int metadata) {
      return new BlockDemonHeart.TileEntityDemonHeart();
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 1;
   }

   public Item func_149650_a(int par1, Random rand, int fortune) {
      return Witchery.Items.GENERIC;
   }

   public int func_149692_a(int par1) {
      return Witchery.Items.GENERIC.itemDemonHeart.damageValue;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return Witchery.Items.GENERIC.itemDemonHeart.createStack();
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      double yMid = (double)y + 0.8D;
      double mid1 = 0.35D + 0.3D * rand.nextDouble();
      double mid2 = 0.35D + 0.3D * rand.nextDouble();
      if (rand.nextInt(10) == 0) {
         world.func_72869_a(ParticleEffect.FLAME.toString(), (double)x + mid1, yMid, (double)z + mid2, 0.0D, 0.0D, 0.0D);
         world.func_72869_a(ParticleEffect.SMOKE.toString(), (double)x + mid1, yMid, (double)z + mid2, 0.0D, 0.0D, 0.0D);
      }

   }

   public static class TileEntityDemonHeart extends TileEntityBase {
      public long totalTicks() {
         return this.ticks;
      }

      public void func_145845_h() {
         super.func_145845_h();
         if (this.field_145850_b.field_72995_K && this.ticks % 25L == 0L) {
            this.field_145850_b.func_72980_b(0.5D + (double)this.field_145851_c, 0.5D + (double)this.field_145848_d, 0.5D + (double)this.field_145849_e, "witchery:random.heartbeat", 0.8F, 1.0F, false);
         }

      }
   }
}
