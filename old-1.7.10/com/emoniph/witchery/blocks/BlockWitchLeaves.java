package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.MultiItemBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class BlockWitchLeaves extends BlockLeavesBase implements IShearable {
   private static final String[] LEAF_TYPES = new String[]{"rowan", "alder", "hawthorn"};
   private static final String[][] field_94396_b = new String[][]{{"_rowan", "_alder", "_hawthorn"}, {"_rowan_opaque", "_alder_opaque", "_hawthorn_opaque"}};
   private int field_94394_cP;
   private int[] adjacentTreeBlocks;
   private IIcon[][] iconsForModes = new IIcon[2][];
   private int[] decayMatrix;

   public BlockWitchLeaves() {
      super(Material.field_151584_j, false);
      this.func_149711_c(0.2F);
      this.func_149713_g(1);
      this.func_149672_a(Block.field_149779_h);
      this.func_149675_a(true);
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, BlockWitchLeaves.ClassItemBlock.class, blockName);
      super.func_149663_c(blockName);
      Blocks.field_150480_ab.setFireInfo(this, 30, 60);
      return this;
   }

   @SideOnly(Side.CLIENT)
   public int func_149635_D() {
      double d0 = 0.5D;
      double d1 = 1.0D;
      return ColorizerFoliage.func_77470_a(d0, d1);
   }

   @SideOnly(Side.CLIENT)
   public int func_149741_i(int par1) {
      return (par1 & 3) == 1 ? getFoliageColorAlder() : ((par1 & 3) == 2 ? getFoliageColorHawthorn() : getFoliageColorBasic());
   }

   @SideOnly(Side.CLIENT)
   public static int getFoliageColorAlder() {
      return 3774771;
   }

   @SideOnly(Side.CLIENT)
   public static int getFoliageColorHawthorn() {
      return 6728294;
   }

   @SideOnly(Side.CLIENT)
   public static int getFoliageColorBasic() {
      return 4764952;
   }

   @SideOnly(Side.CLIENT)
   public int func_149720_d(IBlockAccess world, int x, int y, int z) {
      int l = world.func_72805_g(x, y, z);
      if ((l & 3) == 1) {
         return getFoliageColorAlder();
      } else if ((l & 3) == 2) {
         return getFoliageColorHawthorn();
      } else {
         int i1 = 0;
         int j1 = 0;
         int k1 = 0;

         for(int l1 = -1; l1 <= 1; ++l1) {
            for(int i2 = -1; i2 <= 1; ++i2) {
               int j2 = world.func_72807_a(x + i2, z + l1).func_150571_c(x + l1, y, z + k1);
               i1 += (j2 & 16711680) >> 16;
               j1 += (j2 & '\uff00') >> 8;
               k1 += j2 & 255;
            }
         }

         return (i1 / 9 & 255) << 16 | (j1 / 9 & 255) << 8 | k1 / 9 & 255;
      }
   }

   public void func_149749_a(World world, int x, int y, int z, Block block0, int meta0) {
      byte b0 = 1;
      int i1 = b0 + 1;
      if (world.func_72904_c(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
         for(int j1 = -b0; j1 <= b0; ++j1) {
            for(int k1 = -b0; k1 <= b0; ++k1) {
               for(int l1 = -b0; l1 <= b0; ++l1) {
                  Block block = world.func_147439_a(x + j1, y + k1, z + l1);
                  if (block.isLeaves(world, x + j1, y + k1, z + l1)) {
                     block.beginLeavesDecay(world, x + j1, y + k1, z + l1);
                  }
               }
            }
         }
      }

   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      if (!world.field_72995_K) {
         int meta = world.func_72805_g(x, y, z);
         if ((meta & 8) != 0 && (meta & 4) == 0) {
            byte b0 = 4;
            int i1 = b0 + 1;
            byte b1 = 32;
            int j1 = b1 * b1;
            int k1 = b1 / 2;
            if (this.decayMatrix == null) {
               this.decayMatrix = new int[b1 * b1 * b1];
            }

            int l1;
            if (world.func_72904_c(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
               l1 = -b0;

               label110:
               while(true) {
                  int i2;
                  int j2;
                  if (l1 > b0) {
                     l1 = 1;

                     while(true) {
                        if (l1 > 4) {
                           break label110;
                        }

                        for(i2 = -b0; i2 <= b0; ++i2) {
                           for(j2 = -b0; j2 <= b0; ++j2) {
                              for(int k2 = -b0; k2 <= b0; ++k2) {
                                 if (this.decayMatrix[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1) {
                                    if (this.decayMatrix[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                       this.decayMatrix[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.decayMatrix[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                       this.decayMatrix[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.decayMatrix[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] == -2) {
                                       this.decayMatrix[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.decayMatrix[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] == -2) {
                                       this.decayMatrix[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.decayMatrix[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] == -2) {
                                       this.decayMatrix[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] = l1;
                                    }

                                    if (this.decayMatrix[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] == -2) {
                                       this.decayMatrix[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] = l1;
                                    }
                                 }
                              }
                           }
                        }

                        ++l1;
                     }
                  }

                  for(i2 = -b0; i2 <= b0; ++i2) {
                     for(j2 = -b0; j2 <= b0; ++j2) {
                        Block block = world.func_147439_a(x + l1, y + i2, z + j2);
                        if (!block.canSustainLeaves(world, x + l1, y + i2, z + j2)) {
                           if (block.isLeaves(world, x + l1, y + i2, z + j2)) {
                              this.decayMatrix[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2;
                           } else {
                              this.decayMatrix[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1;
                           }
                        } else {
                           this.decayMatrix[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0;
                        }
                     }
                  }

                  ++l1;
               }
            }

            l1 = this.decayMatrix[k1 * j1 + k1 * b1 + k1];
            if (l1 >= 0) {
               world.func_72921_c(x, y, z, meta & -9, 4);
            } else {
               this.removeLeaves(world, x, y, z);
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      if (world.func_72951_B(x, y + 1, z) && !World.func_147466_a(world, x, y - 1, z) && rand.nextInt(15) == 1) {
         double d0 = (double)((float)x + rand.nextFloat());
         double d1 = (double)y - 0.05D;
         double d2 = (double)((float)z + rand.nextFloat());
         world.func_72869_a("dripWater", d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   private void removeLeaves(World world, int x, int y, int z) {
      this.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), 0);
      world.func_147468_f(x, y, z);
   }

   public int func_149745_a(Random rand) {
      return rand.nextInt(20) == 0 ? 1 : 0;
   }

   public Item func_149650_a(int metadata, Random rand, int fortune) {
      return Item.func_150898_a(Witchery.Blocks.SAPLING);
   }

   public void func_149690_a(World world, int x, int y, int z, int par5, float par6, int par7) {
      if (!world.field_72995_K) {
         int j1 = 20;
         if ((par5 & 3) == 3) {
            j1 = 40;
         }

         if (par7 > 0) {
            j1 -= 2 << par7;
            if (j1 < 10) {
               j1 = 10;
            }
         }

         if (world.field_73012_v.nextInt(j1) == 0) {
            Item k1 = this.func_149650_a(par5, world.field_73012_v, par7);
            this.func_149642_a(world, x, y, z, new ItemStack(k1, 1, this.func_149692_a(par5)));
         }

         j1 = 200;
         if (par7 > 0) {
            j1 -= 10 << par7;
            if (j1 < 40) {
               j1 = 40;
            }
         }

         if ((par5 & 3) == 0 && world.field_73012_v.nextInt(j1) == 0) {
            this.func_149642_a(world, x, y, z, Witchery.Items.GENERIC.itemRowanBerries.createStack());
         }
      }

   }

   public void func_149636_a(World world, EntityPlayer player, int par3, int par4, int par5, int par6) {
      super.func_149636_a(world, player, par3, par4, par5, par6);
   }

   public int func_149692_a(int par1) {
      return par1 & 3;
   }

   public boolean func_149662_c() {
      this.setGraphicsLevel(Witchery.proxy.getGraphicsLevel());
      return !this.field_150121_P;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int par2) {
      return (par2 & 3) == 1 ? this.iconsForModes[this.field_94394_cP][1] : ((par2 & 3) == 2 ? this.iconsForModes[this.field_94394_cP][2] : this.iconsForModes[this.field_94394_cP][0]);
   }

   public void setGraphicsLevel(boolean par1) {
      this.field_150121_P = par1;
      this.field_94394_cP = par1 ? 0 : 1;
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item item, CreativeTabs creativeTabs, List list) {
      for(int i = 0; i < LEAF_TYPES.length; ++i) {
         list.add(new ItemStack(item, 1, i));
      }

   }

   protected ItemStack func_149644_j(int par1) {
      return new ItemStack(this, 1, par1 & 3);
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
      for(int i = 0; i < field_94396_b.length; ++i) {
         this.iconsForModes[i] = new IIcon[field_94396_b[i].length];

         for(int j = 0; j < field_94396_b[i].length; ++j) {
            this.iconsForModes[i][j] = par1IconRegister.func_94245_a(this.func_149641_N() + field_94396_b[i][j]);
         }
      }

   }

   public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
      ArrayList<ItemStack> ret = new ArrayList();
      ret.add(new ItemStack(this, 1, world.func_72805_g(x, y, z) & 3));
      return ret;
   }

   public void beginLeavesDecay(World world, int x, int y, int z) {
      world.func_72921_c(x, y, z, world.func_72805_g(x, y, z) | 8, 4);
   }

   public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockWitchLeaves.LEAF_TYPES;
      }
   }
}
