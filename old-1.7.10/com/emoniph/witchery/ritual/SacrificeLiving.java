package com.emoniph.witchery.ritual;

import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.util.Const;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SacrificeLiving extends Sacrifice {
   final Class<? extends EntityLiving> entityLivingClass;

   public SacrificeLiving(Class<? extends EntityLiving> entityLivingClass) {
      this.entityLivingClass = entityLivingClass;
   }

   public void addDescription(StringBuffer sb) {
      String s = (String)EntityList.field_75626_c.get(this.entityLivingClass);
      if (s == null) {
         s = "generic";
      }

      sb.append("§8>§0 ");
      sb.append(StatCollector.func_74838_a("entity." + s + ".name"));
      sb.append(Const.BOOK_NEWLINE);
   }

   public boolean isMatch(World world, int posX, int posY, int posZ, int maxDistance, ArrayList<Entity> entities, ArrayList<ItemStack> grassperStacks) {
      return true;
   }

   public void addSteps(ArrayList<RitualStep> steps, AxisAlignedBB bounds, int maxDistance) {
      steps.add(new SacrificeLiving.StepSacrificeLiving(this, bounds, maxDistance));
   }

   private static class StepSacrificeLiving extends RitualStep {
      private final SacrificeLiving sacrifice;
      private final AxisAlignedBB bounds;
      private final int maxDistance;

      public StepSacrificeLiving(SacrificeLiving sacrifice, AxisAlignedBB bounds, int maxDistance) {
         super(false);
         this.sacrifice = sacrifice;
         this.bounds = bounds;
         this.maxDistance = maxDistance + 1;
      }

      public RitualStep.Result process(World worldObj, int xCoord, int yCoord, int zCoord, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            Iterator i$ = worldObj.func_72872_a(EntityLiving.class, this.bounds).iterator();

            EntityLiving entity;
            do {
               if (!i$.hasNext()) {
                  RiteRegistry.RiteError("witchery.rite.missinglivingsacrifice", ritual.getInitiatingPlayerName(), worldObj);
                  return RitualStep.Result.ABORTED_REFUND;
               }

               Object obj = i$.next();
               entity = (EntityLiving)obj;
            } while(!this.sacrifice.entityLivingClass.isInstance(entity) || !(Sacrifice.distance((double)xCoord, (double)yCoord, (double)zCoord, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v) <= (double)this.maxDistance));

            if (!worldObj.field_72995_K) {
               entity.func_70106_y();
               ParticleEffect.PORTAL.send(SoundEffect.RANDOM_POP, entity, 1.0D, 2.0D, 16);
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
