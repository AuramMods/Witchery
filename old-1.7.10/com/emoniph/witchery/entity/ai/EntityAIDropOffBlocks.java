package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityGoblin;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;

public class EntityAIDropOffBlocks extends EntityAIBase {
   protected final EntityGoblin entity;
   protected final double range;
   private TileEntity targetTile = null;

   public EntityAIDropOffBlocks(EntityGoblin entity, double range) {
      this.entity = entity;
      this.range = range;
      this.func_75248_a(7);
   }

   public boolean func_75250_a() {
      if ((this.entity == null || this.entity.isWorshipping() || this.entity.func_70694_bm() != null) && this.entity.func_110167_bD() && !(this.entity.func_70694_bm().func_77973_b() instanceof ItemTool)) {
         if (this.targetTile != null && !this.targetTile.func_145837_r() && this.entity.func_70661_as().func_75488_a((double)this.targetTile.field_145851_c, (double)this.targetTile.field_145848_d, (double)this.targetTile.field_145849_e) != null) {
            return true;
         } else {
            this.targetTile = null;
            if (this.entity.field_70170_p.field_73012_v.nextInt(60) != 0) {
               return false;
            } else {
               this.setTargetTile();
               return this.targetTile != null;
            }
         }
      } else {
         return false;
      }
   }

   public void func_75249_e() {
   }

   private void setTargetTile() {
      this.targetTile = null;
      new ArrayList();
      double bestDist = Double.MAX_VALUE;
      double RANGE_SQ = this.range * this.range;

      for(int i = 0; i < this.entity.field_70170_p.field_147482_g.size(); ++i) {
         try {
            Object te = this.entity.field_70170_p.field_147482_g.get(i);
            if (te != null && te instanceof IInventory) {
               TileEntity tile = (TileEntity)te;
               if (!tile.func_145837_r() && ((IInventory)tile).func_70302_i_() >= 27) {
                  double distSq = this.entity.func_70092_e((double)tile.field_145851_c, (double)tile.field_145848_d, (double)tile.field_145849_e);
                  if (distSq <= RANGE_SQ && this.entity.func_70661_as().func_75488_a((double)tile.field_145851_c, (double)tile.field_145848_d, (double)tile.field_145849_e) != null && distSq < bestDist) {
                     bestDist = distSq;
                     this.targetTile = tile;
                  }
               }
            }
         } catch (Throwable var11) {
         }
      }

   }

   public boolean func_75253_b() {
      return this.entity != null && !this.entity.isWorshipping() && this.entity.func_70694_bm() != null && this.entity.func_110167_bD() && this.targetTile != null;
   }

   public void func_75246_d() {
      double SPEED = 0.6D;
      if (this.entity.func_70661_as().func_75500_f()) {
         this.setTargetTile();
         if (this.targetTile != null) {
            this.entity.func_70661_as().func_75492_a((double)this.targetTile.field_145851_c, (double)this.targetTile.field_145848_d, (double)this.targetTile.field_145849_e, 0.6D);
         }
      }

      double DROP_RANGE = 2.5D;
      double DROP_RANGE_SQ = 6.25D;
      if (this.targetTile != null && this.entity.func_70092_e((double)this.targetTile.field_145851_c + 0.5D, (double)this.targetTile.field_145848_d + 0.5D, (double)this.targetTile.field_145849_e + 0.5D) <= 6.25D) {
         IInventory inventory = (IInventory)this.targetTile;
         inventory.func_70295_k_();
         if (this.addItemStackToInventory(this.entity.func_70694_bm(), inventory) && this.entity.func_70694_bm().field_77994_a == 0) {
            this.entity.func_70062_b(0, (ItemStack)null);
         }

         inventory.func_70305_f();
      }

   }

   public boolean addItemStackToInventory(final ItemStack par1ItemStack, IInventory inventory) {
      if (par1ItemStack != null && par1ItemStack.field_77994_a != 0 && par1ItemStack.func_77973_b() != null) {
         try {
            int i;
            if (par1ItemStack.func_77951_h()) {
               i = this.getFirstEmptyStack(inventory);
               if (i >= 0) {
                  inventory.func_70299_a(i, ItemStack.func_77944_b(par1ItemStack));
                  par1ItemStack.field_77994_a = 0;
                  par1ItemStack.field_77992_b = 5;
                  return true;
               } else {
                  return false;
               }
            } else {
               do {
                  i = par1ItemStack.field_77994_a;
                  par1ItemStack.field_77994_a = this.storePartialItemStack(par1ItemStack, inventory);
               } while(par1ItemStack.field_77994_a > 0 && par1ItemStack.field_77994_a < i);

               return par1ItemStack.field_77994_a < i;
            }
         } catch (Throwable var6) {
            CrashReport crashreport = CrashReport.func_85055_a(var6, "Adding item to inventory");
            CrashReportCategory crashreportcategory = crashreport.func_85058_a("Item being added");
            crashreportcategory.func_71507_a("Item ID", Item.func_150891_b(par1ItemStack.func_77973_b()));
            crashreportcategory.func_71507_a("Item data", par1ItemStack.func_77960_j());
            crashreportcategory.func_71500_a("Item name", new Callable() {
               private static final String __OBFID = "CL_00001710";

               public String call() {
                  return par1ItemStack.func_82833_r();
               }
            });
            throw new ReportedException(crashreport);
         }
      } else {
         return false;
      }
   }

   public int getFirstEmptyStack(IInventory inventory) {
      for(int i = 0; i < inventory.func_70302_i_(); ++i) {
         if (inventory.func_70301_a(i) == null) {
            return i;
         }
      }

      return -1;
   }

   private int storePartialItemStack(ItemStack par1ItemStack, IInventory inventory) {
      Item item = par1ItemStack.func_77973_b();
      int i = par1ItemStack.field_77994_a;
      int j;
      if (par1ItemStack.func_77976_d() == 1) {
         j = this.getFirstEmptyStack(inventory);
         if (j < 0) {
            return i;
         } else {
            if (inventory.func_70301_a(j) == null) {
               inventory.func_70299_a(j, ItemStack.func_77944_b(par1ItemStack));
            }

            return 0;
         }
      } else {
         j = this.storeItemStack(par1ItemStack, inventory);
         if (j < 0) {
            j = this.getFirstEmptyStack(inventory);
         }

         if (j < 0) {
            return i;
         } else {
            if (inventory.func_70301_a(j) == null) {
               inventory.func_70299_a(j, new ItemStack(item, 0, par1ItemStack.func_77960_j()));
               if (par1ItemStack.func_77942_o()) {
                  inventory.func_70301_a(j).func_77982_d((NBTTagCompound)par1ItemStack.func_77978_p().func_74737_b());
               }
            }

            int k = i;
            if (i > inventory.func_70301_a(j).func_77976_d() - inventory.func_70301_a(j).field_77994_a) {
               k = inventory.func_70301_a(j).func_77976_d() - inventory.func_70301_a(j).field_77994_a;
            }

            if (k > 64 - inventory.func_70301_a(j).field_77994_a) {
               k = 64 - inventory.func_70301_a(j).field_77994_a;
            }

            if (k == 0) {
               return i;
            } else {
               i -= k;
               ItemStack var10000 = inventory.func_70301_a(j);
               var10000.field_77994_a += k;
               inventory.func_70301_a(j).field_77992_b = 5;
               return i;
            }
         }
      }
   }

   private int storeItemStack(ItemStack par1ItemStack, IInventory inventory) {
      for(int i = 0; i < inventory.func_70302_i_(); ++i) {
         if (inventory.func_70301_a(i) != null && inventory.func_70301_a(i).func_77973_b() == par1ItemStack.func_77973_b() && inventory.func_70301_a(i).func_77985_e() && inventory.func_70301_a(i).field_77994_a < inventory.func_70301_a(i).func_77976_d() && inventory.func_70301_a(i).field_77994_a < 64 && (!inventory.func_70301_a(i).func_77981_g() || inventory.func_70301_a(i).func_77960_j() == par1ItemStack.func_77960_j()) && ItemStack.func_77970_a(inventory.func_70301_a(i), par1ItemStack)) {
            return i;
         }
      }

      return -1;
   }
}
