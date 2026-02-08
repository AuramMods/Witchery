package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPitGrass extends BlockBase {
   private static final Material passThrough;
   @SideOnly(Side.CLIENT)
   private IIcon iconTop;
   @SideOnly(Side.CLIENT)
   private IIcon iconSideSnowed;
   @SideOnly(Side.CLIENT)
   private IIcon iconSideOverlay;

   public BlockPitGrass() {
      super(passThrough);
      this.func_149711_c(0.6F);
      this.func_149672_a(field_149779_h);
      this.func_149675_a(false);
   }

   public int func_149645_b() {
      return Witchery.proxy.getPitGrassRenderId();
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public Item func_149650_a(int p_149650_1_, Random rand, int p_149650_3_) {
      return Item.func_150898_a(this);
   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
   }

   public boolean func_149662_c() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int p_149691_1_, int p_149691_2_) {
      return p_149691_1_ == 1 ? this.iconTop : (p_149691_1_ == 0 ? Blocks.field_150346_d.func_149733_h(p_149691_1_) : this.field_149761_L);
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149673_e(IBlockAccess world, int x, int y, int z, int side) {
      if (side == 1) {
         return this.iconTop;
      } else if (side == 0) {
         return Blocks.field_150346_d.func_149733_h(side);
      } else {
         Material material = world.func_147439_a(x, y + 1, z).func_149688_o();
         return material != Material.field_151597_y && material != Material.field_151596_z ? this.field_149761_L : this.iconSideSnowed;
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.field_149761_L = iconRegister.func_94245_a(this.func_149641_N() + "_side");
      this.iconTop = iconRegister.func_94245_a(this.func_149641_N() + "_top");
      this.iconSideSnowed = iconRegister.func_94245_a(this.func_149641_N() + "_side_snowed");
      this.iconSideOverlay = iconRegister.func_94245_a(this.func_149641_N() + "_side_overlay");
   }

   @SideOnly(Side.CLIENT)
   public int func_149635_D() {
      double d0 = 0.5D;
      double d1 = 1.0D;
      return ColorizerGrass.func_77480_a(d0, d1);
   }

   @SideOnly(Side.CLIENT)
   public int func_149741_i(int meta) {
      return this.func_149635_D();
   }

   @SideOnly(Side.CLIENT)
   public int func_149720_d(IBlockAccess world, int x, int y, int z) {
      int l = 0;
      int i1 = 0;
      int j1 = 0;

      for(int k1 = -1; k1 <= 1; ++k1) {
         for(int l1 = -1; l1 <= 1; ++l1) {
            int i2 = world.func_72807_a(x + l1, z + k1).func_150558_b(x + l1, y, z + k1);
            l += (i2 & 16711680) >> 16;
            i1 += (i2 & '\uff00') >> 8;
            j1 += i2 & 255;
         }
      }

      return (l / 9 & 255) << 16 | (i1 / 9 & 255) << 8 | j1 / 9 & 255;
   }

   static {
      passThrough = new Material(MapColor.field_151661_c) {
         public boolean func_76230_c() {
            return true;
         }

         public boolean func_76218_k() {
            return false;
         }
      };
   }
}
