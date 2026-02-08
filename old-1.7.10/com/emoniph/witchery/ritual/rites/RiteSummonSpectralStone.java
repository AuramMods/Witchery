package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.entity.EntitySummonedUndead;
import com.emoniph.witchery.item.ItemSpectralStone;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RiteRegistry;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class RiteSummonSpectralStone extends Rite {
   private final int radius;

   public RiteSummonSpectralStone(int radius) {
      this.radius = radius;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteSummonSpectralStone.StepSummonItem(this));
   }

   private static class StepSummonItem extends RitualStep {
      private final RiteSummonSpectralStone rite;

      public StepSummonItem(RiteSummonSpectralStone rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               int r = this.rite.radius;
               int r2 = r * r;
               AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)(posX - r), (double)(posY - r), (double)(posZ - r), (double)(posX + r), (double)(posY + r), (double)(posZ + r));
               List entities = world.func_72872_a(EntitySummonedUndead.class, bb);
               Class<? extends EntitySummonedUndead> entityType = null;
               int count = 0;
               Iterator i$ = entities.iterator();

               while(i$.hasNext()) {
                  Object obj = i$.next();
                  EntitySummonedUndead entity = (EntitySummonedUndead)obj;
                  if (entity.func_70092_e(0.5D + (double)posX, (double)posY, 0.5D + (double)posZ) <= (double)r2) {
                     Class<? extends EntitySummonedUndead> foundType = entity.getClass();
                     if (entityType == null) {
                        entityType = foundType;
                     }

                     if (entityType == foundType) {
                        ++count;
                        if (!world.field_72995_K) {
                           entity.func_70106_y();
                           ParticleEffect.PORTAL.send(SoundEffect.RANDOM_POP, entity, 1.0D, 2.0D, 16);
                        }

                        if (count >= 3) {
                           break;
                        }
                     }
                  }
               }

               if (count <= 0) {
                  RiteRegistry.RiteError("witchery.rite.missinglivingsacrifice", ritual.getInitiatingPlayerName(), world);
                  return RitualStep.Result.ABORTED_REFUND;
               }

               ItemStack stack = new ItemStack(Witchery.Items.SPECTRAL_STONE, 1, ItemSpectralStone.metaFromCreature(entityType, count));
               EntityItem entity = new EntityItem(world, 0.5D + (double)posX, (double)posY + 1.5D, 0.5D + (double)posZ, stack);
               entity.field_70159_w = 0.0D;
               entity.field_70181_x = 0.3D;
               entity.field_70179_y = 0.0D;
               world.func_72838_d(entity);
               ParticleEffect.SPELL.send(SoundEffect.RANDOM_FIZZ, entity, 0.5D, 0.5D, 16);
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
