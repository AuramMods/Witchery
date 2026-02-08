package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityWolfman;
import com.emoniph.witchery.infusion.infusions.InfusionInfernal;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBeartrap extends BlockBaseContainer {
   private final boolean silvered;

   public BlockBeartrap(boolean silvered) {
      super(Material.field_151573_f, BlockBeartrap.TileEntityBeartrap.class);
      this.silvered = silvered;
      this.func_149711_c(5.0F);
      this.func_149752_b(10.0F);
      this.func_149672_a(field_149777_j);
      float w = 0.3F;
      this.func_149676_a(0.19999999F, 0.01F, 0.19999999F, 0.8F, 0.1F, 0.8F);
   }

   public TileEntity func_149915_a(World world, int metadata) {
      BlockBeartrap.TileEntityBeartrap tile = new BlockBeartrap.TileEntityBeartrap(this.silvered);
      return tile;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public int func_149645_b() {
      return -1;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149646_a(IBlockAccess world, int x, int y, int z, int side) {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public void func_149689_a(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
      switch(MathHelper.func_76128_c((double)(entity.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3) {
      case 0:
         world.func_72921_c(x, y, z, 2, 2);
         break;
      case 1:
         world.func_72921_c(x, y, z, 5, 2);
         break;
      case 2:
         world.func_72921_c(x, y, z, 3, 2);
         break;
      case 3:
         world.func_72921_c(x, y, z, 4, 2);
      }

      if (!world.field_72995_K && entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         BlockBeartrap.TileEntityBeartrap tile = (BlockBeartrap.TileEntityBeartrap)BlockUtil.getTileEntity(world, x, y, z, BlockBeartrap.TileEntityBeartrap.class);
         if (tile != null) {
            tile.owner = player.func_146103_bH();
            tile.sprung = true;
            tile.markBlockForUpdate(false);
         }
      }

   }

   public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
      return this.silvered ? new ArrayList() : super.getDrops(world, x, y, z, metadata, fortune);
   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && entity instanceof EntityLivingBase) {
         EntityLivingBase living = (EntityLivingBase)entity;
         BlockBeartrap.TileEntityBeartrap tile = (BlockBeartrap.TileEntityBeartrap)BlockUtil.getTileEntity(world, x, y, z, BlockBeartrap.TileEntityBeartrap.class);
         if (tile != null && !tile.sprung && world.func_82737_E() > tile.setTime + 20L && (!this.silvered || CreatureUtil.isWerewolf(entity, false))) {
            AxisAlignedBB trapBounds = AxisAlignedBB.func_72330_a((double)x + this.field_149759_B, (double)y + this.field_149760_C, (double)z + this.field_149754_D, (double)x + this.field_149755_E, (double)y + this.field_149756_F, (double)z + this.field_149757_G);
            if (trapBounds.func_72326_a(entity.field_70121_D) && (!this.silvered || tile.tryTrapWolf(living))) {
               boolean isCreative = entity instanceof EntityPlayer && ((EntityPlayer)entity).field_71075_bZ.field_75098_d;
               if (!isCreative) {
                  living.func_70690_d(new PotionEffect(Witchery.Potions.PARALYSED.field_76415_H, TimeUtil.secsToTicks(30), 2, true));
               }

               living.func_70097_a(DamageSource.field_82728_o, 4.0F);
               ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_MANTRAP, world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, 0.25D, 0.5D, 16);
               tile.sprung = true;
               tile.markBlockForUpdate(true);
            }
         }
      }

   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      if (!world.field_72995_K) {
         BlockBeartrap.TileEntityBeartrap tile = (BlockBeartrap.TileEntityBeartrap)BlockUtil.getTileEntity(world, x, y, z, BlockBeartrap.TileEntityBeartrap.class);
         if (tile != null) {
            SoundEffect.WITCHERY_RANDOM_CLICK.playAtPlayer(world, player);
            tile.sprung = !tile.sprung;
            if (!tile.sprung) {
               tile.setTime = world.func_82737_E();
            }

            tile.markBlockForUpdate(false);
         }
      }

      return true;
   }

   @SideOnly(Side.CLIENT)
   public static boolean checkForHiddenTrap(EntityPlayer player, MovingObjectPosition mop) {
      if (mop != null && mop.field_72313_a == MovingObjectType.BLOCK && player.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d) == Witchery.Blocks.BEARTRAP) {
         BlockBeartrap.TileEntityBeartrap tile = (BlockBeartrap.TileEntityBeartrap)BlockUtil.getTileEntity(player.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, BlockBeartrap.TileEntityBeartrap.class);
         if (tile != null) {
            return !tile.isVisibleTo(player);
         }
      }

      return false;
   }

   public static class TileEntityBeartrap extends TileEntityBase {
      private final boolean silvered;
      private GameProfile owner;
      private boolean sprung = true;
      private long setTime = 0L;
      private long startTime = 0L;
      private UUID spawnedWolfID = null;
      private static final int MIN_LURE_TIME = TimeUtil.minsToTicks(1);
      private static final int LURE_EXTRA = TimeUtil.minsToTicks(1);

      public TileEntityBeartrap() {
         this.silvered = false;
      }

      public TileEntityBeartrap(boolean silvered) {
         this.silvered = silvered;
      }

      public boolean tryTrapWolf(EntityLivingBase living) {
         if (this.silvered && living instanceof EntityWolfman) {
            EntityWolfman wolf = (EntityWolfman)living;
            if (this.spawnedWolfID != null && wolf != null && wolf.getPersistentID().equals(this.spawnedWolfID)) {
               SoundEffect.WITCHERY_MOB_WOLFMAN_LORD.playAt((TileEntity)this, 1.0F);
               wolf.setInfectious();
               return true;
            }
         }

         return false;
      }

      public boolean isSprung() {
         return this.sprung;
      }

      public boolean canUpdate() {
         return this.silvered;
      }

      public void func_145845_h() {
         super.func_145845_h();
         if (!this.field_145850_b.field_72995_K && this.silvered && !this.sprung && this.spawnedWolfID == null && TimeUtil.secondsElapsed(10, this.ticks)) {
            if (this.baitFound() && CreatureUtil.isFullMoon(this.field_145850_b)) {
               long time = this.field_145850_b.func_82737_E();
               if (this.startTime > 0L) {
                  long activateTime = this.startTime;
                  if (time > activateTime && CreatureUtil.isFullMoon(this.field_145850_b)) {
                     EntityCreature creature = InfusionInfernal.spawnCreature(this.field_145850_b, EntityWolfman.class, this.field_145851_c, this.field_145848_d, this.field_145849_e, (EntityLivingBase)null, 16, 32, ParticleEffect.SMOKE, SoundEffect.WITCHERY_MOB_WOLFMAN_TALK);
                     if (creature != null) {
                        creature.func_110163_bv();
                        this.spawnedWolfID = creature.getPersistentID();
                     }
                  }
               } else {
                  this.startTime = time;
               }
            } else {
               this.startTime = 0L;
            }
         }

      }

      private boolean baitFound() {
         double R = 8.0D;
         double RSQ = 64.0D;
         boolean foundSheep = false;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(0.5D + (double)this.field_145851_c - 8.0D, 0.5D + (double)this.field_145848_d - 8.0D, 0.5D + (double)this.field_145849_e - 8.0D, 0.5D + (double)this.field_145851_c + 8.0D, 0.5D + (double)this.field_145848_d + 8.0D, 0.5D + (double)this.field_145849_e + 8.0D);
         List<EntitySheep> sheep = this.field_145850_b.func_72872_a(EntitySheep.class, bounds);
         Iterator i$ = sheep.iterator();

         while(i$.hasNext()) {
            EntitySheep aSheep = (EntitySheep)i$.next();
            if (aSheep.func_70092_e(0.5D + (double)this.field_145851_c, 0.5D + (double)this.field_145848_d, 0.5D + (double)this.field_145849_e) <= 64.0D && aSheep.func_110167_bD()) {
               foundSheep = true;
               break;
            }
         }

         boolean wolfaltar = this.field_145850_b.func_147439_a(this.field_145851_c + 1, this.field_145848_d, this.field_145849_e) == Witchery.Blocks.WOLF_ALTAR || this.field_145850_b.func_147439_a(this.field_145851_c - 1, this.field_145848_d, this.field_145849_e) == Witchery.Blocks.WOLF_ALTAR || this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e + 1) == Witchery.Blocks.WOLF_ALTAR || this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e - 1) == Witchery.Blocks.WOLF_ALTAR;
         return wolfaltar && foundSheep;
      }

      public boolean isVisibleTo(EntityPlayer player) {
         if (!this.sprung && this.owner != null && !this.silvered) {
            if (player == null) {
               return false;
            } else {
               return player.func_146103_bH().equals(this.owner);
            }
         } else {
            return true;
         }
      }

      public void func_145841_b(NBTTagCompound nbtRoot) {
         super.func_145841_b(nbtRoot);
         nbtRoot.func_74757_a("Sprung", this.sprung);
         nbtRoot.func_74772_a("WolftrapStart", this.startTime);
         if (this.spawnedWolfID != null) {
            nbtRoot.func_74772_a("WolfLeast", this.spawnedWolfID.getLeastSignificantBits());
            nbtRoot.func_74772_a("WolfMost", this.spawnedWolfID.getMostSignificantBits());
         }

         if (this.owner != null) {
            NBTTagCompound nbtPlayer = new NBTTagCompound();
            NBTUtil.func_152460_a(nbtPlayer, this.owner);
            nbtRoot.func_74782_a("Owner", nbtPlayer);
         }

      }

      public void func_145839_a(NBTTagCompound nbtRoot) {
         super.func_145839_a(nbtRoot);
         this.sprung = nbtRoot.func_74767_n("Sprung");
         this.startTime = nbtRoot.func_74763_f("WolftrapStart");
         if (nbtRoot.func_150297_b("Owner", 10)) {
            this.owner = NBTUtil.func_152459_a(nbtRoot.func_74775_l("Owner"));
         } else {
            this.owner = null;
         }

         if (nbtRoot.func_74764_b("WolfLeast") && nbtRoot.func_74764_b("WolfMost")) {
            this.spawnedWolfID = new UUID(nbtRoot.func_74763_f("WolfMost"), nbtRoot.func_74763_f("WolfLeast"));
         } else {
            this.spawnedWolfID = null;
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
