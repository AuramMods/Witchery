package com.emoniph.witchery.blocks;

import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.predictions.PredictionManager;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCrystalBall extends BlockBaseContainer {
   private static final float ALTAR_POWER_PER_READING = 500.0F;
   private static final int POWER_SOURCE_RADIUS = 16;

   public BlockCrystalBall() {
      super(Material.field_151574_g, BlockCrystalBall.TileEntityCrystalBall.class);
      this.func_149711_c(2.0F);
      this.func_149672_a(field_149777_j);
      this.func_149676_a(0.3F, 0.0F, 0.3F, 0.7F, 0.6F, 0.7F);
   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      if (world.field_72995_K) {
         return true;
      } else {
         TileEntity tile = world.func_147438_o(x, y, z);
         if (tile != null && tile instanceof BlockCrystalBall.TileEntityCrystalBall) {
            BlockCrystalBall.TileEntityCrystalBall ball = (BlockCrystalBall.TileEntityCrystalBall)tile;
            if (ball.canBeUsed()) {
               if (tryConsumePower(world, player, x, y, z)) {
                  AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(0.5D + (double)x - 5.0D, 0.5D + (double)y - 2.0D, 0.5D + (double)z - 5.0D, 0.5D + (double)x + 5.0D, 0.5D + (double)y + 2.0D, 0.5D + (double)z + 5.0D);
                  List players = world.func_72872_a(EntityPlayer.class, bounds);
                  EntityPlayer victim = player;
                  double closest = 10000.0D;

                  for(int i = 0; i < players.size(); ++i) {
                     EntityPlayer nearbyPlayer = players.get(i) != null ? (EntityPlayer)players.get(i) : null;
                     if (nearbyPlayer != null && nearbyPlayer != player) {
                        double distSq = player.func_70092_e(0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z);
                        if (distSq < closest) {
                           victim = nearbyPlayer;
                           closest = distSq;
                        }
                     }
                  }

                  PredictionManager.instance().makePrediction(victim, player, true);
                  ball.onUsed();
                  ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_LEVELUP, world, 0.5D + (double)x, 0.2D + (double)y, 0.5D + (double)z, 0.2D, 0.2D, 16);
               }
            } else {
               ChatUtil.sendTranslated(EnumChatFormatting.GRAY, player, "witchery.prediction.recharging");
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }
         }

         return true;
      }
   }

   public static boolean tryConsumePower(World world, EntityPlayer player, int x, int y, int z) {
      IPowerSource powerSource = findNewPowerSource(world, x, y, z);
      if (powerSource != null && powerSource.consumePower(500.0F)) {
         return true;
      } else {
         if (!world.field_72995_K) {
            ChatUtil.sendTranslated(EnumChatFormatting.GRAY, player, "witchery.prediction.nopower");
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         }

         return false;
      }
   }

   private static IPowerSource findNewPowerSource(World world, int posX, int posY, int posZ) {
      List<PowerSources.RelativePowerSource> sources = PowerSources.instance() != null ? PowerSources.instance().get(world, new Coord(posX, posY, posZ), 16) : null;
      return sources != null && sources.size() > 0 ? ((PowerSources.RelativePowerSource)sources.get(0)).source() : null;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 1;
   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      this.func_111046_k(par1World, par2, par3, par4);
   }

   private boolean func_111046_k(World par1World, int par2, int par3, int par4) {
      if (!this.func_149718_j(par1World, par2, par3, par4)) {
         this.func_149697_b(par1World, par2, par3, par4, par1World.func_72805_g(par2, par3, par4), 0);
         par1World.func_147468_f(par2, par3, par4);
         return false;
      } else {
         return true;
      }
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

   public static class TileEntityCrystalBall extends TileEntity {
      private long lastUsedTime = 0L;

      public boolean canUpdate() {
         return false;
      }

      public void onUsed() {
         this.lastUsedTime = this.field_145850_b.func_82737_E();
      }

      public boolean canBeUsed() {
         return this.field_145850_b.func_82737_E() - this.lastUsedTime > 100L;
      }

      public void func_145841_b(NBTTagCompound nbtTag) {
         super.func_145841_b(nbtTag);
         nbtTag.func_74772_a("LastUsedTime", this.lastUsedTime);
      }

      public void func_145839_a(NBTTagCompound nbtTag) {
         super.func_145839_a(nbtTag);
         this.lastUsedTime = nbtTag.func_74763_f("LastUsedTime");
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
