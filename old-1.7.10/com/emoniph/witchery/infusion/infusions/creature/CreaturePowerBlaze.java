package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class CreaturePowerBlaze extends CreaturePower {
   public CreaturePowerBlaze(int powerID) {
      super(powerID, EntityBlaze.class);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      if (!world.field_72995_K) {
         world.func_72889_a((EntityPlayer)null, 1009, (int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v, 0);
         int BALLS = true;

         for(int i = 0; i < 3; ++i) {
            float f = 1.0F;
            double motionX = (double)(-MathHelper.func_76126_a(player.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(player.field_70125_A / 180.0F * 3.1415927F) * f);
            double motionZ = (double)(MathHelper.func_76134_b(player.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(player.field_70125_A / 180.0F * 3.1415927F) * f);
            double motionY = (double)(-MathHelper.func_76126_a(player.field_70125_A / 180.0F * 3.1415927F) * f);
            EntitySmallFireball fireball = new EntitySmallFireball(world, player, motionX, motionY, motionZ);
            fireball.func_70012_b(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v, fireball.field_70177_z, fireball.field_70125_A);
            fireball.func_70107_b(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v);
            motionX += world.field_73012_v.nextDouble() * 0.2D;
            motionZ += world.field_73012_v.nextDouble() * 0.2D;
            double d6 = (double)MathHelper.func_76133_a(motionX * motionX + motionY * motionY + motionZ * motionZ);
            fireball.field_70232_b = motionX / d6 * 0.1D;
            fireball.field_70233_c = motionY / d6 * 0.1D;
            fireball.field_70230_d = motionZ / d6 * 0.1D;
            double d8 = 1.0D;
            Vec3 vec3 = player.func_70676_i(1.0F);
            fireball.field_70165_t = player.field_70165_t + vec3.field_72450_a * d8;
            fireball.field_70163_u = player.field_70163_u + (double)(player.field_70131_O / 2.0F) + 0.5D;
            fireball.field_70161_v = player.field_70161_v + vec3.field_72449_c * d8;
            world.func_72838_d(fireball);
         }
      }

   }

   public void onDamage(World world, EntityPlayer player, LivingHurtEvent event) {
      if (event.source.func_76347_k() && event.isCancelable()) {
         int currentEnergy = Infusion.getCurrentEnergy(player);
         if (currentEnergy >= 3 && !player.func_82165_m(Potion.field_76426_n.field_76415_H)) {
            Infusion.setCurrentEnergy(player, currentEnergy - 3);
            player.func_70690_d(new PotionEffect(Potion.field_76426_n.field_76415_H, 200, 0));
            SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
         }
      }

   }
}
