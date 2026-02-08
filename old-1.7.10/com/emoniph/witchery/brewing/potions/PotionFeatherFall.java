package com.emoniph.witchery.brewing.potions;

import net.minecraft.entity.EntityLivingBase;

public class PotionFeatherFall extends PotionBase {
   public PotionFeatherFall(int id, int color) {
      super(id, color);
   }

   public boolean func_76397_a(int duration, int amplifier) {
      return true;
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      int activationDistance = amplifier >= 2 ? 3 : (amplifier >= 1 ? 4 : 5);
      int maxFallDistance = amplifier >= 3 ? 3 : (amplifier >= 2 ? 4 : (amplifier >= 1 ? 5 : 6));
      if (entity.field_70143_R >= (float)activationDistance && entity.field_70181_x < -0.2D) {
         entity.field_70181_x = -0.2D;
         if (entity.field_70143_R > (float)maxFallDistance) {
            entity.field_70143_R = (float)maxFallDistance;
         }
      }

   }
}
