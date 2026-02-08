package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.ritual.Circle;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RiteBindCircleToTalisman extends Rite {
   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteBindCircleToTalisman.StepSummonItem(this));
   }

   private static class StepSummonItem extends RitualStep {
      private final RiteBindCircleToTalisman rite;

      public StepSummonItem(RiteBindCircleToTalisman rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               Circle a = new Circle(16);
               Circle b = new Circle(28);
               Circle c = new Circle(40);
               Circle _ = new Circle(0);
               Circle[][] PATTERN = new Circle[][]{{_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _}, {_, _, _, _, _, c, c, c, c, c, c, c, _, _, _, _, _}, {_, _, _, _, c, _, _, _, _, _, _, _, c, _, _, _, _}, {_, _, _, c, _, _, b, b, b, b, b, _, _, c, _, _, _}, {_, _, c, _, _, b, _, _, _, _, _, b, _, _, c, _, _}, {_, c, _, _, b, _, _, a, a, a, _, _, b, _, _, c, _}, {_, c, _, b, _, _, a, _, _, _, a, _, _, b, _, c, _}, {_, c, _, b, _, a, _, _, _, _, _, a, _, b, _, c, _}, {_, c, _, b, _, a, _, _, _, _, _, a, _, b, _, c, _}, {_, c, _, b, _, a, _, _, _, _, _, a, _, b, _, c, _}, {_, c, _, b, _, _, a, _, _, _, a, _, _, b, _, c, _}, {_, c, _, _, b, _, _, a, a, a, _, _, b, _, _, c, _}, {_, _, c, _, _, b, _, _, _, _, _, b, _, _, c, _, _}, {_, _, _, c, _, _, b, b, b, b, b, _, _, c, _, _, _}, {_, _, _, _, c, _, _, _, _, _, _, _, c, _, _, _, _}, {_, _, _, _, _, c, c, c, c, c, c, c, _, _, _, _, _}, {_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _}};
               int offsetZ = (PATTERN.length - 1) / 2;

               int z;
               for(z = 0; z < PATTERN.length - 1; ++z) {
                  int worldZ = posZ - offsetZ + z;
                  int offsetX = (PATTERN[z].length - 1) / 2;

                  for(int x = 0; x < PATTERN[z].length; ++x) {
                     int worldX = posX - offsetX + x;
                     PATTERN[PATTERN.length - 1 - z][x].addGlyph(world, worldX, posY, worldZ, true);
                  }
               }

               z = c.getExclusiveMetadataValue() << 6 | b.getExclusiveMetadataValue() << 3 | a.getExclusiveMetadataValue();
               ItemStack itemstack = new ItemStack(Witchery.Items.CIRCLE_TALISMAN, 1, z);
               EntityItem entity = new EntityItem(world, (double)posX, (double)posY + 0.05D, (double)posZ, itemstack);
               world.func_72838_d(entity);
               ParticleEffect.PORTAL.send(SoundEffect.RANDOM_FIZZ, entity, 0.5D, 1.0D, 16);
               if (z > 0) {
                  world.func_147468_f(posX, posY, posZ);
               }
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
