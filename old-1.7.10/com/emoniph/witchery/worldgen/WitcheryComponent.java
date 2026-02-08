package com.emoniph.witchery.worldgen;

import com.emoniph.witchery.util.Log;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraftforge.common.ChestGenHooks;

public class WitcheryComponent extends StructureComponent {
   public WitcheryComponent() {
   }

   public WitcheryComponent(int direction, Random random, int x, int z, int dimX, int dimY, int dimZ) {
      super(direction);
      super.field_74885_f = direction;
      this.field_74887_e = this.calcBox(direction, x + (16 - dimX) / 2, 64, z + (16 - dimZ) / 2, dimX, dimY, dimZ, 0);
   }

   public boolean addComponentParts(World world, Random random) {
      return true;
   }

   protected void func_151554_b(World par1World, Block par2, int par3, int par4, int par5, int par6, StructureBoundingBox par7StructureBoundingBox) {
      int j1 = this.func_74865_a(par4, par6);
      int k0 = this.func_74862_a(par5);
      int l1 = this.func_74873_b(par4, par6);
      if (par7StructureBoundingBox.func_78890_b(j1, k0, l1)) {
         if (par1World.func_147437_c(j1, k0, l1)) {
            return;
         }

         for(int k1 = k0 - 1; (par1World.func_147437_c(j1, k1, l1) || !par1World.func_147439_a(j1, k1, l1).func_149688_o().func_76220_a() || par1World.func_147439_a(j1, k1, l1) == Blocks.field_150432_aD) && k1 > 1; --k1) {
            par1World.func_147465_d(j1, k1, l1, par2, par3, 2);
         }
      }

   }

   protected void func_74871_b(World par1World, int par2, int par3, int par4, StructureBoundingBox par5StructureBoundingBox) {
      int l = this.func_74865_a(par2, par4);
      int i1 = this.func_74862_a(par3);
      int j1 = this.func_74873_b(par2, par4);
      if (par5StructureBoundingBox.func_78890_b(l, i1, j1)) {
         int i = 0;

         while(true) {
            ++i;
            if (i >= 20 && par1World.func_147437_c(l, i1, j1) || i1 >= 255) {
               break;
            }

            par1World.func_147465_d(l, i1, j1, Blocks.field_150350_a, 0, 2);
            ++i1;
         }
      }

   }

   protected boolean isWaterBelow(World par1World, int par4, int par5, int par6, StructureBoundingBox par7StructureBoundingBox) {
      int j1 = this.func_74865_a(par4, par6);
      int k1 = this.func_74862_a(par5);
      int l1 = this.func_74873_b(par4, par6);

      for(int i = 0; i < 10; ++i) {
         Material material = par1World.func_147439_a(j1, k1, l1).func_149688_o();
         if (material.func_76224_d() || material == Material.field_151588_w) {
            return true;
         }

         if (!par1World.func_147437_c(j1, k1, l1)) {
            return false;
         }
      }

      return false;
   }

   public void setDispenser(int x, int y, int z, Random random, World world, int facing) {
      int i1 = this.func_74865_a(x, z);
      int j1 = this.func_74862_a(y);
      int k1 = this.func_74873_b(x, z);
      world.func_147465_d(i1, j1, k1, Blocks.field_150367_z, facing, 0);
      TileEntity tileDispenser = world.func_147438_o(i1, j1, k1);
      if (tileDispenser != null && tileDispenser instanceof TileEntityDispenser) {
         ChestGenHooks info = ChestGenHooks.getInfo("mineshaftCorridor");
         WeightedRandomChestContent.func_76293_a(random, info.getItems(random), (TileEntityDispenser)tileDispenser, info.getCount(random));
      } else {
         Log.instance().warning("Failed to fetch dispenser entity at (" + i1 + ", " + j1 + ", " + k1 + ")");
      }

   }

   protected void setSpawner(int x, int y, int z, String mobName, World world) {
      int i1 = this.func_74865_a(x, z);
      int j1 = this.func_74862_a(y);
      int k1 = this.func_74873_b(x, z);
      world.func_147465_d(i1, j1, k1, Blocks.field_150474_ac, 0, 2);
      TileEntity tileSpawner = world.func_147438_o(i1, j1, k1);
      if (tileSpawner != null && tileSpawner instanceof TileEntityMobSpawner) {
         ((TileEntityMobSpawner)tileSpawner).func_145881_a().func_98272_a(mobName);
      } else {
         Log.instance().warning("Failed to fetch mob spawner entity at (" + i1 + ", " + j1 + ", " + k1 + ")");
      }

   }

   protected void setFurnace(int x, int y, int z, World world) {
      int i1 = this.func_74865_a(x, z);
      int j1 = this.func_74862_a(y);
      int k1 = this.func_74873_b(x, z);
      world.func_147465_d(i1, j1, k1, Blocks.field_150460_al, this.func_151555_a(Blocks.field_150331_J, 3), 2);
   }

   protected void placeAirBlockAtPos(int x, int y, int z, StructureBoundingBox bounds, World world) {
      this.func_151550_a(world, Blocks.field_150350_a, 0, x, y, z, bounds);
   }

   protected void place(Block block, int meta, int x, int y, int z, StructureBoundingBox bounds, World world) {
      this.func_151550_a(world, block, meta, x, y, z, bounds);
   }

   protected StructureBoundingBox calcBox(int direction, int x, int y, int z, int xLength, int height, int zLength, int xShift) {
      int minX = 0;
      int maxX = 0;
      int maxY = y + height;
      int minZ = 0;
      int maxZ = 0;
      switch(direction) {
      case 0:
         minX = x - xShift;
         maxX = x - xShift + xLength;
         minZ = z;
         maxZ = z + zLength;
         break;
      case 1:
         minX = x - zLength;
         maxX = x;
         minZ = z - xShift;
         maxZ = z - xShift + xLength;
         break;
      case 2:
         minX = x - xShift;
         maxX = x - xShift + xLength;
         minZ = z - zLength;
         maxZ = z;
         break;
      case 3:
         minX = x;
         maxX = x + zLength;
         minZ = z - xShift;
         maxZ = z - xShift + xLength;
      }

      return new StructureBoundingBox(minX, y, minZ, maxX, maxY, maxZ);
   }

   protected int calcGroundHeight(World world, StructureBoundingBox boundingBox) {
      int height = 0;
      int count = 0;

      for(int z = boundingBox.field_78896_c; z <= boundingBox.field_78892_f; ++z) {
         for(int x = boundingBox.field_78897_a; x <= boundingBox.field_78893_d; ++x) {
            if (boundingBox.func_78890_b(x, 64, z)) {
               height += Math.max(world.func_72825_h(x, z), world.field_73011_w.func_76557_i());
               ++count;
            }
         }
      }

      if (count == 0) {
         return -1;
      } else {
         return height / count;
      }
   }

   protected void func_143012_a(NBTTagCompound nbttagcompound) {
   }

   protected void func_143011_b(NBTTagCompound nbttagcompound) {
   }

   public boolean func_74875_a(World world, Random random, StructureBoundingBox structureboundingbox) {
      return true;
   }
}
