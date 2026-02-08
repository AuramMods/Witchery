package com.emoniph.witchery.brewing;

import com.emoniph.witchery.blocks.BlockBaseContainer;
import com.emoniph.witchery.util.BlockUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class BlockButtonBase extends BlockBaseContainer {
   private final boolean isWood;

   protected BlockButtonBase(boolean wooden) {
      super(Material.field_151594_q, TileEntityCursedBlock.class);
      this.func_149675_a(true);
      this.isWood = wooden;
      this.registerWithCreateTab = false;
      this.func_149711_c(0.5F);
      this.func_149672_a(field_149780_i);
   }

   public void replaceButton(World world, int x, int y, int z, ModifiersImpact impactModifiers, NBTTagCompound nbtBrew) {
      int meta = world.func_72805_g(x, y, z);
      world.func_147465_d(x, y, z, this, meta & 7, 3);
      TileEntityCursedBlock tile = (TileEntityCursedBlock)BlockUtil.getTileEntity(world, x, y, z, TileEntityCursedBlock.class);
      if (tile != null) {
         tile.initalise(impactModifiers, nbtBrew);
      }

   }

   public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
      return Item.func_150898_a(this.isWood ? Blocks.field_150471_bO : Blocks.field_150430_aB);
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return new ItemStack(this.isWood ? Blocks.field_150471_bO : Blocks.field_150430_aB);
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public int func_149738_a(World world) {
      return this.isWood ? 30 : 20;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public boolean func_149707_d(World world, int x, int y, int z, int side) {
      ForgeDirection dir = ForgeDirection.getOrientation(side);
      return dir == ForgeDirection.NORTH && world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH) || dir == ForgeDirection.SOUTH && world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH) || dir == ForgeDirection.WEST && world.isSideSolid(x + 1, y, z, ForgeDirection.WEST) || dir == ForgeDirection.EAST && world.isSideSolid(x - 1, y, z, ForgeDirection.EAST);
   }

   public boolean func_149742_c(World world, int x, int y, int z) {
      return world.isSideSolid(x - 1, y, z, ForgeDirection.EAST) || world.isSideSolid(x + 1, y, z, ForgeDirection.WEST) || world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH) || world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH);
   }

   public int func_149660_a(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int p_149660_9_) {
      int j1 = world.func_72805_g(x, y, z);
      int k1 = j1 & 8;
      j1 &= 7;
      ForgeDirection dir = ForgeDirection.getOrientation(side);
      if (dir == ForgeDirection.NORTH && world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH)) {
         j1 = 4;
      } else if (dir == ForgeDirection.SOUTH && world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH)) {
         j1 = 3;
      } else if (dir == ForgeDirection.WEST && world.isSideSolid(x + 1, y, z, ForgeDirection.WEST)) {
         j1 = 2;
      } else if (dir == ForgeDirection.EAST && world.isSideSolid(x - 1, y, z, ForgeDirection.EAST)) {
         j1 = 1;
      } else {
         j1 = this.func_150045_e(world, x, y, z);
      }

      return j1 + k1;
   }

   private int func_150045_e(World world, int x, int y, int z) {
      if (world.isSideSolid(x - 1, y, z, ForgeDirection.EAST)) {
         return 1;
      } else if (world.isSideSolid(x + 1, y, z, ForgeDirection.WEST)) {
         return 2;
      } else if (world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH)) {
         return 3;
      } else {
         return world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH) ? 4 : 1;
      }
   }

   public void func_149695_a(World world, int x, int y, int z, Block block) {
      if (this.func_150044_m(world, x, y, z)) {
         int l = world.func_72805_g(x, y, z) & 7;
         boolean flag = false;
         if (!world.isSideSolid(x - 1, y, z, ForgeDirection.EAST) && l == 1) {
            flag = true;
         }

         if (!world.isSideSolid(x + 1, y, z, ForgeDirection.WEST) && l == 2) {
            flag = true;
         }

         if (!world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH) && l == 3) {
            flag = true;
         }

         if (!world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH) && l == 4) {
            flag = true;
         }

         if (flag) {
            this.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), 0);
            world.func_147468_f(x, y, z);
         }
      }

   }

   private boolean func_150044_m(World world, int x, int y, int z) {
      if (!this.func_149742_c(world, x, y, z)) {
         this.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), 0);
         world.func_147468_f(x, y, z);
         return false;
      } else {
         return true;
      }
   }

   public void func_149719_a(IBlockAccess world, int x, int y, int z) {
      int l = world.func_72805_g(x, y, z);
      this.func_150043_b(l);
   }

   private void func_150043_b(int p_150043_1_) {
      int j = p_150043_1_ & 7;
      boolean flag = (p_150043_1_ & 8) > 0;
      float f = 0.375F;
      float f1 = 0.625F;
      float f2 = 0.1875F;
      float f3 = 0.125F;
      if (flag) {
         f3 = 0.0625F;
      }

      if (j == 1) {
         this.func_149676_a(0.0F, f, 0.5F - f2, f3, f1, 0.5F + f2);
      } else if (j == 2) {
         this.func_149676_a(1.0F - f3, f, 0.5F - f2, 1.0F, f1, 0.5F + f2);
      } else if (j == 3) {
         this.func_149676_a(0.5F - f2, f, 0.0F, 0.5F + f2, f1, f3);
      } else if (j == 4) {
         this.func_149676_a(0.5F - f2, f, 1.0F - f3, 0.5F + f2, f1, 1.0F);
      }

   }

   public void func_149699_a(World world, int x, int y, int z, EntityPlayer player) {
   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
      int i1 = world.func_72805_g(x, y, z);
      int j1 = i1 & 7;
      int k1 = 8 - (i1 & 8);
      if (k1 == 0) {
         return true;
      } else {
         if (!world.field_72995_K) {
            TileEntityCursedBlock tile = (TileEntityCursedBlock)BlockUtil.getTileEntity(world, x, y, z, TileEntityCursedBlock.class);
            if (tile != null && tile.nbtEffect != null && !tile.applyToEntityAndDestroy(player)) {
               world.func_147465_d(x, y, z, this.isWood ? Blocks.field_150471_bO : Blocks.field_150430_aB, j1 + k1, 3);
               world.func_147458_c(x, y, z, x, y, z);
               world.func_72908_a((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.click", 0.3F, 0.6F);
               this.func_150042_a(world, x, y, z, j1);
               world.func_72921_c(x, y, z, j1 + k1, 3);
               world.func_147464_a(x, y, z, this.isWood ? Blocks.field_150471_bO : Blocks.field_150430_aB, this.func_149738_a(world));
               return true;
            }

            world.func_147458_c(x, y, z, x, y, z);
            world.func_72908_a((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.click", 0.3F, 0.6F);
            this.func_150042_a(world, x, y, z, j1);
            world.func_72921_c(x, y, z, j1 + k1, 3);
            world.func_147464_a(x, y, z, this, this.func_149738_a(world));
         }

         return true;
      }
   }

   public void func_149749_a(World world, int x, int y, int z, Block block, int p_149749_6_) {
      if ((p_149749_6_ & 8) > 0) {
         int i1 = p_149749_6_ & 7;
         this.func_150042_a(world, x, y, z, i1);
      }

      super.func_149749_a(world, x, y, z, block, p_149749_6_);
   }

   public int func_149709_b(IBlockAccess world, int x, int y, int z, int p_149709_5_) {
      return (world.func_72805_g(x, y, z) & 8) > 0 ? 15 : 0;
   }

   public int func_149748_c(IBlockAccess world, int x, int y, int z, int p_149748_5_) {
      int i1 = world.func_72805_g(x, y, z);
      if ((i1 & 8) == 0) {
         return 0;
      } else {
         int j1 = i1 & 7;
         return j1 == 5 && p_149748_5_ == 1 ? 15 : (j1 == 4 && p_149748_5_ == 2 ? 15 : (j1 == 3 && p_149748_5_ == 3 ? 15 : (j1 == 2 && p_149748_5_ == 4 ? 15 : (j1 == 1 && p_149748_5_ == 5 ? 15 : 0))));
      }
   }

   public boolean func_149744_f() {
      return true;
   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      if (!world.field_72995_K) {
         int l = world.func_72805_g(x, y, z);
         if ((l & 8) != 0) {
            if (this.isWood) {
               this.func_150046_n(world, x, y, z);
            } else {
               world.func_72921_c(x, y, z, l & 7, 3);
               int i1 = l & 7;
               this.func_150042_a(world, x, y, z, i1);
               world.func_72908_a((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.click", 0.3F, 0.5F);
               world.func_147458_c(x, y, z, x, y, z);
            }
         }
      }

   }

   public void func_149683_g() {
      float f = 0.1875F;
      float f1 = 0.125F;
      float f2 = 0.125F;
      this.func_149676_a(0.5F - f, 0.5F - f1, 0.5F - f2, 0.5F + f, 0.5F + f1, 0.5F + f2);
   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && this.isWood && (world.func_72805_g(x, y, z) & 8) == 0) {
         this.func_150046_n(world, x, y, z);
      }

   }

   private void func_150046_n(World world, int x, int y, int z) {
      int l = world.func_72805_g(x, y, z);
      int i1 = l & 7;
      boolean flag = (l & 8) != 0;
      this.func_150043_b(l);
      List list = world.func_72872_a(EntityArrow.class, AxisAlignedBB.func_72330_a((double)x + this.field_149759_B, (double)y + this.field_149760_C, (double)z + this.field_149754_D, (double)x + this.field_149755_E, (double)y + this.field_149756_F, (double)z + this.field_149757_G));
      boolean flag1 = !list.isEmpty();
      if (flag1 && !flag) {
         world.func_72921_c(x, y, z, i1 | 8, 3);
         this.func_150042_a(world, x, y, z, i1);
         world.func_147458_c(x, y, z, x, y, z);
         world.func_72908_a((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.click", 0.3F, 0.6F);
      }

      if (!flag1 && flag) {
         world.func_72921_c(x, y, z, i1, 3);
         this.func_150042_a(world, x, y, z, i1);
         world.func_147458_c(x, y, z, x, y, z);
         world.func_72908_a((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "random.click", 0.3F, 0.5F);
      }

      if (flag1) {
         world.func_147464_a(x, y, z, this, this.func_149738_a(world));
      }

   }

   private void func_150042_a(World world, int x, int y, int z, int p_150042_5_) {
      world.func_147459_d(x, y, z, this);
      if (p_150042_5_ == 1) {
         world.func_147459_d(x - 1, y, z, this);
      } else if (p_150042_5_ == 2) {
         world.func_147459_d(x + 1, y, z, this);
      } else if (p_150042_5_ == 3) {
         world.func_147459_d(x, y, z - 1, this);
      } else if (p_150042_5_ == 4) {
         world.func_147459_d(x, y, z + 1, this);
      } else {
         world.func_147459_d(x, y - 1, z, this);
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
   }
}
