package com.emoniph.witchery.brewing.potions;

public class PotionGasMask extends PotionBase {
   public PotionGasMask(int id, int color) {
      super(id, color);
   }

   public void postContructInitialize() {
      this.setIncurable();
   }
}
