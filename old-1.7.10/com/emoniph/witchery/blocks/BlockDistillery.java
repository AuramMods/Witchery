package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.crafting.DistilleryRecipes;
import com.emoniph.witchery.util.BlockSide;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SlotClayJar;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDistillery extends BlockBaseContainer {
   private final Random furnaceRand = new Random();
   private final boolean isActive;
   private static boolean keepFurnaceInventory;

   public BlockDistillery(boolean burning) {
      super(Material.field_151573_f, BlockDistillery.TileEntityDistillery.class);
      super.registerTileEntity = !burning;
      super.registerWithCreateTab = !burning;
      this.isActive = burning;
      this.func_149711_c(3.5F);
      this.func_149672_a(field_149777_j);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      if (burning) {
         this.func_149715_a(0.4F);
      }

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

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
      this.field_149761_L = par1IconRegister.func_94245_a(this.func_149641_N());
   }

   public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
      return Item.func_150898_a(Witchery.Blocks.DISTILLERY_IDLE);
   }

   public void func_149726_b(World par1World, int par2, int par3, int par4) {
      super.func_149726_b(par1World, par2, par3, par4);
      BlockUtil.setBlockDefaultDirection(par1World, par2, par3, par4);
   }

   public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
      if (par1World.field_72995_K) {
         return true;
      } else {
         BlockDistillery.TileEntityDistillery tileentityfurnace = (BlockDistillery.TileEntityDistillery)par1World.func_147438_o(par2, par3, par4);
         if (tileentityfurnace != null) {
            par5EntityPlayer.openGui(Witchery.instance, 3, par1World, par2, par3, par4);
         }

         return true;
      }
   }

   public static void updateDistilleryBlockState(boolean par0, World par1World, int par2, int par3, int par4) {
      int l = par1World.func_72805_g(par2, par3, par4);
      TileEntity tileentity = par1World.func_147438_o(par2, par3, par4);
      keepFurnaceInventory = true;
      if (par0) {
         par1World.func_147449_b(par2, par3, par4, Witchery.Blocks.DISTILLERY_BURNING);
      } else {
         par1World.func_147449_b(par2, par3, par4, Witchery.Blocks.DISTILLERY_IDLE);
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
         double d0 = (double)((float)par2 + 0.4F + par5Random.nextFloat() * 0.2F);
         double d1 = (double)((float)par3 + 1.0F + par5Random.nextFloat() * 0.3F);
         double d2 = (double)((float)par4 + 0.4F + par5Random.nextFloat() * 0.2F);
         par1World.func_72869_a(ParticleEffect.INSTANT_SPELL.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
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
         BlockDistillery.TileEntityDistillery tileentityfurnace = (BlockDistillery.TileEntityDistillery)par1World.func_147438_o(par2, par3, par4);
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
      return Container.func_94526_b((IInventory)par1World.func_147438_o(par2, par3, par4));
   }

   public Item func_149694_d(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_) {
      return Item.func_150898_a(Witchery.Blocks.DISTILLERY_IDLE);
   }

   public static class ContainerDistillery extends Container {
      private BlockDistillery.TileEntityDistillery furnace;
      private int lastCookTime;
      private int lastPowerLevel;

      public ContainerDistillery(InventoryPlayer par1InventoryPlayer, BlockDistillery.TileEntityDistillery par2TileEntityFurnace) {
         this.furnace = par2TileEntityFurnace;
         this.func_75146_a(new Slot(par2TileEntityFurnace, 0, 48, 16));
         this.func_75146_a(new Slot(par2TileEntityFurnace, 1, 48, 34));
         this.func_75146_a(new SlotClayJar(par2TileEntityFurnace, 2, 48, 54));
         this.func_75146_a(new SlotFurnace(par1InventoryPlayer.field_70458_d, par2TileEntityFurnace, 3, 110, 16));
         this.func_75146_a(new SlotFurnace(par1InventoryPlayer.field_70458_d, par2TileEntityFurnace, 4, 128, 16));
         this.func_75146_a(new SlotFurnace(par1InventoryPlayer.field_70458_d, par2TileEntityFurnace, 5, 110, 34));
         this.func_75146_a(new SlotFurnace(par1InventoryPlayer.field_70458_d, par2TileEntityFurnace, 6, 128, 34));

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
         par1ICrafting.func_71112_a(this, 1, this.furnace.powerLevel);
      }

      public void func_75142_b() {
         super.func_75142_b();

         for(int i = 0; i < this.field_75149_d.size(); ++i) {
            ICrafting icrafting = (ICrafting)this.field_75149_d.get(i);
            if (this.lastCookTime != this.furnace.furnaceCookTime) {
               icrafting.func_71112_a(this, 0, this.furnace.furnaceCookTime);
            }

            if (this.lastPowerLevel != this.furnace.powerLevel) {
               icrafting.func_71112_a(this, 1, this.furnace.powerLevel);
            }
         }

         this.lastCookTime = this.furnace.furnaceCookTime;
         this.lastPowerLevel = this.furnace.powerLevel;
      }

      @SideOnly(Side.CLIENT)
      public void func_75137_b(int par1, int par2) {
         if (par1 == 0) {
            this.furnace.furnaceCookTime = par2;
         }

         if (par1 == 1) {
            this.furnace.powerLevel = par2;
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
            if (slotIndex >= 3 && slotIndex <= 6) {
               if (!this.func_75135_a(itemstack1, 7, 43, true)) {
                  return null;
               }

               slot.func_75220_a(itemstack1, itemstack);
            } else if (slotIndex != 1 && slotIndex != 0 && slotIndex != 2) {
               if (FurnaceRecipes.func_77602_a().func_151395_a(itemstack1) != null) {
                  if (!this.func_75135_a(itemstack1, 0, 2, false)) {
                     return null;
                  }
               } else if (Witchery.Items.GENERIC.itemEmptyClayJar.isMatch(itemstack1)) {
                  if (!this.func_75135_a(itemstack1, 2, 3, false)) {
                     return null;
                  }
               } else if (slotIndex >= 7 && slotIndex < 34) {
                  if (!this.func_75135_a(itemstack1, 34, 43, false)) {
                     return null;
                  }
               } else if (slotIndex >= 34 && slotIndex < 43 && !this.func_75135_a(itemstack1, 7, 34, false)) {
                  return null;
               }
            } else if (!this.func_75135_a(itemstack1, 7, 43, false)) {
               return null;
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

   public static class TileEntityDistillery extends TileEntityBase implements ISidedInventory {
      private ItemStack[] furnaceItemStacks = new ItemStack[7];
      public int currentItemBurnTime;
      public int furnaceCookTime;
      public int powerLevel;
      static final int COOK_TIME = 800;
      Coord powerSourceCoord;
      static final int POWER_SOURCE_RADIUS = 16;
      static final float POWER_PER_TICK = 0.6F;
      private long lastUpdate = 0L;
      private boolean needUpdate = false;
      private static final int THROTTLE = 20;
      private static final int[] slots_top = new int[]{0, 1, 2};
      private static final int[] slots_bottom = new int[]{0, 1, 2};
      private static final int[] slots_sides = new int[]{0, 1, 2, 3, 4, 5, 6};

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

         this.furnaceCookTime = par1NBTTagCompound.func_74765_d("CookTime");
      }

      public void func_145841_b(NBTTagCompound par1NBTTagCompound) {
         super.func_145841_b(par1NBTTagCompound);
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
         return this.furnaceCookTime * par1 / 800;
      }

      IPowerSource getPowerSource() {
         if (this.powerSourceCoord != null && this.ticks % 100L != 0L) {
            TileEntity tileEntity = this.powerSourceCoord.getBlockTileEntity(this.field_145850_b);
            if (!(tileEntity instanceof BlockAltar.TileEntityAltar)) {
               return this.findNewPowerSource();
            } else {
               BlockAltar.TileEntityAltar altarTileEntity = (BlockAltar.TileEntityAltar)tileEntity;
               return (IPowerSource)(!altarTileEntity.isValid() ? this.findNewPowerSource() : altarTileEntity);
            }
         } else {
            return this.findNewPowerSource();
         }
      }

      private IPowerSource findNewPowerSource() {
         List<PowerSources.RelativePowerSource> sources = PowerSources.instance() != null ? PowerSources.instance().get(this.field_145850_b, new Coord(this), 16) : null;
         return sources != null && sources.size() > 0 ? ((PowerSources.RelativePowerSource)sources.get(0)).source() : null;
      }

      public void func_145845_h() {
         super.func_145845_h();
         boolean flag1 = false;
         if (!this.field_145850_b.field_72995_K) {
            boolean cooking = this.furnaceCookTime > 0;
            boolean powered = this.powerLevel > 0;
            IPowerSource powerSource;
            if (!this.canSmelt()) {
               if (this.ticks % 40L == 0L) {
                  powerSource = this.getPowerSource();
                  if (powerSource != null && !powerSource.isLocationEqual(this.powerSourceCoord)) {
                     this.powerSourceCoord = powerSource.getLocation();
                  }

                  this.powerLevel = powerSource == null ? 0 : 1;
               }

               this.furnaceCookTime = 0;
            } else {
               powerSource = this.getPowerSource();
               if (powerSource != null && !powerSource.isLocationEqual(this.powerSourceCoord)) {
                  this.powerSourceCoord = powerSource.getLocation();
               } else {
                  this.powerSourceCoord = null;
               }

               this.powerLevel = powerSource == null ? 0 : 1;
               if (powerSource != null && powerSource.consumePower(0.6F)) {
                  ++this.furnaceCookTime;
                  if (this.furnaceCookTime == 800) {
                     this.furnaceCookTime = 0;
                     this.smeltItem();
                     flag1 = true;
                  }
               } else {
                  this.powerLevel = 0;
               }
            }

            if (cooking != this.furnaceCookTime > 0) {
               BlockDistillery.updateDistilleryBlockState(this.furnaceCookTime > 0 && this.powerLevel > 0, this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
               this.lastUpdate = this.ticks;
               this.needUpdate = false;
            } else if (powered != this.powerLevel > 0) {
               if (this.ticks - this.lastUpdate <= 20L) {
                  this.needUpdate = true;
               } else {
                  BlockDistillery.updateDistilleryBlockState(this.furnaceCookTime > 0 && this.powerLevel > 0, this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
                  this.lastUpdate = this.ticks;
                  this.needUpdate = false;
               }
            } else if (this.needUpdate && this.ticks - this.lastUpdate > 20L) {
               BlockDistillery.updateDistilleryBlockState(this.furnaceCookTime > 0 && this.powerLevel > 0, this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e);
               this.lastUpdate = this.ticks;
               this.needUpdate = false;
            }

            if (flag1) {
               this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
            }
         }

      }

      private boolean canSmelt() {
         DistilleryRecipes.DistilleryRecipe recipe = this.getActiveRecipe();
         if (recipe == null) {
            return false;
         } else {
            ItemStack[] itemstacks = recipe.getOutputs();

            for(int i = 0; i < itemstacks.length; ++i) {
               ItemStack current = this.furnaceItemStacks[i + 3];
               if (itemstacks[i] != null && current != null && current.func_77969_a(itemstacks[i])) {
                  int newSize = current.field_77994_a + itemstacks[i].field_77994_a;
                  if (newSize > this.func_70297_j_() || newSize > current.func_77976_d()) {
                     return false;
                  }
               }
            }

            return true;
         }
      }

      public DistilleryRecipes.DistilleryRecipe getActiveRecipe() {
         if (this.furnaceItemStacks[0] == null && this.furnaceItemStacks[1] == null) {
            return null;
         } else {
            DistilleryRecipes.DistilleryRecipe recipe = DistilleryRecipes.instance().getDistillingResult(this.furnaceItemStacks[0], this.furnaceItemStacks[1], this.furnaceItemStacks[2]);
            return recipe;
         }
      }

      public void smeltItem() {
         if (this.canSmelt()) {
            DistilleryRecipes.DistilleryRecipe recipe = DistilleryRecipes.instance().getDistillingResult(this.furnaceItemStacks[0], this.furnaceItemStacks[1], this.furnaceItemStacks[2]);
            ItemStack[] itemstacks = recipe.getOutputs();

            ItemStack var10000;
            for(int i = 0; i < itemstacks.length; ++i) {
               int furnaceIndex = i + 3;
               if (itemstacks[i] != null) {
                  if (this.furnaceItemStacks[furnaceIndex] == null) {
                     this.furnaceItemStacks[furnaceIndex] = itemstacks[i].func_77946_l();
                  } else if (this.furnaceItemStacks[furnaceIndex].func_77969_a(itemstacks[i])) {
                     var10000 = this.furnaceItemStacks[furnaceIndex];
                     var10000.field_77994_a += itemstacks[i].field_77994_a;
                  }
               }
            }

            if (this.furnaceItemStacks[0] != null) {
               --this.furnaceItemStacks[0].field_77994_a;
               if (this.furnaceItemStacks[0].field_77994_a <= 0) {
                  this.furnaceItemStacks[0] = null;
               }
            }

            if (this.furnaceItemStacks[1] != null) {
               --this.furnaceItemStacks[1].field_77994_a;
               if (this.furnaceItemStacks[1].field_77994_a <= 0) {
                  this.furnaceItemStacks[1] = null;
               }
            }

            if (this.furnaceItemStacks[2] != null) {
               var10000 = this.furnaceItemStacks[2];
               var10000.field_77994_a -= recipe.getJars();
               if (this.furnaceItemStacks[2].field_77994_a <= 0) {
                  this.furnaceItemStacks[2] = null;
               }
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
         if (slot > 3) {
            return false;
         } else if (slot == 2) {
            return Witchery.Items.GENERIC.itemEmptyClayJar.isMatch(itemstack);
         } else {
            return !Witchery.Items.GENERIC.itemEmptyClayJar.isMatch(itemstack);
         }
      }

      public int[] func_94128_d(int side) {
         return BlockSide.BOTTOM.isEqual(side) ? slots_bottom : (BlockSide.TOP.isEqual(side) ? slots_top : slots_sides);
      }

      public boolean func_102007_a(int slot, ItemStack itemstack, int par3) {
         return this.func_94041_b(slot, itemstack);
      }

      public boolean func_102008_b(int slot, ItemStack itemstack, int side) {
         return side != 0 && side != 1 && (slot == 3 || slot == 4 || slot == 5 || slot == 6);
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
