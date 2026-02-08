package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.client.particle.NaturePowerFX;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.util.BlockSide;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.oredict.OreDictionary;

public class BlockAltar extends BlockBaseContainer {
   @SideOnly(Side.CLIENT)
   private IIcon blockIconTop;
   @SideOnly(Side.CLIENT)
   private IIcon blockIconJoined;
   @SideOnly(Side.CLIENT)
   private IIcon blockIconTopJoined;
   private static final int ELEMENTS_IN_COMPLETE_ALTAR = 6;

   public BlockAltar() {
      super(Material.field_151576_e, BlockAltar.TileEntityAltar.class);
      this.func_149711_c(2.0F);
   }

   public TileEntity func_149915_a(World world, int metadata) {
      return new BlockAltar.TileEntityAltar();
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int side, int metadata) {
      switch(BlockSide.fromInteger(side)) {
      case TOP:
         return metadata == 0 ? this.blockIconTop : this.blockIconTopJoined;
      case BOTTOM:
         return this.blockIconTop;
      default:
         return metadata == 0 ? this.field_149761_L : this.blockIconJoined;
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      int i = Minecraft.func_71410_x().field_71474_y.field_74362_aa;
      if (i != 2 && (i != 1 || rand.nextInt(3) != 0)) {
         if (world.func_72805_g(x, y, z) == 1) {
            int RADIUS = true;
            int VERT = true;
            int plantX = x - 16 + rand.nextInt(32) + 1;
            int plantY = y - 4 + rand.nextInt(8) + 1;
            int plantZ = z - 16 + rand.nextInt(32) + 1;
            Block block = world.func_147439_a(plantX, plantY, plantZ);
            if (block != null && (block instanceof BlockFlower || block instanceof BlockLeaves || block instanceof BlockCrops || block instanceof IPlantable)) {
               int dir_x = x - plantX;
               int dir_y = y - plantY;
               int dir_z = z - plantZ;
               double distance = Math.sqrt((double)(dir_x * dir_x + dir_y * dir_y + dir_z * dir_z));
               double speed = 0.25D;
               double factor = speed / distance;
               double vel_x = (double)dir_x * factor;
               double vel_y = (double)dir_y * factor;
               double vel_z = (double)dir_z * factor;
               NaturePowerFX sparkle = new NaturePowerFX(world, 0.5D + (double)plantX, 0.5D + (double)plantY, 0.5D + (double)plantZ);
               sparkle.setMaxAge((int)(distance / speed)).setGravity(0.0F).setScale(1.0F).func_70016_h(vel_x, vel_y, vel_z);
               sparkle.setCanMove(true);
               sparkle.func_70538_b(0.2F, 0.8F, 0.0F);
               Minecraft.func_71410_x().field_71452_i.func_78873_a(sparkle);
            }
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.field_149761_L = iconRegister.func_94245_a(this.func_149641_N());
      this.blockIconTop = iconRegister.func_94245_a(this.func_149641_N() + "_top");
      this.blockIconJoined = iconRegister.func_94245_a(this.func_149641_N() + "_joined");
      this.blockIconTopJoined = iconRegister.func_94245_a(this.func_149641_N() + "_joined_top");
   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
      BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)world.func_147438_o(x, y, z);
      if (tileEntity.isValidAndUpdate()) {
         player.openGui(Witchery.instance, 0, world, x, y, z);
         return true;
      } else {
         return false;
      }
   }

   public void func_149689_a(World world, int x, int y, int z, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
      super.func_149689_a(world, x, y, z, par5EntityLivingBase, par6ItemStack);
      this.updateMultiblock(world, x, y, z, (Coord)null);
   }

   public void func_149749_a(World world, int x, int y, int z, Block block, int par6) {
      this.updateMultiblock(world, x, y, z, new Coord(x, y, z));
      super.func_149749_a(world, x, y, z, block, par6);
   }

   public void func_149723_a(World world, int posX, int posY, int posZ, Explosion explosion) {
      BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)world.func_147438_o(posX, posY, posZ);
      this.updateMultiblock(world, posX, posY, posZ, (Coord)null);
   }

   public void func_149695_a(World world, int posX, int posY, int posZ, Block block) {
      TileEntity tileEntity = world.func_147438_o(posX, posY, posZ);
      if (tileEntity != null && tileEntity instanceof BlockAltar.TileEntityAltar && !world.field_72995_K) {
         BlockAltar.TileEntityAltar tileEntityAltar = (BlockAltar.TileEntityAltar)tileEntity;
         tileEntityAltar.updateCoreArtefacts();
      }

      super.func_149695_a(world, posX, posY, posZ, block);
   }

   private void updateMultiblock(World world, int x, int y, int z, Coord exclude) {
      if (!world.field_72995_K) {
         ArrayList<Coord> visited = new ArrayList();
         ArrayList<Coord> toVisit = new ArrayList();
         toVisit.add(new Coord(x, y, z));
         boolean valid = true;

         while(true) {
            Coord coord;
            int neighbours;
            do {
               if (toVisit.size() <= 0) {
                  coord = valid && visited.size() == 6 ? (Coord)visited.get(0) : null;
                  Iterator i$ = visited.iterator();

                  while(i$.hasNext()) {
                     Coord coord = (Coord)i$.next();
                     TileEntity te = coord.getBlockTileEntity(world);
                     if (te != null && te instanceof BlockAltar.TileEntityAltar) {
                        BlockAltar.TileEntityAltar tile = (BlockAltar.TileEntityAltar)te;
                        tile.setCore(coord);
                     }
                  }

                  if (exclude != null) {
                     TileEntity te = exclude.getBlockTileEntity(world);
                     if (te != null && te instanceof BlockAltar.TileEntityAltar) {
                        BlockAltar.TileEntityAltar tile = (BlockAltar.TileEntityAltar)te;
                        tile.setCore((Coord)null);
                        return;
                     }
                  }

                  return;
               }

               coord = (Coord)toVisit.get(0);
               toVisit.remove(0);
               neighbours = 0;
               Coord[] arr$ = new Coord[]{coord.north(), coord.south(), coord.east(), coord.west()};
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Coord newCoord = arr$[i$];
                  if (newCoord.getBlock(world) == this) {
                     if (!visited.contains(newCoord) && !toVisit.contains(newCoord)) {
                        toVisit.add(newCoord);
                     }

                     ++neighbours;
                  }
               }
            } while(coord.equals(exclude));

            if (neighbours < 2 || neighbours > 3) {
               valid = false;
            }

            visited.add(coord);
         }
      }
   }

   public static class TileEntityAltar extends TileEntityBase implements IPowerSource {
      private Coord core;
      private float power;
      private float maxPower;
      private int powerScale;
      private int rechargeScale;
      private int enhancementLevel;
      private int rangeScale = 1;
      long lastPowerUpdate = 0L;
      private static final int SCAN_DISTANCE = 14;
      private ArrayList<Block> extraNatureIDs = null;

      public boolean isPowerInvalid() {
         return this.func_145837_r();
      }

      protected void initiate() {
         super.initiate();
         if (!this.field_145850_b.field_72995_K && this.isCore()) {
            if (this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e) == Witchery.Blocks.ALTAR) {
               Log.instance().debug("Initiating altar tile at: " + this.field_145851_c + ", " + this.field_145848_d + ", " + this.field_145849_e);
               PowerSources.instance().registerPowerSource(this);
            } else {
               Log.instance().warning("Altar tile entity exists without a corresponding block at: " + this.field_145851_c + ", " + this.field_145848_d + ", " + this.field_145849_e);
            }
         }

      }

      public void func_145843_s() {
         super.func_145843_s();
         if (!this.field_145850_b.field_72995_K) {
            if (this.isCore()) {
               Log.instance().debug("Invalidating void bramble tile at: " + this.field_145851_c + ", " + this.field_145848_d + ", " + this.field_145849_e);
            }

            PowerSources.instance().removePowerSource(this);
         }

      }

      public void func_145845_h() {
         super.func_145845_h();
         if (!this.field_145850_b.field_72995_K) {
            float maxPowerScaled = this.maxPower * (float)this.powerScale;
            if (this.isCore()) {
               if (this.power < maxPowerScaled) {
                  float basePowerPerUpdate = 10.0F;
                  if (this.ticks % 20L == 0L) {
                     this.power = (float)((int)Math.min(this.power + 10.0F * (float)this.rechargeScale, maxPowerScaled));
                     this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
                  }
               } else if (this.power > maxPowerScaled && this.ticks % 20L == 0L) {
                  this.power = maxPowerScaled;
                  this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
               }
            }
         }

      }

      public float getRange() {
         return (float)(16 * this.rangeScale);
      }

      public int getEnhancementLevel() {
         return this.enhancementLevel;
      }

      public boolean isValidAndUpdate() {
         if (this.isValid() && !this.field_145850_b.field_72995_K) {
            TileEntity tile = this.core.getBlockTileEntity(this.field_145850_b);
            if (tile != null && tile instanceof BlockAltar.TileEntityAltar) {
               BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)tile;
               tileEntity.updateArtefacts();
               tileEntity.updatePower(true);
               return true;
            } else {
               return false;
            }
         } else {
            return this.isValid();
         }
      }

      public void func_145841_b(NBTTagCompound nbtTag) {
         if (this.core != null) {
            this.core.setNBT(nbtTag, "Core");
         }

         if (this.isCore()) {
            nbtTag.func_74776_a("Power", this.power);
            nbtTag.func_74776_a("MaxPower", this.maxPower);
            nbtTag.func_74768_a("PowerScale", this.powerScale);
            nbtTag.func_74768_a("RechargeScale", this.rechargeScale);
            nbtTag.func_74768_a("RangeScale", this.rangeScale);
            nbtTag.func_74768_a("EnhancementLevel", this.enhancementLevel);
         }

         super.func_145841_b(nbtTag);
      }

      public void func_145839_a(NBTTagCompound nbtTag) {
         this.core = Coord.createFrom(nbtTag, "Core");
         this.power = nbtTag.func_74760_g("Power");
         this.maxPower = nbtTag.func_74760_g("MaxPower");
         this.powerScale = nbtTag.func_74762_e("PowerScale");
         this.rechargeScale = nbtTag.func_74762_e("RechargeScale");
         if (nbtTag.func_74764_b("RangeScale")) {
            this.rangeScale = nbtTag.func_74762_e("RangeScale");
         } else {
            this.rangeScale = 1;
         }

         if (nbtTag.func_74764_b("EnhancementLevel")) {
            this.enhancementLevel = nbtTag.func_74762_e("EnhancementLevel");
         } else {
            this.enhancementLevel = 0;
         }

         super.func_145839_a(nbtTag);
      }

      private void setCore(Coord coord) {
         this.core = coord;
         if (this.isCore()) {
            this.updatePower(false);
            PowerSources.instance().registerPowerSource(this);
         }

         if (coord == null) {
            PowerSources.instance().removePowerSource(this);
            this.power = 0.0F;
            this.maxPower = 0.0F;
            this.powerScale = 1;
            this.rechargeScale = 1;
            this.rangeScale = 1;
            this.enhancementLevel = 0;
         }

         this.field_145850_b.func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, coord != null ? 1 : 0, 3);
         this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
      }

      private boolean isCore() {
         return this.core != null && this.core.isAtPosition(this);
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

      public float getCorePower() {
         if (this.core != null) {
            TileEntity te = this.core.getBlockTileEntity(this.field_145850_b);
            if (te != null && te instanceof BlockAltar.TileEntityAltar) {
               BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)te;
               return tileEntity.power;
            }
         }

         return 0.0F;
      }

      private void updateCoreArtefacts() {
         if (this.core != null) {
            TileEntity tile = this.core.getBlockTileEntity(this.field_145850_b);
            if (tile != null && tile instanceof BlockAltar.TileEntityAltar) {
               BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)tile;
               tileEntity.updateArtefacts();
            }
         }

      }

      public boolean consumePower(float requiredPower) {
         if (this.core != null) {
            BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)this.core.getBlockTileEntity(this.field_145850_b);
            if (tileEntity != null) {
               return tileEntity.consumeOurPower(requiredPower);
            }
         }

         return false;
      }

      private boolean consumeOurPower(float requiredPower) {
         if (!this.field_145850_b.field_72995_K && this.power >= requiredPower) {
            this.power -= requiredPower;
            return true;
         } else {
            return false;
         }
      }

      public float getCurrentPower() {
         if (this.core != null) {
            BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)this.core.getBlockTileEntity(this.field_145850_b);
            if (tileEntity != null) {
               return tileEntity.getOurCurrentPower();
            }
         }

         return -1.0F;
      }

      private float getOurCurrentPower() {
         return !this.field_145850_b.field_72995_K ? this.power : -2.0F;
      }

      public float getCoreMaxPower() {
         if (this.core != null) {
            TileEntity tile = this.core.getBlockTileEntity(this.field_145850_b);
            if (tile != null && tile instanceof BlockAltar.TileEntityAltar) {
               BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)tile;
               return tileEntity.maxPower * (float)tileEntity.powerScale;
            }
         }

         return 0.0F;
      }

      public int getCoreSpeed() {
         if (this.core != null) {
            TileEntity tile = this.core.getBlockTileEntity(this.field_145850_b);
            if (tile != null && tile instanceof BlockAltar.TileEntityAltar) {
               BlockAltar.TileEntityAltar tileEntity = (BlockAltar.TileEntityAltar)tile;
               return tileEntity.rechargeScale;
            }
         }

         return 0;
      }

      public boolean isValid() {
         return this.core != null;
      }

      public World getWorld() {
         return this.field_145850_b;
      }

      public Coord getLocation() {
         return new Coord(this);
      }

      public boolean isLocationEqual(Coord location) {
         return location != null && location.isAtPosition(this);
      }

      private void updatePower(boolean throttle) {
         if (!this.field_145850_b.field_72995_K && (!throttle || this.ticks - this.lastPowerUpdate <= 0L || this.ticks - this.lastPowerUpdate > 100L)) {
            this.lastPowerUpdate = this.ticks;
            Hashtable powerObjectTable = new Hashtable();

            Iterator iterator;
            ItemStack blockItem;
            Block block;
            try {
               iterator = OreDictionary.getOres("treeSapling").iterator();

               while(iterator.hasNext()) {
                  blockItem = (ItemStack)iterator.next();
                  block = Block.func_149634_a(blockItem.func_77973_b());
                  if (block != null) {
                     BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, block, 4, 20);
                  }
               }
            } catch (Exception var11) {
               Log.instance().warning(var11, "Exception occurred while creating power source list for sapling ores");
            }

            try {
               iterator = OreDictionary.getOres("logWood").iterator();

               while(iterator.hasNext()) {
                  blockItem = (ItemStack)iterator.next();
                  block = Block.func_149634_a(blockItem.func_77973_b());
                  if (block != null) {
                     BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, block, 2, 50);
                  }
               }
            } catch (Exception var10) {
               Log.instance().warning(var10, "Exception occurred while creating power source list for log ores");
            }

            try {
               iterator = OreDictionary.getOres("treeLeaves").iterator();

               while(iterator.hasNext()) {
                  blockItem = (ItemStack)iterator.next();
                  block = Block.func_149634_a(blockItem.func_77973_b());
                  if (block != null) {
                     BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, block, 3, 100);
                  }
               }
            } catch (Exception var9) {
               Log.instance().warning(var9, "Exception occurred while creating power source list for leaf ores");
            }

            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150349_c, 2, 80);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150346_d, 1, 80);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150458_ak, 1, 100);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150329_H, 3, 50);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150327_N, 4, 30);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150328_O, 4, 30);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150464_aj, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150355_j, 1, 50);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150337_Q, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150338_P, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150434_aF, 3, 50);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150436_aH, 3, 50);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150423_aK, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150393_bb, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150420_aW, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150419_aX, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150440_ba, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150394_bc, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150395_bd, 2, 50);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150391_bh, 1, 80);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150380_bt, 250, 1);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.DEMON_HEART, 40, 2);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150375_by, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150459_bM, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Blocks.field_150469_bN, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.CROP_BELLADONNA, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.CROP_MANDRAKE, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.CROP_ARTICHOKE, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.CROP_SNOWBELL, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.EMBER_MOSS, 4, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.LEAVES, 4, 50);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.LOG, 3, 100);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.SPANISH_MOSS, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.GLINT_WEED, 2, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.CRITTER_SNARE, 2, 10);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.BLOOD_ROSE, 2, 10);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.GRASSPER, 2, 10);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.WISPY_COTTON, 3, 20);
            BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, Witchery.Blocks.INFINITY_EGG, 1000, 1);
            Block block;
            if (this.extraNatureIDs == null) {
               try {
                  this.extraNatureIDs = new ArrayList();
                  iterator = Block.field_149771_c.iterator();

                  label112:
                  while(true) {
                     do {
                        if (!iterator.hasNext()) {
                           break label112;
                        }

                        block = (Block)iterator.next();
                     } while(!(block instanceof BlockFlower) && !(block instanceof BlockCrops));

                     if (!powerObjectTable.containsKey(block)) {
                        this.extraNatureIDs.add(block);
                        Log.instance().debug(block.func_149739_a());
                     }
                  }
               } catch (Exception var8) {
                  Log.instance().warning(var8, "Exception occurred while creating power source list for other mod flowers and crops");
               }
            }

            iterator = this.extraNatureIDs.iterator();

            while(iterator.hasNext()) {
               block = (Block)iterator.next();
               BlockAltar.TileEntityAltar.PowerSource.createInMap(powerObjectTable, block, 2, 4);
            }

            for(int y = this.field_145848_d - 14; y <= this.field_145848_d + 14; ++y) {
               for(int z = this.field_145849_e + 14; z >= this.field_145849_e - 14; --z) {
                  for(int x = this.field_145851_c - 14; x <= this.field_145851_c + 14; ++x) {
                     Block block = this.field_145850_b.func_147439_a(x, y, z);
                     BlockAltar.TileEntityAltar.PowerSource source = (BlockAltar.TileEntityAltar.PowerSource)powerObjectTable.get(block);
                     if (source != null) {
                        ++source.count;
                     }
                  }
               }
            }

            float newMax = 0.0F;

            BlockAltar.TileEntityAltar.PowerSource source;
            for(Iterator i$ = powerObjectTable.values().iterator(); i$.hasNext(); newMax += (float)source.getPower()) {
               source = (BlockAltar.TileEntityAltar.PowerSource)i$.next();
            }

            if (newMax != this.maxPower) {
               this.maxPower = newMax;
               this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
            }
         }

      }

      private void updateArtefacts() {
         ArrayList<Coord> visited = new ArrayList();
         ArrayList<Coord> toVisit = new ArrayList();
         toVisit.add(new Coord(this.field_145851_c, this.field_145848_d, this.field_145849_e));
         boolean headfound = false;
         boolean candlefound = false;
         boolean cupfound = false;
         boolean knifeFound = false;
         boolean wandFound = false;
         boolean pentacleFound = false;
         boolean infinityFound = false;
         int newPowerScale = 1;
         int newRechargeScale = 1;
         int newRangeScale = 1;
         int newEnhancementLevel = 0;

         while(true) {
            while(toVisit.size() > 0) {
               Coord coord = (Coord)toVisit.get(0);
               toVisit.remove(0);
               Coord[] arr$ = new Coord[]{coord.north(), coord.south(), coord.east(), coord.west()};
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Coord newCoord = arr$[i$];
                  if (newCoord.getBlock(this.field_145850_b) == Witchery.Blocks.ALTAR && !visited.contains(newCoord) && !toVisit.contains(newCoord)) {
                     toVisit.add(newCoord);
                  }
               }

               visited.add(coord);
               int offsetY = true;
               Block block = coord.getBlock(this.field_145850_b, 0, 1, 0);
               TileEntity tile;
               if (!headfound && block == Blocks.field_150465_bP) {
                  tile = coord.getBlockTileEntity(this.field_145850_b, 0, 1, 0);
                  if (tile != null && tile instanceof TileEntitySkull) {
                     TileEntitySkull skullTileEntity = (TileEntitySkull)tile;
                     switch(skullTileEntity.func_145904_a()) {
                     case 0:
                        ++newRechargeScale;
                        ++newPowerScale;
                        headfound = true;
                        break;
                     case 1:
                        newRechargeScale += 2;
                        newPowerScale += 2;
                        headfound = true;
                     case 2:
                     case 4:
                     default:
                        break;
                     case 3:
                        newRechargeScale += 3;
                        newPowerScale += 3;
                        headfound = true;
                     }
                  }
               } else if (!candlefound && block == Witchery.Blocks.CANDELABRA) {
                  candlefound = true;
                  newRechargeScale += 2;
               } else if (!candlefound && block == Blocks.field_150478_aa) {
                  candlefound = true;
                  ++newRechargeScale;
               } else if (block == Witchery.Blocks.PLACED_ITEMSTACK) {
                  tile = coord.getBlockTileEntity(this.field_145850_b, 0, 1, 0);
                  if (tile != null && tile instanceof BlockPlacedItem.TileEntityPlacedItem) {
                     BlockPlacedItem.TileEntityPlacedItem placeItem = (BlockPlacedItem.TileEntityPlacedItem)tile;
                     if (placeItem.getStack() != null) {
                        if (!knifeFound && placeItem.getStack().func_77973_b() == Witchery.Items.ARTHANA) {
                           knifeFound = true;
                           ++newRangeScale;
                        } else if (!wandFound && placeItem.getStack().func_77973_b() == Witchery.Items.MYSTIC_BRANCH) {
                           wandFound = true;
                           ++newEnhancementLevel;
                        } else if (!pentacleFound && Witchery.Items.GENERIC.itemKobolditePentacle.isMatch(placeItem.getStack())) {
                           pentacleFound = true;
                        }
                     }
                  }
               } else if (!cupfound && block == Witchery.Blocks.CHALICE) {
                  cupfound = true;
                  tile = coord.getBlockTileEntity(this.field_145850_b, 0, 1, 0);
                  if (tile != null && tile instanceof BlockChalice.TileEntityChalice) {
                     BlockChalice.TileEntityChalice tileEntityChalice = (BlockChalice.TileEntityChalice)tile;
                     newPowerScale += tileEntityChalice != null && tileEntityChalice.isFilled() ? 2 : 1;
                  }
               } else if (!infinityFound && block == Witchery.Blocks.INFINITY_EGG) {
                  infinityFound = true;
               }
            }

            if (pentacleFound) {
               newRechargeScale *= 2;
            }

            if (infinityFound) {
               newRechargeScale *= 10;
               newPowerScale *= 10;
            }

            if (newRechargeScale != this.rechargeScale || newPowerScale != this.powerScale || newRangeScale != this.rangeScale || newEnhancementLevel != this.enhancementLevel) {
               this.rechargeScale = newRechargeScale;
               this.powerScale = newPowerScale;
               this.rangeScale = newRangeScale;
               this.enhancementLevel = newEnhancementLevel;
               if (!this.field_145850_b.field_72995_K) {
                  this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
               }
            }

            return;
         }
      }

      static class PowerSource {
         private final Block block;
         private final int factor;
         private final int limit;
         private int count;

         public PowerSource(Block block, int factor, int limit) {
            this.block = block;
            this.factor = factor;
            this.limit = limit;
            this.count = 0;
         }

         public int getPower() {
            return Math.min(this.count, this.limit) * this.factor;
         }

         static void createInMap(Map<Block, BlockAltar.TileEntityAltar.PowerSource> map, Block block, int factor, int limit) {
            BlockAltar.TileEntityAltar.PowerSource source = new BlockAltar.TileEntityAltar.PowerSource(block, factor, limit);
            map.put(block, source);
         }
      }
   }
}
