package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class CreaturePowerSkeleton extends CreaturePower {
   public CreaturePowerSkeleton(int powerID) {
      super(powerID, EntitySkeleton.class);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      if (!world.field_72995_K) {
         float f = (float)elapsedTicks / 20.0F;
         f = (f * f + f * 2.0F) / 3.0F;
         if (f > 1.0F) {
            f = 1.0F;
         }

         world.func_72956_a(player, "random.bow", 1.0F, 1.0F / (world.field_73012_v.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
         EntityArrow entityarrow = new EntityArrow(world, player, f * 2.0F);
         if (f == 1.0F) {
            entityarrow.func_70243_d(true);
         }

         int EXTRA_PUNCH = true;
         entityarrow.func_70240_a(1);
         int EXTRA_DAMAGE = true;
         entityarrow.func_70239_b(entityarrow.func_70242_d() + 0.5D + 0.5D);
         world.func_72838_d(entityarrow);
      }

   }

   public void onDamage(World world, EntityPlayer player, LivingHurtEvent event) {
      if (!world.field_72995_K && event.source == DamageSource.field_76369_e) {
         int currentEnergy = Infusion.getCurrentEnergy(player);
         if (currentEnergy >= 5) {
            Infusion.setCurrentEnergy(player, currentEnergy - 5);
            SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
            player.func_70690_d(new PotionEffect(Potion.field_76427_o.field_76415_H, 600, 0));
            player.func_70050_g(30);
            event.setCanceled(true);
         }
      }

      super.onDamage(world, player, event);
   }
}
