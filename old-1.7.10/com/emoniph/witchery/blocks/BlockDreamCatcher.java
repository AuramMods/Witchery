package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.util.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDreamCatcher extends BlockBaseContainer {
   public BlockDreamCatcher() {
      super(Material.field_151582_l, BlockDreamCatcher.TileEntityDreamCatcher.class);
      super.registerWithCreateTab = false;
      this.func_149649_H();
      this.func_149711_c(1.0F);
      this.func_149672_a(field_149766_f);
      float f = 0.25F;
      float f1 = 1.0F;
      this.func_149676_a(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int par2) {
      return Blocks.field_150344_f.func_149733_h(par1);
   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      return null;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB func_149633_g(World par1World, int par2, int par3, int par4) {
      this.func_149719_a(par1World, par2, par3, par4);
      return super.func_149633_g(par1World, par2, par3, par4);
   }

   public void func_149719_a(IBlockAccess par1IBlockAccess, int posX, int posY, int posZ) {
      int side = par1IBlockAccess.func_72805_g(posX, posY, posZ);
      float bottom = 0.28125F;
      float top = 0.78125F;
      float left = 0.0F;
      float width = 1.0F;
      float depth = 0.125F;
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      float minY = 0.0F;
      float maxY = 0.87F;
      float minX = 0.0F;
      float maxX = 0.08F;
      float minZ = 0.25F;
      float maxZ = 0.75F;
      if (side == 2) {
         this.func_149676_a(minZ, minY, 1.0F - minX, maxZ, maxY, 1.0F - maxX);
      } else if (side == 3) {
         this.func_149676_a(1.0F - maxZ, minY, minX, 1.0F - minZ, maxY, maxX);
      } else if (side == 4) {
         this.func_149676_a(1.0F - minX, minY, minZ, 1.0F - maxX, maxY, maxZ);
      } else if (side == 5) {
         this.func_149676_a(minX, minY, minZ, maxX, maxY, maxZ);
      }

   }

   public boolean func_149686_d() {
      return false;
   }

   public boolean func_149655_b(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
      return true;
   }

   public boolean func_149646_a(IBlockAccess iblockaccess, int i, int j, int k, int l) {
      return false;
   }

   public boolean func_149662_c() {
      return false;
   }

   public void func_149749_a(World world, int posX, int posY, int posZ, Block par5, int par6) {
      if (!world.field_72995_K) {
         TileEntity tileEntity = world.func_147438_o(posX, posY, posZ);
         if (tileEntity != null && tileEntity instanceof BlockDreamCatcher.TileEntityDreamCatcher) {
            BlockDreamCatcher.TileEntityDreamCatcher tileEntityDreamCatcher = (BlockDreamCatcher.TileEntityDreamCatcher)tileEntity;
            ItemGeneral.DreamWeave weave = tileEntityDreamCatcher.getWeave();
            if (weave != null) {
               world.func_72838_d(new EntityItem(world, (double)posX, (double)posY, (double)posZ, weave.createStack()));
            }
         }
      }

      super.func_149749_a(world, posX, posY, posZ, par5, par6);
   }

   public ArrayList<ItemStack> getDrops(World world, int posX, int posY, int posZ, int metadata, int fortune) {
      ArrayList<ItemStack> ret = new ArrayList();
      return ret;
   }

   public void func_149695_a(World world, int posX, int posY, int posZ, Block par5) {
      int metadata = world.func_72805_g(posX, posY, posZ);
      boolean flag = true;
      if (metadata == 2 && world.func_147439_a(posX, posY, posZ + 1).func_149688_o().func_76220_a()) {
         flag = false;
      }

      if (metadata == 3 && world.func_147439_a(posX, posY, posZ - 1).func_149688_o().func_76220_a()) {
         flag = false;
      }

      if (metadata == 4 && world.func_147439_a(posX + 1, posY, posZ).func_149688_o().func_76220_a()) {
         flag = false;
      }

      if (metadata == 5 && world.func_147439_a(posX - 1, posY, posZ).func_149688_o().func_76220_a()) {
         flag = false;
      }

      if (flag) {
         this.func_149697_b(world, posX, posY, posZ, world.func_72805_g(posX, posY, posZ), 0);
         world.func_147468_f(posX, posY, posZ);
      }

      super.func_149695_a(world, posX, posY, posZ, par5);
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
   }

   public static boolean causesNightmares(World world, int posX, int posY, int posZ) {
      TileEntity tileEntity = world.func_147438_o(posX, posY, posZ);
      if (tileEntity != null && tileEntity instanceof BlockDreamCatcher.TileEntityDreamCatcher) {
         BlockDreamCatcher.TileEntityDreamCatcher tileEntityDreamCatcher = (BlockDreamCatcher.TileEntityDreamCatcher)tileEntity;
         return tileEntityDreamCatcher.dreamWeave == Witchery.Items.GENERIC.itemDreamNightmare;
      } else {
         return false;
      }
   }

   public static boolean enhancesDreams(World world, int x, int y, int z) {
      TileEntity tileEntity = world.func_147438_o(x, y, z);
      if (tileEntity != null && tileEntity instanceof BlockDreamCatcher.TileEntityDreamCatcher) {
         BlockDreamCatcher.TileEntityDreamCatcher tileEntityDreamCatcher = (BlockDreamCatcher.TileEntityDreamCatcher)tileEntity;
         return tileEntityDreamCatcher.dreamWeave == Witchery.Items.GENERIC.itemDreamIntensity;
      } else {
         return false;
      }
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      TileEntity tileEntity = world.func_147438_o(x, y, z);
      if (tileEntity != null && tileEntity instanceof BlockDreamCatcher.TileEntityDreamCatcher) {
         BlockDreamCatcher.TileEntityDreamCatcher catcherEntity = (BlockDreamCatcher.TileEntityDreamCatcher)tileEntity;
         if (catcherEntity.getWeave() != null) {
            return catcherEntity.getWeave().createStack();
         }
      }

      return Witchery.Items.GENERIC.itemDreamMove.createStack();
   }

   public static class TileEntityDreamCatcher extends TileEntity {
      private boolean buffIfDay;
      private boolean buffIfNight;
      private ItemGeneral.DreamWeave dreamWeave;
      private static final String DREAM_WEAVE_KEY = "WITCWeaveID";

      public void setEffect(ItemGeneral.DreamWeave dreamWeave) {
         this.dreamWeave = dreamWeave;
         if (!this.field_145850_b.field_72995_K) {
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
         }

      }

      public ItemGeneral.DreamWeave getWeave() {
         return this.dreamWeave;
      }

      public void func_145845_h() {
         super.func_145845_h();
         if (!this.field_145850_b.field_72995_K && this.dreamWeave != null) {
            if (this.buffIfDay || this.buffIfNight) {
               boolean day = this.field_145850_b.func_72935_r();
               if (this.buffIfDay && day || this.buffIfNight && !day) {
                  boolean isDream = true;
                  boolean isEnhanced = false;
                  int r = true;
                  boolean done = false;

                  for(int y = this.field_145848_d - 5; y <= this.field_145848_d + 5 && !done; ++y) {
                     for(int x = this.field_145851_c - 5; x <= this.field_145851_c + 5 && !done; ++x) {
                        for(int z = this.field_145849_e - 5; z <= this.field_145849_e + 5 && !done; ++z) {
                           if ((y != this.field_145848_d || x != this.field_145851_c || z != this.field_145849_e) && this.field_145850_b.func_147439_a(x, y, z) == Witchery.Blocks.DREAM_CATCHER) {
                              if (BlockDreamCatcher.causesNightmares(this.field_145850_b, x, y, z)) {
                                 isDream = false;
                                 done = isEnhanced;
                              } else if (BlockDreamCatcher.enhancesDreams(this.field_145850_b, x, y, z)) {
                                 isEnhanced = true;
                                 done = !isDream;
                              }
                           }
                        }
                     }
                  }

                  AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)(this.field_145851_c - 5), (double)(this.field_145848_d - 5), (double)(this.field_145849_e - 5), (double)(this.field_145851_c + 5), (double)(this.field_145848_d + 5), (double)(this.field_145849_e + 5));
                  List<EntityPlayer> list = this.field_145850_b.func_72872_a(EntityPlayer.class, bounds);
                  Iterator i$ = list.iterator();

                  label82:
                  while(true) {
                     EntityPlayer player;
                     ExtendedPlayer playerEx;
                     do {
                        if (!i$.hasNext()) {
                           break label82;
                        }

                        player = (EntityPlayer)i$.next();
                        playerEx = ExtendedPlayer.get(player);
                     } while((!day || playerEx.isVampire()) && (day || !playerEx.isVampire()));

                     this.dreamWeave.applyEffect(player, isDream, isEnhanced);
                  }
               }

               this.buffIfDay = this.buffIfNight = false;
            }

            if (!this.buffIfDay && !this.buffIfNight && this.areAllPlayersAsleep(this.field_145850_b)) {
               this.buffIfDay = !this.field_145850_b.field_73011_w.isDaytime();
               this.buffIfNight = !this.buffIfDay;
            }
         }

      }

      private boolean areAllPlayersAsleep(World world) {
         Iterator iterator = world.field_73010_i.iterator();
         int sleepThreshold = MathHelper.func_76141_d(0.01F * (float)Config.instance().percentageOfPlayersSleepingForBuff * (float)world.field_73010_i.size());

         while(iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();
            if (entityplayer.func_70608_bn()) {
               --sleepThreshold;
               if (sleepThreshold <= 0) {
                  return true;
               }
            }
         }

         return false;
      }

      public void func_145841_b(NBTTagCompound nbtTag) {
         super.func_145841_b(nbtTag);
         if (this.dreamWeave != null) {
            nbtTag.func_74768_a("WITCWeaveID", this.dreamWeave.weaveID);
         }

      }

      public void func_145839_a(NBTTagCompound nbtTag) {
         super.func_145839_a(nbtTag);
         if (nbtTag.func_74764_b("WITCWeaveID")) {
            int dreamWeaveID = nbtTag.func_74762_e("WITCWeaveID");
            this.dreamWeave = (ItemGeneral.DreamWeave)Witchery.Items.GENERIC.weaves.get(dreamWeaveID);
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
   }
}
