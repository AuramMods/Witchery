package com.emoniph.witchery.worldgen;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBaseContainer;
import com.emoniph.witchery.blocks.TileEntityBase;
import com.emoniph.witchery.entity.EntityVillageGuard;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Log;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces.Church;
import net.minecraft.world.gen.structure.StructureVillagePieces.Field1;
import net.minecraft.world.gen.structure.StructureVillagePieces.Field2;
import net.minecraft.world.gen.structure.StructureVillagePieces.Hall;
import net.minecraft.world.gen.structure.StructureVillagePieces.House1;
import net.minecraft.world.gen.structure.StructureVillagePieces.House2;
import net.minecraft.world.gen.structure.StructureVillagePieces.House3;
import net.minecraft.world.gen.structure.StructureVillagePieces.House4Garden;
import net.minecraft.world.gen.structure.StructureVillagePieces.Path;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraft.world.gen.structure.StructureVillagePieces.WoodHut;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID;
import net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockMeta;

public class WorldHandlerVillageDistrict implements IVillageCreationHandler {
   private final Class<? extends Village> pieceClazz;
   private final int weight;
   private final int quantityMin;
   private final int quantityMax;

   public WorldHandlerVillageDistrict(Class<? extends Village> clazz, int weight, int min) {
      this(clazz, weight, min, min);
   }

   public WorldHandlerVillageDistrict(Class<? extends Village> clazz, int weight, int min, int max) {
      this.pieceClazz = clazz;
      this.weight = weight;
      this.quantityMin = min;
      this.quantityMax = max;
   }

   public PieceWeight getVillagePieceWeight(Random rand, int size) {
      return new PieceWeight(this.pieceClazz, this.weight, this.quantityMax <= this.quantityMin ? this.quantityMin : this.quantityMin + rand.nextInt(this.quantityMax - this.quantityMin + 1));
   }

   public Class getComponentClass() {
      return this.pieceClazz;
   }

   public Object buildComponent(PieceWeight weight, Start startPiece, List pieces, Random rand, int p1, int p2, int p3, int p4, int p5) {
      Object object = null;
      if (this.pieceClazz == House4Garden.class) {
         object = House4Garden.func_74912_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == Church.class) {
         object = Church.func_74919_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == House1.class) {
         object = House1.func_74898_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == WoodHut.class) {
         object = WoodHut.func_74908_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == Hall.class) {
         object = Hall.func_74906_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == Field1.class) {
         object = Field1.func_74900_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == Field2.class) {
         object = Field2.func_74902_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == House2.class) {
         object = House2.func_74915_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == House3.class) {
         object = House3.func_74921_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == WorldHandlerVillageDistrict.Wall.class) {
         object = WorldHandlerVillageDistrict.Wall.func_74921_a(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == ComponentVillageWatchTower.class) {
         object = ComponentVillageWatchTower.construct(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      } else if (this.pieceClazz == ComponentVillageKeep.class) {
         object = ComponentVillageKeep.construct(startPiece, pieces, rand, p1, p2, p3, p4, p5);
      }

      return object == null ? null : (Village)object;
   }

   public static void registerComponent(Class<? extends Village> clazz, int weight, int min, int max) {
      VillagerRegistry.instance().registerVillageCreationHandler(new WorldHandlerVillageDistrict(clazz, weight, min, max));
   }

   public static void preInit() {
      try {
         MapGenStructureIO.func_143031_a(WorldHandlerVillageDistrict.Wall.class, "witchery:villagewall");
         MapGenStructureIO.func_143031_a(ComponentVillageKeep.class, "witchery:villagekeep");
         MapGenStructureIO.func_143031_a(ComponentVillageWatchTower.class, "witchery:villagewatchtower");
      } catch (Throwable var4) {
      }

      if (Config.instance().townWallChance > 0) {
         registerComponent(WorldHandlerVillageDistrict.Wall.class, Config.instance().townWallWeight, Config.instance().townWallChance == 2 ? 1 : 0, 1);
      }

      if (Config.instance().townKeepChance > 0) {
         registerComponent(ComponentVillageKeep.class, Config.instance().townKeepWeight, Config.instance().townKeepChance == 2 ? 1 : 0, 1);
      }

      VillagerRegistry register = VillagerRegistry.instance();
      Iterator i$ = Config.instance().townParts.iterator();

      while(i$.hasNext()) {
         Config.Building building = (Config.Building)i$.next();

         for(int i = 0; i < building.groups; ++i) {
            register.registerVillageCreationHandler(new WorldHandlerVillageDistrict(building.clazz, building.weight, building.min, building.max));
         }
      }

   }

   public static void init() {
      BiomeGenBase[] arr$ = BiomeGenBase.func_150565_n();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         BiomeGenBase biome = arr$[i$];
         if (biome != null && !BiomeDictionary.isBiomeOfType(biome, Type.WET) && !BiomeDictionary.isBiomeOfType(biome, Type.OCEAN) && !BiomeDictionary.isBiomeOfType(biome, Type.BEACH) && !BiomeDictionary.isBiomeOfType(biome, Type.END) && !BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE) && !BiomeDictionary.isBiomeOfType(biome, Type.NETHER) && !BiomeDictionary.isBiomeOfType(biome, Type.RIVER) && !BiomeDictionary.isBiomeOfType(biome, Type.WATER)) {
            boolean disallowed = !Config.instance().townAllowSandy && BiomeDictionary.isBiomeOfType(biome, Type.SANDY) || !Config.instance().townAllowPlains && BiomeDictionary.isBiomeOfType(biome, Type.PLAINS) || !Config.instance().townAllowMountain && BiomeDictionary.isBiomeOfType(biome, Type.MOUNTAIN) || !Config.instance().townAllowHills && BiomeDictionary.isBiomeOfType(biome, Type.HILLS) || !Config.instance().townAllowForest && BiomeDictionary.isBiomeOfType(biome, Type.FOREST) || !Config.instance().townAllowSnowy && BiomeDictionary.isBiomeOfType(biome, Type.SNOWY) || !Config.instance().townAllowWasteland && BiomeDictionary.isBiomeOfType(biome, Type.WASTELAND) || !Config.instance().townAllowJungle && BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE) || !Config.instance().townAllowMesa && BiomeDictionary.isBiomeOfType(biome, Type.MESA);
            if (!disallowed) {
               net.minecraftforge.common.BiomeManager.addVillageBiome(biome, true);
            }
         }
      }

   }

   public static class Wall extends Village {
      private Start start;
      private List pieces;
      private boolean hasMadeWallBlock;

      public Wall() {
      }

      public Wall(Start start, int componentType, Random rand, StructureBoundingBox bounds, int baseMode) {
         super(start, componentType);
         this.field_74885_f = baseMode;
         this.field_74887_e = bounds;
         this.start = start;
      }

      public static WorldHandlerVillageDistrict.Wall func_74921_a(Start startPiece, List pieces, Random rand, int p1, int p2, int p3, int p4, int p5) {
         StructureBoundingBox bounds = StructureBoundingBox.func_78889_a(p1, p2, p3, 0, 0, 0, 2, 7, 2, p4);
         boolean create = func_74895_a(bounds) && StructureComponent.func_74883_a(pieces, bounds) == null && !containsWalls(pieces);
         return create ? new WorldHandlerVillageDistrict.Wall(startPiece, p5, rand, bounds, p4) : null;
      }

      private static boolean containsWalls(List pieces2) {
         return false;
      }

      public void func_74861_a(StructureComponent component, List pieces, Random rand) {
         super.func_74861_a(component, pieces, rand);
         this.pieces = pieces;
      }

      public boolean func_74875_a(World world, Random rand, StructureBoundingBox bounds) {
         if (this.field_143015_k < 0) {
            this.field_143015_k = this.func_74889_b(world, bounds);
            if (this.field_143015_k < 0) {
               return true;
            }

            this.field_74887_e.func_78886_a(0, this.field_143015_k - this.field_74887_e.field_78894_e + 7 - 1, 0);
         }

         if (!this.hasMadeWallBlock) {
            int x = 1;
            int z = 1;
            int xCoord = this.func_74865_a(x, z);
            int yCoord = this.func_74862_a(1);
            int zCoord = this.func_74873_b(x, z);
            if (this.pieces != null && bounds.func_78890_b(xCoord, yCoord, zCoord)) {
               this.hasMadeWallBlock = true;
               world.func_147449_b(xCoord, yCoord, zCoord, Witchery.Blocks.WALLGEN);
               WorldHandlerVillageDistrict.Wall.BlockVillageWallGen.TileEntityVillageWallGen tile = (WorldHandlerVillageDistrict.Wall.BlockVillageWallGen.TileEntityVillageWallGen)BlockUtil.getTileEntity(world, xCoord, yCoord, zCoord, WorldHandlerVillageDistrict.Wall.BlockVillageWallGen.TileEntityVillageWallGen.class);
               if (tile != null) {
                  tile.setStructure(this.pieces, this.start);
               }
            }
         }

         return true;
      }

      protected void func_143012_a(NBTTagCompound nbtRoot) {
         super.func_143012_a(nbtRoot);
         nbtRoot.func_74757_a("WallBlock", this.hasMadeWallBlock);
      }

      protected void func_143011_b(NBTTagCompound nbtRoot) {
         super.func_143011_b(nbtRoot);
         this.hasMadeWallBlock = nbtRoot.func_74767_n("WallBlock");
      }

      public static void placeWalls(World world, List<WorldHandlerVillageDistrict.Wall.StructureBounds> bb, int xCoord, int yCoord, int zCoord, BiomeGenBase biome, boolean desert) {
         int minX = Integer.MAX_VALUE;
         int minZ = Integer.MAX_VALUE;
         int maxX = Integer.MIN_VALUE;
         int maxZ = Integer.MIN_VALUE;
         Log.instance().debug(String.format("Generating town walls at [%d %d %d]", xCoord, yCoord, zCoord));

         for(int i = 0; i < bb.size(); ++i) {
            minX = Math.min(((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78897_a, minX);
            minZ = Math.min(((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78896_c, minZ);
            maxX = Math.max(((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78893_d, maxX);
            maxZ = Math.max(((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78892_f, maxZ);
         }

         if (maxX != Integer.MIN_VALUE && minX != Integer.MAX_VALUE && maxZ != Integer.MIN_VALUE && minZ != Integer.MAX_VALUE) {
            byte[][] a = new byte[maxX - minX + 3][maxZ - minZ + 3];
            short[][] b = new short[maxX - minX + 3][maxZ - minZ + 3];

            int x;
            int z;
            int p;
            int blockBaseMeta;
            int guardDist;
            int x;
            for(int i = 0; i < bb.size(); ++i) {
               x = ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78893_d - ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78897_a + 1;
               z = x / 2 + ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78897_a - 1;
               p = ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78892_f - ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78896_c + 1;
               blockBaseMeta = p / 2 + ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78896_c - 1;

               for(int x = ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78897_a; x <= ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78893_d; ++x) {
                  for(int z = ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78896_c; z <= ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78892_f; ++z) {
                     guardDist = x - minX + 1;
                     x = z - minZ + 1;
                     if (!((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).ew && (z == ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78896_c || z == ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78892_f) && x >= z - 1 && x <= z + 1) {
                        a[guardDist][x] = 3;
                     } else if (((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).ew && (x == ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78897_a || x == ((WorldHandlerVillageDistrict.Wall.StructureBounds)bb.get(i)).field_78893_d) && z >= blockBaseMeta - 1 && z <= blockBaseMeta + 1) {
                        a[guardDist][x] = 3;
                     } else {
                        a[guardDist][x] = 2;
                     }
                  }
               }
            }

            int range = 7;

            for(x = 1; x < a.length - range; ++x) {
               for(z = 1; z < a[x].length - range; ++z) {
                  if (a[x][z] == 2) {
                     for(p = 1; p < range; ++p) {
                        if (a[x + p][z] == 2 && a[x + p - 1][z] == 0) {
                           for(blockBaseMeta = p; blockBaseMeta > 0; --blockBaseMeta) {
                              a[x + blockBaseMeta][z] = 2;
                           }
                        }

                        if (a[x][z + p] == 2 && a[x][z + p - 1] == 0) {
                           for(blockBaseMeta = p; blockBaseMeta > 0; --blockBaseMeta) {
                              a[x][z + blockBaseMeta] = 2;
                           }
                        }
                     }
                  }
               }
            }

            boolean n;
            for(x = 1; x < a.length - 1; ++x) {
               for(z = 1; z < a[x].length - 1; ++z) {
                  boolean n = a[x][z - 1] == 0;
                  boolean s = a[x][z + 1] == 0;
                  boolean e = a[x + 1][z] == 0;
                  boolean w = a[x - 1][z] == 0;
                  boolean ne = a[x + 1][z - 1] == 0;
                  boolean sw = a[x - 1][z + 1] == 0;
                  boolean se = a[x + 1][z + 1] == 0;
                  n = a[x - 1][z - 1] == 0;
                  if (!n && !s && !e && !w && !ne && !se && !n && !sw) {
                     a[x][z] = 1;
                  }
               }
            }

            Block blockBase = Blocks.field_150417_aV;
            Block blockFence = Blocks.field_150422_aJ;
            Block stairsBlock = Blocks.field_150390_bg;
            blockBaseMeta = 0;
            GetVillageBlockID event = new GetVillageBlockID(biome, blockBase, blockBaseMeta);
            MinecraftForge.TERRAIN_GEN_BUS.post(event);
            if (event.getResult() == Result.DENY) {
               blockBase = event.replacement;
            } else if (desert) {
               blockBase = Blocks.field_150322_A;
            }

            event = new GetVillageBlockID(biome, blockFence, 0);
            MinecraftForge.TERRAIN_GEN_BUS.post(event);
            if (event.getResult() == Result.DENY) {
               blockFence = event.replacement;
            }

            event = new GetVillageBlockID(biome, stairsBlock, 0);
            MinecraftForge.TERRAIN_GEN_BUS.post(event);
            if (event.getResult() == Result.DENY) {
               stairsBlock = event.replacement;
            } else if (desert) {
               stairsBlock = Blocks.field_150372_bz;
            }

            GetVillageBlockMeta event2 = new GetVillageBlockMeta(biome, blockBase, blockBaseMeta);
            MinecraftForge.TERRAIN_GEN_BUS.post(event2);
            if (event2.getResult() == Result.DENY) {
               blockBaseMeta = event2.replacement;
            } else if (desert) {
               blockBaseMeta = 2;
            }

            guardDist = 0;

            for(x = 1; x < a.length - 1; ++x) {
               for(int z = 1; z < a[x].length - 1; ++z) {
                  n = a[x][z - 1] >= 2;
                  boolean s = a[x][z + 1] >= 2;
                  boolean e = a[x + 1][z] >= 2;
                  boolean w = a[x - 1][z] >= 2;
                  boolean ne = a[x + 1][z - 1] >= 2;
                  boolean sw = a[x - 1][z + 1] >= 2;
                  boolean se = a[x + 1][z + 1] >= 2;
                  boolean nw = a[x - 1][z - 1] >= 2;
                  if (a[x][z] >= 2) {
                     int dx = minX + x;
                     int dz = minZ + z;
                     int solidCount = 0;

                     int dy;
                     int startY;
                     for(dy = yCoord; dy > 1 && solidCount < 9; --dy) {
                        solidCount = 0;

                        for(int ddx = dx - 1; ddx <= dx + 1; ++ddx) {
                           for(startY = dz - 1; startY <= dz + 1; ++startY) {
                              Block block = world.func_147439_a(ddx, dy, startY);
                              boolean replaceable = block.func_149688_o().func_76222_j() || block.func_149688_o() == Material.field_151584_j || block.func_149688_o() == Material.field_151575_d || block.func_149688_o() == Material.field_151585_k;
                              if (block.func_149721_r() && !replaceable) {
                                 ++solidCount;
                              }
                           }
                        }
                     }

                     int minHeight = true;
                     startY = dy + 9;
                     int near = Math.max(Math.max(Math.max(b[x - 1][z], b[x + 1][z]), b[x][z + 1]), b[x][z - 1]);
                     if (near > 0) {
                        if (near > startY) {
                           startY = near - 1;
                        } else if (near < startY) {
                           startY = near + 1;
                        }
                     }

                     int lowestY = dy;
                     if (startY - dy > 0) {
                        b[x][z] = (short)Math.min(Math.max(startY, 0), 32767);
                     }

                     for(dy = startY; dy > lowestY; --dy) {
                        if (dy == startY) {
                           if (!ne && !n && !e) {
                              setBlock(world, dx + 2, dy, dz - 2, blockBase, blockBaseMeta);
                              setBlock(world, dx + 2, dy, dz - 1, blockBase, blockBaseMeta);
                              setBlock(world, dx + 1, dy, dz - 2, blockBase, blockBaseMeta);
                              setBlock(world, dx + 2, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                              setBlock(world, dx + 2, dy + 1, dz - 1, blockBase, blockBaseMeta, false);
                              setBlock(world, dx + 1, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                           }

                           if (!nw && !n && !w) {
                              setBlock(world, dx - 2, dy, dz - 2, blockBase, blockBaseMeta);
                              setBlock(world, dx - 1, dy, dz - 2, blockBase, blockBaseMeta);
                              setBlock(world, dx - 2, dy, dz - 1, blockBase, blockBaseMeta);
                              setBlock(world, dx - 2, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                              setBlock(world, dx - 1, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                              setBlock(world, dx - 2, dy + 1, dz - 1, blockBase, blockBaseMeta, false);
                           }

                           if (!se && !s && !e) {
                              setBlock(world, dx + 2, dy, dz + 2, blockBase, blockBaseMeta);
                              setBlock(world, dx + 1, dy, dz + 2, blockBase, blockBaseMeta);
                              setBlock(world, dx + 2, dy, dz + 1, blockBase, blockBaseMeta);
                              setBlock(world, dx + 2, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                              setBlock(world, dx + 1, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                              setBlock(world, dx + 2, dy + 1, dz + 1, blockBase, blockBaseMeta, false);
                           }

                           if (!sw && !s && !w) {
                              setBlock(world, dx - 2, dy, dz + 2, blockBase, blockBaseMeta);
                              setBlock(world, dx - 1, dy, dz + 2, blockBase, blockBaseMeta);
                              setBlock(world, dx - 2, dy, dz + 1, blockBase, blockBaseMeta);
                              setBlock(world, dx - 2, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                              setBlock(world, dx - 1, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                              setBlock(world, dx - 2, dy + 1, dz + 1, blockBase, blockBaseMeta, false);
                           }

                           if (!n && !ne && !nw) {
                              setBlock(world, dx, dy, dz - 2, blockBase, blockBaseMeta);
                              setBlock(world, dx, dy + 1, dz - 2, stairsBlock, 0, false);
                           }

                           if (!e && !se && !ne) {
                              setBlock(world, dx + 2, dy, dz, blockBase, blockBaseMeta);
                              setBlock(world, dx + 2, dy + 1, dz, stairsBlock, 2, false);
                           }

                           if (!s && !se && !sw) {
                              setBlock(world, dx, dy, dz + 2, blockBase, blockBaseMeta);
                              setBlock(world, dx, dy + 1, dz + 2, stairsBlock, 0, false);
                           }

                           if (!w && !nw && !sw) {
                              setBlock(world, dx - 2, dy, dz, blockBase, blockBaseMeta);
                              setBlock(world, dx - 2, dy + 1, dz, stairsBlock, 2, false);
                           }

                           ++guardDist;
                           if (guardDist > 200) {
                              spawnGuard(world, dx, dy, dz);
                              guardDist = 0;
                           }
                        } else {
                           int distCheck = 4;
                           boolean gate = a[x][z] == 3 && (x > distCheck && x < a.length - distCheck && a[x - distCheck][z] == 2 && a[x + distCheck][z] == 2 || z > distCheck && z < a[x].length - distCheck && a[x][z - distCheck] == 2 && a[x][z + distCheck] == 2);
                           if (gate && dy == startY - 3) {
                              world.func_147449_b(dx, dy, dz, blockFence);
                              if (a[x + 1][z] != 3 || a[x - 1][z] != 3) {
                                 if (a[x + 1][z] == 3) {
                                    world.func_147465_d(dx, dy, dz - 1, stairsBlock, 5, 2);
                                    world.func_147465_d(dx, dy, dz + 1, stairsBlock, 5, 2);
                                 } else if (a[x - 1][z] == 3) {
                                    world.func_147465_d(dx, dy, dz - 1, stairsBlock, 4, 2);
                                    world.func_147465_d(dx, dy, dz + 1, stairsBlock, 4, 2);
                                 } else if (a[x][z + 1] != 3 || a[x][z - 1] != 3) {
                                    if (a[x][z - 1] == 3) {
                                       world.func_147465_d(dx - 1, dy, dz, stairsBlock, 6, 2);
                                       world.func_147465_d(dx + 1, dy, dz, stairsBlock, 6, 2);
                                    } else if (a[x][z + 1] == 3) {
                                       world.func_147465_d(dx - 1, dy, dz, stairsBlock, 7, 2);
                                       world.func_147465_d(dx + 1, dy, dz, stairsBlock, 7, 2);
                                    }
                                 }
                              }
                           }

                           if (!gate || dy > startY - 3) {
                              setBlock(world, dx, dy, dz, blockBase, blockBaseMeta);
                              boolean ng = a[x][z - 1] == 3;
                              boolean sg = a[x][z + 1] == 3;
                              boolean eg = a[x + 1][z] == 3;
                              boolean wg = a[x - 1][z] == 3;
                              if (!ng) {
                                 setBlock(world, dx, dy, dz - 1, blockBase, blockBaseMeta);
                              }

                              if (!ng && !eg) {
                                 setBlock(world, dx + 1, dy, dz - 1, blockBase, blockBaseMeta);
                              }

                              if (!ng && !wg) {
                                 setBlock(world, dx - 1, dy, dz - 1, blockBase, blockBaseMeta);
                              }

                              if (!eg) {
                                 setBlock(world, dx + 1, dy, dz, blockBase, blockBaseMeta);
                              }

                              if (!sg) {
                                 setBlock(world, dx, dy, dz + 1, blockBase, blockBaseMeta);
                              }

                              if (!sg && !eg) {
                                 setBlock(world, dx + 1, dy, dz + 1, blockBase, blockBaseMeta);
                              }

                              if (!sg && !wg) {
                                 setBlock(world, dx - 1, dy, dz + 1, blockBase, blockBaseMeta);
                              }

                              if (!wg) {
                                 setBlock(world, dx - 1, dy, dz, blockBase, blockBaseMeta);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

      }

      private static void spawnGuard(World world, int x, int y, int z) {
         EntityVillageGuard guard = new EntityVillageGuard(world);
         guard.func_70012_b((double)x + 0.5D, (double)y, (double)z + 0.5D, 0.0F, 0.0F);
         guard.func_110163_bv();
         guard.func_110161_a((IEntityLivingData)null);
         world.func_72838_d(guard);
      }

      private static void setBlock(World world, int x, int y, int z, Block block, int meta) {
         setBlock(world, x, y, z, block, meta, true);
      }

      private static void setBlock(World world, int x, int y, int z, Block block, int meta, boolean notStacked) {
         Block replaceBlock = world.func_147439_a(x, y, z);
         Material material = replaceBlock.func_149688_o();
         if (material.func_76222_j() || material == Material.field_151584_j || material == Material.field_151575_d || material == Material.field_151585_k) {
            world.func_147465_d(x, y, z, block, meta, 2);
         }

      }

      public static class BlockVillageWallGen extends BlockBaseContainer {
         public BlockVillageWallGen() {
            super(Material.field_151576_e, WorldHandlerVillageDistrict.Wall.BlockVillageWallGen.TileEntityVillageWallGen.class);
            this.registerWithCreateTab = false;
            this.func_149722_s();
            this.func_149752_b(10000.0F);
         }

         public static class TileEntityVillageWallGen extends TileEntityBase {
            private List<WorldHandlerVillageDistrict.Wall.StructureBounds> bb;
            private BiomeGenBase biome;
            private boolean desert;

            public void func_145845_h() {
               super.func_145845_h();
               if (!this.field_145850_b.field_72995_K && this.bb != null && this.ticks > 40L) {
                  WorldHandlerVillageDistrict.Wall.placeWalls(this.field_145850_b, this.bb, this.field_145851_c, this.field_145848_d, this.field_145849_e, this.biome, this.desert);
                  this.bb = null;
                  this.field_145850_b.func_147468_f(this.field_145851_c, this.field_145848_d, this.field_145849_e);
               } else if (!this.field_145850_b.field_72995_K && this.ticks > 1000L) {
                  this.bb = null;
                  this.field_145850_b.func_147468_f(this.field_145851_c, this.field_145848_d, this.field_145849_e);
               }

            }

            public void setStructure(List pieces, Start start) {
               this.biome = start.biome;
               this.desert = start.field_74927_b;
               this.bb = new ArrayList();
               Iterator i$ = pieces.iterator();

               while(i$.hasNext()) {
                  Object obj = i$.next();
                  if (obj instanceof Path) {
                     this.bb.add(new WorldHandlerVillageDistrict.Wall.StructureBounds((Path)obj, 20, 7));
                  }
               }

            }
         }
      }

      public static class StructureBounds extends StructureBoundingBox {
         public final boolean ew;

         public StructureBounds(Path path, int expansionX, int expansionZ) {
            this(path.func_74874_b(), expansionX, expansionZ);
         }

         public StructureBounds(StructureBoundingBox bb, int expansionX, int expansionZ) {
            this(bb.field_78897_a, bb.field_78895_b, bb.field_78896_c, bb.field_78893_d, bb.field_78894_e, bb.field_78892_f, expansionX, expansionZ);
         }

         public StructureBounds(int x, int y, int z, int x2, int y2, int z2, int expansionX, int expansionZ) {
            this.ew = x2 - x > z2 - z;
            if (this.ew) {
               this.field_78897_a = x - expansionZ;
               this.field_78893_d = x2 + expansionZ;
               this.field_78896_c = z - expansionX;
               this.field_78892_f = z2 + expansionX;
            } else {
               this.field_78897_a = x - expansionX;
               this.field_78893_d = x2 + expansionX;
               this.field_78896_c = z - expansionZ;
               this.field_78892_f = z2 + expansionZ;
            }

            this.field_78895_b = y;
            this.field_78894_e = y2;
         }
      }
   }

   public static class EventHooks {
      @SubscribeEvent
      public void onGetVillageBlock(GetVillageBlockID event) {
         if (event.biome != null) {
            Block b = event.original;
            if (BiomeDictionary.isBiomeOfType(event.biome, Type.SANDY)) {
               if (b != Blocks.field_150364_r && b != Blocks.field_150363_s) {
                  if (b == Blocks.field_150347_e) {
                     event.replacement = Blocks.field_150322_A;
                  } else if (b == Blocks.field_150344_f) {
                     event.replacement = Blocks.field_150344_f;
                     event.setResult(Result.DENY);
                  } else if (b == Blocks.field_150476_ad) {
                     event.replacement = Blocks.field_150487_bG;
                  } else if (b == Blocks.field_150446_ar) {
                     event.replacement = Blocks.field_150372_bz;
                  } else if (b == Blocks.field_150351_n) {
                     event.replacement = Blocks.field_150322_A;
                  } else if (b == Blocks.field_150417_aV) {
                     event.replacement = Blocks.field_150322_A;
                  } else if (b == Blocks.field_150376_bx) {
                     event.replacement = Blocks.field_150376_bx;
                  } else if (b == Blocks.field_150390_bg) {
                     event.replacement = Blocks.field_150372_bz;
                  }
               } else {
                  event.replacement = Blocks.field_150322_A;
               }
            } else if (BiomeDictionary.isBiomeOfType(event.biome, Type.SNOWY)) {
               if (b != Blocks.field_150364_r && b != Blocks.field_150363_s) {
                  if (b == Blocks.field_150347_e) {
                     event.replacement = Blocks.field_150433_aE;
                  } else if (b == Blocks.field_150344_f) {
                     event.replacement = Blocks.field_150433_aE;
                  } else if (b == Blocks.field_150476_ad) {
                     event.replacement = Witchery.Blocks.SNOW_STAIRS;
                  } else if (b == Blocks.field_150446_ar) {
                     event.replacement = Witchery.Blocks.SNOW_STAIRS;
                  } else if (b == Blocks.field_150351_n) {
                     event.replacement = Blocks.field_150403_cj;
                  } else if (b == Blocks.field_150417_aV) {
                     event.replacement = Blocks.field_150433_aE;
                  } else if (b == Blocks.field_150333_U) {
                     event.replacement = Witchery.Blocks.SNOW_SLAB_SINGLE;
                  } else if (b == Blocks.field_150376_bx) {
                     event.replacement = Witchery.Blocks.SNOW_SLAB_SINGLE;
                  } else if (b == Blocks.field_150422_aJ) {
                     event.replacement = Witchery.Blocks.PERPETUAL_ICE_FENCE;
                  } else if (b == Blocks.field_150346_d) {
                     event.replacement = Blocks.field_150433_aE;
                  } else if (b == Blocks.field_150452_aw) {
                     event.replacement = Witchery.Blocks.SNOW_PRESSURE_PLATE;
                  } else if (b == Blocks.field_150390_bg) {
                     event.replacement = Witchery.Blocks.SNOW_STAIRS;
                  }
               } else {
                  event.replacement = Blocks.field_150403_cj;
               }
            }

            if (event.replacement != null && event.replacement != event.original) {
               event.setResult(Result.DENY);
            }

         }
      }

      @SubscribeEvent
      public void onGetVillageBlockMeta(GetVillageBlockMeta event) {
         Block b = event.original;
         if (event.biome != null) {
            if (BiomeDictionary.isBiomeOfType(event.biome, Type.SANDY)) {
               if (b != Blocks.field_150364_r && b != Blocks.field_150363_s) {
                  if (b == Blocks.field_150347_e) {
                     event.replacement = 0;
                     event.setResult(Result.DENY);
                  } else if (b == Blocks.field_150344_f) {
                     event.replacement = 2;
                     event.setResult(Result.DENY);
                  } else if (b == Blocks.field_150376_bx) {
                     event.replacement = 2;
                     event.setResult(Result.DENY);
                  } else if (b == Blocks.field_150333_U) {
                     if (event.type != 3 && event.type != 0) {
                        if (event.type == 11 || event.type == 8) {
                           event.replacement = 9;
                           event.setResult(Result.DENY);
                        }
                     } else {
                        event.replacement = 1;
                        event.setResult(Result.DENY);
                     }
                  } else if (b == Blocks.field_150417_aV) {
                     event.replacement = 2;
                     event.setResult(Result.DENY);
                  }
               } else {
                  event.replacement = 2;
                  event.setResult(Result.DENY);
               }
            } else if (BiomeDictionary.isBiomeOfType(event.biome, Type.SNOWY)) {
               if (b != Blocks.field_150364_r && b != Blocks.field_150363_s) {
                  if (b == Blocks.field_150347_e) {
                     event.replacement = 0;
                     event.setResult(Result.DENY);
                  } else if (b == Blocks.field_150344_f) {
                     event.replacement = 0;
                     event.setResult(Result.DENY);
                  } else if (b == Blocks.field_150333_U) {
                     if (event.type >= 8) {
                        event.replacement = 8;
                        event.setResult(Result.DENY);
                     } else {
                        event.replacement = 0;
                        event.setResult(Result.DENY);
                     }
                  } else if (b == Blocks.field_150417_aV) {
                     event.replacement = 0;
                     event.setResult(Result.DENY);
                  }
               } else {
                  event.replacement = 0;
                  event.setResult(Result.DENY);
               }
            }

         }
      }
   }
}
