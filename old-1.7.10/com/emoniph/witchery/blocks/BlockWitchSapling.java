package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.MultiItemBlock;
import com.emoniph.witchery.worldgen.WorldGenLargeWitchTree;
import com.emoniph.witchery.worldgen.WorldGenWitchTree;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.TerrainGen;

public class BlockWitchSapling extends BlockBaseBush implements IFuelHandler, IGrowable {
   private static final String[] WOOD_TYPES = new String[]{"rowan", "alder", "hawthorn"};
   @SideOnly(Side.CLIENT)
   private IIcon[] saplingIcon;

   public BlockWitchSapling() {
      super(Material.field_151585_k, BlockWitchSapling.ClassItemBlock.class);
      this.func_149711_c(0.0F);
      this.func_149672_a(Block.field_149779_h);
      float f = 0.4F;
      this.func_149676_a(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
      GameRegistry.registerFuelHandler(this);
   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      if (!world.field_72995_K) {
         super.func_149674_a(world, x, y, z, rand);
         if (world.func_72957_l(x, y + 1, z) >= 9 && rand.nextInt(7) == 0) {
            markOrGrowMarked(world, x, y, z, rand);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int metadata) {
      metadata &= 3;
      if (metadata < 0 || metadata >= this.saplingIcon.length) {
         metadata = 0;
      }

      return this.saplingIcon[metadata];
   }

   public static void markOrGrowMarked(World world, int x, int y, int z, Random rand) {
      int l = world.func_72805_g(x, y, z);
      if ((l & 8) == 0) {
         world.func_72921_c(x, y, z, l | 8, 4);
      } else {
         growTree(world, x, y, z, rand);
      }

   }

   public static void growTree(World world, int x, int y, int z, Random rand) {
      if (TerrainGen.saplingGrowTree(world, rand, x, y, z)) {
         int l = world.func_72805_g(x, y, z) & 3;
         Object object = null;
         int i1 = 0;
         int j1 = 0;
         boolean flag = false;
         WorldGenLargeWitchTree tree;
         if (l == 1) {
            tree = new WorldGenLargeWitchTree(true, 1, 1, 0.5D);
            tree.func_76487_a(0.6D, 0.5D, 0.5D);
            object = tree;
         } else if (l == 2) {
            tree = new WorldGenLargeWitchTree(true, 2, 2);
            tree.func_76487_a(0.8D, 1.2D, 1.0D);
            object = tree;
         } else {
            object = new WorldGenWitchTree(true, 5, 0, 0, 1, false);
         }

         if (flag) {
            world.func_147465_d(x + i1, y, z + j1, Blocks.field_150350_a, 0, 4);
            world.func_147465_d(x + i1 + 1, y, z + j1, Blocks.field_150350_a, 0, 4);
            world.func_147465_d(x + i1, y, z + j1 + 1, Blocks.field_150350_a, 0, 4);
            world.func_147465_d(x + i1 + 1, y, z + j1 + 1, Blocks.field_150350_a, 0, 4);
         } else {
            world.func_147465_d(x, y, z, Blocks.field_150350_a, 0, 4);
         }

         if (!((WorldGenerator)object).func_76484_a(world, rand, x + i1, y, z + j1)) {
            if (flag) {
               world.func_147465_d(x + i1, y, z + j1, Witchery.Blocks.SAPLING, l, 4);
               world.func_147465_d(x + i1 + 1, y, z + j1, Witchery.Blocks.SAPLING, l, 4);
               world.func_147465_d(x + i1, y, z + j1 + 1, Witchery.Blocks.SAPLING, l, 4);
               world.func_147465_d(x + i1 + 1, y, z + j1 + 1, Witchery.Blocks.SAPLING, l, 4);
            } else {
               world.func_147465_d(x, y, z, Witchery.Blocks.SAPLING, l, 4);
            }
         }

      }
   }

   public boolean isSameSapling(World world, int x, int y, int z, int metadata) {
      return world.func_147439_a(x, y, z) == this && (world.func_72805_g(x, y, z) & 3) == metadata;
   }

   public int func_149692_a(int metadata) {
      return metadata & 3;
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item item, CreativeTabs creativeTabs, List list) {
      for(int i = 0; i < WOOD_TYPES.length; ++i) {
         list.add(new ItemStack(item, 1, i));
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.saplingIcon = new IIcon[WOOD_TYPES.length];

      for(int i = 0; i < this.saplingIcon.length; ++i) {
         this.saplingIcon[i] = iconRegister.func_94245_a(this.func_149641_N() + "_" + WOOD_TYPES[i]);
      }

   }

   public int getBurnTime(ItemStack fuel) {
      return Item.func_150898_a(this) == fuel.func_77973_b() ? 100 : 0;
   }

   public boolean func_149851_a(World world, int rand, int x, int y, boolean z) {
      return true;
   }

   public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
      return (double)world.field_73012_v.nextFloat() < 0.75D;
   }

   public void func_149853_b(World world, Random rand, int x, int y, int z) {
      markOrGrowMarked(world, x, y, z, rand);
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockWitchSapling.WOOD_TYPES;
      }

      @SideOnly(Side.CLIENT)
      public IIcon func_77617_a(int par1) {
         return this.field_150939_a.func_149691_a(0, par1);
      }
   }
}
