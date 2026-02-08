package com.emoniph.witchery.blocks;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.entity.EntityVampire;
import com.emoniph.witchery.ritual.rites.RiteProtectionCircleRepulsive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGarlicGarland extends BlockBaseContainer {
   public BlockGarlicGarland() {
      super(Material.field_151594_q, BlockGarlicGarland.TileEntityGarlicGarland.class);
      this.registerWithCreateTab = true;
      this.func_149711_c(0.2F);
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

   public boolean func_149718_j(World world, int x, int y, int z) {
      return super.func_149718_j(world, x, y, z);
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess world, int x, int y, int z, int side) {
      return false;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public void func_149719_a(IBlockAccess world, int x, int y, int z) {
      int side = world.func_72805_g(x, y, z);
      float minY = 0.8F;
      float maxY = 1.0F;
      float minX = 0.0F;
      float maxX = 0.15F;
      float minZ = 0.1F;
      float maxZ = 0.9F;
      if (side == 2) {
         this.func_149676_a(0.1F, 0.8F, 1.0F, 0.9F, 1.0F, 0.85F);
      } else if (side == 3) {
         this.func_149676_a(0.100000024F, 0.8F, 0.0F, 0.9F, 1.0F, 0.15F);
      } else if (side == 4) {
         this.func_149676_a(1.0F, 0.8F, 0.1F, 0.85F, 1.0F, 0.9F);
      } else if (side == 5) {
         this.func_149676_a(0.0F, (float)y + 0.8F, 0.1F, 0.15F, 1.0F, 0.9F);
      } else {
         this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      }

   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && entity instanceof EntityVampire) {
         RiteProtectionCircleRepulsive.push(world, entity, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z);
      } else if (world.field_72995_K && entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if (!player.field_71075_bZ.field_75098_d && ExtendedPlayer.get((EntityPlayer)entity).isVampire()) {
            RiteProtectionCircleRepulsive.push(world, entity, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, false);
         }
      }

   }

   public AxisAlignedBB func_149633_g(World world, int x, int y, int z) {
      return super.func_149633_g(world, x, y, z);
   }

   public void func_149689_a(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
      int facing = MathHelper.func_76128_c((double)(entity.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
      if (facing == 0) {
         world.func_72921_c(x, y, z, 2, 2);
      } else if (facing == 1) {
         world.func_72921_c(x, y, z, 5, 2);
      } else if (facing == 2) {
         world.func_72921_c(x, y, z, 3, 2);
      } else if (facing == 3) {
         world.func_72921_c(x, y, z, 4, 2);
      }

   }

   public static class TileEntityGarlicGarland extends TileEntity {
      public boolean canUpdate() {
         return false;
      }
   }
}
