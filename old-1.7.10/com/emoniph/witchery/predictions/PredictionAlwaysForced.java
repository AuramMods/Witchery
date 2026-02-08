package com.emoniph.witchery.predictions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class PredictionAlwaysForced extends Prediction {
   protected final int regularFulfillmentDurationInTicks;
   protected final double regularFulfillmentProbability;

   public PredictionAlwaysForced(int id, int itemWeight, double selfFulfillmentProbabilityPerSec, String translationKey, int regularFulfillmentDurationInTicks, double regularFulfillmentProbability) {
      super(id, itemWeight, selfFulfillmentProbabilityPerSec, translationKey);
      this.regularFulfillmentDurationInTicks = regularFulfillmentDurationInTicks;
      this.regularFulfillmentProbability = regularFulfillmentProbability;
   }

   public boolean isPredictionPastDue(long predictionTime, long currentTime) {
      return currentTime - predictionTime > (long)this.regularFulfillmentDurationInTicks;
   }

   protected boolean shouldWeActivate(World world, EntityPlayer player, boolean isPastDue) {
      return world.field_73012_v.nextDouble() < (isPastDue ? this.selfFulfillmentProbabilityPerSec : this.regularFulfillmentProbability);
   }
}
