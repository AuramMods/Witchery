package com.emoniph.witchery.dimension;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.Config;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class WorldChunkManagerMirror implements IChunkProvider {
   private World world;

   public WorldChunkManagerMirror(World world) {
      this.world = world;
   }

   public boolean func_73149_a(int i, int j) {
      return true;
   }

   public Chunk func_73154_d(int x, int z) {
      Chunk chunk = new Chunk(this.world, x, z);
      byte[] abyte = chunk.func_76605_m();
      Block wall = Witchery.Blocks.MIRROR_WALL;
      Block air = Blocks.field_150350_a;

      for(int k = 0; k < abyte.length; ++k) {
         abyte[k] = (byte)BiomeGenBase.field_76778_j.field_76756_M;
      }

      int[] wallPointsXZ = new int[]{1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1};
      int[] wallPointsY = new int[]{1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

      for(int y = 0; y < 255; ++y) {
         int l = y >> 4;
         ExtendedBlockStorage extendedblockstorage = chunk.func_76587_i()[l];
         if (extendedblockstorage == null) {
            extendedblockstorage = new ExtendedBlockStorage(y, !this.world.field_73011_w.field_76576_e);
            chunk.func_76587_i()[l] = extendedblockstorage;
         }

         int _y = y & 15;

         for(int _x = 0; _x < 16; ++_x) {
            for(int _z = 0; _z < 16; ++_z) {
               if (Config.instance().shrinkMirrorWorld && y >= 128 || wallPointsY[_y] != 1 && wallPointsXZ[_x] != 1 && wallPointsXZ[_z] != 1) {
                  extendedblockstorage.func_150818_a(_x, _y, _z, air);
                  extendedblockstorage.func_76654_b(_x, _y, _z, 0);
               } else {
                  extendedblockstorage.func_150818_a(_x, _y, _z, wall);
                  extendedblockstorage.func_76654_b(_x, _y, _z, 0);
               }
            }
         }
      }

      chunk.func_76603_b();
      return chunk;
   }

   public Chunk func_73158_c(int x, int z) {
      return this.func_73154_d(x, z);
   }

   public void func_73153_a(IChunkProvider ichunkprovider, int i, int j) {
   }

   public boolean func_73151_a(boolean flag, IProgressUpdate iprogressupdate) {
      return true;
   }

   public boolean func_73156_b() {
      return false;
   }

   public boolean func_73157_c() {
      return true;
   }

   public String func_73148_d() {
      return "MirrorChunk";
   }

   public List func_73155_a(EnumCreatureType enumcreaturetype, int i, int j, int k) {
      return null;
   }

   public ChunkPosition func_147416_a(World world, String s, int i, int j, int k) {
      return null;
   }

   public int func_73152_e() {
      return 0;
   }

   public void func_82695_e(int i, int j) {
   }

   public void func_104112_b() {
   }
}
