package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.util.KeyBindHelper;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class CreaturePowerSpeed extends CreaturePower {
   public CreaturePowerSpeed(int powerID, Class<? extends EntityLiving> creatureType) {
      super(powerID, creatureType);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      player.func_70690_d(new PotionEffect(Potion.field_76424_c.field_76415_H, 400, 3));
      SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
   }

   public void onUpdate(World world, EntityPlayer player) {
      Minecraft minecraft = Minecraft.func_71410_x();
      int var5 = MathHelper.func_76128_c(player.field_70165_t);
      int var6 = MathHelper.func_76128_c(player.field_70163_u - 2.0D);
      int var7 = MathHelper.func_76128_c(player.field_70161_v);
      if (KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74351_w) || KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74368_y) || KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74370_x) || KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74366_z)) {
         if (world.func_147439_a(var5, var6, var7) != Blocks.field_150432_aD) {
            if (player.field_70122_E && !player.func_70090_H()) {
               player.field_70159_w *= 1.4500000476837158D;
               player.field_70179_y *= 1.4500000476837158D;
            }
         } else {
            player.field_70159_w *= 1.100000023841858D;
            player.field_70179_y *= 1.100000023841858D;
         }
      }

   }
}
