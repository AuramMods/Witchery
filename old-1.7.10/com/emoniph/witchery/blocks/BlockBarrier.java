package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBarrier extends BlockBaseContainer {
   public static void setBlock(World world, int posX, int posY, int posZ, int ticksUntilExpiration, boolean blocksPlayers, EntityPlayer owner) {
      setBlock(world, posX, posY, posZ, ticksUntilExpiration, blocksPlayers, owner, false);
   }

   public static void setBlock(World world, int posX, int posY, int posZ, int ticksUntilExpiration, boolean blocksPlayers, EntityPlayer owner, boolean skipCreate) {
      if (!skipCreate) {
         world.func_147465_d(posX, posY, posZ, Witchery.Blocks.BARRIER, 0, 3);
      }

      TileEntity entity = world.func_147438_o(posX, posY, posZ);
      if (entity != null && entity instanceof BlockBarrier.TileEntityBarrier) {
         BlockBarrier.TileEntityBarrier tileEntity = (BlockBarrier.TileEntityBarrier)entity;
         tileEntity.setTicksUntilExpiration(ticksUntilExpiration);
         tileEntity.setBlocksPlayers(blocksPlayers);
         tileEntity.setOwner(owner);
      }

   }

   public BlockBarrier() {
      super(Material.field_151592_s, BlockBarrier.TileEntityBarrier.class);
      super.registerWithCreateTab = false;
      this.func_149722_s();
      this.func_149752_b(1000.0F);
      this.func_149713_g(0);
   }

   public AxisAlignedBB func_149668_a(World world, int posX, int posY, int posZ) {
      TileEntity te = world.func_147438_o(posX, posY, posZ);
      if (te != null && te instanceof BlockBarrier.TileEntityBarrier) {
         BlockBarrier.TileEntityBarrier tileEntity = (BlockBarrier.TileEntityBarrier)te;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)posX, (double)posY, (double)posZ, (double)(posX + 1), (double)posY + 0.9D, (double)(posZ + 1));
         List<EntityPlayer> players = world.func_72872_a(EntityPlayer.class, bounds);
         Iterator i$ = players.iterator();

         while(i$.hasNext()) {
            EntityPlayer player = (EntityPlayer)i$.next();
            if (player != null && (!tileEntity.getBlocksPlayers() || player.field_71075_bZ.field_75098_d && player.func_70093_af() || tileEntity.isOwner(player))) {
               return null;
            }
         }
      }

      float f = 0.0625F;
      return AxisAlignedBB.func_72330_a((double)((float)posX + 0.0625F), (double)((float)posY + 0.0625F), (double)((float)posZ + 0.0625F), (double)((float)(posX + 1) - 0.0625F), (double)((float)(posY + 1) - 0.0625F), (double)((float)(posZ + 1) - 0.0625F));
   }

   protected boolean func_149700_E() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 0;
   }

   public int func_149701_w() {
      return 0;
   }

   public boolean func_149646_a(IBlockAccess blockAccess, int posX, int posY, int posZ, int side) {
      Block i1 = blockAccess.func_147439_a(posX, posY, posZ);
      return i1 == this ? false : super.func_149646_a(blockAccess, posX, posY, posZ, side);
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }

   public static class TileEntityBarrier extends TileEntity {
      private int ticksUntilExpiration = 60;
      private boolean blocksPlayers;
      private String ownerName = "";
      private static final String KEY_REMAINING_TICKS = "remainingTicks";
      private static final String KEY_BLOCKS_PLAYERS = "blocksPlayers";
      private static final String KEY_OWNER_NAME = "owner";

      public void func_145845_h() {
         super.func_145845_h();
         if (--this.ticksUntilExpiration <= 0 && !this.field_145850_b.field_72995_K) {
            this.field_145850_b.func_147465_d(this.field_145851_c, this.field_145848_d, this.field_145849_e, Blocks.field_150350_a, 0, 3);
         }

      }

      public void func_145841_b(NBTTagCompound nbtTag) {
         super.func_145841_b(nbtTag);
         nbtTag.func_74768_a("remainingTicks", this.ticksUntilExpiration);
         nbtTag.func_74757_a("blocksPlayers", this.blocksPlayers);
         nbtTag.func_74778_a("owner", this.ownerName);
      }

      public void func_145839_a(NBTTagCompound nbtTag) {
         super.func_145839_a(nbtTag);
         this.ticksUntilExpiration = nbtTag.func_74762_e("remainingTicks");
         this.blocksPlayers = nbtTag.func_74767_n("blocksPlayers");
         this.ownerName = nbtTag.func_74779_i("owner");
      }

      public void setTicksUntilExpiration(int ticksUntilExpiration) {
         this.ticksUntilExpiration = ticksUntilExpiration;
      }

      public void setBlocksPlayers(boolean blocksPlayers) {
         this.blocksPlayers = blocksPlayers;
      }

      public boolean getBlocksPlayers() {
         return this.blocksPlayers;
      }

      public void setOwner(EntityPlayer owner) {
         this.ownerName = owner != null ? owner.func_70005_c_() : "";
      }

      public boolean isOwner(EntityPlayer player) {
         return player != null && player.func_70005_c_().equals(this.ownerName);
      }
   }
}
