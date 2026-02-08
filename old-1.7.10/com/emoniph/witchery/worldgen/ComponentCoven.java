package com.emoniph.witchery.worldgen;

import com.emoniph.witchery.entity.EntityCovenWitch;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class ComponentCoven extends WitcheryComponent {
   public static final int DIM_X = 11;
   public static final int DIM_Y = 4;
   public static final int DIM_Z = 11;
   private int witchesSpawned = 0;

   public ComponentCoven() {
   }

   public ComponentCoven(int direction, Random random, int x, int z) {
      super(direction, random, x, z, 11, 4, 11);
   }

   private void spawnWitches(World par1World, StructureBoundingBox par2StructureBoundingBox, int par3, int par4, int par5, int par6) {
      if (this.witchesSpawned < par6) {
         for(int i1 = this.witchesSpawned; i1 < par6; ++i1) {
            int j1 = this.func_74865_a(par3 + i1, par5);
            int k1 = this.func_74862_a(par4);
            int l1 = this.func_74873_b(par3 + i1, par5);
            if (!par2StructureBoundingBox.func_78890_b(j1, k1, l1)) {
               break;
            }

            ++this.witchesSpawned;
            if (par1World.field_73012_v.nextInt(10) != 0) {
               EntityCovenWitch entityvillager = new EntityCovenWitch(par1World);
               entityvillager.func_110163_bv();
               entityvillager.func_70012_b((double)j1 + 0.5D, (double)k1, (double)l1 + 0.5D, 0.0F, 0.0F);
               par1World.func_72838_d(entityvillager);
            }
         }
      }

   }

   public boolean addComponentParts(World world, Random random) {
      BiomeGenBase biom = world.func_72807_a(this.func_74865_a(0, 0), this.func_74873_b(0, 0));
      int groundAvg = this.calcGroundHeight(world, this.field_74887_e);
      if (groundAvg < 0) {
         return true;
      } else {
         this.field_74887_e.func_78886_a(0, groundAvg - this.field_74887_e.field_78894_e + 4 - 1, 0);
         if (!this.isWaterBelow(world, 0, -1, 0, this.field_74887_e) && !this.isWaterBelow(world, 0, -1, 10, this.field_74887_e) && !this.isWaterBelow(world, 10, -1, 0, this.field_74887_e) && !this.isWaterBelow(world, 10, -1, 10, this.field_74887_e)) {
            Block stone = Blocks.field_150347_e;
            int stoneMeta = 0;
            Block brick = Blocks.field_150417_aV;
            int brickMeta = 2;
            Object groundID;
            Object undergroundID;
            if (biom.field_76756_M != BiomeGenBase.field_76769_d.field_76756_M && biom.field_76756_M != BiomeGenBase.field_76786_s.field_76756_M && biom.field_76756_M != BiomeGenBase.field_76787_r.field_76756_M) {
               groundID = Blocks.field_150349_c;
               undergroundID = Blocks.field_150346_d;
            } else {
               groundID = Blocks.field_150354_m;
               undergroundID = Blocks.field_150354_m;
               stone = Blocks.field_150322_A;
               stoneMeta = 0;
               brick = Blocks.field_150322_A;
               brickMeta = 2;
            }

            this.func_74878_a(world, this.field_74887_e, 3, 1, 0, 7, 3, 10);
            this.func_74878_a(world, this.field_74887_e, 0, 1, 3, 10, 3, 7);
            this.func_74878_a(world, this.field_74887_e, 1, 1, 1, 9, 3, 9);
            this.func_151549_a(world, this.field_74887_e, 3, 0, 0, 7, 0, 10, (Block)groundID, (Block)groundID, false);
            this.func_151549_a(world, this.field_74887_e, 0, 0, 3, 10, 0, 7, (Block)groundID, (Block)groundID, false);
            this.func_151549_a(world, this.field_74887_e, 1, 0, 1, 9, 0, 9, (Block)groundID, (Block)groundID, false);
            int ground = 1;
            Block altarBrick = Blocks.field_150417_aV;
            int altarMeta = true;
            this.place(altarBrick, 3, 4, ground, 4, this.field_74887_e, world);
            this.place(altarBrick, 3, 4, ground, 5, this.field_74887_e, world);
            this.place(altarBrick, 3, 4, ground, 6, this.field_74887_e, world);
            this.place(altarBrick, 3, 5, ground, 4, this.field_74887_e, world);
            this.place(Blocks.field_150355_j, 0, 5, ground, 5, this.field_74887_e, world);
            this.setDispenser(5, ground - 1, 5, random, world, 5);
            this.place(altarBrick, 3, 5, ground, 6, this.field_74887_e, world);
            this.place(altarBrick, 3, 6, ground, 4, this.field_74887_e, world);
            this.place(altarBrick, 3, 6, ground, 5, this.field_74887_e, world);
            this.place(altarBrick, 3, 6, ground, 6, this.field_74887_e, world);
            String mobType = "Witch";
            this.setSpawner(4, ground - 1, 4, "Witch", world);
            this.setSpawner(6, ground - 1, 6, "Witch", world);
            this.place(stone, stoneMeta, 1, ground, 2, this.field_74887_e, world);
            this.place(stone, stoneMeta, 2, ground, 1, this.field_74887_e, world);
            this.place(Blocks.field_150321_G, 0, 3, ground, 0, this.field_74887_e, world);
            this.place(stone, stoneMeta, 4, ground, 0, this.field_74887_e, world);
            this.place(stone, stoneMeta, 6, ground, 0, this.field_74887_e, world);
            this.place(stone, stoneMeta, 8, ground, 1, this.field_74887_e, world);
            this.place(stone, stoneMeta, 9, ground, 2, this.field_74887_e, world);
            this.place(stone, stoneMeta, 10, ground, 4, this.field_74887_e, world);
            this.place(stone, stoneMeta, 10, ground, 6, this.field_74887_e, world);
            this.place(stone, stoneMeta, 9, ground, 8, this.field_74887_e, world);
            this.place(stone, stoneMeta, 8, ground, 9, this.field_74887_e, world);
            this.place(stone, stoneMeta, 6, ground, 10, this.field_74887_e, world);
            this.place(stone, stoneMeta, 4, ground, 10, this.field_74887_e, world);
            this.place(stone, stoneMeta, 2, ground, 9, this.field_74887_e, world);
            this.place(stone, stoneMeta, 1, ground, 8, this.field_74887_e, world);
            this.place(stone, stoneMeta, 0, ground, 4, this.field_74887_e, world);
            this.place(stone, stoneMeta, 0, ground, 6, this.field_74887_e, world);
            int ground = ground + 1;
            this.place(brick, brickMeta, 1, ground, 2, this.field_74887_e, world);
            this.place(brick, brickMeta, 2, ground, 1, this.field_74887_e, world);
            this.place(stone, stoneMeta, 4, ground, 0, this.field_74887_e, world);
            this.place(stone, stoneMeta, 6, ground, 0, this.field_74887_e, world);
            this.place(brick, brickMeta, 8, ground, 1, this.field_74887_e, world);
            this.place(brick, brickMeta, 9, ground, 2, this.field_74887_e, world);
            this.place(stone, stoneMeta, 10, ground, 4, this.field_74887_e, world);
            this.place(stone, stoneMeta, 10, ground, 6, this.field_74887_e, world);
            this.place(brick, brickMeta, 9, ground, 8, this.field_74887_e, world);
            this.place(brick, brickMeta, 8, ground, 9, this.field_74887_e, world);
            this.place(stone, stoneMeta, 6, ground, 10, this.field_74887_e, world);
            this.place(stone, stoneMeta, 4, ground, 10, this.field_74887_e, world);
            this.place(brick, brickMeta, 2, ground, 9, this.field_74887_e, world);
            this.place(brick, brickMeta, 1, ground, 8, this.field_74887_e, world);
            this.place(stone, stoneMeta, 0, ground, 4, this.field_74887_e, world);
            this.place(stone, stoneMeta, 0, ground, 6, this.field_74887_e, world);
            ++ground;
            this.place(stone, stoneMeta, 1, ground, 2, this.field_74887_e, world);
            this.place(stone, stoneMeta, 2, ground, 1, this.field_74887_e, world);
            this.place(brick, brickMeta, 3, ground, 1, this.field_74887_e, world);
            this.place(brick, brickMeta, 4, ground, 0, this.field_74887_e, world);
            this.place(brick, brickMeta, 5, ground, 0, this.field_74887_e, world);
            this.place(brick, brickMeta, 6, ground, 0, this.field_74887_e, world);
            this.place(brick, brickMeta, 7, ground, 1, this.field_74887_e, world);
            this.place(stone, stoneMeta, 8, ground, 1, this.field_74887_e, world);
            this.place(stone, stoneMeta, 9, ground, 2, this.field_74887_e, world);
            this.place(brick, brickMeta, 9, ground, 3, this.field_74887_e, world);
            this.place(brick, brickMeta, 10, ground, 4, this.field_74887_e, world);
            this.place(brick, brickMeta, 10, ground, 5, this.field_74887_e, world);
            this.place(brick, brickMeta, 10, ground, 6, this.field_74887_e, world);
            this.place(brick, brickMeta, 9, ground, 7, this.field_74887_e, world);
            this.place(Blocks.field_150321_G, 0, 10, ground - 1, 7, this.field_74887_e, world);
            this.place(stone, stoneMeta, 9, ground, 8, this.field_74887_e, world);
            this.place(stone, stoneMeta, 8, ground, 9, this.field_74887_e, world);
            this.place(brick, brickMeta, 7, ground, 9, this.field_74887_e, world);
            this.place(brick, brickMeta, 6, ground, 10, this.field_74887_e, world);
            this.place(brick, brickMeta, 5, ground, 10, this.field_74887_e, world);
            this.place(brick, brickMeta, 4, ground, 10, this.field_74887_e, world);
            this.place(brick, brickMeta, 3, ground, 9, this.field_74887_e, world);
            this.place(stone, stoneMeta, 2, ground, 9, this.field_74887_e, world);
            this.place(stone, stoneMeta, 1, ground, 8, this.field_74887_e, world);
            this.place(Blocks.field_150321_G, 0, 0, ground, 7, this.field_74887_e, world);
            this.place(brick, brickMeta, 1, ground, 3, this.field_74887_e, world);
            this.place(brick, brickMeta, 0, ground, 4, this.field_74887_e, world);
            this.place(brick, brickMeta, 0, ground, 5, this.field_74887_e, world);
            this.place(brick, brickMeta, 0, ground, 6, this.field_74887_e, world);
            this.place(brick, brickMeta, 1, ground, 7, this.field_74887_e, world);

            for(int x = 0; x < 11; ++x) {
               for(int z = 0; z < 11; ++z) {
                  this.func_151554_b(world, (Block)undergroundID, 0, x, 0, z, this.field_74887_e);
                  this.func_74871_b(world, x, 4, z, this.field_74887_e);
               }
            }

            this.spawnWitches(world, this.field_74887_e, 7, 1, 4, 1);
            return true;
         } else {
            return false;
         }
      }
   }

   protected void func_143012_a(NBTTagCompound par1NBTTagCompound) {
      super.func_143012_a(par1NBTTagCompound);
      par1NBTTagCompound.func_74768_a("WITCWCount", this.witchesSpawned);
   }

   protected void func_143011_b(NBTTagCompound par1NBTTagCompound) {
      super.func_143011_b(par1NBTTagCompound);
      if (par1NBTTagCompound.func_74764_b("WITCWCount")) {
         this.witchesSpawned = par1NBTTagCompound.func_74762_e("WITCWCount");
      } else {
         this.witchesSpawned = 0;
      }

   }
}
