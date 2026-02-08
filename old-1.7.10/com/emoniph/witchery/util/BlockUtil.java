package com.emoniph.witchery.util;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockFetish;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockUtil {
   public static Block registerBlock(Block block, String blockName) {
      int index = blockName.indexOf(58);
      if (index != -1) {
         blockName = blockName.substring(index + 1);
      }

      return GameRegistry.registerBlock(block, blockName);
   }

   public static Block registerBlock(Block block, Class<? extends ItemBlock> clazzItem, String blockName) {
      int index = blockName.indexOf(58);
      if (index != -1) {
         blockName = blockName.substring(index + 1);
      }

      return GameRegistry.registerBlock(block, clazzItem, blockName);
   }

   public static Block getBlock(World world, int posX, int posY, int posZ) {
      return world.func_147439_a(posX, posY, posZ);
   }

   public static Block getBlock(World world, double posX, double posY, double posZ) {
      int x = MathHelper.func_76128_c(posX);
      int y = MathHelper.func_76128_c(posY);
      int z = MathHelper.func_76128_c(posZ);
      return getBlock(world, x, y, z);
   }

   public static Block getBlock(World world, MovingObjectPosition mop) {
      return getBlock(world, mop, false);
   }

   public static boolean isReplaceableBlock(World world, int posX, int posY, int posZ) {
      return isReplaceableBlock(world, posX, posY, posZ, (EntityLivingBase)null);
   }

   public static boolean isReplaceableBlock(World world, int posX, int posY, int posZ, EntityLivingBase player) {
      Block block = getBlock(world, posX, posY, posZ);
      if (player != null) {
         int meta = world.func_72805_g(posX, posY, posZ);
         if (!BlockProtect.checkModsForBreakOK(world, posX, posY, posZ, block, meta, player)) {
            return false;
         }
      }

      return block != null ? block.func_149688_o().func_76222_j() : true;
   }

   public static Material getBlockMaterial(EntityPlayer player) {
      return getBlockMaterial(player, 0);
   }

   public static Material getBlockMaterial(EntityPlayer player, int yOffset) {
      int posX = MathHelper.func_76128_c(player.field_70165_t);
      int posY = MathHelper.func_76128_c(player.field_70121_D.field_72338_b) + yOffset;
      int posZ = MathHelper.func_76128_c(player.field_70161_v);
      return getBlockMaterial(player.field_70170_p, posX, posY, posZ);
   }

   public static Material getBlockMaterial(World world, int posX, int posY, int posZ) {
      Block block = getBlock(world, posX, posY, posZ);
      return block != null ? block.func_149688_o() : Material.field_151579_a;
   }

   public static Block getBlock(World world, MovingObjectPosition mop, boolean before) {
      if (mop == null) {
         return null;
      } else {
         int x;
         int z;
         int y;
         if (mop.field_72313_a == MovingObjectType.BLOCK) {
            if (before) {
               x = mop.field_72311_b + (mop.field_72310_e == 4 ? -1 : (mop.field_72310_e == 5 ? 1 : 0));
               z = mop.field_72309_d + (mop.field_72310_e == 2 ? -1 : (mop.field_72310_e == 3 ? 1 : 0));
               y = mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0));
               if (mop.field_72310_e == 1 && !world.func_147439_a(x, mop.field_72312_c, z).func_149688_o().func_76220_a()) {
                  --y;
               }

               return getBlock(world, x, y, z);
            } else {
               return getBlock(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
            }
         } else {
            x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
            z = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u) - 1;
            y = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
            return getBlock(world, x, z, y);
         }
      }
   }

   public static int[] getBlockCoords(World world, MovingObjectPosition mop, boolean before) {
      if (mop == null) {
         return null;
      } else {
         int x;
         int z;
         int y;
         if (mop.field_72313_a == MovingObjectType.BLOCK) {
            if (before) {
               x = mop.field_72311_b + (mop.field_72310_e == 4 ? -1 : (mop.field_72310_e == 5 ? 1 : 0));
               z = mop.field_72309_d + (mop.field_72310_e == 2 ? -1 : (mop.field_72310_e == 3 ? 1 : 0));
               y = mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0));
               if (mop.field_72310_e == 1 && !world.func_147439_a(x, mop.field_72312_c, z).func_149688_o().func_76220_a()) {
                  --y;
               }

               return new int[]{x, y, z};
            } else {
               return new int[]{mop.field_72311_b, mop.field_72312_c, mop.field_72309_d};
            }
         } else {
            x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
            z = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u) - 1;
            y = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
            return new int[]{x, z, y};
         }
      }
   }

   public static int getBlockMetadata(World world, int posX, int posY, int posZ) {
      int blockMetadata = world.func_72805_g(posX, posY, posZ);
      return blockMetadata;
   }

   public static <T> T getTileEntity(IBlockAccess world, int posX, int posY, int posZ, Class<T> clazz) {
      TileEntity tile = world.func_147438_o(posX, posY, posZ);
      return tile != null && clazz.isAssignableFrom(tile.getClass()) ? clazz.cast(tile) : null;
   }

   public static void setBlock(World world, int posX, int posY, int posZ, Block newBlock, int newMetadata, int updateFlags) {
      world.func_147465_d(posX, posY, posZ, newBlock != null ? newBlock : Blocks.field_150350_a, newMetadata, updateFlags);
   }

   public static void setBlock(World world, int posX, int posY, int posZ, Block newBlock) {
      world.func_147449_b(posX, posY, posZ, newBlock != null ? newBlock : Blocks.field_150350_a);
   }

   public static void setBlock(World world, double posX, double posY, double posZ, Block block) {
      int x = MathHelper.func_76128_c(posX);
      int y = MathHelper.func_76128_c(posY);
      int z = MathHelper.func_76128_c(posZ);
      setBlock(world, x, y, z, block);
   }

   public static void setBlock(World world, int posX, int posY, int posZ, ItemBlock item, int damage, int updateFlags) {
      world.func_147465_d(posX, posY, posZ, item.field_150939_a, damage, updateFlags);
   }

   public static void setMetadata(World world, int posX, int posY, int posZ, int newMetadata) {
      setMetadata(world, posX, posY, posZ, newMetadata, 3);
   }

   public static void setMetadata(World world, int posX, int posY, int posZ, int newMetadata, int updateFlags) {
      world.func_72921_c(posX, posY, posZ, newMetadata, updateFlags);
   }

   public static void setAirBlock(World world, int x, int y, int z) {
      world.func_147468_f(x, y, z);
   }

   public static void setAirBlock(World world, int x, int y, int z, int updateFlags) {
      world.func_147465_d(x, y, z, Blocks.field_150350_a, 0, updateFlags);
   }

   public static void notifyNeighborsOfBlockChange(World world, int xCoord, int yCoord, int zCoord, Block blockType) {
      world.func_147459_d(xCoord, yCoord, zCoord, blockType);
   }

   public static boolean isImmovableBlock(Block block) {
      return block == Witchery.Blocks.ALTAR || block == Witchery.Blocks.VOID_BRAMBLE;
   }

   public static boolean isImmovableBlock(TileEntity tile) {
      return tile instanceof BlockFetish.TileEntityFetish ? ((BlockFetish.TileEntityFetish)tile).isSpectral() : false;
   }

   public static void setBlockDefaultDirection(World world, int posX, int posY, int posZ) {
      if (!world.field_72995_K) {
         Block l = world.func_147439_a(posX, posY, posZ - 1);
         Block i1 = world.func_147439_a(posX, posY, posZ + 1);
         Block j1 = world.func_147439_a(posX - 1, posY, posZ);
         Block k1 = world.func_147439_a(posX + 1, posY, posZ);
         byte b0 = 3;
         if (l.func_149662_c() && !i1.func_149662_c()) {
            b0 = 3;
         }

         if (i1.func_149662_c() && !l.func_149662_c()) {
            b0 = 2;
         }

         if (j1.func_149662_c() && !k1.func_149662_c()) {
            b0 = 5;
         }

         if (k1.func_149662_c() && !j1.func_149662_c()) {
            b0 = 4;
         }

         world.func_72921_c(posX, posY, posZ, b0, 2);
      }

   }

   public static boolean isSolid(World world, int posX, int posY, int posZ) {
      Block block = getBlock(world, posX, posY, posZ);
      if (block != null) {
         return !block.func_149688_o().func_76222_j();
      } else {
         return false;
      }
   }

   public static boolean isNormalCube(Block block) {
      return block.func_149688_o().func_76230_c() && block.func_149686_d();
   }

   public static Coord getClosestPlantableBlock(World world, int x, int y, int z, ForgeDirection side, EntityLivingBase entity) {
      return getClosestPlantableBlock(world, x, y, z, side, entity, false);
   }

   public static Coord getClosestPlantableBlock(World world, int x, int y, int z, ForgeDirection side, EntityLivingBase entity, boolean allowAir) {
      boolean foundBase = false;
      if (isReplaceableBlock(world, x, y, z) && (!allowAir || !world.func_147437_c(x, y, z))) {
         do {
            --y;
         } while(isReplaceableBlock(world, x, y, z));

         foundBase = true;
      } else if (side != ForgeDirection.UP && side != ForgeDirection.UNKNOWN) {
         if (side != ForgeDirection.DOWN) {
            x += side.offsetX;
            z += side.offsetZ;
            if (isReplaceableBlock(world, x, y, z)) {
               --y;
               foundBase = !isReplaceableBlock(world, x, y, z);
            }
         }
      } else {
         foundBase = true;
      }

      if (foundBase) {
         Block replaceBlock = world.func_147439_a(x, y + 1, z);
         int replaceMeta = world.func_72805_g(x, y + 1, z);
         if (BlockProtect.checkModsForBreakOK(world, x, y + 1, z, replaceBlock, replaceMeta, entity)) {
            return new Coord(x, y + 1, z);
         }
      }

      return null;
   }

   public static boolean setBlockIfReplaceable(World world, int x, int y, int z, Block block) {
      return setBlockIfReplaceable(world, x, y, z, block, 0);
   }

   public static boolean setBlockIfReplaceable(World world, int x, int y, int z, Block block, int meta) {
      Block currentBlock = world.func_147439_a(x, y, z);
      if (currentBlock != null && currentBlock.isReplaceable(world, x, y, z)) {
         world.func_147465_d(x, y, z, block, meta, 3);
         return true;
      } else {
         return false;
      }
   }
}
