package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class CreaturePowerPigMan extends CreaturePower {
   public CreaturePowerPigMan(int powerID) {
      super(powerID, EntityPigZombie.class);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      if (!world.field_72995_K) {
         player.func_70690_d(new PotionEffect(Potion.field_76429_m.field_76415_H, 600, 2));
         player.func_70690_d(new PotionEffect(Potion.field_76420_g.field_76415_H, 600, 2));
         SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
      }

   }

   public void onDamage(World world, EntityPlayer player, LivingHurtEvent event) {
      if (event.source.func_76347_k() && event.isCancelable()) {
         int currentEnergy = Infusion.getCurrentEnergy(player);
         if (currentEnergy >= 3) {
            Infusion.setCurrentEnergy(player, currentEnergy - 3);
            player.func_70690_d(new PotionEffect(Potion.field_76426_n.field_76415_H, 1200, 0));
            SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
            event.setCanceled(true);
         }
      }

   }
}
