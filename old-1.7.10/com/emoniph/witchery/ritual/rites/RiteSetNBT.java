package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class RiteSetNBT extends Rite {
   private final int radius;
   private final String nbtKey;
   private final int nbtValue;
   private final int nbtCovenBonus;

   public RiteSetNBT(int radius, String nbtKey, int value, int covenMemberBonus) {
      this.radius = radius;
      this.nbtKey = nbtKey;
      this.nbtValue = value;
      this.nbtCovenBonus = covenMemberBonus;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteSetNBT.StepSetNBT(this));
   }

   private static class StepSetNBT extends RitualStep {
      private final RiteSetNBT rite;

      public StepSetNBT(RiteSetNBT rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               int r = this.rite.radius;
               AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)(posX - r), (double)posY, (double)(posZ - r), (double)(posX + r), (double)(posY + 1), (double)(posZ + r));
               boolean bound = false;
               new ArrayList();
               Iterator i$ = world.func_72872_a(EntityPlayer.class, bounds).iterator();

               while(i$.hasNext()) {
                  Object obj = i$.next();
                  EntityPlayer player = (EntityPlayer)obj;
                  if (Coord.distance(player.field_70165_t, player.field_70163_u, player.field_70161_v, (double)posX, (double)posY, (double)posZ) <= (double)r) {
                     NBTTagCompound nbtPlayer = Infusion.getNBT(player);
                     if (nbtPlayer != null) {
                        nbtPlayer.func_74768_a(this.rite.nbtKey, this.rite.nbtValue + ritual.covenSize * this.rite.nbtCovenBonus);
                        bound = true;
                     }
                  }
               }

               if (!bound) {
                  return RitualStep.Result.ABORTED_REFUND;
               }

               ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_FIZZ, world, (double)posX, (double)posY, (double)posZ, 3.0D, 3.0D, 16);
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
