package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class RiteClimateChange extends Rite {
   protected final int radius;

   public RiteClimateChange(int radius) {
      this.radius = radius;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteClimateChange.StepClimateChange(this, intialStage));
   }

   private static class StepClimateChange extends RitualStep {
      private final RiteClimateChange rite;
      private int stage = 0;
      private boolean activated;

      public StepClimateChange(RiteClimateChange rite, int initialStage) {
         super(false);
         this.rite = rite;
         this.stage = initialStage;
      }

      public int getCurrentStage() {
         return (byte)this.stage;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (!this.activated) {
            if (ticks % 20L != 0L) {
               return RitualStep.Result.STARTING;
            }

            this.activated = true;
            SoundEffect.RANDOM_FIZZ.playAt(world, (double)posX, (double)posY, (double)posZ);
         }

         if (!world.field_72995_K) {
            EntityPlayer player = ritual.getInitiatingPlayer(world);
            if (!Config.instance().allowBiomeChanging) {
               SoundEffect.NOTE_SNARE.playAt(world, (double)posX, (double)posY, (double)posZ);
               if (player != null) {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.disabled");
               }

               return RitualStep.Result.ABORTED_REFUND;
            } else {
               BiomeGenBase biome = world.func_72807_a(posX, posZ);
               if (world.field_73011_w.field_76574_g != 1 && world.field_73011_w.field_76574_g != -1 && biome != BiomeGenBase.field_76779_k && biome != BiomeGenBase.field_76778_j) {
                  if (ritual.covenSize < 4) {
                     SoundEffect.NOTE_SNARE.playAt(world, (double)posX, (double)posY, (double)posZ);
                     if (player != null) {
                        ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.coventoosmall");
                     }

                     return RitualStep.Result.ABORTED_REFUND;
                  } else if (ticks % 20L != 0L) {
                     return RitualStep.Result.UPKEEP;
                  } else {
                     ++this.stage;
                     if (this.stage < 5) {
                        ParticleEffect.INSTANT_SPELL.send(SoundEffect.NONE, world, 0.5D + (double)posX, 1.0D + (double)posY, 0.5D + (double)posZ, (double)((float)this.stage * 1.5F), (double)((float)this.stage * 1.1F), 16);
                     } else if (this.stage == 5) {
                        ParticleEffect.HUGE_EXPLOSION.send(SoundEffect.NONE, world, 0.5D + (double)posX, 1.0D + (double)posY, 0.5D + (double)posZ, (double)((float)this.stage * 2.0F), (double)((float)this.stage * 1.5F), 16);
                        double RADIUS = 8.0D;
                        List items = world.func_72872_a(EntityItem.class, AxisAlignedBB.func_72330_a((double)posX - 8.0D, (double)(posY - 2), (double)posZ - 8.0D, (double)posX + 8.0D, (double)(posY + 2), (double)posZ + 8.0D));
                        Type biomeType = Type.END;
                        RiteClimateChange.WeatherChange weather = RiteClimateChange.WeatherChange.NONE;
                        int glowstone = 0;
                        Iterator i$ = items.iterator();

                        while(true) {
                           EntityItem item;
                           while(true) {
                              if (!i$.hasNext()) {
                                 if (biomeType == Type.END) {
                                    SoundEffect.NOTE_SNARE.playAt(world, (double)posX, (double)posY, (double)posZ);
                                    if (player != null) {
                                       ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.missingbiomefoci");
                                    }

                                    return RitualStep.Result.ABORTED_REFUND;
                                 }

                                 BiomeGenBase[] biomes = BiomeDictionary.getBiomesForType(biomeType);
                                 if (biomes != null && biomes.length != 0) {
                                    int biomeID = biomes[glowstone > 0 ? Math.min(glowstone, biomes.length) - 1 : (biomes.length >= 3 ? world.field_73012_v.nextInt(3) : 0)].field_76756_M;
                                    int maxRadius = this.rite.radius * (ritual.covenSize - 3);
                                    HashMap<RiteClimateChange.StepClimateChange.ChunkCoord, byte[]> chunkMap = new HashMap();
                                    this.drawFilledCircle(world, posX, posZ, maxRadius, chunkMap, weather, biomeID);
                                    ArrayList chunks = new ArrayList();
                                    Iterator i$ = chunkMap.entrySet().iterator();

                                    while(i$.hasNext()) {
                                       Entry<RiteClimateChange.StepClimateChange.ChunkCoord, byte[]> entry = (Entry)i$.next();
                                       Chunk chunk = ((RiteClimateChange.StepClimateChange.ChunkCoord)entry.getKey()).getChunk(world);
                                       chunk.func_76616_a((byte[])entry.getValue());
                                       chunks.add(chunk);
                                    }

                                    S26PacketMapChunkBulk packet = new S26PacketMapChunkBulk(chunks);
                                    Witchery.packetPipeline.sendToDimension(packet, world);
                                    Iterator i$ = chunks.iterator();

                                    while(i$.hasNext()) {
                                       Object obj = i$.next();
                                       Chunk chunk = (Chunk)obj;
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

                                    if (world instanceof WorldServer) {
                                       WorldInfo worldinfo = ((WorldServer)world).func_72912_H();
                                       int i = (300 + world.field_73012_v.nextInt(600)) * 20;
                                       switch(weather) {
                                       case SUN:
                                          if (world.func_72896_J() || world.func_72911_I()) {
                                             worldinfo.func_76080_g(0);
                                             worldinfo.func_76090_f(0);
                                             worldinfo.func_76084_b(false);
                                             worldinfo.func_76069_a(false);
                                          }
                                          break;
                                       case RAIN:
                                          if (!world.func_72896_J() && !world.func_72911_I()) {
                                             worldinfo.func_76080_g(i);
                                             worldinfo.func_76090_f(i);
                                             worldinfo.func_76084_b(true);
                                             worldinfo.func_76069_a(false);
                                          }
                                          break;
                                       case THUNDER:
                                          if (!world.func_72911_I()) {
                                             worldinfo.func_76080_g(i);
                                             worldinfo.func_76090_f(i);
                                             worldinfo.func_76084_b(true);
                                             worldinfo.func_76069_a(true);
                                          }
                                       case NONE:
                                       }
                                    }

                                    return RitualStep.Result.COMPLETED;
                                 }

                                 SoundEffect.NOTE_SNARE.playAt(world, (double)posX, (double)posY, (double)posZ);
                                 if (player != null) {
                                    ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "witchery.rite.missingbiomefoci");
                                 }

                                 return RitualStep.Result.ABORTED_REFUND;
                              }

                              Object obj = i$.next();
                              item = (EntityItem)obj;
                              ItemStack stack = item.func_92059_d();
                              if (stack.func_77969_a(new ItemStack(Blocks.field_150345_g, 1, 0))) {
                                 biomeType = Type.FOREST;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150329_H, 1, 1))) {
                                 biomeType = Type.PLAINS;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150343_Z))) {
                                 biomeType = Type.MOUNTAIN;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150348_b))) {
                                 biomeType = Type.HILLS;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Items.field_151123_aH))) {
                                 biomeType = Type.SWAMP;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Items.field_151131_as))) {
                                 biomeType = Type.WATER;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150434_aF))) {
                                 biomeType = Type.DESERT;
                                 weather = RiteClimateChange.WeatherChange.SUN;
                                 break;
                              }

                              if (stack.func_77969_a(Witchery.Items.GENERIC.itemIcyNeedle.createStack())) {
                                 biomeType = Type.FROZEN;
                                 weather = RiteClimateChange.WeatherChange.RAIN;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150345_g, 1, 3))) {
                                 biomeType = Type.JUNGLE;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150424_aL))) {
                                 biomeType = Type.WASTELAND;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150354_m))) {
                                 biomeType = Type.BEACH;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Blocks.field_150337_Q))) {
                                 biomeType = Type.MUSHROOM;
                                 break;
                              }

                              if (stack.func_77969_a(new ItemStack(Items.field_151144_bL))) {
                                 biomeType = Type.MAGICAL;
                                 break;
                              }

                              if (stack.func_77973_b() == Items.field_151114_aO) {
                                 glowstone += stack.field_77994_a;
                                 break;
                              }
                           }

                           world.func_72900_e(item);
                           ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_POP, item, 0.5D, 1.0D, 16);
                        }
                     }

                     return RitualStep.Result.UPKEEP;
                  }
               } else {
                  SoundEffect.NOTE_SNARE.playAt(world, (double)posX, (double)posY, (double)posZ);
                  if (player != null) {
                     ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.wrongdimension");
                  }

                  return RitualStep.Result.ABORTED_REFUND;
               }
            }
         } else {
            return RitualStep.Result.COMPLETED;
         }
      }

      private static byte[] rotateMatrix(byte[] matrix, int n) {
         byte[] ret = new byte[matrix.length];

         for(int i = 0; i < matrix.length / n; ++i) {
            for(int j = 0; j < n; ++j) {
               ret[j * n + i] = matrix[i * n + n - j];
            }
         }

         return ret;
      }

      protected void drawFilledCircle(World world, int x0, int z0, int radius, HashMap<RiteClimateChange.StepClimateChange.ChunkCoord, byte[]> chunkMap, RiteClimateChange.WeatherChange weather, int biomeID) {
         int x = radius;
         int z = 0;
         int radiusError = 1 - radius;

         while(x >= z) {
            this.drawLine(world, -x + x0, x + x0, z + z0, chunkMap, weather, biomeID);
            this.drawLine(world, -z + x0, z + x0, x + z0, chunkMap, weather, biomeID);
            this.drawLine(world, -x + x0, x + x0, -z + z0, chunkMap, weather, biomeID);
            this.drawLine(world, -z + x0, z + x0, -x + z0, chunkMap, weather, biomeID);
            ++z;
            if (radiusError < 0) {
               radiusError += 2 * z + 1;
            } else {
               --x;
               radiusError += 2 * (z - x + 1);
            }
         }

      }

      protected void drawLine(World world, int x1, int x2, int z, HashMap<RiteClimateChange.StepClimateChange.ChunkCoord, byte[]> chunkMap, RiteClimateChange.WeatherChange weather, int biomeID) {
         for(int x = x1; x <= x2; ++x) {
            RiteClimateChange.StepClimateChange.ChunkCoord coord = new RiteClimateChange.StepClimateChange.ChunkCoord(x >> 4, z >> 4);
            byte[] map = (byte[])chunkMap.get(coord);
            if (map == null) {
               Chunk chunk = world.func_72938_d(x, z);
               map = (byte[])chunk.func_76605_m().clone();
               chunkMap.put(coord, map);
            }

            map[(z & 15) << 4 | x & 15] = (byte)biomeID;
            if (weather == RiteClimateChange.WeatherChange.SUN) {
               int y = world.func_72825_h(x, z);
               if (world.func_147439_a(x, y, z) == Blocks.field_150433_aE) {
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
               RiteClimateChange.StepClimateChange.ChunkCoord other = (RiteClimateChange.StepClimateChange.ChunkCoord)obj;
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

   public static enum WeatherChange {
      NONE,
      SUN,
      RAIN,
      THUNDER;
   }
}
