package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class RiteHellOnEarth extends RiteExpandingEffect {
   private final float upkeepCost;
   static final int POWER_SOURCE_RADIUS = 16;

   public RiteHellOnEarth(int radius, int height, float upkeepCost) {
      super(radius, height, true);
      this.upkeepCost = upkeepCost;
   }

   public boolean isComplete(World world, int posX, int posY, int posZ, int radius, EntityPlayer player, long ticks, boolean fullyExpanded, boolean enhanced) {
      if (fullyExpanded && ticks % 40L == 0L) {
         IPowerSource powerSource = this.findNewPowerSource(world, posX, posY, posZ);
         if (powerSource == null) {
            return true;
         }

         if (!powerSource.consumePower(this.upkeepCost)) {
            return true;
         }

         double roll = world.field_73012_v.nextDouble();
         EntityLiving entity = null;
         if (roll < 0.02D) {
            entity = new EntityDemon(world);
         } else if (roll < 0.1D) {
            entity = new EntityGhast(world);
         } else if (roll < 0.4D) {
            entity = new EntityBlaze(world);
         } else if (roll < 0.6D) {
            entity = new EntityMagmaCube(world);
         } else {
            entity = new EntityPigZombie(world);
         }

         if (entity != null) {
            ((EntityLiving)entity).func_110161_a((IEntityLivingData)null);
            ((EntityLiving)entity).func_70012_b(0.5D + (double)posX, 2.0D + (double)posY, 0.5D + (double)posZ, 0.0F, 0.0F);
            world.func_72838_d((Entity)entity);
            ParticleEffect.LARGE_EXPLODE.send(SoundEffect.MOB_BLAZE_DEATH, world, 0.5D + (double)posX, 2.0D + (double)posY, 0.5D + (double)posZ, 1.0D, 2.0D, 16);
         }
      }

      return false;
   }

   public boolean doRadiusAction(World world, int posX, int posY, int posZ, int radius, EntityPlayer player, boolean enhanced) {
      return true;
   }

   public void doBlockAction(World world, int posX, int posY, int posZ, int currentRadius, EntityPlayer player, boolean enhanced) {
      if (!world.field_72995_K) {
         Block blockID = world.func_147439_a(posX, posY, posZ);
         Block blockBelowID = world.func_147439_a(posX, posY - 1, posZ);
         if (blockID == Blocks.field_150329_H) {
            if (Config.instance().allowHellOnEarthFires && enhanced) {
               world.func_147449_b(posX, posY, posZ, Blocks.field_150480_ab);
            }

            this.blightGround(world, posX, posY - 1, posZ, blockBelowID, currentRadius);
         } else if (blockID != Blocks.field_150328_O && blockID != Blocks.field_150327_N && blockID != Blocks.field_150459_bM && blockID != Blocks.field_150464_aj && blockID != Blocks.field_150469_bN && blockID != Blocks.field_150393_bb && blockID != Blocks.field_150394_bc && blockID != Blocks.field_150440_ba && blockID != Blocks.field_150423_aK) {
            if (blockID.func_149688_o().func_76220_a()) {
               this.blightGround(world, posX, posY, posZ, blockID, currentRadius);
            } else if (blockBelowID.func_149688_o().func_76220_a()) {
               this.blightGround(world, posX, posY - 1, posZ, blockBelowID, currentRadius);
            }
         } else {
            if (Config.instance().allowHellOnEarthFires && enhanced) {
               world.func_147449_b(posX, posY, posZ, Blocks.field_150480_ab);
            }

            this.blightGround(world, posX, posY - 1, posZ, blockBelowID, currentRadius);
         }
      }

   }

   public void blightGround(World world, int posX, int posY, int posZ, Block blockBelowID, int currentRadius) {
      if (blockBelowID == Blocks.field_150346_d || blockBelowID == Blocks.field_150349_c || blockBelowID == Blocks.field_150391_bh || blockBelowID == Blocks.field_150458_ak || blockBelowID == Blocks.field_150354_m) {
         int rand = world.field_73012_v.nextInt(currentRadius < this.maxRadius / 3 ? 2 : (currentRadius < this.maxRadius / 2 ? 4 : 6));
         if (rand == 0) {
            world.func_147449_b(posX, posY, posZ, Blocks.field_150424_aL);
         }
      }

   }

   private IPowerSource findNewPowerSource(World world, int posX, int posY, int posZ) {
      List<PowerSources.RelativePowerSource> sources = PowerSources.instance() != null ? PowerSources.instance().get(world, new Coord(posX, posY, posZ), 16) : null;
      return sources != null && sources.size() > 0 ? ((PowerSources.RelativePowerSource)sources.get(0)).source() : null;
   }
}
