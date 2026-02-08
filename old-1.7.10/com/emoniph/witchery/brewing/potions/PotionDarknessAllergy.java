package com.emoniph.witchery.brewing.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

public class PotionDarknessAllergy extends PotionBase {
   public PotionDarknessAllergy(int id, int color) {
      super(id, true, color);
      this.setIncurable();
   }

   public boolean func_76397_a(int duration, int amplifier) {
      return duration % 20 == 4;
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      int x = MathHelper.func_76128_c(entity.field_70165_t);
      int y = MathHelper.func_76128_c(entity.field_70163_u);
      int z = MathHelper.func_76128_c(entity.field_70161_v);
      int lightLevel = entity.field_70170_p.func_72957_l(x, y, z);
      if (lightLevel < 2 + amplifier * 2) {
         entity.func_70097_a(DamageSource.field_76380_i, 1.0F);
      }

   }
}
