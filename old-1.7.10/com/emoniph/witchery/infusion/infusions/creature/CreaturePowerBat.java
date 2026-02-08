package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.util.KeyBindHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class CreaturePowerBat extends CreaturePower {
   public CreaturePowerBat(int powerID, Class<? extends EntityLiving> creatureType) {
      super(powerID, creatureType);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      player.func_70690_d(new PotionEffect(Potion.field_76439_r.field_76415_H, 400));
   }

   public void onUpdate(World world, EntityPlayer player) {
      Minecraft minecraft = Minecraft.func_71410_x();
      if (KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74314_A) && player.field_70181_x > 0.0D) {
         player.field_70181_x += 0.06699999910593032D;
      }

      if (!player.field_70122_E && KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74314_A)) {
         player.field_70181_x = 0.41999998688697815D;
      }

   }

   public void onFalling(World worldObj, EntityPlayer player, LivingFallEvent event) {
      if (event.distance > 5.0F) {
         event.distance = 5.0F;
      }

   }
}
