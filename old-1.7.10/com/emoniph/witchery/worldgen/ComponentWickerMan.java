package com.emoniph.witchery.worldgen;

import com.emoniph.witchery.util.Config;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class ComponentWickerMan extends WitcheryComponent {
   public static final int DIM_X = 6;
   public static final int DIM_Y = 8;
   public static final int DIM_Z = 5;

   public ComponentWickerMan() {
   }

   public ComponentWickerMan(int direction, Random random, int x, int z) {
      super(direction, random, x, z, 6, 8, 5);
   }

   public boolean addComponentParts(World world, Random random) {
      BiomeGenBase biom = world.func_72807_a(this.func_74865_a(0, 0), this.func_74873_b(0, 0));
      int groundAvg = this.calcGroundHeight(world, this.field_74887_e);
      if (groundAvg < 0) {
         return true;
      } else {
         this.field_74887_e.func_78886_a(0, groundAvg - this.field_74887_e.field_78894_e + 8 - 1, 0);
         Block wicker = Blocks.field_150407_cf;
         Block plant = Blocks.field_150328_O;
         Object groundID;
         Object undergroundID;
         if (biom.field_76756_M != BiomeGenBase.field_76769_d.field_76756_M && biom.field_76756_M != BiomeGenBase.field_76786_s.field_76756_M && biom.field_76756_M != BiomeGenBase.field_76787_r.field_76756_M) {
            groundID = Blocks.field_150349_c;
            undergroundID = Blocks.field_150346_d;
         } else {
            groundID = Blocks.field_150354_m;
            undergroundID = Blocks.field_150354_m;
            plant = Blocks.field_150330_I;
         }

         boolean flip = this.field_74885_f == 0 || this.field_74885_f == 2;
         this.func_74878_a(world, this.field_74887_e, 1, 1, 0, 4, 7, 4);
         this.func_74878_a(world, this.field_74887_e, 0, 1, 2, 5, 7, 2);
         this.func_151549_a(world, this.field_74887_e, 1, 0, 0, 4, 0, 4, (Block)groundID, (Block)groundID, false);
         this.func_151549_a(world, this.field_74887_e, 0, 0, 2, 5, 0, 2, (Block)groundID, (Block)groundID, false);
         int ground = 1;
         this.place((Block)plant, 0, 1, ground, 0, this.field_74887_e, world);
         this.place((Block)plant, 0, 4, ground, 0, this.field_74887_e, world);
         this.place((Block)plant, 0, 0, ground, 2, this.field_74887_e, world);
         this.place((Block)plant, 0, 5, ground, 2, this.field_74887_e, world);
         this.place((Block)plant, 0, 4, ground, 4, this.field_74887_e, world);
         this.place((Block)plant, 0, 1, ground, 4, this.field_74887_e, world);
         int horz = false;
         int vert = flip ? 4 : 8;
         int flat = flip ? 8 : 4;
         int spawnables = Config.instance().strawmanSpawnerRules.length;
         String spawnable = spawnables > 0 ? Config.instance().strawmanSpawnerRules[world.field_73012_v.nextInt(spawnables)] : "Zombie";
         this.setSpawner(2, 0, 2, spawnable != null && !spawnable.isEmpty() ? spawnable : "Zombie", world);
         this.place(wicker, vert, 2, ground, 2, this.field_74887_e, world);
         this.place(wicker, vert, 3, ground, 2, this.field_74887_e, world);
         this.place(wicker, vert, 2, ground + 1, 2, this.field_74887_e, world);
         this.place(wicker, vert, 3, ground + 1, 2, this.field_74887_e, world);
         this.place(wicker, flat, 1, ground + 2, 2, this.field_74887_e, world);
         this.place(wicker, vert, 2, ground + 2, 2, this.field_74887_e, world);
         this.place(wicker, vert, 3, ground + 2, 2, this.field_74887_e, world);
         this.place(wicker, flat, 4, ground + 2, 2, this.field_74887_e, world);
         this.place(wicker, vert, 1, ground + 3, 2, this.field_74887_e, world);
         this.place(wicker, 0, 2, ground + 3, 2, this.field_74887_e, world);
         this.place(wicker, 0, 3, ground + 3, 2, this.field_74887_e, world);
         this.place(wicker, vert, 4, ground + 3, 2, this.field_74887_e, world);
         this.place(wicker, 0, 1, ground + 4, 2, this.field_74887_e, world);
         this.place(wicker, 0, 2, ground + 4, 2, this.field_74887_e, world);
         this.place(wicker, 0, 3, ground + 4, 2, this.field_74887_e, world);
         this.place(wicker, 0, 4, ground + 4, 2, this.field_74887_e, world);
         this.place(wicker, flat, 2, ground + 5, 2, this.field_74887_e, world);
         this.place(wicker, flat, 3, ground + 5, 2, this.field_74887_e, world);
         this.place(wicker, flat, 2, ground + 6, 2, this.field_74887_e, world);
         this.place(wicker, flat, 3, ground + 6, 2, this.field_74887_e, world);

         for(int x = 0; x < 6; ++x) {
            for(int z = 0; z < 5; ++z) {
               this.func_151554_b(world, (Block)undergroundID, 0, x, 0, z, this.field_74887_e);
               this.func_74871_b(world, x, 8, z, this.field_74887_e);
            }
         }

         return true;
      }
   }
}
