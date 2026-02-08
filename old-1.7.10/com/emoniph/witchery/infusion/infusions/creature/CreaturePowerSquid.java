package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.KeyBindHelper;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class CreaturePowerSquid extends CreaturePower {
   public CreaturePowerSquid(int powerID) {
      super(powerID, EntitySquid.class);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      if (mop != null && mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g != null && mop.field_72308_g instanceof EntityLivingBase) {
         EntityLivingBase targetPlayer = (EntityLivingBase)mop.field_72308_g;
         world.func_72956_a(player, "random.fizz", 1.0F, 1.0F / (world.field_73012_v.nextFloat() * 0.4F + 1.2F) + 0.5F);
         targetPlayer.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, 200));
      }

   }

   public void onUpdate(World world, EntityPlayer player) {
      if (player.func_70090_H()) {
         Minecraft minecraft = Minecraft.func_71410_x();
         if (KeyBindHelper.isKeyBindDown(Minecraft.func_71410_x().field_71474_y.field_74351_w)) {
            player.field_70159_w *= 1.149999976158142D;
            player.field_70179_y *= 1.149999976158142D;
         }
      }

   }

   public void onDamage(World world, EntityPlayer player, LivingHurtEvent event) {
      if (event.source == DamageSource.field_76369_e) {
         int currentEnergy = Infusion.getCurrentEnergy(player);
         if (currentEnergy >= 1) {
            Infusion.setCurrentEnergy(player, currentEnergy - 1);
            SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
            player.func_70690_d(new PotionEffect(Potion.field_76427_o.field_76415_H, 1200, 1));
            player.func_70050_g(300);
            event.setCanceled(true);
         }
      }

      super.onDamage(world, player, event);
   }
}
