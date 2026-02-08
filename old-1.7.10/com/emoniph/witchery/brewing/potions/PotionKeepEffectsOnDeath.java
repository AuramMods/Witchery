package com.emoniph.witchery.brewing.potions;

public class PotionKeepEffectsOnDeath extends PotionBase {
   public PotionKeepEffectsOnDeath(int id, int color) {
      super(id, color);
   }

   public void postContructInitialize() {
      this.setPermenant();
   }
}
