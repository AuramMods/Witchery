package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPlacedItem extends BlockBaseContainer {
   public static void placeItemInWorld(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
      int meta = 0;
      if (player != null) {
         int l = MathHelper.func_76128_c((double)(player.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
         if (l == 0) {
            meta = 2;
         }

         if (l == 1) {
            meta = 5;
         }

         if (l == 2) {
            meta = 3;
         }

         if (l == 3) {
            meta = 4;
         }
      }

      world.func_147465_d(x, y, z, Witchery.Blocks.PLACED_ITEMSTACK, meta, 3);
      TileEntity tile = world.func_147438_o(x, y, z);
      if (tile != null && tile instanceof BlockPlacedItem.TileEntityPlacedItem) {
         ((BlockPlacedItem.TileEntityPlacedItem)tile).setStack(stack);
      }

   }

   public BlockPlacedItem() {
      super(Material.field_151578_c, BlockPlacedItem.TileEntityPlacedItem.class);
      super.registerWithCreateTab = false;
      this.func_149711_c(0.0F);
      this.func_149672_a(field_149777_j);
      this.func_149676_a(0.2F, 0.0F, 0.2F, 0.8F, 0.05F, 0.8F);
   }

   public void func_149651_a(IIconRegister p_149651_1_) {
   }

   protected String func_149641_N() {
      return null;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public void func_149681_a(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer) {
      if (par6EntityPlayer.field_71075_bZ.field_75098_d) {
         par5 |= 8;
         par1World.func_72921_c(par2, par3, par4, par5, 4);
      }

      this.func_149697_b(par1World, par2, par3, par4, par5, 0);
      super.func_149681_a(par1World, par2, par3, par4, par5, par6EntityPlayer);
   }

   public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
      ArrayList<ItemStack> drops = new ArrayList();
      if ((metadata & 8) == 0) {
         TileEntity tile = world.func_147438_o(x, y, z);
         if (tile != null && tile instanceof BlockPlacedItem.TileEntityPlacedItem && ((BlockPlacedItem.TileEntityPlacedItem)tile).getStack() != null) {
            drops.add(((BlockPlacedItem.TileEntityPlacedItem)tile).getStack());
         }
      }

      return drops;
   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      this.func_111046_k(par1World, par2, par3, par4);
   }

   private boolean func_111046_k(World par1World, int par2, int par3, int par4) {
      if (!this.func_149718_j(par1World, par2, par3, par4)) {
         if (!par1World.field_72995_K) {
            this.func_149697_b(par1World, par2, par3, par4, par1World.func_72805_g(par2, par3, par4), 0);
            par1World.func_147468_f(par2, par3, par4);
         }

         return false;
      } else {
         return true;
      }
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      TileEntity tile = world.func_147438_o(x, y, z);
      return tile != null && tile instanceof BlockPlacedItem.TileEntityPlacedItem && ((BlockPlacedItem.TileEntityPlacedItem)tile).getStack() != null ? ((BlockPlacedItem.TileEntityPlacedItem)tile).getStack().func_77946_l() : new ItemStack(Witchery.Items.ARTHANA);
   }

   public boolean func_149718_j(World world, int x, int y, int z) {
      Material material = world.func_147439_a(x, y - 1, z).func_149688_o();
      return !world.func_147437_c(x, y - 1, z) && material != null && material.func_76218_k() && material.func_76220_a();
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
   }

   @SideOnly(Side.CLIENT)
   public String func_149702_O() {
      return this.func_149641_N();
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int par2) {
      return Blocks.field_150339_S.func_149733_h(0);
   }

   public static class TileEntityPlacedItem extends TileEntity {
      private static final String ITEM_KEY = "WITCPlacedItem";
      private ItemStack stack;

      public boolean canUpdate() {
         return false;
      }

      public void func_145841_b(NBTTagCompound nbtRoot) {
         super.func_145841_b(nbtRoot);
         if (this.stack != null) {
            NBTTagCompound nbtItem = new NBTTagCompound();
            this.stack.func_77955_b(nbtItem);
            nbtRoot.func_74782_a("WITCPlacedItem", nbtItem);
         }

      }

      public void func_145839_a(NBTTagCompound nbtRoot) {
         super.func_145839_a(nbtRoot);
         if (nbtRoot.func_74764_b("WITCPlacedItem")) {
            NBTTagCompound nbtItem = nbtRoot.func_74775_l("WITCPlacedItem");
            ItemStack stack = ItemStack.func_77949_a(nbtItem);
            this.stack = stack;
         }

      }

      public void setStack(ItemStack stack) {
         this.stack = stack;
         if (!this.field_145850_b.field_72995_K) {
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
         }

      }

      public ItemStack getStack() {
         return this.stack;
      }

      public Packet func_145844_m() {
         NBTTagCompound nbtTag = new NBTTagCompound();
         this.func_145841_b(nbtTag);
         return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTag);
      }

      public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
         super.onDataPacket(net, packet);
         this.func_145839_a(packet.func_148857_g());
         this.field_145850_b.func_147479_m(this.field_145851_c, this.field_145848_d, this.field_145849_e);
      }
   }
}
