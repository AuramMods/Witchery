package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.MultiItemBlock;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockShadedGlass extends BlockBreakable {
   private final IIcon[] icons = new IIcon[16];
   private static final String[] colors;
   private boolean shaded;

   public BlockShadedGlass(boolean shaded) {
      super(shaded ? "witchery:shadedglass" : "glass", shaded ? Material.field_151576_e : Material.field_151592_s, false);
      this.shaded = shaded;
      this.func_149711_c(0.3F);
      if (shaded) {
         this.func_149713_g(15);
      }

      this.func_149672_a(field_149778_k);
      if (!shaded) {
         this.func_149647_a(WitcheryCreativeTab.INSTANCE);
      }

   }

   public int func_149660_a(World p_149660_1_, int p_149660_2_, int p_149660_3_, int p_149660_4_, int p_149660_5_, float p_149660_6_, float p_149660_7_, float p_149660_8_, int p_149660_9_) {
      return super.func_149660_a(p_149660_1_, p_149660_2_, p_149660_3_, p_149660_4_, p_149660_5_, p_149660_6_, p_149660_7_, p_149660_8_, p_149660_9_);
   }

   public void func_149726_b(World world, int x, int y, int z) {
      this.updatePoweredState(world, x, y, z);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, BlockShadedGlass.ClassItemBlock.class, blockName);
      return super.func_149663_c(blockName);
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int p_149691_1_, int meta) {
      return this.icons[meta % this.icons.length];
   }

   public int func_149692_a(int meta) {
      return meta;
   }

   public int func_149745_a(Random rand) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public static int func_149997_b(int meta) {
      return ~meta & 15;
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item item, CreativeTabs tabs, List list) {
      for(int i = 0; i < this.icons.length; ++i) {
         list.add(new ItemStack(item, 1, i));
      }

   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return 1;
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      for(int i = 0; i < this.icons.length; ++i) {
         this.icons[i] = iconRegister.func_94245_a(this.func_149641_N() + "_" + ItemDye.field_150921_b[func_149997_b(i)]);
      }

   }

   protected boolean func_149700_E() {
      return true;
   }

   public boolean func_149686_d() {
      return false;
   }

   public void func_149695_a(World world, int x, int y, int z, Block block) {
      this.updatePoweredState(world, x, y, z);
   }

   public void updatePoweredState(World world, int x, int y, int z) {
      if (world.func_72864_z(x, y, z)) {
         if (!this.shaded) {
            world.func_147465_d(x, y, z, Witchery.Blocks.SHADED_GLASS_ON, world.func_72805_g(x, y, z), 3);
         }
      } else if (this.shaded) {
         world.func_147465_d(x, y, z, Witchery.Blocks.SHADED_GLASS, world.func_72805_g(x, y, z), 3);
      }

   }

   static {
      colors = (String[])Lists.reverse(Arrays.asList(ItemDye.field_150921_b)).toArray(new String[ItemDye.field_150921_b.length]);
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockShadedGlass.colors;
      }
   }
}
