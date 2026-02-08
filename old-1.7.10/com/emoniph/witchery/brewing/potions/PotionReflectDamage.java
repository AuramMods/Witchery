package com.emoniph.witchery.brewing.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PotionReflectDamage extends PotionBase implements IHandleLivingHurt {
   public PotionReflectDamage(int id, int color) {
      super(id, color);
   }

   public boolean handleAllHurtEvents() {
      return false;
   }

   public void onLivingHurt(World world, EntityLivingBase entity, LivingHurtEvent event, int amplifier) {
      if (!world.field_72995_K) {
         EntityLivingBase attacker = event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityLivingBase ? (EntityLivingBase)event.source.func_76346_g() : null;
         if (attacker != null && attacker != entity && (!event.source.func_76352_a() || amplifier >= 2)) {
            float amount = (float)Math.ceil((double)(event.ammount * 0.1F * (float)(amplifier + (!event.source.func_76352_a() ? 1 : 0))));
            attacker.func_70097_a(event.source, amount);
            event.ammount -= amount;
         }
      }

   }
}
