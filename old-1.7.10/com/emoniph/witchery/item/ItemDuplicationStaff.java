package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.infusion.infusions.InfusionOtherwhere;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStairs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class ItemDuplicationStaff extends ItemBase {
   public ItemDuplicationStaff() {
      this.func_77625_d(1);
      this.func_77664_n();
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.epic;
   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      if (!world.field_72995_K && player.func_70093_af()) {
         MovingObjectPosition pickedBlock = InfusionOtherwhere.doCustomRayTrace(world, player, true, 6.0D);
         if ((pickedBlock == null || pickedBlock.field_72313_a != MovingObjectType.BLOCK) && stack.func_77942_o()) {
            NBTTagCompound nbtRoot = stack.func_77978_p();
            nbtRoot.func_82580_o("SavedSchematic");
            nbtRoot.func_82580_o("Marker");
            SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
         }
      }

      return stack;
   }

   public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
      if (stack.func_77942_o() && stack.func_77978_p().func_74764_b("SavedSchematic")) {
         if (!player.func_70093_af()) {
            int rotation = stack.func_77978_p().func_74762_e("Rotation");
            ++rotation;
            if (rotation > 3) {
               rotation = 0;
            }

            stack.func_77978_p().func_74768_a("Rotation", rotation);
            this.placeSchematic(stack, world, x, y + 1, z, player, ItemDuplicationStaff.Rotation.values()[rotation], true);
         } else {
            this.placeSchematic(stack, world, x, y + 1, z, player, ItemDuplicationStaff.Rotation.NONE, true);
            stack.func_77978_p().func_74768_a("Rotation", 0);
         }
      } else if (stack.func_77942_o() && stack.func_77978_p().func_74764_b("Marker")) {
         this.saveSchematic(stack, world, x, y, z, player);
      } else {
         this.setMarker(stack, world, x, y, z, player);
      }

      return !world.field_72995_K;
   }

   private void setMarker(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
      if (!world.field_72995_K) {
         if (!stack.func_77942_o()) {
            stack.func_77982_d(new NBTTagCompound());
         }

         NBTTagCompound nbtRoot = stack.func_77978_p();
         Coord marker = new Coord(x, y, z);
         nbtRoot.func_74782_a("Marker", marker.toTagNBT());
         ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_POP, world, 0.5D + (double)x, (double)y, 0.5D + (double)z, 1.0D, 1.0D, 16);
      }

   }

   private void saveSchematic(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
      if (!world.field_72995_K) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         if (nbtRoot != null) {
            PrintWriter writer = null;
            PrintWriter writer2 = null;

            try {
               Coord marker = Coord.fromTagNBT(nbtRoot.func_74775_l("Marker"));
               NBTTagList nbtList = new NBTTagList();
               new ArrayList();
               int width = Math.max(marker.x, x) - Math.min(marker.x, x) - 1;
               int height = Math.max(marker.y, y) - Math.min(marker.y, y) - 1;
               int depth = Math.max(marker.z, z) - Math.min(marker.z, z) - 1;
               File file = Config.instance().dupStaffSaveTemplate ? new File(String.format("%s/schematic.txt", Witchery.configDirectoryPath)) : null;
               File file2 = Config.instance().dupStaffSaveTemplate ? new File(String.format("%s/schematic2.txt", Witchery.configDirectoryPath)) : null;
               if (file != null && !file.exists()) {
                  file.createNewFile();
               }

               writer = new PrintWriter(file);
               if (writer != null) {
                  writer.println(String.format("final NBTTagCompound nbtSchematic = new NBTTagCompound();"));
                  writer.println(String.format("final NBTTagList nbtList = new NBTTagList();"));
                  writer.println(String.format("NBTTagCompound nbtBlock;"));
               }

               if (file2 != null && !file2.exists()) {
                  file2.createNewFile();
               }

               writer2 = new PrintWriter(file2);
               if (writer2 != null) {
               }

               if (marker.x != x && marker.y != y && marker.z != z) {
                  int minX = Math.min(marker.x, x) + 1;
                  int minZ = Math.min(marker.z, z) + 1;
                  int minY = Math.min(marker.y, y) + 1;

                  for(int dx = minX; dx < Math.max(marker.x, x); ++dx) {
                     for(int dz = minZ; dz < Math.max(marker.z, z); ++dz) {
                        for(int dy = minY; dy < Math.max(marker.y, y); ++dy) {
                           Block block = BlockUtil.getBlock(world, dx, dy, dz);
                           int meta = world.func_72805_g(dx, dy, dz);
                           NBTTagCompound nbtBlock = new NBTTagCompound();
                           String blockName = Block.field_149771_c.func_148750_c(block);
                           nbtBlock.func_74778_a("n", blockName);
                           if (meta != 0) {
                              nbtBlock.func_74768_a("m", meta);
                           }

                           nbtList.func_74742_a(nbtBlock);
                           if (writer != null) {
                              writer.println(String.format("nbtBlock = new NBTTagCompound();"));
                              writer.println(String.format("nbtBlock.setString(\"n\", \"%s\");", blockName));
                              if (meta != 0) {
                                 writer.println(String.format("nbtBlock.setInteger(\"m\", %d);", meta));
                              }

                              writer.println(String.format("nbtList.appendTag(nbtBlock);"));
                           }

                           if (writer2 != null && block != Blocks.field_150350_a) {
                              String blockNameStripped = blockName.substring(blockName.indexOf(58) + 1);
                              writer2.println(String.format("placeBlockAtCurrentPosition(world, Blocks.%s, %s, %d, %d, %d, bounds);", blockNameStripped, blockNameStripped, meta, dx - minX, dy - minY, dz - minZ));
                           }
                        }
                     }
                  }
               }

               if (nbtList.func_74745_c() > 0) {
                  NBTTagCompound nbtSchematic = new NBTTagCompound();
                  nbtSchematic.func_74782_a("blocks", nbtList);
                  nbtSchematic.func_74768_a("xMax", width);
                  nbtSchematic.func_74768_a("yMax", height);
                  nbtSchematic.func_74768_a("zMax", depth);
                  if (writer != null) {
                     writer.println(String.format("nbtSchematic.setTag(\"blocks\", nbtList);"));
                     writer.println(String.format("nbtSchematic.setInteger(\"xMax\", %d);", width));
                     writer.println(String.format("nbtSchematic.setInteger(\"yMax\", %d);", height));
                     writer.println(String.format("nbtSchematic.setInteger(\"zMax\", %d);", depth));
                  }

                  nbtRoot.func_74782_a("SavedSchematic", nbtSchematic);
                  nbtRoot.func_82580_o("Marker");
                  ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_POP, world, 0.5D + (double)x, (double)y, 0.5D + (double)z, 1.0D, 1.0D, 16);
               } else {
                  ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, world, 0.5D + (double)x, (double)y, 0.5D + (double)z, 1.0D, 1.0D, 16);
               }
            } catch (IOException var32) {
            } finally {
               if (writer != null) {
                  writer.close();
               }

               if (writer2 != null) {
                  writer2.close();
               }

            }
         }
      }

   }

   private void placeSchematic(ItemStack stack, World world, int x, int y, int z, EntityPlayer player, ItemDuplicationStaff.Rotation rotation, boolean clearAir) {
      if (!world.field_72995_K) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         if (nbtRoot != null) {
            NBTTagCompound nbtSchematic = nbtRoot.func_74775_l("SavedSchematic");
            drawSchematicInWorld(world, x, y, z, rotation, clearAir, nbtSchematic);
         }
      }

   }

   public static void drawSchematicInWorld(World world, int x, int y, int z, ItemDuplicationStaff.Rotation rotation, boolean clearAir, NBTTagCompound nbtSchematic) {
      if (nbtSchematic != null) {
         NBTTagList nbtBlocks = nbtSchematic.func_150295_c("blocks", 10);
         int width = nbtSchematic.func_74762_e("xMax");
         int height = nbtSchematic.func_74762_e("yMax");
         int depth = nbtSchematic.func_74762_e("zMax");
         if (nbtBlocks != null && width > 0 && height > 0 && depth > 0) {
            int blockIndex = 0;
            int dx;
            int dz;
            if (rotation == ItemDuplicationStaff.Rotation.DEGREES_180) {
               for(dx = width - 1; dx >= 0; --dx) {
                  for(dz = depth - 1; dz >= 0; --dz) {
                     blockIndex = drawBlocks(world, dx + x, y, dz + z, nbtBlocks, height, blockIndex, rotation, clearAir);
                  }
               }
            } else if (rotation == ItemDuplicationStaff.Rotation.DEGREES_90) {
               for(dx = width - 1; dx >= 0; --dx) {
                  for(dz = 0; dz < depth; ++dz) {
                     blockIndex = drawBlocks(world, dz + x, y, dx + z, nbtBlocks, height, blockIndex, rotation, clearAir);
                  }
               }
            } else if (rotation == ItemDuplicationStaff.Rotation.DEGREES_270) {
               for(dx = 0; dx < width; ++dx) {
                  for(dz = depth - 1; dz >= 0; --dz) {
                     blockIndex = drawBlocks(world, dz + x, y, dx + z, nbtBlocks, height, blockIndex, rotation, clearAir);
                  }
               }
            } else {
               for(dx = 0; dx < width; ++dx) {
                  for(dz = 0; dz < depth; ++dz) {
                     blockIndex = drawBlocks(world, dx + x, y, dz + z, nbtBlocks, height, blockIndex, rotation, clearAir);
                  }
               }
            }
         }
      }

   }

   private static int drawBlocks(World world, int x, int y, int z, NBTTagList nbtBlocks, int height, int blockIndex, ItemDuplicationStaff.Rotation rotation, boolean clearAir) {
      for(int dy = 0; dy < height; ++dy) {
         NBTTagCompound nbtBlock = nbtBlocks.func_150305_b(blockIndex++);
         String blockName = nbtBlock.func_74779_i("n");
         int blockMeta = nbtBlock.func_74762_e("m");
         Block block = Block.func_149684_b(blockName);
         int direction;
         int other;
         if (block instanceof BlockStairs) {
            direction = blockMeta & 3;
            other = blockMeta >> 2 & 1;
            if (rotation == ItemDuplicationStaff.Rotation.DEGREES_180) {
               direction = (new int[]{1, 0, 3, 2})[direction];
            } else if (rotation == ItemDuplicationStaff.Rotation.DEGREES_90) {
               direction = (new int[]{3, 2, 0, 1})[direction];
            } else if (rotation == ItemDuplicationStaff.Rotation.DEGREES_270) {
               direction = (new int[]{2, 3, 1, 0})[direction];
            }

            blockMeta = direction | other << 2;
         } else if (block instanceof BlockRotatedPillar) {
            direction = blockMeta & 3;
            other = blockMeta >> 2 & 3;
            int other = blockMeta >> 4;
            if (rotation == ItemDuplicationStaff.Rotation.DEGREES_90 || rotation == ItemDuplicationStaff.Rotation.DEGREES_270) {
               other = (new int[]{0, 2, 1, 3})[other];
               blockMeta = direction | other << 2 | other << 4;
            }
         } else if (block instanceof BlockDoor && (blockMeta >> 4 & 1) == 0) {
            direction = blockMeta & 3;
            other = blockMeta >> 2;
            if (rotation == ItemDuplicationStaff.Rotation.DEGREES_180) {
               direction = (new int[]{2, 3, 0, 1})[direction];
            } else if (rotation == ItemDuplicationStaff.Rotation.DEGREES_90) {
               direction = (new int[]{3, 0, 1, 2})[direction];
            } else if (rotation == ItemDuplicationStaff.Rotation.DEGREES_270) {
               direction = (new int[]{1, 2, 3, 0})[direction];
            }

            blockMeta = direction | other << 2;
         }

         if (block != null && (clearAir || block != Blocks.field_150350_a)) {
            world.func_147465_d(x, dy + y, z, block, blockMeta, 2);
         }
      }

      return blockIndex;
   }

   public static enum Rotation {
      NONE,
      DEGREES_90,
      DEGREES_180,
      DEGREES_270;
   }
}
