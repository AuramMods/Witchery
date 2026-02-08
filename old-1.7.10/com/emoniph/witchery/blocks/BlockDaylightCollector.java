package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDaylightCollector extends BlockBase {
   @SideOnly(Side.CLIENT)
   private IIcon iconGlobe;
   @SideOnly(Side.CLIENT)
   private IIcon iconGlobeCharged;

   public BlockDaylightCollector() {
      super(Material.field_151573_f);
      this.func_149676_a(0.2F, 0.0F, 0.2F, 0.8F, 0.8F, 0.8F);
      this.func_149711_c(3.5F);
      this.func_149672_a(field_149777_j);
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149645_b() {
      return 1;
   }

   public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
      if (y == tileY && (x == tileX || z == tileZ) && world.func_147439_a(tileX, tileY, tileZ) == Blocks.field_150453_bW) {
         ForgeDirection direction = ForgeDirection.UNKNOWN;
         if (x - tileX < 0) {
            direction = ForgeDirection.WEST;
         } else if (x - tileX > 0) {
            direction = ForgeDirection.EAST;
         } else if (z - tileZ < 0) {
            direction = ForgeDirection.NORTH;
         } else if (z - tileZ > 0) {
            direction = ForgeDirection.SOUTH;
         }

         Blocks.field_150453_bW.func_149748_c(world, tileX, tileY, tileZ, direction.ordinal());
      }

   }

   public void func_149695_a(World world, int x, int y, int z, Block block) {
      if (!world.field_72995_K) {
         ForgeDirection[] directions = new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
         int meta = world.func_72805_g(x, y, z);
         if (meta > 0 && meta < 15) {
            ForgeDirection[] arr$ = directions;
            int len$ = directions.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               ForgeDirection direction = arr$[i$];
               int otherX = x + direction.offsetX;
               int otherZ = z + direction.offsetZ;
               Block otherBlock = world.func_147439_a(otherX, y, otherZ);
               if (otherBlock == Blocks.field_150453_bW) {
                  int power = Blocks.field_150453_bW.func_149709_b(world, otherX, y, otherZ, direction.ordinal());
                  if (power == meta + 1) {
                     world.func_72921_c(x, y, z, meta + 1, 3);
                  }
                  break;
               }
            }
         }
      }

   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      if (!world.field_72995_K) {
         int meta = world.func_72805_g(x, y, z);
         if (meta == 0) {
            ItemStack stack = player.func_70694_bm();
            if (Witchery.Items.GENERIC.itemQuartzSphere.isMatch(stack)) {
               --stack.field_77994_a;
               world.func_72921_c(x, y, z, 1, 3);
            }
         } else if (meta < 15) {
            world.func_72838_d(new EntityItem(world, player.field_70165_t, player.field_70163_u + 1.0D, player.field_70161_v, Witchery.Items.GENERIC.itemQuartzSphere.createStack()));
            world.func_72921_c(x, y, z, 0, 3);
         } else if (meta == 15) {
            world.func_72838_d(new EntityItem(world, player.field_70165_t, player.field_70163_u + 1.0D, player.field_70161_v, new ItemStack(Witchery.Items.SUN_GRENADE)));
            world.func_72921_c(x, y, z, 0, 3);
         }

         return true;
      } else {
         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int side, int meta) {
      switch(meta) {
      case 0:
         return super.func_149691_a(side, meta);
      case 15:
         return this.iconGlobeCharged;
      default:
         return this.iconGlobe;
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      super.func_149651_a(iconRegister);
      this.iconGlobe = iconRegister.func_94245_a(this.func_149641_N() + "1");
      this.iconGlobeCharged = iconRegister.func_94245_a(this.func_149641_N() + "2");
   }
}
