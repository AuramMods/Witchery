package com.emoniph.witchery.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPitDirt extends BlockBase {
   private static final Material passThrough;
   @SideOnly(Side.CLIENT)
   private IIcon iconPodzolTop;
   @SideOnly(Side.CLIENT)
   private IIcon iconPodzolSide;

   public BlockPitDirt() {
      super(passThrough);
      this.func_149711_c(0.5F);
      this.func_149672_a(field_149767_g);
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public boolean func_149662_c() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int p_149691_1_, int p_149691_2_) {
      if (p_149691_2_ == 2) {
         if (p_149691_1_ == 1) {
            return this.iconPodzolTop;
         }

         if (p_149691_1_ != 0) {
            return this.iconPodzolSide;
         }
      }

      return this.field_149761_L;
   }

   public int func_149692_a(int meta) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149673_e(IBlockAccess world, int x, int y, int z, int side) {
      int i1 = world.func_72805_g(x, y, z);
      if (i1 == 2) {
         if (side == 1) {
            return this.iconPodzolTop;
         }

         if (side != 0) {
            Material material = world.func_147439_a(x, y + 1, z).func_149688_o();
            if (material == Material.field_151597_y || material == Material.field_151596_z) {
               return Blocks.field_150349_c.func_149673_e(world, x, y, z, side);
            }

            Block block = world.func_147439_a(x, y + 1, z);
            if (block != Blocks.field_150346_d && block != Blocks.field_150349_c) {
               return this.iconPodzolSide;
            }
         }
      }

      return this.field_149761_L;
   }

   protected ItemStack func_149644_j(int p_149644_1_) {
      if (p_149644_1_ == 1) {
         p_149644_1_ = 0;
      }

      return super.func_149644_j(p_149644_1_);
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item item, CreativeTabs tabs, List list) {
      list.add(new ItemStack(this, 1, 0));
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      super.func_149651_a(iconRegister);
      this.iconPodzolTop = iconRegister.func_94245_a(this.func_149641_N() + "_" + "podzol_top");
      this.iconPodzolSide = iconRegister.func_94245_a(this.func_149641_N() + "_" + "podzol_side");
   }

   public int func_149643_k(World world, int x, int y, int z) {
      int l = world.func_72805_g(x, y, z);
      if (l == 1) {
         l = 0;
      }

      return l;
   }

   static {
      passThrough = new Material(MapColor.field_151664_l) {
         public boolean func_76230_c() {
            return true;
         }

         public boolean func_76218_k() {
            return false;
         }
      };
   }
}
