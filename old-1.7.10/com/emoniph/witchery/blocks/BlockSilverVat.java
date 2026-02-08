package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSilverVat extends BlockBaseContainer {
   public static final ItemStack GOLD_INGOT;

   public BlockSilverVat() {
      super(Material.field_151573_f, BlockSilverVat.TileEntitySilverVat.class);
      this.func_149711_c(8.0F);
      this.func_149672_a(field_149777_j);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.64F, 1.0F);
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149646_a(IBlockAccess iblockaccess, int i, int j, int k, int l) {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public void func_149726_b(World world, int x, int y, int z) {
      super.func_149726_b(world, x, y, z);
   }

   public boolean func_149740_M() {
      return true;
   }

   public int func_149736_g(World world, int x, int y, int z, int side) {
      TileEntity tile = world.func_147438_o(x, y, z);
      return tile != null && tile instanceof IInventory ? Container.func_94526_b((IInventory)tile) : 0;
   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      if (!world.field_72995_K) {
         BlockSilverVat.TileEntitySilverVat tile = (BlockSilverVat.TileEntitySilverVat)BlockUtil.getTileEntity(world, x, y, z, BlockSilverVat.TileEntitySilverVat.class);
         if (tile != null) {
            ItemStack stack = tile.func_70301_a(0);
            if (stack != null) {
               EntityItem entity = new EntityItem(world, player.field_70165_t, player.field_70163_u + 1.0D, player.field_70161_v, stack);
               entity.field_70159_w = entity.field_70181_x = entity.field_70179_y = 0.0D;
               world.func_72838_d(entity);
               tile.func_70299_a(0, (ItemStack)null);
               tile.markBlockForUpdate(true);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
      if (y == tileY && (x == tileX || z == tileZ)) {
         BlockSilverVat.TileEntitySilverVat vat = (BlockSilverVat.TileEntitySilverVat)BlockUtil.getTileEntity(world, x, y, z, BlockSilverVat.TileEntitySilverVat.class);
         if (vat != null && !vat.reenterLock) {
            vat.reenterLock = true;

            try {
               TileEntity tile = world.func_147438_o(tileX, tileY, tileZ);
               if (tile != null && tile instanceof ISidedInventory) {
                  ISidedInventory inv = (ISidedInventory)tile;
                  int offsetX = x - tileX;
                  int offsetZ = z - tileZ;
                  int side = offsetX == 0 ? (offsetZ > 0 ? 3 : 2) : (offsetX > 0 ? 5 : 4);

                  for(int slot = 0; slot < inv.func_70302_i_(); ++slot) {
                     if (inv.func_102008_b(slot, GOLD_INGOT, side) && !inv.func_102007_a(slot, GOLD_INGOT, side)) {
                        ItemStack stack = inv.func_70301_a(slot);
                        if (stack != null && stack.func_77973_b() == GOLD_INGOT.func_77973_b()) {
                           if (stack.field_77994_a > vat.getLastStackSizeForSide(side) && vat.func_145831_w().field_73012_v.nextInt(5) == 0) {
                              ItemStack silver = vat.func_70301_a(0);
                              if (silver == null) {
                                 silver = Witchery.Items.GENERIC.itemSilverDust.createStack();
                                 vat.func_70299_a(0, silver);
                                 vat.markBlockForUpdate(true);
                              } else if (silver.field_77994_a < silver.func_77976_d()) {
                                 ++silver.field_77994_a;
                                 vat.markBlockForUpdate(true);
                              }
                           }

                           vat.setLastStackSizeForSide(side, stack.field_77994_a);
                           break;
                        }
                     }
                  }
               }
            } finally {
               vat.reenterLock = false;
            }
         }
      }

   }

   static {
      GOLD_INGOT = new ItemStack(Items.field_151043_k);
   }

   public static class TileEntitySilverVat extends TileEntity implements IInventory {
      private ItemStack[] slots = new ItemStack[1];
      private final int[] sides = new int[6];
      private boolean reenterLock;

      public boolean canUpdate() {
         return false;
      }

      public void markBlockForUpdate(boolean notifyNeighbours) {
         this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
         if (notifyNeighbours && this.field_145850_b != null) {
            this.field_145850_b.func_147444_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, this.func_145838_q());
         }

      }

      public int func_70302_i_() {
         return this.slots.length;
      }

      public void setLastStackSizeForSide(int side, int stackSize) {
         this.sides[side] = stackSize;
      }

      public int getLastStackSizeForSide(int side) {
         return this.sides[side];
      }

      public ItemStack func_70301_a(int slot) {
         return this.slots[slot];
      }

      public ItemStack func_70298_a(int slot, int quantity) {
         if (this.slots[slot] != null) {
            ItemStack itemstack;
            if (this.slots[slot].field_77994_a <= quantity) {
               itemstack = this.slots[slot];
               this.slots[slot] = null;
               return itemstack;
            } else {
               itemstack = this.slots[slot].func_77979_a(quantity);
               if (this.slots[slot].field_77994_a == 0) {
                  this.slots[slot] = null;
               }

               return itemstack;
            }
         } else {
            return null;
         }
      }

      public ItemStack func_70304_b(int slot) {
         if (this.slots[slot] != null) {
            ItemStack itemstack = this.slots[slot];
            this.slots[slot] = null;
            return itemstack;
         } else {
            return null;
         }
      }

      public void func_70299_a(int slot, ItemStack stack) {
         this.slots[slot] = stack;
         if (stack != null && stack.field_77994_a > this.func_70297_j_()) {
            stack.field_77994_a = this.func_70297_j_();
         }

      }

      public String func_145825_b() {
         return this.func_145838_q().func_149732_F();
      }

      public boolean func_145818_k_() {
         return true;
      }

      public void func_145839_a(NBTTagCompound nbtRoot) {
         super.func_145839_a(nbtRoot);
         NBTTagList nbtSlotList = nbtRoot.func_150295_c("Items", 10);
         this.slots = new ItemStack[this.func_70302_i_()];

         for(int i = 0; i < nbtSlotList.func_74745_c(); ++i) {
            NBTTagCompound nbtSlot = nbtSlotList.func_150305_b(i);
            byte b0 = nbtSlot.func_74771_c("Slot");
            if (b0 >= 0 && b0 < this.slots.length) {
               this.slots[b0] = ItemStack.func_77949_a(nbtSlot);
            }
         }

      }

      public void func_145841_b(NBTTagCompound nbtRoot) {
         super.func_145841_b(nbtRoot);
         NBTTagList nbtSlotList = new NBTTagList();

         for(int i = 0; i < this.slots.length; ++i) {
            if (this.slots[i] != null) {
               NBTTagCompound nbtSlot = new NBTTagCompound();
               nbtSlot.func_74774_a("Slot", (byte)i);
               this.slots[i].func_77955_b(nbtSlot);
               nbtSlotList.func_74742_a(nbtSlot);
            }
         }

         nbtRoot.func_74782_a("Items", nbtSlotList);
      }

      public int func_70297_j_() {
         return 64;
      }

      public void func_70296_d() {
         super.func_70296_d();
         if (!this.field_145850_b.field_72995_K) {
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
         }

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

      public boolean func_70300_a(EntityPlayer par1EntityPlayer) {
         return this.field_145850_b.func_147438_o(this.field_145851_c, this.field_145848_d, this.field_145849_e) != this ? false : par1EntityPlayer.func_70092_e((double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.5D, (double)this.field_145849_e + 0.5D) <= 64.0D;
      }

      public void func_70295_k_() {
      }

      public void func_70305_f() {
      }

      public boolean func_94041_b(int slot, ItemStack itemstack) {
         return false;
      }
   }
}
