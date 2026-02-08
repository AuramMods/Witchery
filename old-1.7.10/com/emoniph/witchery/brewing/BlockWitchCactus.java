package com.emoniph.witchery.brewing;

import com.emoniph.witchery.blocks.BlockBase;
import com.emoniph.witchery.util.BlockUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockWitchCactus extends BlockBase {
   @SideOnly(Side.CLIENT)
   private IIcon iconTop;
   @SideOnly(Side.CLIENT)
   private IIcon iconBottom;

   public BlockWitchCactus() {
      super(Material.field_151570_A);
      this.func_149711_c(0.4F);
      this.func_149672_a(field_149775_l);
      this.registerWithCreateTab = false;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      float f = 0.0625F;
      return AxisAlignedBB.func_72330_a((double)((float)x + f), (double)y, (double)((float)z + f), (double)((float)(x + 1) - f), (double)((float)(y + 1) - f), (double)((float)(z + 1) - f));
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB func_149633_g(World world, int x, int y, int z) {
      float f = 0.0625F;
      return AxisAlignedBB.func_72330_a((double)((float)x + f), (double)y, (double)((float)z + f), (double)((float)(x + 1) - f), (double)(y + 1), (double)((float)(z + 1) - f));
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int side, int meta) {
      return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.field_149761_L);
   }

   public boolean func_149686_d() {
      return false;
   }

   public boolean func_149662_c() {
      return false;
   }

   public int func_149645_b() {
      return 13;
   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      entity.func_70097_a(DamageSource.field_76367_g, 1.0F);
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.field_149761_L = iconRegister.func_94245_a(this.func_149641_N() + "_side");
      this.iconTop = iconRegister.func_94245_a(this.func_149641_N() + "_top");
      this.iconBottom = iconRegister.func_94245_a(this.func_149641_N() + "_bottom");
   }

   public Item func_149650_a(int meta, Random random, int fortune) {
      return null;
   }

   public int quantityDropped(int meta, int fortune, Random random) {
      return 0;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }

   public boolean func_149718_j(World world, int x, int y, int z) {
      return !BlockUtil.isReplaceableBlock(world, x, y - 1, z);
   }

   public void func_149695_a(World world, int x, int y, int z, Block block) {
      if (!this.func_149718_j(world, x, y, z)) {
         world.func_147480_a(x, y, z, true);
      }

   }
}
