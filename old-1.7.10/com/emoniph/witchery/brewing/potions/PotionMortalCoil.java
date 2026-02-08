package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.util.EntityUtil;
import net.minecraft.entity.EntityLivingBase;

public class PotionMortalCoil extends PotionBase {
   public PotionMortalCoil(int id, int color) {
      super(id, true, 0);
      this.setIncurable();
   }

   public boolean func_76397_a(int duration, int amplifier) {
      return duration == 1;
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      EntityUtil.instantDeath(entity, (EntityLivingBase)null);
   }
}
