package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBaseContainer;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.EntityPosition;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class BlockBrewGas extends BlockBaseContainer implements IFluidBlock {
   public static final Material MATERIAL;

   public BlockBrewGas() {
      super(MATERIAL, TileEntityBrewFluid.class);
      this.registerWithCreateTab = false;
      this.func_149675_a(true);
      this.func_149649_H();
      this.func_149722_s();
   }

   public boolean isAir(IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public int func_149720_d(IBlockAccess world, int x, int y, int z) {
      TileEntityBrewFluid gas = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
      return gas != null ? gas.color : 3385907;
   }

   public int func_149741_i(int metadata) {
      return 3385907;
   }

   public int func_149635_D() {
      return 3385907;
   }

   public boolean func_149686_d() {
      return false;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public boolean func_149678_a(int p_149678_1_, boolean p_149678_2_) {
      return false;
   }

   public boolean func_149662_c() {
      return false;
   }

   public Item func_149650_a(int metadata, Random rand, int fortune) {
      return null;
   }

   public int func_149745_a(Random par1Random) {
      return 0;
   }

   public int func_149738_a(World world) {
      return 5;
   }

   public boolean func_149646_a(IBlockAccess world, int x, int y, int z, int side) {
      Block block = world.func_147439_a(x, y, z);
      return block == this ? false : super.func_149646_a(world, x, y, z, side);
   }

   public boolean func_149747_d(IBlockAccess world, int x, int y, int z, int side) {
      return false;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }

   public Fluid getFluid() {
      return Witchery.Fluids.BREW_GAS;
   }

   public int func_149645_b() {
      return Witchery.proxy.getGasRenderId();
   }

   public int func_149701_w() {
      return 1;
   }

   public void func_149726_b(World world, int x, int y, int z) {
      world.func_147464_a(x, y, z, this, 5);
   }

   public void onNeighborBlockChange(World world, int x, int y, int z, int side) {
      this.func_149726_b(world, x, y, z);
   }

   public FluidStack drain(World world, int x, int y, int z, boolean doDrain) {
      return null;
   }

   public boolean canDrain(World world, int x, int y, int z) {
      return false;
   }

   public float getFilledPercentage(World world, int x, int y, int z) {
      return 0.0F;
   }

   private boolean isTargetBlock(Block block) {
      return block != null && block != Blocks.field_150350_a && block != this;
   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      if (!world.field_72995_K) {
         int initialMetadata = world.func_72805_g(x, y, z);
         TileEntityBrewFluid oldTile = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
         if (oldTile == null) {
            world.func_147468_f(x, y, z);
            return;
         }

         int maxMeta = oldTile.expansion;
         if (oldTile.incRunTicks() > 120) {
            world.func_147468_f(x, y, z);
            return;
         }

         if (initialMetadata >= maxMeta) {
            label63: {
               if (oldTile != null) {
                  if (oldTile.duration != 0 && (oldTile.duration <= 0 || rand.nextInt(oldTile.duration) != 0)) {
                     break label63;
                  }
               } else if (rand.nextInt(40) != 0) {
                  break label63;
               }

               world.func_147468_f(x, y, z);
               return;
            }
         } else {
            int metadata = initialMetadata;
            double[] pb = new double[]{0.2D, 0.4D, 0.8D, 0.8D, 0.8D, 0.8D};
            int[] dX = new int[]{0, 0, 1, -1, 0, 0};
            int[] dY = new int[]{1, -1, 0, 0, 0, 0};
            int[] dZ = new int[]{0, 0, 0, 0, 1, -1};
            boolean expanded = false;
            if (oldTile != null) {
               for(int i = 0; i < pb.length && metadata < maxMeta; ++i) {
                  if (rand.nextDouble() < pb[i]) {
                     int newX = x + dX[i];
                     int newY = y + dY[i];
                     int newZ = z + dZ[i];
                     Block block = world.func_147439_a(newX, newY, newZ);
                     if (block == Blocks.field_150350_a || block == Blocks.field_150431_aC) {
                        world.func_147465_d(newX, newY, newZ, this, Math.min(metadata + 1, maxMeta), 3);
                        TileEntityBrewFluid newTile = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, newX, newY, newZ, TileEntityBrewFluid.class);
                        newTile.nbtEffect = (NBTTagCompound)oldTile.nbtEffect.func_74737_b();
                        newTile.expansion = oldTile.expansion;
                        newTile.color = oldTile.color;
                        newTile.duration = oldTile.duration;
                        newTile.thrower = oldTile.thrower;
                        expanded = true;
                     }
                  }
               }
            }

            if (expanded) {
               world.func_72921_c(x, y, z, Math.min(metadata + 1, maxMeta), 3);
            }
         }

         world.func_147464_a(x, y, z, this, 5);
      }

   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (entity != null && entity instanceof EntityLivingBase && !world.field_72995_K && world.field_73012_v.nextInt(10) == 4) {
         TileEntityBrewFluid gas = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
         if (gas != null && gas.nbtEffect != null) {
            EntityLivingBase living = (EntityLivingBase)entity;
            ModifiersEffect modifiers = new ModifiersEffect(0.25D, 0.5D, false, new EntityPosition(x, y, z), false, 0, world.func_72924_a(gas.thrower));
            modifiers.protectedFromNegativePotions = living.func_70644_a(Witchery.Potions.GAS_MASK);
            WitcheryBrewRegistry.INSTANCE.applyToEntity(world, living, gas.nbtEffect, modifiers);
         }
      }

   }

   static {
      MATERIAL = (new Material(MapColor.field_151660_b) {
         public boolean func_76220_a() {
            return false;
         }

         public boolean func_76230_c() {
            return false;
         }
      }).func_76231_i();
   }
}
