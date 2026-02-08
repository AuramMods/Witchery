package com.emoniph.witchery.brewing.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionStoutBelly extends PotionBase implements IHandleLivingUpdate {
   public PotionStoutBelly(int id, int color) {
      super(id, color);
      this.setIncurable();
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (!world.field_72995_K && world.func_72820_D() % 20L == 3L && amplifier > 0 && entity.func_70644_a(Potion.field_76431_k)) {
         entity.func_82170_o(Potion.field_76431_k.field_76415_H);
      }

   }
}
