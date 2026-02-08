package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.common.Shapeshift;
import com.emoniph.witchery.entity.EntityFollower;
import com.emoniph.witchery.entity.EntityMirrorFace;
import com.emoniph.witchery.entity.EntityReflection;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.item.ItemSunGrenade;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import com.emoniph.witchery.util.TransformCreature;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockMirror extends BlockBaseContainer {
   private final boolean unbreakable;
   private static String sendMeHome = null;
   private static String iGiveUp = null;

   public BlockMirror(boolean unbreakable) {
      super(Material.field_151592_s, BlockMirror.TileEntityMirror.class);
      this.unbreakable = unbreakable;
      this.registerWithCreateTab = false;
      this.func_149715_a(0.7F);
      this.func_149649_H();
      this.func_149672_a(field_149778_k);
      if (unbreakable) {
         this.func_149722_s();
         this.func_149752_b(9999.0F);
      } else {
         this.func_149711_c(1.0F);
         this.func_149752_b(9999.0F);
      }

   }

   public static int getDirection(int meta) {
      return meta & 3;
   }

   public static boolean isBlockTopOfMirror(int meta) {
      return (meta & 4) != 0;
   }

   public boolean trySayMirrorMirrorSendMeHome(EntityPlayer player, String message) {
      if (player != null && !player.field_70170_p.field_72995_K && player.field_71093_bK == Config.instance().dimensionMirrorID) {
         if (sendMeHome == null) {
            sendMeHome = Witchery.resource("witchery.rite.mirrormirrorsendmehome").toLowerCase().replace("'", "").replace(",", "").trim();
         }

         if (iGiveUp == null) {
            iGiveUp = Witchery.resource("witchery.rite.mirrormirrorigiveup").toLowerCase().replace("'", "").replace(",", "").trim();
         }

         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         ItemGeneral var10000;
         if (message.toLowerCase().replace("'", "").replace(",", "").trim().startsWith(sendMeHome)) {
            if (!playerEx.canEscapeMirrorWorld(1)) {
               ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.mirrormirror.escapecooldown", Long.valueOf(playerEx.getCooldownSecs(1)).toString());
               return true;
            }

            Coord c = playerEx.getMirrorWorldEntryPoint();
            if (c != null) {
               playerEx.escapedMirrorWorld(1);
               player.func_70080_a(player.field_70165_t, player.field_70163_u, player.field_70161_v, 270.0F, player.field_70125_A);
               var10000 = Witchery.Items.GENERIC;
               ItemGeneral.teleportToLocation(player.field_70170_p, 0.5D + (double)c.x, 0.01D + (double)c.y, 0.5D + (double)c.z, player.field_71093_bK, player, true, ParticleEffect.SPLASH, SoundEffect.RANDOM_SPLASH);
               return true;
            }
         } else if (message.toLowerCase().replace("'", "").replace(",", "").trim().startsWith(iGiveUp)) {
            if (!playerEx.canEscapeMirrorWorld(2)) {
               ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.mirrormirror.escapecooldown", Long.valueOf(playerEx.getCooldownSecs(2)).toString());
               return true;
            }

            ChunkCoordinates coords = player.getBedLocation(player.field_71093_bK);
            int dimension = player.field_71093_bK;
            World world = player.field_70170_p;
            if (coords == null) {
               coords = player.getBedLocation(0);
               dimension = 0;
               world = MinecraftServer.func_71276_C().func_71218_a(0);
               if (coords == null) {
                  for(coords = ((World)world).func_72861_E(); ((World)world).func_147439_a(coords.field_71574_a, coords.field_71572_b, coords.field_71573_c).func_149721_r() && coords.field_71572_b < 255; ++coords.field_71572_b) {
                  }
               }
            }

            if (coords != null) {
               coords = Blocks.field_150324_C.getBedSpawnPosition((IBlockAccess)world, coords.field_71574_a, coords.field_71572_b, coords.field_71573_c, (EntityPlayer)null);
               if (coords != null) {
                  playerEx.escapedMirrorWorld(2);
                  var10000 = Witchery.Items.GENERIC;
                  ItemGeneral.teleportToLocation(player.field_70170_p, (double)coords.field_71574_a, (double)(coords.field_71572_b + 1), (double)coords.field_71573_c, dimension, player, true);
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && entity.field_70173_aa % 5 == 1 && this.isTransportableEntity(entity)) {
         int i1 = world.func_72805_g(x, y, z);
         int hitZoneyShift = 0;
         if (!isBlockTopOfMirror(i1)) {
            ++y;
            if (entity.field_70131_O <= 1.0F) {
               hitZoneyShift = -1;
            }

            if (world.func_147439_a(x, y, z) != this) {
               return;
            }
         }

         AxisAlignedBB box = this.getServerSelectedBoundingBoxFromPool(world, x, y + hitZoneyShift, z);
         double f = (double)entity.field_70130_N * 0.5D;
         double f1 = (double)entity.field_70131_O;
         AxisAlignedBB entityBox = AxisAlignedBB.func_72330_a(entity.field_70165_t - f, entity.field_70163_u - (double)entity.field_70129_M + (double)entity.field_70139_V, entity.field_70161_v - f, entity.field_70165_t + f, entity.field_70163_u - (double)entity.field_70129_M + (double)entity.field_70139_V + f1, entity.field_70161_v + f);
         if (entityBox.func_72326_a(box)) {
            BlockMirror.TileEntityMirror tile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(world, x, y, z, BlockMirror.TileEntityMirror.class);
            if (tile != null) {
               int side = getDirection(world.func_72805_g(x, y, z));
               int facing = MathHelper.func_76128_c((double)(entity.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
               int dx = 0;
               int dz = 0;
               float shift = 0.7F;
               float xShift = 0.0F;
               float zShift = 0.0F;
               int scale = 1;
               int requiredSide = 0;
               boolean isLiving = entity instanceof EntityLivingBase;
               if (side == 0) {
                  dz = scale;
                  zShift = -shift;
                  requiredSide = 1;
                  if (isLiving && facing != 0) {
                     return;
                  }
               } else if (side == 1) {
                  dz = -scale;
                  zShift = shift;
                  requiredSide = 0;
                  if (isLiving && facing != 2) {
                     return;
                  }
               } else if (side == 2) {
                  dx = scale;
                  xShift = -shift;
                  requiredSide = 3;
                  if (isLiving && facing != 3) {
                     return;
                  }
               } else if (side == 3) {
                  dx = -scale;
                  requiredSide = 2;
                  xShift = shift;
                  if (isLiving && facing != 1) {
                     return;
                  }
               }

               boolean inMirrorWorld = entity.field_71093_bK == Config.instance().dimensionMirrorID;
               ItemGeneral var10000;
               int cy;
               if (!this.unbreakable) {
                  int dy;
                  int nx;
                  int nz;
                  Block block;
                  if (inMirrorWorld || tile.demonKilled) {
                     for(dy = 1; dy < 32; ++dy) {
                        nx = x + dx * dy;
                        nz = z + dz * dy;
                        block = world.func_147439_a(nx, y, nz);
                        if (block == this && getDirection(world.func_72805_g(nx, y, nz)) == requiredSide) {
                           var10000 = Witchery.Items.GENERIC;
                           ItemGeneral.teleportToLocation(world, 0.5D + (double)nx - (double)xShift, (double)(y - 1) + 0.01D, 0.5D + (double)nz - (double)zShift, world.field_73011_w.field_76574_g, entity, true, ParticleEffect.SPLASH, SoundEffect.RANDOM_SPLASH);
                           return;
                        }
                     }
                  }

                  int meta;
                  if (inMirrorWorld) {
                     for(dy = 1; dy < 10; ++dy) {
                        nx = x + dx * dy;
                        nz = z + dz * dy;
                        if (world.func_147437_c(nx, y, nz) && world.func_147437_c(nx, y - 1, nz)) {
                           boolean isPlayerEntryCell = false;
                           if (entity instanceof EntityPlayer) {
                              EntityPlayer player = (EntityPlayer)entity;
                              ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                              isPlayerEntryCell = playerEx.isMirrorWorldEntryPoint(nx, y, nz);
                           }

                           meta = (nx >> 4 << 4) + 4;
                           cy = (y >> 4 << 4) + 8;
                           int cz = (nz >> 4 << 4) + 8;
                           boolean isEntryCell = world.func_147439_a(meta, cy, cz) == Witchery.Blocks.MIRROR_UNBREAKABLE;
                           if (!isEntryCell || isPlayerEntryCell) {
                              IPowerSource power = PowerSources.findClosestPowerSource(world, x, y, z);
                              if (power != null && power.consumePower(3000.0F)) {
                                 var10000 = Witchery.Items.GENERIC;
                                 ItemGeneral.teleportToLocation(world, 0.5D + (double)nx - (double)xShift, (double)y + 0.01D, 0.5D + (double)nz - (double)zShift, world.field_73011_w.field_76574_g, entity, true, ParticleEffect.SPLASH, SoundEffect.RANDOM_SPLASH);
                              }
                           }

                           return;
                        }
                     }
                  } else if (tile.demonKilled) {
                     int ny;
                     EntityPlayer player;
                     double yaw;
                     float rev;
                     S08PacketPlayerPosLook packet;
                     for(dy = 2; dy < 16; ++dy) {
                        ny = y + dy;
                        block = world.func_147439_a(x, ny, z);
                        if (block == this) {
                           meta = world.func_72805_g(x, ny, z);
                           if (getDirection(meta) == side) {
                              if (isBlockTopOfMirror(meta)) {
                                 --ny;
                              }

                              var10000 = Witchery.Items.GENERIC;
                              ItemGeneral.teleportToLocation(world, 0.5D + (double)x + (double)xShift, (double)ny + 0.01D, 0.5D + (double)z + (double)zShift, world.field_73011_w.field_76574_g, entity, true, ParticleEffect.SPLASH, SoundEffect.RANDOM_SPLASH);
                              if (entity instanceof EntityPlayer) {
                                 player = (EntityPlayer)entity;
                                 yaw = (double)(player.field_70177_z + 180.0F);
                                 rev = (float)yaw % 360.0F;
                                 packet = new S08PacketPlayerPosLook(player.field_70165_t, player.field_70163_u, player.field_70161_v, rev, player.field_70125_A, false);
                                 Witchery.packetPipeline.sendTo((Packet)packet, (EntityPlayer)player);
                              }

                              return;
                           }
                        }
                     }

                     for(dy = 2; dy < 16; ++dy) {
                        ny = y - dy;
                        block = world.func_147439_a(x, ny, z);
                        if (block == this) {
                           meta = world.func_72805_g(x, ny, z);
                           if (getDirection(meta) == side) {
                              if (isBlockTopOfMirror(meta)) {
                                 --ny;
                              }

                              var10000 = Witchery.Items.GENERIC;
                              ItemGeneral.teleportToLocation(world, 0.5D + (double)x + (double)xShift, (double)ny + 0.01D, 0.5D + (double)z + (double)zShift, world.field_73011_w.field_76574_g, entity, true, ParticleEffect.SPLASH, SoundEffect.RANDOM_SPLASH);
                              if (entity instanceof EntityPlayer) {
                                 player = (EntityPlayer)entity;
                                 yaw = (double)(player.field_70177_z + 180.0F);
                                 rev = (float)yaw % 360.0F;
                                 packet = new S08PacketPlayerPosLook(player.field_70165_t, player.field_70163_u, player.field_70161_v, rev, player.field_70125_A, false);
                                 Witchery.packetPipeline.sendTo((Packet)packet, (EntityPlayer)player);
                              }

                              return;
                           }
                        }
                     }
                  }
               }

               if (entity instanceof EntityPlayer) {
                  EntityPlayer player = (EntityPlayer)entity;
                  ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                  if (!inMirrorWorld || playerEx.isMirrorWorldEntryPoint(x, y, z)) {
                     Coord dimCoords = tile.getDimCoords();
                     if (dimCoords != null) {
                        float dimX = (float)dimCoords.x + 0.5F;
                        float dimY = (float)dimCoords.y + 0.01F;
                        float dimZ = (float)dimCoords.z + 0.5F;
                        cy = !inMirrorWorld ? Config.instance().dimensionMirrorID : tile.dim;
                        World otherWorld = MinecraftServer.func_71276_C().func_71218_a(cy);
                        float face = 0.0F;
                        if (otherWorld != null) {
                           Block block = otherWorld.func_147439_a(dimCoords.x, dimCoords.y, dimCoords.z);
                           if (block == Witchery.Blocks.MIRROR || block == Witchery.Blocks.MIRROR_UNBREAKABLE) {
                              int mside = getDirection(otherWorld.func_72805_g(dimCoords.x, dimCoords.y, dimCoords.z));
                              float distance = 1.0F;
                              if (mside == 0) {
                                 face = 180.0F;
                                 dimZ -= distance;
                              } else if (mside == 1) {
                                 face = 0.0F;
                                 dimZ += distance;
                              } else if (mside == 2) {
                                 face = 90.0F;
                                 dimX -= distance;
                              } else if (mside == 3) {
                                 face = 270.0F;
                                 dimX += distance;
                              }

                              player.field_70177_z = face;
                              BlockMirror.TileEntityMirror otherTile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(otherWorld, dimCoords.x, dimCoords.y, dimCoords.z, BlockMirror.TileEntityMirror.class);
                              if (otherTile != null) {
                                 if (otherTile.onCooldown()) {
                                    return;
                                 }

                                 otherTile.addCooldown(60);
                              }
                           }
                        }

                        ParticleEffect.SPLASH.send(SoundEffect.RANDOM_SPLASH, entity, 0.5D, 2.0D, 16);
                        double targetX;
                        double targetY;
                        if (entity.field_71093_bK != Config.instance().dimensionMirrorID) {
                           if (!tile.demonKilled) {
                              targetX = 7.0D;
                              targetY = 6.0D;
                              float cellMidX = (float)(dimCoords.x + 4);
                              float cellMidY = (float)dimCoords.y;
                              float cellMidZ = (float)dimCoords.z;
                              AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)cellMidX - targetX, (double)cellMidY - targetY, (double)cellMidZ - targetX, (double)cellMidX + targetX, (double)cellMidY + targetY, (double)cellMidZ + targetX);
                              List<EntityReflection> EntityReflection = otherWorld.func_72872_a(EntityReflection.class, bounds);
                              if (EntityReflection.size() == 0) {
                                 EntityReflection reflection = new EntityReflection(otherWorld);
                                 reflection.func_70080_a(0.5D + (double)cellMidX, 1.1D + (double)cellMidY, 0.5D + (double)cellMidZ, 0.0F, 0.0F);
                                 reflection.func_110163_bv();
                                 reflection.field_70170_p.func_72838_d(reflection);
                              }
                           }

                           playerEx.setMirrorWorldEntryPoint(dimCoords.x, dimCoords.y, dimCoords.z);
                           player.func_70080_a((double)dimX, (double)(dimY - 1.0F), (double)dimZ, face, player.field_70125_A);
                           var10000 = Witchery.Items.GENERIC;
                           ItemGeneral.travelToDimension(player, Config.instance().dimensionMirrorID);
                           player.func_70634_a((double)dimX, (double)(dimY - 1.0F), (double)dimZ);
                        } else if (tile.isConnected) {
                           player.func_70080_a((double)dimX, (double)(dimY - 1.0F), (double)dimZ, face, player.field_70125_A);
                           var10000 = Witchery.Items.GENERIC;
                           ItemGeneral.travelToDimension(player, tile.dim);
                           player.func_70634_a((double)dimX, (double)(dimY - 1.0F), (double)dimZ);
                        } else {
                           targetX = (double)dimX;
                           targetY = (double)(dimY - 1.0F);
                           double targetZ = (double)dimZ;
                           int targetDim = tile.dim;
                           boolean CHECK_PLAYER_INV = true;
                           MinecraftServer server = MinecraftServer.func_71276_C();
                           WorldServer[] arr$ = server.field_71305_c;
                           int len$ = arr$.length;
                           int i$ = 0;

                           while(true) {
                              if (i$ >= len$) {
                                 player.func_70080_a(targetX, targetY, targetZ, face, player.field_70125_A);
                                 var10000 = Witchery.Items.GENERIC;
                                 ItemGeneral.travelToDimension(player, targetDim);
                                 player.func_70634_a(targetX, targetY, targetZ);
                                 break;
                              }

                              WorldServer worldServer = arr$[i$];
                              Iterator i$ = worldServer.field_73010_i.iterator();

                              while(true) {
                                 while(i$.hasNext()) {
                                    Object obj = i$.next();
                                    EntityPlayer otherPlayer = (EntityPlayer)obj;
                                    ItemStack[] arr$ = otherPlayer.field_71071_by.field_70462_a;
                                    int len$ = arr$.length;

                                    for(int i$ = 0; i$ < len$; ++i$) {
                                       ItemStack stack = arr$[i$];
                                       if (stack != null && stack.func_77973_b() == Witchery.Items.MIRROR) {
                                          boolean isMirror = tile.isTargettedBy(stack);
                                          if (isMirror) {
                                             if (otherPlayer.field_71093_bK != Config.instance().dimensionMirrorID) {
                                                targetX = otherPlayer.field_70165_t;
                                                targetY = otherPlayer.field_70163_u;
                                                targetZ = otherPlayer.field_70161_v;
                                                targetDim = otherPlayer.field_71093_bK;
                                             }
                                             break;
                                          }
                                       }
                                    }
                                 }

                                 ++i$;
                                 break;
                              }
                           }
                        }

                        ParticleEffect.SPLASH.send(SoundEffect.RANDOM_SPLASH, entity, 0.5D, 2.0D, 16);
                     }
                  }
               }
            }
         }
      }

   }

   public void demonSlain(World world, double posX, double posY, double posZ) {
      if (!world.field_72995_K) {
         double R = 7.0D;
         double RY = 6.0D;
         int x = (MathHelper.func_76128_c(posX) >> 4 << 4) + 4;
         int xmid = x + 4;
         int y = (MathHelper.func_76128_c(posY) >> 4 << 4) + 8;
         int z = (MathHelper.func_76128_c(posZ) >> 4 << 4) + 8;
         if (world.func_147439_a(x, y, z) == Witchery.Blocks.MIRROR_UNBREAKABLE) {
            AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)xmid - R, (double)y - RY, (double)z - R, (double)xmid + R, (double)y + RY, (double)z + R);
            List<EntityReflection> reflections = world.func_72872_a(EntityReflection.class, bounds);
            int livingDemons = 0;
            Iterator i$ = reflections.iterator();

            while(i$.hasNext()) {
               EntityReflection entity = (EntityReflection)i$.next();
               if (entity != null && entity.func_70089_S()) {
                  ++livingDemons;
               }
            }

            if (livingDemons == 0) {
               BlockMirror.TileEntityMirror tile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(world, x, y, z, BlockMirror.TileEntityMirror.class);
               if (tile != null) {
                  Coord dimCoords = tile.getDimCoords();
                  int dim = tile.dim;
                  World otherWorld = MinecraftServer.func_71276_C().func_71218_a(dim);
                  if (otherWorld != null) {
                     BlockMirror.TileEntityMirror otherTile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(otherWorld, dimCoords.x, dimCoords.y, dimCoords.z, BlockMirror.TileEntityMirror.class);
                     if (otherTile != null) {
                        otherTile.demonKilled = true;
                     }
                  }
               }
            }
         }
      }

   }

   private boolean isTransportableEntity(Entity entity) {
      return !(entity instanceof EntityMirrorFace) && (entity instanceof EntityLivingBase || entity instanceof EntityItem);
   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      if (world.field_72995_K) {
         return true;
      } else {
         if (!this.unbreakable) {
            int i1 = world.func_72805_g(x, y, z);
            if (!isBlockTopOfMirror(i1)) {
               ++y;
               if (world.func_147439_a(x, y, z) != this) {
                  return true;
               }
            }

            BlockMirror.TileEntityMirror tile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(world, x, y, z, BlockMirror.TileEntityMirror.class);
            if (tile == null) {
               return true;
            }

            tile.depolyDemon(player);
         }

         return true;
      }
   }

   public int func_149645_b() {
      return -1;
   }

   public boolean func_149686_d() {
      return false;
   }

   public boolean func_149662_c() {
      return false;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      int side = getDirection(world.func_72805_g(x, y, z));
      float w = 0.15F;
      if (side == 0) {
         this.func_149676_a(0.0F, 0.0F, 0.85F, 1.0F, 1.0F, 1.0F);
      } else if (side == 1) {
         this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.15F);
      } else if (side == 2) {
         this.func_149676_a(0.85F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      } else if (side == 3) {
         this.func_149676_a(0.0F, 0.0F, 0.0F, 0.15F, 1.0F, 1.0F);
      }

      AxisAlignedBB bounds = super.func_149668_a(world, x, y, z);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      return bounds;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB func_149633_g(World world, int x, int y, int z) {
      int side = getDirection(world.func_72805_g(x, y, z));
      float w = 0.32F;
      if (side == 0) {
         this.func_149676_a(0.0F, 0.0F, 0.68F, 1.0F, 1.0F, 1.0F);
      } else if (side == 1) {
         this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.32F);
      } else if (side == 2) {
         this.func_149676_a(0.68F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      } else if (side == 3) {
         this.func_149676_a(0.0F, 0.0F, 0.0F, 0.32F, 1.0F, 1.0F);
      }

      AxisAlignedBB bounds = super.func_149633_g(world, x, y, z);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      return bounds;
   }

   public AxisAlignedBB getServerSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
      int side = getDirection(world.func_72805_g(x, y, z));
      float w = 0.32F;
      float minX = 0.0F;
      float minY = 0.0F;
      float minZ = 0.0F;
      float maxX = 1.0F;
      float maxY = 1.0F;
      float maxZ = 1.0F;
      if (side == 0) {
         minZ = 0.68F;
      } else if (side == 1) {
         maxZ = 0.32F;
      } else if (side == 2) {
         minX = 0.68F;
      } else if (side == 3) {
         maxZ = 0.32F;
      }

      AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)((float)x + minX), (double)((float)y + minY), (double)((float)z + minZ), (double)((float)x + maxX), (double)((float)y + maxY), (double)((float)z + maxZ));
      return bounds;
   }

   public void func_149695_a(World world, int x, int y, int z, Block block) {
      int l = world.func_72805_g(x, y, z);
      int i1 = getDirection(l);
      if (isBlockTopOfMirror(l)) {
         if (world.func_147439_a(x, y - 1, z) != this) {
            if (!world.field_72995_K) {
               this.func_149697_b(world, x, y, z, l, 0);
            }

            world.func_147468_f(x, y, z);
         }
      } else if (world.func_147439_a(x, y + 1, z) != this) {
         world.func_147468_f(x, y, z);
      }

   }

   public Item func_149650_a(int meta, Random rand, int p_149650_3_) {
      return isBlockTopOfMirror(meta) ? Witchery.Items.MIRROR : Item.func_150899_d(0);
   }

   public void func_149690_a(World world, int x, int y, int z, int p_149690_5_, float p_149690_6_, int p_149690_7_) {
      if (isBlockTopOfMirror(p_149690_5_)) {
         super.func_149690_a(world, x, y, z, p_149690_5_, p_149690_6_, 0);
      }

   }

   public int func_149656_h() {
      return super.func_149656_h();
   }

   @SideOnly(Side.CLIENT)
   public Item func_149694_d(World world, int x, int y, int z) {
      return Witchery.Items.MIRROR;
   }

   public void func_149689_a(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
   }

   public void func_149681_a(World world, int x, int y, int z, int meta, EntityPlayer player) {
      if (player.field_71075_bZ.field_75098_d && isBlockTopOfMirror(meta)) {
         if (world.func_147439_a(x, y - 1, z) == this) {
            world.func_147468_f(x, y - 1, z);
         }

         meta |= 8;
         world.func_72921_c(x, y, z, meta, 4);
      }

      this.func_149697_b(world, x, y, z, meta, 0);
      super.func_149681_a(world, x, y, z, meta, player);
   }

   public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
      ArrayList<ItemStack> drops = new ArrayList();
      boolean brokenInCreativeMode = (meta & 8) != 0;
      if (!brokenInCreativeMode) {
         BlockMirror.TileEntityMirror tile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(world, x, y, z, BlockMirror.TileEntityMirror.class);
         if (tile != null) {
            ItemStack stack = new ItemStack(Witchery.Items.MIRROR);
            NBTTagCompound nbtRoot = new NBTTagCompound();
            tile.writeItemDataToNBT(nbtRoot);
            stack.func_77982_d(nbtRoot);
            if (world.field_73011_w.field_76574_g != Config.instance().dimensionMirrorID && tile.isDimLinked()) {
               Coord dimCoords = tile.getDimCoords();
               World otherWorld = MinecraftServer.func_71276_C().func_71218_a(Config.instance().dimensionMirrorID);
               if (otherWorld != null && otherWorld.func_147439_a(dimCoords.x, dimCoords.y, dimCoords.z) == Witchery.Blocks.MIRROR) {
                  BlockMirror.TileEntityMirror otherTile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(otherWorld, dimCoords.x, dimCoords.y, dimCoords.z, BlockMirror.TileEntityMirror.class);
                  if (otherTile != null) {
                     otherTile.isConnected = false;
                     otherTile.markBlockForUpdate(false);
                  }
               }
            }

            drops.add(stack);
         }
      }

      return drops;
   }

   public void loadFromItem(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
      BlockMirror.TileEntityMirror tile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(world, x, y, z, BlockMirror.TileEntityMirror.class);
      if (tile != null) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         if (nbtRoot != null) {
            tile.readItemDataFromNBT(nbtRoot);
            if (world.field_73011_w.field_76574_g != Config.instance().dimensionMirrorID && tile.isDimLinked()) {
               Coord dimCoords = tile.getDimCoords();
               World otherWorld = MinecraftServer.func_71276_C().func_71218_a(Config.instance().dimensionMirrorID);
               if (otherWorld != null && otherWorld.func_147439_a(dimCoords.x, dimCoords.y, dimCoords.z) == Witchery.Blocks.MIRROR_UNBREAKABLE) {
                  BlockMirror.TileEntityMirror otherTile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(otherWorld, dimCoords.x, dimCoords.y, dimCoords.z, BlockMirror.TileEntityMirror.class);
                  if (otherTile != null) {
                     otherTile.isConnected = true;
                     otherTile.dimCoords = new Coord(x, y, z);
                     otherTile.markBlockForUpdate(false);
                  }
               }
            }
         }
      }

   }

   public static class TileEntityMirror extends TileEntityBase {
      public int men;
      private Coord dimCoords;
      private int dim;
      private boolean isConnected;
      private boolean demonKilled;
      private GameProfile favorite;
      private UUID fairest;
      private Set<String> playersSeen = new HashSet();
      long cooldown;
      long lastFairestSpawn = 0L;

      public void func_145845_h() {
         super.func_145845_h();
         if (this.ticks % (long)(this.field_145850_b.field_72995_K ? 10 : 40) == 1L) {
            int side = BlockMirror.getDirection(this.field_145850_b.func_72805_g(this.field_145851_c, this.field_145848_d, this.field_145849_e));
            int xMin = -1;
            int xMax = 1;
            int zMin = -1;
            int zMax = 1;
            int scale = true;
            if (side == 0) {
               zMin = -4;
               zMax = 0;
            } else if (side == 1) {
               zMin = 0;
               zMax = 4;
            } else if (side == 2) {
               xMin = -4;
               xMax = 0;
            } else if (side == 3) {
               xMin = 0;
               xMax = 4;
            }

            AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)(this.field_145851_c + xMin), (double)this.field_145848_d, (double)(this.field_145849_e + zMin), (double)(this.field_145851_c + xMax + 1), (double)(this.field_145848_d + 1), (double)(this.field_145849_e + zMax + 1));
            List<EntityLivingBase> entities = this.field_145850_b.func_72872_a(EntityLivingBase.class, bounds);
            this.men = entities.size();
            if (!this.field_145850_b.field_72995_K) {
               Iterator i$ = entities.iterator();

               while(i$.hasNext()) {
                  EntityLivingBase entity = (EntityLivingBase)i$.next();
                  if (entity instanceof EntityPlayer) {
                     this.playersSeen.add(entity.func_70005_c_());
                  }
               }
            }
         }

      }

      public void addCooldown(int i) {
         this.cooldown = this.ticks + (long)i;
      }

      public boolean onCooldown() {
         return this.ticks < this.cooldown;
      }

      public boolean isTargettedBy(ItemStack stack) {
         if (stack != null && stack.func_77973_b() == Witchery.Items.MIRROR && stack.func_77978_p() != null) {
            NBTTagCompound nbtRoot = stack.func_77978_p();
            if (nbtRoot.func_74764_b("DimCoords") && nbtRoot.func_74764_b("Dimension") && this.field_145850_b.field_73011_w.field_76574_g == nbtRoot.func_74762_e("Dimension")) {
               Coord coords = Coord.fromTagNBT(nbtRoot.func_74775_l("DimCoords"));
               if (coords != null) {
                  return coords.isMatch(this.field_145851_c, this.field_145848_d, this.field_145849_e);
               }
            }
         }

         return false;
      }

      private void depolyDemon(EntityPlayer player) {
         if (!this.demonKilled && this.field_145850_b.field_73011_w.field_76574_g != Config.instance().dimensionMirrorID) {
            if (player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() == Witchery.Items.TAGLOCK_KIT) {
               ExtendedPlayer playerEx = ExtendedPlayer.get(player);
               TransformCreature currentTransform = playerEx.getCreatureType();
               if (currentTransform == TransformCreature.NONE || currentTransform == TransformCreature.PLAYER) {
                  String username = Witchery.Items.TAGLOCK_KIT.getBoundUsername(player.func_70694_bm(), 1);
                  if (username != null && !username.isEmpty() && !username.equals(player.func_70005_c_())) {
                     IPowerSource power = PowerSources.findClosestPowerSource(this);
                     if (power != null && power.consumePower(4000.0F)) {
                        ParticleEffect.SMOKE.send(SoundEffect.WITCHERY_RANDOM_POOF, player, 0.5D, 2.0D, 16);
                        playerEx.setOtherPlayerSkin(username);
                        Shapeshift.INSTANCE.shiftTo(player, TransformCreature.PLAYER);
                     } else {
                        ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, player, 0.5D, 2.0D, 16);
                     }
                  } else if (currentTransform == TransformCreature.PLAYER) {
                     ParticleEffect.SMOKE.send(SoundEffect.WITCHERY_RANDOM_POOF, player, 0.5D, 2.0D, 16);
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.NONE);
                  } else {
                     ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, player, 0.5D, 2.0D, 16);
                  }
               }
            } else if (player.func_70694_bm() != null && Witchery.Items.GENERIC.itemQuartzSphere.isMatch(player.func_70694_bm())) {
               IPowerSource power = PowerSources.findClosestPowerSource(this);
               if (power != null && power.consumePower(2000.0F)) {
                  ParticleEffect.SMOKE.send(SoundEffect.RANDOM_ORB, player, 0.5D, 2.0D, 16);
                  ItemStack itemstack = player.func_70694_bm();
                  ItemStack newStack;
                  if (itemstack.field_77994_a > 1) {
                     newStack = new ItemStack(Witchery.Items.DUP_GRENADE);
                     ItemSunGrenade.setOwnerName(newStack, player.func_70005_c_());
                     --itemstack.field_77994_a;
                     if (itemstack.field_77994_a <= 0) {
                        player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                     }

                     if (!player.field_71071_by.func_70441_a(newStack)) {
                        this.field_145850_b.func_72838_d(new EntityItem(this.field_145850_b, player.field_70165_t + 0.5D, player.field_70163_u + 1.5D, player.field_70161_v + 0.5D, newStack));
                     } else if (player instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                     }
                  } else {
                     newStack = new ItemStack(Witchery.Items.DUP_GRENADE);
                     ItemSunGrenade.setOwnerName(newStack, player.func_70005_c_());
                     player.func_70062_b(0, newStack);
                     if (player instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                     }
                  }
               } else {
                  ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, player, 0.5D, 2.0D, 16);
               }
            } else {
               List<EntityMirrorFace> faces = this.field_145850_b.func_72872_a(EntityMirrorFace.class, Witchery.Blocks.MIRROR.func_149668_a(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e));
               if (faces.size() == 0) {
                  this.showMirrorHead(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
                  ParticleEffect.SPELL_COLORED.send(SoundEffect.WITCHERY_MOB_REFLECTION_SPEECH, (TileEntity)this, 0.5D, 0.5D, 16, 7829503);
                  double RANGE = 64.0D;
                  List<EntityPlayer> players = this.field_145850_b.field_73010_i;
                  Iterator i$ = players.iterator();

                  while(i$.hasNext()) {
                     EntityPlayer otherPlayer = (EntityPlayer)i$.next();
                     if (player.func_70092_e((double)this.field_145851_c, (double)this.field_145848_d, (double)this.field_145849_e) <= RANGE * RANGE) {
                        ChatUtil.sendTranslated(otherPlayer, "witchery.rite.mirrormirror", player.func_70005_c_());
                     }
                  }

                  boolean fairestFound = false;
                  if (this.fairest != null) {
                     double R = 100.0D;
                     double RY = 32.0D;
                     AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)this.field_145851_c - 100.0D, (double)this.field_145848_d - 32.0D, (double)this.field_145849_e - 100.0D, (double)this.field_145851_c + 100.0D, (double)this.field_145848_d + 32.0D, (double)this.field_145849_e + 100.0D);
                     List<EntityFollower> followers = this.field_145850_b.func_72872_a(EntityFollower.class, bounds);
                     Iterator i$ = followers.iterator();

                     while(i$.hasNext()) {
                        EntityFollower follower = (EntityFollower)i$.next();
                        if (follower.getPersistentID().equals(this.fairest) && follower.func_70089_S()) {
                           this.sayNotFairest(player, follower);
                           fairestFound = true;
                           break;
                        }
                     }
                  }

                  if (!fairestFound) {
                     boolean isFairestAllowed = this.field_145850_b.func_82737_E() > this.lastFairestSpawn + (long)TimeUtil.minsToTicks(2);
                     this.fairest = null;
                     if (this.favorite != null && !this.isFavorite(player)) {
                        ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.anotherplayer");
                        EntityPlayer otherPlayer = this.field_145850_b.func_72924_a(this.favorite.getName());
                        if (otherPlayer != null) {
                           this.sayBearing(player, otherPlayer);
                        }
                     } else {
                        this.favorite = player.func_146103_bH();
                        double CHANCE_OF_NEW_FAIREST = Config.instance().fairestSpawnChance;
                        if (isFairestAllowed && this.field_145850_b.field_73012_v.nextDouble() < CHANCE_OF_NEW_FAIREST) {
                           EntityFollower follower = new EntityFollower(this.field_145850_b);
                           int followerType = this.field_145850_b.field_73012_v.nextInt(4) + 1;
                           follower.func_94058_c(EntityFollower.generateFollowerName(followerType));
                           follower.func_110163_bv();
                           follower.setFollowerType(followerType);
                           Coord coord = null;
                           int minRange = true;

                           for(int i = 0; i < 25 && coord == null; ++i) {
                              int x = this.field_145851_c + (this.field_145850_b.field_73012_v.nextBoolean() ? 1 : -1) * (50 + this.field_145850_b.field_73012_v.nextInt(50));
                              int z = this.field_145849_e + (this.field_145850_b.field_73012_v.nextBoolean() ? 1 : -1) * (50 + this.field_145850_b.field_73012_v.nextInt(50));
                              int y = Math.min(this.field_145848_d + 20, 250);

                              for(int yMin = Math.max(this.field_145848_d - 20, 2); y >= yMin; --y) {
                                 if (this.field_145850_b.func_147439_a(x, y, z).func_149721_r() && this.field_145850_b.func_147439_a(x, y + 1, z).func_149688_o().func_76222_j() && this.field_145850_b.func_147437_c(x, y + 2, z)) {
                                    coord = new Coord(x, y, z);
                                    break;
                                 }
                              }
                           }

                           if (coord != null) {
                              follower.func_70080_a(0.5D + (double)coord.x, 1.01D + (double)coord.y, 0.5D + (double)coord.z, 0.0F, 0.0F);
                              this.field_145850_b.func_72838_d(follower);
                              this.fairest = follower.getPersistentID();
                              fairestFound = true;
                              this.lastFairestSpawn = this.field_145850_b.func_82737_E();
                              this.sayNotFairest(player, follower);
                           }
                        }

                        if (!fairestFound) {
                           ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.you");
                        }
                     }
                  }

                  if (this.playersSeen.size() <= 1) {
                     ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.playersnotseen");
                  } else {
                     List<String> seen = new ArrayList(this.playersSeen);
                     Collections.sort(seen);
                     StringBuffer sb = new StringBuffer();
                     Iterator i$ = seen.iterator();

                     while(i$.hasNext()) {
                        String s = (String)i$.next();
                        if (!s.equals(player.func_70005_c_())) {
                           if (sb.length() > 0) {
                              sb.append(", ");
                           }

                           sb.append(s);
                        }
                     }

                     if (sb.length() > 0) {
                        ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.playersseen", sb.toString());
                     } else {
                        ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.playersnotseen");
                     }
                  }

                  if (this.isFavorite(player)) {
                     this.playersSeen.clear();
                  }
               }
            }
         }

      }

      public void sayNotFairest(EntityPlayer player, EntityFollower follower) {
         if (follower.getFollowerType() == 4) {
            ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.anotherm");
         } else {
            ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.anotherf");
         }

         this.sayBearing(player, follower);
      }

      public void sayBearing(EntityPlayer player, EntityLivingBase otherEntity) {
         double bearingRadians = Math.atan2(0.5D + (double)this.field_145849_e - otherEntity.field_70161_v, 0.5D + (double)this.field_145851_c - otherEntity.field_70165_t);
         double bearing = (Math.toDegrees(bearingRadians) + 180.0D + 90.0D) % 360.0D;
         if (bearing < 0.0D) {
            bearing += 360.0D;
         }

         int bearingIndex = (int)bearing / 45;
         if (bearingIndex > 7 || bearingIndex < 0) {
            bearingIndex = 0;
         }

         ChatUtil.sendTranslated(EnumChatFormatting.AQUA, player, "witchery.rite.mirrormirror.bearing" + bearingIndex);
      }

      private void showMirrorHead(World world, int x, int y, int z) {
         int side = BlockMirror.getDirection(world.func_72805_g(x, y, z));
         float dx = 0.0F;
         float dz = 0.0F;
         float scale = 0.4F;
         float rotation = 0.0F;
         if (side == 0) {
            dz = 0.4F;
            rotation = -90.0F;
         } else if (side == 1) {
            dz = -0.4F;
            rotation = 90.0F;
         } else if (side == 2) {
            dx = 0.4F;
            rotation = 0.0F;
         } else if (side == 3) {
            dx = -0.4F;
            rotation = -90.0F;
         }

         EntityMirrorFace face = new EntityMirrorFace(world);
         face.func_70107_b((double)x + 0.5D + (double)dx, (double)y + 0.1D, (double)z + 0.5D + (double)dz);
         world.func_72838_d(face);
      }

      private boolean isDimLinked() {
         return this.dimCoords != null;
      }

      private Coord getDimCoords() {
         if (this.dimCoords == null && this.field_145850_b.field_73011_w.field_76574_g != Config.instance().dimensionMirrorID) {
            World mworld = MinecraftServer.func_71276_C().func_71218_a(Config.instance().dimensionMirrorID);
            if (mworld != null) {
               int[][] map = new int[][]{{0, 1}, {1, 0}};
               int cellX = 0;
               int cellZ = 0;
               int sign = 1;

               for(int i = 0; i < 256; ++i) {
                  for(int spin = 0; spin <= i; ++spin) {
                     for(int j = 0; j < map.length; ++j) {
                        if (i > 0) {
                           cellX += map[j][0] * sign;
                           cellZ += map[j][1] * sign;
                        }

                        int Y_LEVELS = Config.instance().shrinkMirrorWorld ? 8 : 15;

                        for(int cellY = 0; cellY < Y_LEVELS; ++cellY) {
                           int dimX = (cellX << 4) + 4;
                           int dimY = (cellY << 4) + 8;
                           int dimZ = (cellZ << 4) + 8;
                           if (mworld.func_147437_c(dimX, dimY, dimZ) && mworld.func_147437_c(dimX, dimY - 1, dimZ)) {
                              boolean stop = false;

                              for(int y = dimY - 1; y <= dimY + 6 && !stop; ++y) {
                                 for(int x = dimX; x <= dimX + 8 && !stop; ++x) {
                                    for(int z = dimZ - 4; z <= dimZ + 4 && !stop; ++z) {
                                       mworld.func_147439_a(x, y, z);
                                       if (!mworld.func_147437_c(x, y, z)) {
                                          stop = true;
                                          break;
                                       }
                                    }
                                 }
                              }

                              if (!stop) {
                                 Block mirror = Witchery.Blocks.MIRROR_UNBREAKABLE;
                                 int meta = 3;
                                 mworld.func_147465_d(dimX, dimY, dimZ, mirror, meta | 4, 3);
                                 BlockMirror.TileEntityMirror tile = (BlockMirror.TileEntityMirror)BlockUtil.getTileEntity(mworld, dimX, dimY, dimZ, BlockMirror.TileEntityMirror.class);
                                 if (tile != null) {
                                    tile.dimCoords = new Coord(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                                    tile.dim = this.field_145850_b.field_73011_w.field_76574_g;
                                 }

                                 if (mworld.func_147439_a(dimX, dimY, dimZ) == mirror) {
                                    mworld.func_147465_d(dimX, dimY - 1, dimZ, mirror, meta, 3);
                                 }

                                 this.dimCoords = new Coord(dimX, dimY, dimZ);
                                 return this.dimCoords;
                              }
                           }
                        }
                     }
                  }

                  sign *= -1;
               }
            }
         }

         return this.dimCoords;
      }

      public Packet func_145844_m() {
         NBTTagCompound nbtTag = new NBTTagCompound();
         this.func_145841_b(nbtTag);
         return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTag);
      }

      public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
         super.onDataPacket(net, packet);
         NBTTagCompound nbtTag = packet.func_148857_g();
         this.func_145839_a(nbtTag);
         this.field_145850_b.func_147479_m(this.field_145851_c, this.field_145848_d, this.field_145849_e);
      }

      public void func_145841_b(NBTTagCompound nbtRoot) {
         super.func_145841_b(nbtRoot);
         nbtRoot.func_74772_a("LastFairestSpawnTime", this.lastFairestSpawn);
         this.writeItemDataToNBT(nbtRoot);
      }

      public void func_145839_a(NBTTagCompound nbtRoot) {
         super.func_145839_a(nbtRoot);
         this.lastFairestSpawn = nbtRoot.func_74763_f("LastFairestSpawnTime");
         this.readItemDataFromNBT(nbtRoot);
      }

      private void writeItemDataToNBT(NBTTagCompound nbtRoot) {
         NBTTagCompound nbtPlayer;
         if (this.dimCoords != null) {
            nbtPlayer = this.dimCoords.toTagNBT();
            nbtPlayer.func_74768_a("Dimension", this.dim);
            nbtRoot.func_74782_a("DimCoords", nbtPlayer);
         }

         nbtRoot.func_74757_a("DemonSlain", this.demonKilled);
         if (this.favorite != null) {
            nbtPlayer = new NBTTagCompound();
            NBTUtil.func_152460_a(nbtPlayer, this.favorite);
            nbtRoot.func_74782_a("Favorite", nbtPlayer);
         }

         if (this.fairest != null) {
            nbtRoot.func_74778_a("Fairest", this.fairest.toString());
         }

         NBTTagList players = new NBTTagList();
         Iterator i$ = this.playersSeen.iterator();

         while(i$.hasNext()) {
            String player = (String)i$.next();
            players.func_74742_a(new NBTTagString(player));
         }

         nbtRoot.func_74782_a("PlayersSeen", players);
      }

      public void readItemDataFromNBT(NBTTagCompound nbtRoot) {
         if (nbtRoot.func_74764_b("DimCoords")) {
            NBTTagCompound nbtDim = nbtRoot.func_74775_l("DimCoords");
            this.dimCoords = Coord.fromTagNBT(nbtDim);
            this.dim = nbtDim.func_74762_e("Dimension");
         }

         this.demonKilled = nbtRoot.func_74767_n("DemonSlain");
         if (nbtRoot.func_150297_b("Favorite", 10)) {
            this.favorite = NBTUtil.func_152459_a(nbtRoot.func_74775_l("Favorite"));
         } else {
            this.favorite = null;
         }

         if (nbtRoot.func_74764_b("Fairest")) {
            this.fairest = UUID.fromString(nbtRoot.func_74779_i("Fairest"));
         } else {
            this.fairest = null;
         }

         this.playersSeen.clear();
         if (nbtRoot.func_74764_b("PlayersSeen")) {
            NBTTagList players = nbtRoot.func_150295_c("PlayersSeen", 8);

            for(int i = 0; i < players.func_74745_c(); ++i) {
               this.playersSeen.add(players.func_150307_f(i));
            }
         }

      }

      public boolean isFavorite(EntityPlayer player) {
         return this.favorite != null && player != null && this.favorite.equals(player.func_146103_bH());
      }
   }
}
