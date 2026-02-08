package com.emoniph.witchery.blocks;

import com.emoniph.witchery.util.ChatUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockLeechChest extends BlockBaseContainer {
   private final Random random = new Random();
   public final int chestType = 1;

   public BlockLeechChest() {
      super(Material.field_151576_e, BlockLeechChest.TileEntityLeechChest.class);
      this.func_149711_c(25.0F);
      this.func_149752_b(1000.0F);
      this.func_149676_a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return false;
   }

   public void func_149719_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
      if (par1IBlockAccess.func_147439_a(par2, par3, par4 - 1) == this) {
         this.func_149676_a(0.0625F, 0.0F, 0.0F, 0.9375F, 0.875F, 0.9375F);
      } else if (par1IBlockAccess.func_147439_a(par2, par3, par4 + 1) == this) {
         this.func_149676_a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 1.0F);
      } else if (par1IBlockAccess.func_147439_a(par2 - 1, par3, par4) == this) {
         this.func_149676_a(0.0F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
      } else if (par1IBlockAccess.func_147439_a(par2 + 1, par3, par4) == this) {
         this.func_149676_a(0.0625F, 0.0F, 0.0625F, 1.0F, 0.875F, 0.9375F);
      } else {
         this.func_149676_a(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
      }

   }

   public void func_149726_b(World par1World, int par2, int par3, int par4) {
      super.func_149726_b(par1World, par2, par3, par4);
   }

   public void func_149689_a(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
      Block l = par1World.func_147439_a(par2, par3, par4 - 1);
      Block i1 = par1World.func_147439_a(par2, par3, par4 + 1);
      Block j1 = par1World.func_147439_a(par2 - 1, par3, par4);
      Block k1 = par1World.func_147439_a(par2 + 1, par3, par4);
      byte b0 = 0;
      int l1 = MathHelper.func_76128_c((double)(par5EntityLivingBase.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
      if (l1 == 0) {
         b0 = 2;
      }

      if (l1 == 1) {
         b0 = 5;
      }

      if (l1 == 2) {
         b0 = 3;
      }

      if (l1 == 3) {
         b0 = 4;
      }

      if (l != this && i1 != this && j1 != this && k1 != this) {
         par1World.func_72921_c(par2, par3, par4, b0, 3);
      } else {
         if ((l == this || i1 == this) && (b0 == 4 || b0 == 5)) {
            if (l == this) {
               par1World.func_72921_c(par2, par3, par4 - 1, b0, 3);
            } else {
               par1World.func_72921_c(par2, par3, par4 + 1, b0, 3);
            }

            par1World.func_72921_c(par2, par3, par4, b0, 3);
         }

         if ((j1 == this || k1 == this) && (b0 == 2 || b0 == 3)) {
            if (j1 == this) {
               par1World.func_72921_c(par2 - 1, par3, par4, b0, 3);
            } else {
               par1World.func_72921_c(par2 + 1, par3, par4, b0, 3);
            }

            par1World.func_72921_c(par2, par3, par4, b0, 3);
         }
      }

      TileEntity tile = par1World.func_147438_o(par2, par3, par4);
      if (tile != null && tile instanceof BlockLeechChest.TileEntityLeechChest) {
         BlockLeechChest.TileEntityLeechChest chest = (BlockLeechChest.TileEntityLeechChest)tile;
         if (par6ItemStack.func_82837_s()) {
            chest.setChestGuiName(par6ItemStack.func_82833_r());
         }

         if (!par1World.field_72995_K && par6ItemStack.func_77942_o() && par6ItemStack.func_77978_p().func_74764_b("WITCPlayers")) {
            NBTTagList nbtPlayersList = par6ItemStack.func_77978_p().func_150295_c("WITCPlayers", 10);
            chest.players.clear();

            for(int i = 0; i < nbtPlayersList.func_74745_c(); ++i) {
               NBTTagCompound nbtPlayer = nbtPlayersList.func_150305_b(i);
               String s = nbtPlayer.func_74779_i("Player");
               if (s != null && !s.isEmpty()) {
                  chest.players.add(s);
               }
            }

            chest.sync();
         }
      }

   }

   public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
      return true;
   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      super.func_149695_a(par1World, par2, par3, par4, par5);
      TileEntity tile = par1World.func_147438_o(par2, par3, par4);
      if (tile != null && tile instanceof BlockLeechChest.TileEntityLeechChest) {
         BlockLeechChest.TileEntityLeechChest tileentitychest = (BlockLeechChest.TileEntityLeechChest)tile;
         tileentitychest.func_145836_u();
      }

   }

   public void func_149749_a(World par1World, int par2, int par3, int par4, Block par5, int par6) {
      TileEntity tileentity = par1World.func_147438_o(par2, par3, par4);
      if (tileentity != null && tileentity instanceof BlockLeechChest.TileEntityLeechChest) {
         BlockLeechChest.TileEntityLeechChest tileentitychest = (BlockLeechChest.TileEntityLeechChest)tileentity;

         for(int j1 = 0; j1 < tileentitychest.func_70302_i_(); ++j1) {
            ItemStack itemstack = tileentitychest.func_70301_a(j1);
            if (itemstack != null) {
               float f = this.random.nextFloat() * 0.8F + 0.1F;
               float f1 = this.random.nextFloat() * 0.8F + 0.1F;

               EntityItem entityitem;
               for(float f2 = this.random.nextFloat() * 0.8F + 0.1F; itemstack.field_77994_a > 0; par1World.func_72838_d(entityitem)) {
                  int k1 = this.random.nextInt(21) + 10;
                  if (k1 > itemstack.field_77994_a) {
                     k1 = itemstack.field_77994_a;
                  }

                  itemstack.field_77994_a -= k1;
                  entityitem = new EntityItem(par1World, (double)((float)par2 + f), (double)((float)par3 + f1), (double)((float)par4 + f2), new ItemStack(itemstack.func_77973_b(), k1, itemstack.func_77960_j()));
                  float f3 = 0.05F;
                  entityitem.field_70159_w = (double)((float)this.random.nextGaussian() * f3);
                  entityitem.field_70181_x = (double)((float)this.random.nextGaussian() * f3 + 0.2F);
                  entityitem.field_70179_y = (double)((float)this.random.nextGaussian() * f3);
                  if (itemstack.func_77942_o()) {
                     entityitem.func_92059_d().func_77982_d((NBTTagCompound)itemstack.func_77978_p().func_74737_b());
                  }
               }
            }
         }

         par1World.func_147453_f(par2, par3, par4, par5);
      }

      super.func_149749_a(par1World, par2, par3, par4, par5, par6);
   }

   public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
      ArrayList<ItemStack> drops = new ArrayList();
      return drops;
   }

   public void func_149681_a(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer) {
      if (!par1World.field_72995_K) {
         ItemStack itemstack = new ItemStack(this);
         TileEntity tileentity = par1World.func_147438_o(par2, par3, par4);
         if (tileentity != null && tileentity instanceof BlockLeechChest.TileEntityLeechChest) {
            BlockLeechChest.TileEntityLeechChest chest = (BlockLeechChest.TileEntityLeechChest)tileentity;
            if (chest.players.size() > 0) {
               itemstack.func_77982_d(new NBTTagCompound());
               NBTTagList nbtPlayers = new NBTTagList();

               for(int i = 0; i < chest.players.size(); ++i) {
                  NBTTagCompound nbtPlayer = new NBTTagCompound();
                  nbtPlayer.func_74778_a("Player", (String)chest.players.get(i));
                  nbtPlayers.func_74742_a(nbtPlayer);
               }

               itemstack.func_77978_p().func_74782_a("WITCPlayers", nbtPlayers);
            }
         }

         this.func_149642_a(par1World, par2, par3, par4, itemstack);
      }

   }

   public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer player, int par6, float par7, float par8, float par9) {
      if (par1World.field_72995_K) {
         return true;
      } else {
         IInventory iinventory = this.getInventory(par1World, par2, par3, par4);
         if (iinventory != null) {
            TileEntity tileEntity = par1World.func_147438_o(par2, par3, par4);
            if (tileEntity != null && tileEntity instanceof BlockLeechChest.TileEntityLeechChest) {
               BlockLeechChest.TileEntityLeechChest chest = (BlockLeechChest.TileEntityLeechChest)tileEntity;
               chest.storePlayer(player);
            }

            player.func_71007_a(iinventory);
         }

         return true;
      }
   }

   public IInventory getInventory(World par1World, int par2, int par3, int par4) {
      Object object = par1World.func_147438_o(par2, par3, par4);
      if (object == null) {
         return null;
      } else if (par1World.isSideSolid(par2, par3 + 1, par4, ForgeDirection.DOWN)) {
         return null;
      } else if (isOcelotBlockingChest(par1World, par2, par3, par4)) {
         return null;
      } else if (par1World.func_147439_a(par2 - 1, par3, par4) == this && (par1World.isSideSolid(par2 - 1, par3 + 1, par4, ForgeDirection.DOWN) || isOcelotBlockingChest(par1World, par2 - 1, par3, par4))) {
         return null;
      } else if (par1World.func_147439_a(par2 + 1, par3, par4) == this && (par1World.isSideSolid(par2 + 1, par3 + 1, par4, ForgeDirection.DOWN) || isOcelotBlockingChest(par1World, par2 + 1, par3, par4))) {
         return null;
      } else if (par1World.func_147439_a(par2, par3, par4 - 1) != this || !par1World.isSideSolid(par2, par3 + 1, par4 - 1, ForgeDirection.DOWN) && !isOcelotBlockingChest(par1World, par2, par3, par4 - 1)) {
         return par1World.func_147439_a(par2, par3, par4 + 1) != this || !par1World.isSideSolid(par2, par3 + 1, par4 + 1, ForgeDirection.DOWN) && !isOcelotBlockingChest(par1World, par2, par3, par4 + 1) ? (IInventory)object : null;
      } else {
         return null;
      }
   }

   public boolean func_149744_f() {
      return this.chestType == 1;
   }

   public int func_149709_b(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      if (!this.func_149744_f()) {
         return 0;
      } else {
         TileEntity tile = par1IBlockAccess.func_147438_o(par2, par3, par4);
         if (tile != null && tile instanceof BlockLeechChest.TileEntityLeechChest) {
            int i1 = ((BlockLeechChest.TileEntityLeechChest)tile).numUsingPlayers;
            return MathHelper.func_76125_a(i1, 0, 15);
         } else {
            return 0;
         }
      }
   }

   public int func_149748_c(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return par5 == 1 ? this.func_149709_b(par1IBlockAccess, par2, par3, par4, par5) : 0;
   }

   public static boolean isOcelotBlockingChest(World par0World, int par1, int par2, int par3) {
      Iterator iterator = par0World.func_72872_a(EntityOcelot.class, AxisAlignedBB.func_72330_a((double)par1, (double)(par2 + 1), (double)par3, (double)(par1 + 1), (double)(par2 + 2), (double)(par3 + 1))).iterator();

      while(iterator.hasNext()) {
         EntityOcelot entityocelot1 = (EntityOcelot)iterator.next();
         if (entityocelot1.func_70906_o()) {
            return true;
         }
      }

      return false;
   }

   public boolean func_149740_M() {
      return true;
   }

   public int func_149736_g(World par1World, int par2, int par3, int par4, int par5) {
      return Container.func_94526_b(this.getInventory(par1World, par2, par3, par4));
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
      this.field_149761_L = par1IconRegister.func_94245_a("planks_oak");
   }

   public static class TileEntityLeechChest extends TileEntity implements IInventory {
      private ItemStack[] chestContents = new ItemStack[36];
      public boolean adjacentChestChecked;
      public TileEntity adjacentChestZNeg;
      public TileEntity adjacentChestXPos;
      public TileEntity adjacentChestXNeg;
      public TileEntity adjacentChestZPosition;
      public float lidAngle;
      public float prevLidAngle;
      public int numUsingPlayers;
      private int ticksSinceSync;
      private int cachedChestType;
      private String customName;
      public ArrayList<String> players = new ArrayList();

      public TileEntityLeechChest() {
         this.cachedChestType = -1;
      }

      public void sync() {
         this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
      }

      public void storePlayer(EntityPlayer player) {
         if (!this.field_145850_b.field_72995_K && player != null && !this.players.contains(player.func_70005_c_())) {
            this.players.add(player.func_70005_c_());

            while(this.players.size() > 3) {
               this.players.remove(0);
            }

            this.sync();
         }

      }

      public String popUserExcept(EntityPlayer usingPlayer) {
         String missingPlayers = "";

         for(int i = this.players.size() - 1; i >= 0; --i) {
            String foundPlayerName = (String)this.players.get(i);
            if (!foundPlayerName.equals(usingPlayer.func_70005_c_())) {
               if (usingPlayer.field_70170_p.func_72924_a(foundPlayerName) != null) {
                  this.players.remove(i);
                  this.sync();
                  return foundPlayerName;
               }

               missingPlayers = missingPlayers + foundPlayerName + " ";
            } else if (this.players.size() == 1) {
               ChatUtil.sendTranslated(EnumChatFormatting.RED, usingPlayer, "tile.witcheryLeechChest.onlyowntaglock");
               return null;
            }
         }

         if (!missingPlayers.isEmpty()) {
            ChatUtil.sendTranslated(EnumChatFormatting.RED, usingPlayer, "tile.witcheryLeechChest.playernotloggedin", missingPlayers);
         }

         return null;
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

      @SideOnly(Side.CLIENT)
      public TileEntityLeechChest(int par1) {
         this.cachedChestType = par1;
      }

      public int func_70302_i_() {
         return 27;
      }

      public ItemStack func_70301_a(int par1) {
         return this.chestContents[par1];
      }

      public ItemStack func_70298_a(int par1, int par2) {
         if (this.chestContents[par1] != null) {
            ItemStack itemstack;
            if (this.chestContents[par1].field_77994_a <= par2) {
               itemstack = this.chestContents[par1];
               this.chestContents[par1] = null;
               this.func_70296_d();
               return itemstack;
            } else {
               itemstack = this.chestContents[par1].func_77979_a(par2);
               if (this.chestContents[par1].field_77994_a == 0) {
                  this.chestContents[par1] = null;
               }

               this.func_70296_d();
               return itemstack;
            }
         } else {
            return null;
         }
      }

      public ItemStack func_70304_b(int par1) {
         if (this.chestContents[par1] != null) {
            ItemStack itemstack = this.chestContents[par1];
            this.chestContents[par1] = null;
            return itemstack;
         } else {
            return null;
         }
      }

      public void func_70299_a(int par1, ItemStack par2ItemStack) {
         this.chestContents[par1] = par2ItemStack;
         if (par2ItemStack != null && par2ItemStack.field_77994_a > this.func_70297_j_()) {
            par2ItemStack.field_77994_a = this.func_70297_j_();
         }

         this.func_70296_d();
      }

      public String func_145825_b() {
         return this.func_145818_k_() ? this.customName : "container.chest";
      }

      public boolean func_145818_k_() {
         return this.customName != null && this.customName.length() > 0;
      }

      public void setChestGuiName(String par1Str) {
         this.customName = par1Str;
      }

      public void func_145839_a(NBTTagCompound par1NBTTagCompound) {
         super.func_145839_a(par1NBTTagCompound);
         NBTTagList nbtItemsList = par1NBTTagCompound.func_150295_c("Items", 10);
         this.chestContents = new ItemStack[this.func_70302_i_()];
         if (par1NBTTagCompound.func_74764_b("CustomName")) {
            this.customName = par1NBTTagCompound.func_74779_i("CustomName");
         }

         for(int i = 0; i < nbtItemsList.func_74745_c(); ++i) {
            NBTTagCompound nbtItem = nbtItemsList.func_150305_b(i);
            int j = nbtItem.func_74771_c("Slot") & 255;
            if (j >= 0 && j < this.chestContents.length) {
               this.chestContents[j] = ItemStack.func_77949_a(nbtItem);
            }
         }

         this.players.clear();
         NBTTagList nbtPlayersList = par1NBTTagCompound.func_150295_c("WITCPlayers", 10);

         for(int i = 0; i < nbtPlayersList.func_74745_c(); ++i) {
            NBTTagCompound nbtPlayer = nbtPlayersList.func_150305_b(i);
            String s = nbtPlayer.func_74779_i("Player");
            if (s != null && !s.isEmpty()) {
               this.players.add(s);
            }
         }

      }

      public void func_145841_b(NBTTagCompound nbtTag) {
         super.func_145841_b(nbtTag);
         NBTTagList nbtItemsList = new NBTTagList();

         for(int i = 0; i < this.chestContents.length; ++i) {
            if (this.chestContents[i] != null) {
               NBTTagCompound nbtItem = new NBTTagCompound();
               nbtItem.func_74774_a("Slot", (byte)i);
               this.chestContents[i].func_77955_b(nbtItem);
               nbtItemsList.func_74742_a(nbtItem);
            }
         }

         nbtTag.func_74782_a("Items", nbtItemsList);
         if (this.func_145818_k_()) {
            nbtTag.func_74778_a("CustomName", this.customName);
         }

         NBTTagList nbtPlayers = new NBTTagList();

         for(int i = 0; i < this.players.size(); ++i) {
            NBTTagCompound nbtPlayer = new NBTTagCompound();
            nbtPlayer.func_74778_a("Player", (String)this.players.get(i));
            nbtPlayers.func_74742_a(nbtPlayer);
         }

         nbtTag.func_74782_a("WITCPlayers", nbtPlayers);
      }

      public int func_70297_j_() {
         return 64;
      }

      public boolean func_70300_a(EntityPlayer par1EntityPlayer) {
         return this.field_145850_b.func_147438_o(this.field_145851_c, this.field_145848_d, this.field_145849_e) != this ? false : par1EntityPlayer.func_70092_e((double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.5D, (double)this.field_145849_e + 0.5D) <= 64.0D;
      }

      public void func_145836_u() {
         super.func_145836_u();
         this.adjacentChestChecked = false;
      }

      public void func_145845_h() {
         super.func_145845_h();
         ++this.ticksSinceSync;
         float f;
         if (!this.field_145850_b.field_72995_K && this.numUsingPlayers != 0 && (this.ticksSinceSync + this.field_145851_c + this.field_145848_d + this.field_145849_e) % 200 == 0) {
            this.numUsingPlayers = 0;
            f = 5.0F;
            List list = this.field_145850_b.func_72872_a(EntityPlayer.class, AxisAlignedBB.func_72330_a((double)((float)this.field_145851_c - f), (double)((float)this.field_145848_d - f), (double)((float)this.field_145849_e - f), (double)((float)(this.field_145851_c + 1) + f), (double)((float)(this.field_145848_d + 1) + f), (double)((float)(this.field_145849_e + 1) + f)));
            Iterator iterator = list.iterator();

            while(iterator.hasNext()) {
               EntityPlayer entityplayer = (EntityPlayer)iterator.next();
               if (entityplayer.field_71070_bA instanceof ContainerChest) {
                  IInventory iinventory = ((ContainerChest)entityplayer.field_71070_bA).func_85151_d();
                  if (iinventory == this) {
                     ++this.numUsingPlayers;
                  }
               }
            }
         }

         this.prevLidAngle = this.lidAngle;
         f = 0.1F;
         double d0;
         if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F) {
            double d1 = (double)this.field_145851_c + 0.5D;
            d0 = (double)this.field_145849_e + 0.5D;
            this.field_145850_b.func_72908_a(d1, (double)this.field_145848_d + 0.5D, d0, "random.chestopen", 0.5F, this.field_145850_b.field_73012_v.nextFloat() * 0.1F + 0.9F);
         }

         if (this.numUsingPlayers == 0 && this.lidAngle > 0.0F || this.numUsingPlayers > 0 && this.lidAngle < 1.0F) {
            float f1 = this.lidAngle;
            if (this.numUsingPlayers > 0) {
               this.lidAngle += f;
            } else {
               this.lidAngle -= f;
            }

            if (this.lidAngle > 1.0F) {
               this.lidAngle = 1.0F;
            }

            float f2 = 0.5F;
            if (this.lidAngle < f2 && f1 >= f2) {
               d0 = (double)this.field_145851_c + 0.5D;
               double d2 = (double)this.field_145849_e + 0.5D;
               this.field_145850_b.func_72908_a(d0, (double)this.field_145848_d + 0.5D, d2, "random.chestclosed", 0.5F, this.field_145850_b.field_73012_v.nextFloat() * 0.1F + 0.9F);
            }

            if (this.lidAngle < 0.0F) {
               this.lidAngle = 0.0F;
            }
         }

      }

      public boolean func_145842_c(int par1, int par2) {
         if (par1 == 1) {
            this.numUsingPlayers = par2;
            return true;
         } else {
            return super.func_145842_c(par1, par2);
         }
      }

      public void func_70295_k_() {
         if (this.numUsingPlayers < 0) {
            this.numUsingPlayers = 0;
         }

         ++this.numUsingPlayers;
         this.field_145850_b.func_147452_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, this.func_145838_q(), 1, this.numUsingPlayers);
         this.field_145850_b.func_147459_d(this.field_145851_c, this.field_145848_d, this.field_145849_e, this.func_145838_q());
         this.field_145850_b.func_147459_d(this.field_145851_c, this.field_145848_d - 1, this.field_145849_e, this.func_145838_q());
      }

      public void func_70305_f() {
         if (this.func_145838_q() != null && this.func_145838_q() instanceof BlockLeechChest) {
            --this.numUsingPlayers;
            this.field_145850_b.func_147452_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, this.func_145838_q(), 1, this.numUsingPlayers);
            this.field_145850_b.func_147459_d(this.field_145851_c, this.field_145848_d, this.field_145849_e, this.func_145838_q());
            this.field_145850_b.func_147459_d(this.field_145851_c, this.field_145848_d - 1, this.field_145849_e, this.func_145838_q());
         }

      }

      public boolean func_94041_b(int par1, ItemStack par2ItemStack) {
         return true;
      }

      public void func_145843_s() {
         super.func_145843_s();
         this.func_145836_u();
      }

      public int getChestType() {
         if (this.cachedChestType == -1) {
            if (this.field_145850_b == null || !(this.func_145838_q() instanceof BlockLeechChest)) {
               return 0;
            }

            this.cachedChestType = ((BlockLeechChest)this.func_145838_q()).chestType;
         }

         return this.cachedChestType;
      }
   }
}
