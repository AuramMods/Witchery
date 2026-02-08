package com.emoniph.witchery.blocks;

import com.emoniph.witchery.entity.EntityEnt;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.MultiItemBlock;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockWitchLog extends BlockBaseRotatedPillar implements IFuelHandler {
   public static final String[] WOOD_TYPES = new String[]{"rowan", "alder", "hawthorn"};
   @SideOnly(Side.CLIENT)
   private IIcon[] field_111052_c;
   @SideOnly(Side.CLIENT)
   private IIcon[] tree_top;

   public BlockWitchLog() {
      super(Material.field_151575_d, BlockWitchLog.ClassItemBlock.class);
      this.func_149711_c(2.0F);
      this.func_149672_a(Block.field_149766_f);
      GameRegistry.registerFuelHandler(this);
   }

   public Block func_149663_c(String blockName) {
      super.func_149663_c(blockName);
      Blocks.field_150480_ab.setFireInfo(this, 5, 5);
      return this;
   }

   public void func_149690_a(World world, int x, int y, int z, int par5, float par6, int par7) {
      if (!world.field_72995_K) {
         double chance = 0.01D;
         chance += world.func_147439_a(x, y + 1, z) == this ? 0.01D : 0.0D;
         chance += world.func_147439_a(x, y - 1, z) == this ? 0.01D : 0.0D;
         chance += world.func_147439_a(x + 1, y, z) == this ? 0.01D : 0.0D;
         chance += world.func_147439_a(x - 1, y, z) == this ? 0.01D : 0.0D;
         chance += world.func_147439_a(x, y, z + 1) == this ? 0.01D : 0.0D;
         chance += world.func_147439_a(x, y, z - 1) == this ? 0.01D : 0.0D;
         chance = Math.min(chance, 0.05D);
         double roll = world.field_73012_v.nextDouble();
         Log.instance().debug("Ents: Chance: " + chance + ", roll: " + roll);
         if (roll < chance) {
            int MAX_DISTANCE = true;
            int MIN_DISTANCE = true;
            int activeRadius = 8;
            int ax = world.field_73012_v.nextInt(activeRadius * 2 + 1);
            if (ax > activeRadius) {
               ax += 16;
            }

            int nx = x - 16 + ax;
            int az = world.field_73012_v.nextInt(activeRadius * 2 + 1);
            if (az > activeRadius) {
               az += 16;
            }

            int nz = z - 16 + az;

            int ny;
            for(ny = y; !world.func_147437_c(nx, ny, nz) && ny < y + 8; ++ny) {
            }

            while(world.func_147437_c(nx, ny, nz) && ny > 0) {
               --ny;
            }

            int hy;
            for(hy = 0; world.func_147437_c(nx, ny + hy + 1, nz) && hy < 6; ++hy) {
            }

            Log.instance().debug("Ents: hy: " + hy + " (" + nx + "," + ny + "," + nz + ")");
            if (hy >= 3) {
               EntityEnt ent = new EntityEnt(world);
               ent.func_70012_b(0.5D + (double)nx, 0.05D + (double)ny + 1.0D, 0.5D + (double)nz, 0.0F, 0.0F);
               world.func_72838_d(ent);
               ParticleEffect.INSTANT_SPELL.send(SoundEffect.NOTE_HARP, world, (double)x, (double)y, (double)z, 1.0D, 1.0D, 8);
               ParticleEffect.LARGE_SMOKE.send(SoundEffect.MOB_HORSE_SKELETON_DEATH, ent, 2.0D, 4.0D, 16);
            }
         }
      }

      super.func_149690_a(world, x, y, z, par5, par6, par7);
   }

   public int func_149745_a(Random par1Random) {
      return 1;
   }

   public Item func_149650_a(int metadata, Random rand, int fortune) {
      return Item.func_150898_a(this);
   }

   public void func_149749_a(World world, int x, int y, int z, Block origBlock, int par6) {
      byte b0 = 4;
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

   @SideOnly(Side.CLIENT)
   protected IIcon func_150163_b(int par1) {
      if (par1 < 0 || par1 >= WOOD_TYPES.length) {
         par1 = 0;
      }

      return this.field_111052_c[par1];
   }

   @SideOnly(Side.CLIENT)
   protected IIcon func_150161_d(int par1) {
      if (par1 < 0 || par1 >= WOOD_TYPES.length) {
         par1 = 0;
      }

      return this.tree_top[par1];
   }

   public static int limitToValidMetadata(int par0) {
      return par0 & WOOD_TYPES.length - 1;
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item block, CreativeTabs creativeTabs, List list) {
      for(int i = 0; i < WOOD_TYPES.length; ++i) {
         list.add(new ItemStack(this, 1, i));
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.field_111052_c = new IIcon[WOOD_TYPES.length];
      this.tree_top = new IIcon[WOOD_TYPES.length];

      for(int i = 0; i < this.field_111052_c.length; ++i) {
         this.field_111052_c[i] = iconRegister.func_94245_a(this.func_149641_N() + "_" + WOOD_TYPES[i]);
         this.tree_top[i] = iconRegister.func_94245_a(this.func_149641_N() + "_" + WOOD_TYPES[i] + "_top");
      }

   }

   public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public boolean isWood(IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public int getBurnTime(ItemStack fuel) {
      return Item.func_150898_a(this) == fuel.func_77973_b() ? 300 : 0;
   }

   public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
      return world.func_72805_g(x, y, z) == 2 ? 1 : super.getFlammability(world, x, y, z, face);
   }

   public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
      return world.func_72805_g(x, y, z) == 2 ? 1 : super.getFireSpreadSpeed(world, x, y, z, face);
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockWitchLog.WOOD_TYPES;
      }
   }
}
