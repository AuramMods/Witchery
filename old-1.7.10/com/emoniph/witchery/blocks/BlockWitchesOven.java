package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.BlockSide;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.SlotClayJar;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWitchesOven extends BlockBaseContainer {
   private final Random furnaceRand = new Random();
   private final boolean isActive;
   private static boolean keepFurnaceInventory;

   public BlockWitchesOven(boolean burning) {
      super(Material.field_151573_f, BlockWitchesOven.TileEntityWitchesOven.class);
      super.registerTileEntity = !burning;
      super.registerWithCreateTab = !burning;
      this.isActive = burning;
      this.func_149711_c(3.5F);
      this.func_149672_a(field_149777_j);
      if (this.isActive) {
         this.func_149715_a(0.875F);
      }

      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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

   public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
      return Item.func_150898_a(Witchery.Blocks.OVEN_IDLE);
   }

   public void func_149726_b(World par1World, int par2, int par3, int par4) {
      super.func_149726_b(par1World, par2, par3, par4);
      BlockUtil.setBlockDefaultDirection(par1World, par2, par3, par4);
   }

   public static boolean isOven(Block block) {
      return block == Witchery.Blocks.OVEN_IDLE || block == Witchery.Blocks.OVEN_BURNING;
   }

   public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
      if (par1World.field_72995_K) {
         return true;
      } else {
         TileEntity tileentityfurnace = par1World.func_147438_o(par2, par3, par4);
         if (tileentityfurnace != null) {
            par5EntityPlayer.openGui(Witchery.instance, 2, par1World, par2, par3, par4);
         }

         return true;
      }
   }

   public static void updateWitchesOvenBlockState(boolean par0, World par1World, int par2, int par3, int par4) {
      int l = par1World.func_72805_g(par2, par3, par4);
      TileEntity tileentity = par1World.func_147438_o(par2, par3, par4);
      keepFurnaceInventory = true;
      if (par0) {
         par1World.func_147449_b(par2, par3, par4, Witchery.Blocks.OVEN_BURNING);
      } else {
         par1World.func_147449_b(par2, par3, par4, Witchery.Blocks.OVEN_IDLE);
      }

      keepFurnaceInventory = false;
      par1World.func_72921_c(par2, par3, par4, l, 2);
      if (tileentity != null) {
         tileentity.func_145829_t();
         par1World.func_147455_a(par2, par3, par4, tileentity);
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
      if (this.isActive) {
         int l = par1World.func_72805_g(par2, par3, par4);
         float f = (float)par2 + 0.5F;
         float f1 = (float)par3 + 0.2F + par5Random.nextFloat() * 6.0F / 16.0F;
         float f2 = (float)par4 + 0.5F;
         float f3 = 0.52F;
         float f4 = par5Random.nextFloat() * 0.6F - 0.3F;
         if (l == 4) {
            par1World.func_72869_a("smoke", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
            par1World.func_72869_a("flame", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
         } else if (l == 5) {
            par1World.func_72869_a("smoke", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
            par1World.func_72869_a("flame", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
         } else if (l == 2) {
            par1World.func_72869_a("smoke", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
            par1World.func_72869_a("flame", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
         } else if (l == 3) {
            par1World.func_72869_a("smoke", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
            par1World.func_72869_a("flame", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public void func_149689_a(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
      int l = MathHelper.func_76128_c((double)(par5EntityLivingBase.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
      if (l == 0) {
         par1World.func_72921_c(par2, par3, par4, 2, 2);
      }

      if (l == 1) {
         par1World.func_72921_c(par2, par3, par4, 5, 2);
      }

      if (l == 2) {
         par1World.func_72921_c(par2, par3, par4, 3, 2);
      }

      if (l == 3) {
         par1World.func_72921_c(par2, par3, par4, 4, 2);
      }

   }

   public void func_149749_a(World par1World, int par2, int par3, int par4, Block par5, int par6) {
      if (!keepFurnaceInventory) {
         par1World.func_147438_o(par2, par3, par4);
         BlockWitchesOven.TileEntityWitchesOven tileentityfurnace = (BlockWitchesOven.TileEntityWitchesOven)BlockUtil.getTileEntity(par1World, par2, par3, par4, BlockWitchesOven.TileEntityWitchesOven.class);
         if (tileentityfurnace != null) {
            for(int j1 = 0; j1 < tileentityfurnace.func_70302_i_(); ++j1) {
               ItemStack itemstack = tileentityfurnace.func_70301_a(j1);
               if (itemstack != null) {
                  float f = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
                  float f1 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
                  float f2 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;

                  while(itemstack.field_77994_a > 0) {
                     int k1 = this.furnaceRand.nextInt(21) + 10;
                     if (k1 > itemstack.field_77994_a) {
                        k1 = itemstack.field_77994_a;
                     }

                     itemstack.field_77994_a -= k1;
                     EntityItem entityitem = new EntityItem(par1World, (double)((float)par2 + f), (double)((float)par3 + f1), (double)((float)par4 + f2), new ItemStack(itemstack.func_77973_b(), k1, itemstack.func_77960_j()));
                     if (itemstack.func_77942_o()) {
                        entityitem.func_92059_d().func_77982_d((NBTTagCompound)itemstack.func_77978_p().func_74737_b());
                     }

                     float f3 = 0.05F;
                     entityitem.field_70159_w = (double)((float)this.furnaceRand.nextGaussian() * f3);
                     entityitem.field_70181_x = (double)((float)this.furnaceRand.nextGaussian() * f3 + 0.2F);
                     entityitem.field_70179_y = (double)((float)this.furnaceRand.nextGaussian() * f3);
                     par1World.func_72838_d(entityitem);
                  }
               }
            }

            par1World.func_147453_f(par2, par3, par4, par5);
         }
      }

      super.func_149749_a(par1World, par2, par3, par4, par5, par6);
   }

   public boolean func_149740_M() {
      return true;
   }

   public int func_149736_g(World par1World, int par2, int par3, int par4, int par5) {
      TileEntity te = par1World.func_147438_o(par2, par3, par4);
      return te != null && te instanceof IInventory ? Container.func_94526_b((IInventory)te) : 0;
   }

   public Item func_149694_d(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_) {
      return Item.func_150898_a(Witchery.Blocks.OVEN_IDLE);
   }

   public static class ContainerWitchesOven extends Container {
      private BlockWitchesOven.TileEntityWitchesOven furnace;
      private int lastCookTime;
      private int lastBurnTime;
      private int lastItemBurnTime;

      public ContainerWitchesOven(InventoryPlayer par1InventoryPlayer, BlockWitchesOven.TileEntityWitchesOven par2TileEntityFurnace) {
         this.furnace = par2TileEntityFurnace;
         this.func_75146_a(new Slot(par2TileEntityFurnace, 0, 56, 17));
         this.func_75146_a(new Slot(par2TileEntityFurnace, 1, 56, 53));
         this.func_75146_a(new SlotFurnace(par1InventoryPlayer.field_70458_d, par2TileEntityFurnace, 2, 118, 21));
         this.func_75146_a(new SlotFurnace(par1InventoryPlayer.field_70458_d, par2TileEntityFurnace, 3, 118, 53));
         this.func_75146_a(new SlotClayJar(par2TileEntityFurnace, 4, 83, 53));

         int i;
         for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
               this.func_75146_a(new Slot(par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
         }

         for(i = 0; i < 9; ++i) {
            this.func_75146_a(new Slot(par1InventoryPlayer, i, 8 + i * 18, 142));
         }

      }

      public void func_75132_a(ICrafting par1ICrafting) {
         super.func_75132_a(par1ICrafting);
         par1ICrafting.func_71112_a(this, 0, this.furnace.furnaceCookTime);
         par1ICrafting.func_71112_a(this, 1, this.furnace.furnaceBurnTime);
         par1ICrafting.func_71112_a(this, 2, this.furnace.currentItemBurnTime);
      }

      public void func_75142_b() {
         super.func_75142_b();

         for(int i = 0; i < this.field_75149_d.size(); ++i) {
            ICrafting icrafting = (ICrafting)this.field_75149_d.get(i);
            if (this.lastCookTime != this.furnace.furnaceCookTime) {
               icrafting.func_71112_a(this, 0, this.furnace.furnaceCookTime);
            }

            if (this.lastBurnTime != this.furnace.furnaceBurnTime) {
               icrafting.func_71112_a(this, 1, this.furnace.furnaceBurnTime);
            }

            if (this.lastItemBurnTime != this.furnace.currentItemBurnTime) {
               icrafting.func_71112_a(this, 2, this.furnace.currentItemBurnTime);
            }
         }

         this.lastCookTime = this.furnace.furnaceCookTime;
         this.lastBurnTime = this.furnace.furnaceBurnTime;
         this.lastItemBurnTime = this.furnace.currentItemBurnTime;
      }

      @SideOnly(Side.CLIENT)
      public void func_75137_b(int par1, int par2) {
         if (par1 == 0) {
            this.furnace.furnaceCookTime = par2;
         }

         if (par1 == 1) {
            this.furnace.furnaceBurnTime = par2;
         }

         if (par1 == 2) {
            this.furnace.currentItemBurnTime = par2;
         }

      }

      public boolean func_75145_c(EntityPlayer par1EntityPlayer) {
         return this.furnace.func_70300_a(par1EntityPlayer);
      }

      public ItemStack func_82846_b(EntityPlayer player, int slotIndex) {
         ItemStack itemstack = null;
         Slot slot = (Slot)this.field_75151_b.get(slotIndex);
         if (slot != null && slot.func_75216_d()) {
            ItemStack itemstack1 = slot.func_75211_c();
            itemstack = itemstack1.func_77946_l();
            if (slotIndex != 2 && slotIndex != 3) {
               if (slotIndex != 1 && slotIndex != 0 && slotIndex != 4) {
                  if (FurnaceRecipes.func_77602_a().func_151395_a(itemstack1) != null) {
                     if (!this.func_75135_a(itemstack1, 0, 1, false)) {
                        return null;
                     }
                  } else if (TileEntityFurnace.func_145954_b(itemstack1)) {
                     if (!this.func_75135_a(itemstack1, 1, 2, false)) {
                        return null;
                     }
                  } else if (Witchery.Items.GENERIC.itemEmptyClayJar.isMatch(itemstack1)) {
                     if (!this.func_75135_a(itemstack1, 4, 5, false)) {
                        return null;
                     }
                  } else if (slotIndex >= 5 && slotIndex < 32) {
                     if (!this.func_75135_a(itemstack1, 32, 41, false)) {
                        return null;
                     }
                  } else if (slotIndex >= 32 && slotIndex < 41 && !this.func_75135_a(itemstack1, 5, 32, false)) {
                     return null;
                  }
               } else if (!this.func_75135_a(itemstack1, 5, 41, false)) {
                  return null;
               }
            } else {
               if (!this.func_75135_a(itemstack1, 5, 41, true)) {
                  return null;
               }

               slot.func_75220_a(itemstack1, itemstack);
            }

            if (itemstack1.field_77994_a == 0) {
               slot.func_75215_d((ItemStack)null);
            } else {
               slot.func_75218_e();
            }

            if (itemstack1.field_77994_a == itemstack.field_77994_a) {
               return null;
            }

            slot.func_82870_a(player, itemstack1);
         }

         return itemstack;
      }
   }

   public static class TileEntityWitchesOven extends TileEntity implements ISidedInventory {
      private ItemStack[] furnaceItemStacks = new ItemStack[5];
      public int furnaceBurnTime;
      public int currentItemBurnTime;
      public int furnaceCookTime;
      static final int COOK_TIME = 180;
      private static final double FUNNEL_CHANCE = 0.25D;
      private static final double FILTERED_FUNNEL_CHANCE = 0.3D;
      private static final double DOUBLED_FILTERED_FUNNEL_CHANCE = 0.8D;
      private static final int SLOT_TO_COOK = 0;
      private static final int SLOT_FUEL = 1;
      private static final int SLOT_COOKED = 2;
      private static final int SLOT_BY_PRODUCT = 3;
      private static final int SLOT_JARS = 4;
      private static final int[] slots_top = new int[]{0, 4};
      private static final int[] slots_bottom = new int[]{4, 1};
      private static final int[] slots_sides = new int[]{3, 2, 4, 1};

      public int func_70302_i_() {
         return this.furnaceItemStacks.length;
      }

      public ItemStack func_70301_a(int par1) {
         return this.furnaceItemStacks[par1];
      }

      public ItemStack func_70298_a(int par1, int par2) {
         if (this.furnaceItemStacks[par1] != null) {
            ItemStack itemstack;
            if (this.furnaceItemStacks[par1].field_77994_a <= par2) {
               itemstack = this.furnaceItemStacks[par1];
               this.furnaceItemStacks[par1] = null;
               return itemstack;
            } else {
               itemstack = this.furnaceItemStacks[par1].func_77979_a(par2);
               if (this.furnaceItemStacks[par1].field_77994_a == 0) {
                  this.furnaceItemStacks[par1] = null;
               }

               return itemstack;
            }
         } else {
            return null;
         }
      }

      public ItemStack func_70304_b(int par1) {
         if (this.furnaceItemStacks[par1] != null) {
            ItemStack itemstack = this.furnaceItemStacks[par1];
            this.furnaceItemStacks[par1] = null;
            return itemstack;
         } else {
            return null;
         }
      }

      public void func_70299_a(int par1, ItemStack par2ItemStack) {
         this.furnaceItemStacks[par1] = par2ItemStack;
         if (par2ItemStack != null && par2ItemStack.field_77994_a > this.func_70297_j_()) {
            par2ItemStack.field_77994_a = this.func_70297_j_();
         }

      }

      public String func_145825_b() {
         return this.func_145838_q().func_149732_F();
      }

      public boolean func_145818_k_() {
         return true;
      }

      public void func_145839_a(NBTTagCompound par1NBTTagCompound) {
         super.func_145839_a(par1NBTTagCompound);
         NBTTagList nbttaglist = par1NBTTagCompound.func_150295_c("Items", 10);
         this.furnaceItemStacks = new ItemStack[this.func_70302_i_()];

         for(int i = 0; i < nbttaglist.func_74745_c(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.func_150305_b(i);
            byte b0 = nbttagcompound1.func_74771_c("Slot");
            if (b0 >= 0 && b0 < this.furnaceItemStacks.length) {
               this.furnaceItemStacks[b0] = ItemStack.func_77949_a(nbttagcompound1);
            }
         }

         this.furnaceBurnTime = par1NBTTagCompound.func_74765_d("BurnTime");
         this.furnaceCookTime = par1NBTTagCompound.func_74765_d("CookTime");
         this.currentItemBurnTime = TileEntityFurnace.func_145952_a(this.furnaceItemStacks[1]);
      }

      public void func_145841_b(NBTTagCompound par1NBTTagCompound) {
         super.func_145841_b(par1NBTTagCompound);
         par1NBTTagCompound.func_74777_a("BurnTime", (short)this.furnaceBurnTime);
         par1NBTTagCompound.func_74777_a("CookTime", (short)this.furnaceCookTime);
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.furnaceItemStacks.length; ++i) {
            if (this.furnaceItemStacks[i] != null) {
               NBTTagCompound nbttagcompound1 = new NBTTagCompound();
               nbttagcompound1.func_74774_a("Slot", (byte)i);
               this.furnaceItemStacks[i].func_77955_b(nbttagcompound1);
               nbttaglist.func_74742_a(nbttagcompound1);
            }
         }

         par1NBTTagCompound.func_74782_a("Items", nbttaglist);
      }

      public int func_70297_j_() {
         return 64;
      }

      @SideOnly(Side.CLIENT)
      public int getCookProgressScaled(int par1) {
         return this.furnaceCookTime * par1 / this.getCookTime();
      }

      @SideOnly(Side.CLIENT)
      public int getBurnTimeRemainingScaled(int par1) {
         if (this.currentItemBurnTime == 0) {
            this.currentItemBurnTime = 200;
         }

         return this.furnaceBurnTime * par1 / this.currentItemBurnTime;
      }

      public boolean isBurning() {
         return this.furnaceBurnTime > 0;
      }

      public void func_145845_h() {
         boolean flag = this.furnaceBurnTime > 0;
         boolean flag1 = false;
         if (this.furnaceBurnTime > 0) {
            --this.furnaceBurnTime;
         }

         if (!this.field_145850_b.field_72995_K) {
            if (this.furnaceBurnTime == 0 && this.canSmelt()) {
               this.currentItemBurnTime = this.furnaceBurnTime = TileEntityFurnace.func_145952_a(this.furnaceItemStacks[1]);
               if (this.furnaceBurnTime > 0) {
                  flag1 = true;
                  if (this.furnaceItemStacks[1] != null) {
                     --this.furnaceItemStacks[1].field_77994_a;
                     if (this.furnaceItemStacks[1].field_77994_a == 0) {
                        this.furnaceItemStacks[1] = this.furnaceItemStacks[1].func_77973_b().getContainerItem(this.furnaceItemStacks[1]);
                     }
                  }
               }
            }

            if (this.isBurning() && this.canSmelt()) {
               ++this.furnaceCookTime;
               if (this.furnaceCookTime >= this.getCookTime()) {
                  this.furnaceCookTime = 0;
                  this.smeltItem();
                  flag1 = true;
               }
            } else {
               this.furnaceCookTime = 0;
            }

            if (flag != this.furnaceBurnTime > 0) {
               flag1 = true;
               BlockWitchesOven.updateWitchesOvenBlockState(this.furnaceBurnTime > 0, this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
            }
         }

         if (flag1) {
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
         }

      }

      private boolean canSmelt() {
         if (this.furnaceItemStacks[0] == null) {
            return false;
         } else {
            ItemStack itemstack = FurnaceRecipes.func_77602_a().func_151395_a(this.furnaceItemStacks[0]);
            if (itemstack == null) {
               return false;
            } else {
               Item item = itemstack.func_77973_b();
               if (item != Items.field_151044_h && !(item instanceof ItemFood) && !Witchery.Items.GENERIC.itemAshWood.isMatch(itemstack)) {
                  return false;
               } else if (this.furnaceItemStacks[2] == null) {
                  return true;
               } else if (!this.furnaceItemStacks[2].func_77969_a(itemstack)) {
                  return false;
               } else {
                  int result = this.furnaceItemStacks[2].field_77994_a + itemstack.field_77994_a;
                  return result <= this.func_70297_j_() && result <= itemstack.func_77976_d();
               }
            }
         }
      }

      public void smeltItem() {
         if (this.canSmelt()) {
            ItemStack itemstack = FurnaceRecipes.func_77602_a().func_151395_a(this.furnaceItemStacks[0]);
            if (this.furnaceItemStacks[2] == null) {
               this.furnaceItemStacks[2] = itemstack.func_77946_l();
            } else if (this.furnaceItemStacks[2].func_77969_a(itemstack)) {
               ItemStack var10000 = this.furnaceItemStacks[2];
               var10000.field_77994_a += itemstack.field_77994_a;
            }

            this.generateByProduct(itemstack);
            --this.furnaceItemStacks[0].field_77994_a;
            if (this.furnaceItemStacks[0].field_77994_a <= 0) {
               this.furnaceItemStacks[0] = null;
            }
         }

      }

      private int getFumeFunnels() {
         int funnels = 0;
         int meta = this.field_145850_b.func_72805_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
         switch(meta) {
         case 2:
         case 3:
            funnels += this.isFumeFunnel(this.field_145851_c - 1, this.field_145848_d, this.field_145849_e, meta) ? 1 : 0;
            funnels += this.isFumeFunnel(this.field_145851_c + 1, this.field_145848_d, this.field_145849_e, meta) ? 1 : 0;
            break;
         case 4:
         case 5:
            funnels += this.isFumeFunnel(this.field_145851_c, this.field_145848_d, this.field_145849_e - 1, meta) ? 1 : 0;
            funnels += this.isFumeFunnel(this.field_145851_c, this.field_145848_d, this.field_145849_e + 1, meta) ? 1 : 0;
         }

         funnels += this.isFumeFunnel(this.field_145851_c, this.field_145848_d + 1, this.field_145849_e, meta) ? 1 : 0;
         return funnels;
      }

      private boolean isFumeFunnel(int xCoord, int yCoord, int zCoord, int meta) {
         Block block = this.field_145850_b.func_147439_a(xCoord, yCoord, zCoord);
         return (block == Witchery.Blocks.OVEN_FUMEFUNNEL || block == Witchery.Blocks.OVEN_FUMEFUNNEL_FILTERED) && this.field_145850_b.func_72805_g(xCoord, yCoord, zCoord) == meta;
      }

      private double getFumeFunnelsChance() {
         double funnels = 0.0D;
         switch(this.field_145850_b.func_72805_g(this.field_145851_c, this.field_145848_d, this.field_145849_e)) {
         case 2:
            funnels += this.getFumeFunnelChance(this.field_145851_c + 1, this.field_145848_d, this.field_145849_e, 2);
            funnels += this.getFumeFunnelChance(this.field_145851_c - 1, this.field_145848_d, this.field_145849_e, 2);
            break;
         case 3:
            funnels += this.getFumeFunnelChance(this.field_145851_c + 1, this.field_145848_d, this.field_145849_e, 3);
            funnels += this.getFumeFunnelChance(this.field_145851_c - 1, this.field_145848_d, this.field_145849_e, 3);
            break;
         case 4:
            funnels += this.getFumeFunnelChance(this.field_145851_c, this.field_145848_d, this.field_145849_e + 1, 4);
            funnels += this.getFumeFunnelChance(this.field_145851_c, this.field_145848_d, this.field_145849_e - 1, 4);
            break;
         case 5:
            funnels += this.getFumeFunnelChance(this.field_145851_c, this.field_145848_d, this.field_145849_e + 1, 5);
            funnels += this.getFumeFunnelChance(this.field_145851_c, this.field_145848_d, this.field_145849_e - 1, 5);
         }

         return funnels;
      }

      private double getFumeFunnelChance(int x, int y, int z, int meta) {
         Block block = this.field_145850_b.func_147439_a(x, y, z);
         if (block == Witchery.Blocks.OVEN_FUMEFUNNEL) {
            if (this.field_145850_b.func_72805_g(x, y, z) == meta) {
               return 0.25D;
            }
         } else if (block == Witchery.Blocks.OVEN_FUMEFUNNEL_FILTERED && this.field_145850_b.func_72805_g(x, y, z) == meta) {
            return Config.instance().doubleFumeFilterChance ? 0.8D : 0.3D;
         }

         return 0.0D;
      }

      private int getCookTime() {
         int time = 180 - 20 * this.getFumeFunnels();
         return time;
      }

      private void generateByProduct(ItemStack itemstack) {
         try {
            double BASE_CHANCE = 0.3D;
            double funnels = this.getFumeFunnelsChance();
            Log.instance().debug("" + this.furnaceItemStacks[0] + ": " + this.furnaceItemStacks[0].func_77973_b().func_77658_a());
            if (this.field_145850_b.field_73012_v.nextDouble() <= Math.min(0.3D + funnels, 1.0D) && this.furnaceItemStacks[4] != null) {
               if (this.furnaceItemStacks[0].func_77973_b() == Item.func_150898_a(Blocks.field_150345_g) && this.furnaceItemStacks[0].func_77960_j() != 3) {
                  switch(this.furnaceItemStacks[0].func_77960_j()) {
                  case 0:
                     this.createByProduct(Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(1));
                     break;
                  case 1:
                     this.createByProduct(Witchery.Items.GENERIC.itemHintOfRebirth.createStack(1));
                     break;
                  case 2:
                     this.createByProduct(Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(1));
                  }
               } else if (this.furnaceItemStacks[0].func_77973_b() == Item.func_150898_a(Witchery.Blocks.SAPLING)) {
                  switch(this.furnaceItemStacks[0].func_77960_j()) {
                  case 0:
                     this.createByProduct(Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(1));
                     break;
                  case 1:
                     this.createByProduct(Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(1));
                     break;
                  case 2:
                     this.createByProduct(Witchery.Items.GENERIC.itemOdourOfPurity.createStack(1));
                  }
               } else if (this.furnaceItemStacks[0].func_77977_a().equals("tile.bop.saplings") && this.furnaceItemStacks[0].func_77960_j() == 6) {
                  this.createByProduct(Witchery.Items.GENERIC.itemHintOfRebirth.createStack(1));
               } else if (this.furnaceItemStacks[0].func_77942_o() && this.furnaceItemStacks[0].func_77978_p().func_74764_b("Genome")) {
                  NBTBase tag = this.furnaceItemStacks[0].func_77978_p().func_74781_a("Genome");
                  if (tag != null && tag instanceof NBTTagCompound) {
                     NBTTagCompound compound = (NBTTagCompound)tag;
                     if (compound.func_74764_b("Chromosomes") && compound.func_74781_a("Chromosomes") instanceof NBTTagList) {
                        NBTTagList list = compound.func_150295_c("Chromosomes", 10);
                        if (list != null && list.func_74745_c() > 0) {
                           NBTBase chromoBase = list.func_150305_b(0);
                           if (chromoBase != null && chromoBase instanceof NBTTagCompound) {
                              NBTTagCompound chromosome = (NBTTagCompound)chromoBase;
                              if (chromosome.func_74764_b("UID0")) {
                                 String treeType = chromosome.func_74779_i("UID0");
                                 if (treeType != null) {
                                    Log.instance().debug("Forestry tree: " + treeType);
                                    if (treeType.equals("forestry.treeOak")) {
                                       this.createByProduct(Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(1));
                                    } else if (treeType.equals("forestry.treeSpruce")) {
                                       this.createByProduct(Witchery.Items.GENERIC.itemHintOfRebirth.createStack(1));
                                    } else if (treeType.equals("forestry.treeBirch")) {
                                       this.createByProduct(Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(1));
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } else {
                  this.createByProduct(Witchery.Items.GENERIC.itemFoulFume.createStack(1));
               }
            }
         } catch (Throwable var12) {
            Log.instance().warning(var12, "Exception occured while generating a by product from a witches oven");
         }

      }

      private void createByProduct(ItemStack byProduct) {
         int BY_PRODUCT_INDEX = true;
         if (this.furnaceItemStacks[3] == null) {
            this.furnaceItemStacks[3] = byProduct;
            if (--this.furnaceItemStacks[4].field_77994_a <= 0) {
               this.furnaceItemStacks[4] = null;
            }
         } else if (this.furnaceItemStacks[3].func_77969_a(byProduct) && this.furnaceItemStacks[3].field_77994_a + byProduct.field_77994_a < this.furnaceItemStacks[3].func_77976_d()) {
            ItemStack var10000 = this.furnaceItemStacks[3];
            var10000.field_77994_a += byProduct.field_77994_a;
            if (--this.furnaceItemStacks[4].field_77994_a <= 0) {
               this.furnaceItemStacks[4] = null;
            }
         }

      }

      public boolean func_70300_a(EntityPlayer par1EntityPlayer) {
         return this.field_145850_b.func_147438_o(this.field_145851_c, this.field_145848_d, this.field_145849_e) != this ? false : par1EntityPlayer.func_70092_e((double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.5D, (double)this.field_145849_e + 0.5D) <= 64.0D;
      }

      public void func_70295_k_() {
      }

      public void func_70305_f() {
      }

      public boolean func_94041_b(int slot, ItemStack itemstack) {
         if (slot != 2 && slot != 3) {
            if (slot == 1) {
               return TileEntityFurnace.func_145954_b(itemstack);
            } else if (slot == 4) {
               return Witchery.Items.GENERIC.itemEmptyClayJar.isMatch(itemstack);
            } else {
               return slot != 0 || !Witchery.Items.GENERIC.itemEmptyClayJar.isMatch(itemstack);
            }
         } else {
            return false;
         }
      }

      public int[] func_94128_d(int side) {
         return BlockSide.BOTTOM.isEqual(side) ? slots_bottom : (BlockSide.TOP.isEqual(side) ? slots_top : slots_sides);
      }

      public boolean func_102007_a(int slot, ItemStack itemstack, int par3) {
         return this.func_94041_b(slot, itemstack);
      }

      public boolean func_102008_b(int slot, ItemStack stack, int side) {
         if (BlockSide.TOP.isEqual(side)) {
            return false;
         } else if (BlockSide.BOTTOM.isEqual(side)) {
            return slot == 1 && stack.func_77973_b() == Items.field_151133_ar;
         } else {
            return slot == 3 || slot == 2;
         }
      }
   }
}
