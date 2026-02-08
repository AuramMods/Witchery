package com.emoniph.witchery.brewing.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class PotionFloating extends PotionBase {
   public PotionFloating(int id, int color) {
      super(id, color);
   }

   public boolean func_76397_a(int duration, int amplifier) {
      return true;
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      int height = 3 + amplifier;
      int x = MathHelper.func_76128_c(entity.field_70165_t);
      int y = MathHelper.func_76128_c(entity.field_70163_u);
      int z = MathHelper.func_76128_c(entity.field_70161_v);
      boolean isPlayer = entity instanceof EntityPlayer;
      boolean activeOnSide = isPlayer && entity.field_70170_p.field_72995_K || !isPlayer && !entity.field_70170_p.field_72995_K;
      entity.field_70143_R = 0.0F;
      if (activeOnSide) {
         boolean raised = false;

         for(int i = 1; i <= height; ++i) {
            if (!entity.field_70170_p.func_147437_c(x, y - i, z)) {
               entity.field_70181_x = 0.25D;
               raised = true;
               break;
            }
         }

         if (!raised) {
            entity.field_70181_x = entity.field_70170_p.field_73012_v.nextInt(5) == 0 ? -0.05D : 0.0D;
         }
      }

   }
}
