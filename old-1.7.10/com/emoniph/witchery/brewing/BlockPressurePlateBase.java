package com.emoniph.witchery.brewing;

import com.emoniph.witchery.blocks.BlockBaseContainer;
import com.emoniph.witchery.util.BlockUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockPressurePlate.Sensitivity;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPressurePlateBase extends BlockBaseContainer {
   private Sensitivity sensitivity;
   private String textureName;
   private Block original;

   public BlockPressurePlateBase(Block original, String textureName, Sensitivity sensitivity) {
      super(original.func_149688_o(), TileEntityCursedBlock.class);
      this.textureName = textureName;
      this.sensitivity = sensitivity;
      this.original = original;
      this.func_149711_c(0.5F);
      this.func_149672_a(original.field_149762_H);
      this.func_149675_a(true);
      this.func_150063_b(this.func_150066_d(15));
      this.registerWithCreateTab = false;
   }

   public void replaceButton(World world, int x, int y, int z, ModifiersImpact impactModifiers, NBTTagCompound nbtBrew) {
      int meta = world.func_72805_g(x, y, z);
      world.func_147465_d(x, y, z, this, meta, 3);
      world.func_147464_a(x, y, z, this, this.func_149738_a(world));
      TileEntityCursedBlock tile = (TileEntityCursedBlock)BlockUtil.getTileEntity(world, x, y, z, TileEntityCursedBlock.class);
      if (tile != null) {
         tile.initalise(impactModifiers, nbtBrew);
      }

   }

   public void func_149719_a(IBlockAccess world, int x, int y, int z) {
      this.func_150063_b(world.func_72805_g(x, y, z));
   }

   protected void func_150063_b(int p_150063_1_) {
      boolean flag = this.func_150060_c(p_150063_1_) > 0;
      float f = 0.0625F;
      if (flag) {
         this.func_149676_a(f, 0.0F, f, 1.0F - f, 0.03125F, 1.0F - f);
      } else {
         this.func_149676_a(f, 0.0F, f, 1.0F - f, 0.0625F, 1.0F - f);
      }

   }

   public int func_149738_a(World world) {
      return 20;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public boolean func_149655_b(IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public boolean func_149742_c(World world, int x, int y, int z) {
      return World.func_147466_a(world, x, y - 1, z) || BlockFence.func_149825_a(world.func_147439_a(x, y - 1, z));
   }

   public void func_149695_a(World world, int x, int y, int z, Block p_149695_5_) {
      boolean flag = false;
      if (!World.func_147466_a(world, x, y - 1, z) && !BlockFence.func_149825_a(world.func_147439_a(x, y - 1, z))) {
         flag = true;
      }

      if (flag) {
         this.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), 0);
         world.func_147468_f(x, y, z);
      }

   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      if (!world.field_72995_K) {
         int l = this.func_150060_c(world.func_72805_g(x, y, z));
         if (l > 0) {
            this.func_150062_a(world, x, y, z, l);
         }
      }

   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K) {
         int metadata = world.func_72805_g(x, y, z);
         int l = this.func_150060_c(metadata);
         if (l == 0) {
            int i1 = this.func_150065_e(world, x, y, z);
            boolean flag = l > 0;
            boolean flag1 = i1 > 0;
            if (l != i1) {
               int md = this.func_150066_d(i1);
               if (!world.field_72995_K) {
                  TileEntityCursedBlock tile = (TileEntityCursedBlock)BlockUtil.getTileEntity(world, x, y, z, TileEntityCursedBlock.class);
                  if (tile != null && !tile.applyToEntityAndDestroy(entity)) {
                     world.func_147449_b(x, y, z, this.original);
                     world.func_72921_c(x, y, z, md, 2);
                     this.func_150064_a_(world, x, y, z);
                     world.func_147458_c(x, y, z, x, y, z);
                     if (!flag1 && flag) {
                        world.func_72908_a((double)x + 0.5D, (double)y + 0.1D, (double)z + 0.5D, "random.click", 0.3F, 0.5F);
                     } else if (flag1 && !flag) {
                        world.func_72908_a((double)x + 0.5D, (double)y + 0.1D, (double)z + 0.5D, "random.click", 0.3F, 0.6F);
                     }

                     if (flag1) {
                        world.func_147464_a(x, y, z, this.original, this.func_149738_a(world));
                     }

                     return;
                  }
               }

               world.func_72921_c(x, y, z, md, 2);
               this.func_150064_a_(world, x, y, z);
               world.func_147458_c(x, y, z, x, y, z);
            }

            if (!flag1 && flag) {
               world.func_72908_a((double)x + 0.5D, (double)y + 0.1D, (double)z + 0.5D, "random.click", 0.3F, 0.5F);
            } else if (flag1 && !flag) {
               world.func_72908_a((double)x + 0.5D, (double)y + 0.1D, (double)z + 0.5D, "random.click", 0.3F, 0.6F);
            }

            if (flag1) {
               world.func_147464_a(x, y, z, this, this.func_149738_a(world));
            }
         }
      }

   }

   protected void func_150062_a(World world, int x, int y, int z, int p_150062_5_) {
      int i1 = this.func_150065_e(world, x, y, z);
      boolean flag = p_150062_5_ > 0;
      boolean flag1 = i1 > 0;
      if (p_150062_5_ != i1) {
         int metadata = this.func_150066_d(i1);
         world.func_72921_c(x, y, z, metadata, 2);
         this.func_150064_a_(world, x, y, z);
         world.func_147458_c(x, y, z, x, y, z);
      }

      if (!flag1 && flag) {
         world.func_72908_a((double)x + 0.5D, (double)y + 0.1D, (double)z + 0.5D, "random.click", 0.3F, 0.5F);
      } else if (flag1 && !flag) {
         world.func_72908_a((double)x + 0.5D, (double)y + 0.1D, (double)z + 0.5D, "random.click", 0.3F, 0.6F);
      }

      if (flag1) {
         world.func_147464_a(x, y, z, this, this.func_149738_a(world));
      }

   }

   protected AxisAlignedBB func_150061_a(int x, int y, int z) {
      float f = 0.125F;
      return AxisAlignedBB.func_72330_a((double)((float)x + f), (double)y, (double)((float)z + f), (double)((float)(x + 1) - f), (double)y + 0.25D, (double)((float)(z + 1) - f));
   }

   public void func_149749_a(World world, int x, int y, int z, Block block, int p_149749_6_) {
      if (this.func_150060_c(p_149749_6_) > 0) {
         this.func_150064_a_(world, x, y, z);
      }

      super.func_149749_a(world, x, y, z, block, p_149749_6_);
   }

   protected void func_150064_a_(World world, int x, int y, int z) {
      world.func_147459_d(x, y, z, this);
      world.func_147459_d(x, y - 1, z, this);
   }

   public int func_149709_b(IBlockAccess world, int x, int y, int z, int p_149709_5_) {
      return this.func_150060_c(world.func_72805_g(x, y, z));
   }

   public int func_149748_c(IBlockAccess world, int x, int y, int z, int p_149748_5_) {
      return p_149748_5_ == 1 ? this.func_150060_c(world.func_72805_g(x, y, z)) : 0;
   }

   public boolean func_149744_f() {
      return true;
   }

   public void func_149683_g() {
      float f = 0.5F;
      float f1 = 0.125F;
      float f2 = 0.5F;
      this.func_149676_a(0.5F - f, 0.5F - f1, 0.5F - f2, 0.5F + f, 0.5F + f1, 0.5F + f2);
   }

   public int func_149656_h() {
      return 1;
   }

   protected int func_150066_d(int p_150066_1_) {
      return p_150066_1_ > 0 ? 1 : 0;
   }

   protected int func_150060_c(int p_150060_1_) {
      return p_150060_1_ == 1 ? 15 : 0;
   }

   protected int func_150065_e(World world, int x, int y, int z) {
      List list = null;
      if (this.sensitivity == Sensitivity.everything) {
         list = world.func_72839_b((Entity)null, this.func_150061_a(x, y, z));
      }

      if (this.sensitivity == Sensitivity.mobs) {
         list = world.func_72872_a(EntityLivingBase.class, this.func_150061_a(x, y, z));
      }

      if (this.sensitivity == Sensitivity.players) {
         list = world.func_72872_a(EntityPlayer.class, this.func_150061_a(x, y, z));
      }

      if (list != null && !list.isEmpty()) {
         Iterator iterator = list.iterator();

         while(iterator.hasNext()) {
            Entity entity = (Entity)iterator.next();
            if (!entity.func_145773_az()) {
               return 15;
            }
         }
      }

      return 0;
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.field_149761_L = iconRegister.func_94245_a(this.textureName);
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return new ItemStack(this.original);
   }

   public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
      return Item.func_150898_a(this.original);
   }
}
