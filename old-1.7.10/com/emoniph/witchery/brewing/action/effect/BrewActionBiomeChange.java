package com.emoniph.witchery.brewing.action.effect;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.AltarPower;
import com.emoniph.witchery.brewing.BrewItemKey;
import com.emoniph.witchery.brewing.BrewNamePart;
import com.emoniph.witchery.brewing.EffectLevel;
import com.emoniph.witchery.brewing.ModifiersEffect;
import com.emoniph.witchery.brewing.ModifiersImpact;
import com.emoniph.witchery.brewing.ModifiersRitual;
import com.emoniph.witchery.brewing.Probability;
import com.emoniph.witchery.brewing.action.BrewActionEffect;
import com.emoniph.witchery.brewing.action.BrewActionList;
import com.emoniph.witchery.item.ItemBook;
import com.emoniph.witchery.util.Coord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

public class BrewActionBiomeChange extends BrewActionEffect {
   public BrewActionBiomeChange(BrewItemKey itemKey, BrewNamePart namePart, AltarPower powerCost, Probability baseProbability, EffectLevel effectLevel) {
      super(itemKey, namePart, powerCost, baseProbability, effectLevel);
   }

   public void prepareSplashPotion(World world, BrewActionList actionList, ModifiersImpact modifiers) {
      super.prepareSplashPotion(world, actionList, modifiers);
      modifiers.setOnlyInstant();
   }

   protected void doApplyRitualToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersRitual ritualModifiers, ModifiersEffect modifiers, ItemStack stack) {
      BiomeGenBase biome = ItemBook.getSelectedBiome(stack.func_77960_j());
      int maxRadius = 16 + modifiers.getStrength() * 16;
      this.changeBiome(world, new Coord(x, y, z), maxRadius, biome);
   }

   protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
      BiomeGenBase biome = ItemBook.getSelectedBiome(actionStack.func_77960_j());
      this.changeBiome(world, new Coord(x, y, z), 1 + modifiers.getStrength(), biome);
   }

   protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
   }

   protected void changeBiome(World world, Coord coord, int radius, BiomeGenBase biome) {
      HashMap<BrewActionBiomeChange.ChunkCoord, byte[]> chunkMap = new HashMap();
      this.drawFilledCircle(world, coord.x, coord.z, radius, chunkMap, biome);
      ArrayList<Chunk> chunks = new ArrayList();
      Iterator i$ = chunkMap.entrySet().iterator();

      Chunk chunk;
      while(i$.hasNext()) {
         Entry<BrewActionBiomeChange.ChunkCoord, byte[]> entry = (Entry)i$.next();
         chunk = ((BrewActionBiomeChange.ChunkCoord)entry.getKey()).getChunk(world);
         chunk.func_76616_a((byte[])entry.getValue());
         chunks.add(chunk);
      }

      S26PacketMapChunkBulk packet = new S26PacketMapChunkBulk(chunks);
      Witchery.packetPipeline.sendToDimension(packet, world);
      Iterator i$ = chunks.iterator();

      while(i$.hasNext()) {
         chunk = (Chunk)i$.next();
         Iterator i$ = chunk.field_150816_i.values().iterator();

         while(i$.hasNext()) {
            Object tileObj = i$.next();
            TileEntity tile = (TileEntity)tileObj;
            Packet packet2 = tile.func_145844_m();
            if (packet2 != null) {
               world.func_147471_g(tile.field_145851_c, tile.field_145848_d, tile.field_145849_e);
            }
         }
      }

   }

   private void drawFilledCircle(World world, int x0, int z0, int radius, HashMap<BrewActionBiomeChange.ChunkCoord, byte[]> chunkMap, BiomeGenBase biome) {
      if (radius == 1) {
         this.drawLine(world, x0, x0, z0, chunkMap, biome);
      } else {
         --radius;
         int x = radius;
         int z = 0;
         int radiusError = 1 - radius;

         while(x >= z) {
            this.drawLine(world, -x + x0, x + x0, z + z0, chunkMap, biome);
            this.drawLine(world, -z + x0, z + x0, x + z0, chunkMap, biome);
            this.drawLine(world, -x + x0, x + x0, -z + z0, chunkMap, biome);
            this.drawLine(world, -z + x0, z + x0, -x + z0, chunkMap, biome);
            ++z;
            if (radiusError < 0) {
               radiusError += 2 * z + 1;
            } else {
               --x;
               radiusError += 2 * (z - x + 1);
            }
         }
      }

   }

   private void drawLine(World world, int x1, int x2, int z, HashMap<BrewActionBiomeChange.ChunkCoord, byte[]> chunkMap, BiomeGenBase biome) {
      for(int x = x1; x <= x2; ++x) {
         BrewActionBiomeChange.ChunkCoord coord = new BrewActionBiomeChange.ChunkCoord(x >> 4, z >> 4);
         byte[] map = (byte[])chunkMap.get(coord);
         if (map == null) {
            Chunk chunk = world.func_72938_d(x, z);
            map = (byte[])chunk.func_76605_m().clone();
            chunkMap.put(coord, map);
         }

         map[(z & 15) << 4 | x & 15] = (byte)biome.field_76756_M;
         if (biome.field_76751_G == 0.0F) {
            int y = world.func_72825_h(x, z);
            if (world.func_147439_a(x, y, z) == Blocks.field_150431_aC) {
               world.func_147468_f(x, y, z);
            }
         }
      }

   }

   private static class ChunkCoord {
      public final int X;
      public final int Z;

      public ChunkCoord(int x, int z) {
         this.X = x;
         this.Z = z;
      }

      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && obj.getClass() == this.getClass()) {
            BrewActionBiomeChange.ChunkCoord other = (BrewActionBiomeChange.ChunkCoord)obj;
            return this.X == other.X && this.Z == other.Z;
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.X ^ this.X >>> 32;
         result = 31 * result + (this.Z ^ this.Z >>> 32);
         return result;
      }

      public Chunk getChunk(World world) {
         return world.func_72964_e(this.X, this.Z);
      }
   }
}
