package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.TimeUtil;
import java.util.ArrayList;
import java.util.Hashtable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class RiteEclipse extends Rite {
   private static Hashtable<Integer, Long> lastEclipseTimes = new Hashtable();

   public void addSteps(ArrayList<RitualStep> steps, int initialStage) {
      steps.add(new RiteEclipse.StepEclipse(this, initialStage));
   }

   private static class StepEclipse extends RitualStep {
      private final RiteEclipse rite;
      private int stage;

      public StepEclipse(RiteEclipse rite, int initialStage) {
         super(false);
         this.rite = rite;
         this.stage = initialStage;
      }

      public int getCurrentStage() {
         return this.stage;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 30L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               long riteOfEclipseCooldown = (long)TimeUtil.secsToTicks(Config.instance().riteOfEclipseCooldownInSecs);
               EntityPlayer player = ritual.getInitiatingPlayer(world);
               long lastActivation;
               if (riteOfEclipseCooldown > 0L && world.field_73010_i.size() > 1 && RiteEclipse.lastEclipseTimes.containsKey(world.field_73011_w.field_76574_g)) {
                  lastActivation = (Long)RiteEclipse.lastEclipseTimes.get(world.field_73011_w.field_76574_g);
                  if (world.func_82737_E() < lastActivation + riteOfEclipseCooldown) {
                     if (player != null) {
                        ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.eclipse.cooldown");
                     }

                     return RitualStep.Result.ABORTED_REFUND;
                  }
               }

               lastActivation = world.func_72912_H().func_76073_f();
               world.func_72912_H().func_76068_b(lastActivation - lastActivation % 24000L + 18000L);
               RiteEclipse.lastEclipseTimes.put(world.field_73011_w.field_76574_g, world.func_82737_E());
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
