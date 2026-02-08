package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.network.PacketSound;
import com.emoniph.witchery.util.KeyBindHelper;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TargetPointUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class CreaturePowerJump extends CreaturePower {
   public CreaturePowerJump(int powerID, Class<? extends EntityLiving> creatureType) {
      super(powerID, creatureType);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      player.func_70690_d(new PotionEffect(Potion.field_76430_j.field_76415_H, 400, 3));
      SoundEffect.RANDOM_FIZZ.playAtPlayer(world, player);
   }

   public void onUpdate(World world, EntityPlayer player) {
      Minecraft minecraft = Minecraft.func_71410_x();
      if (KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74314_A) && player.field_70181_x > 0.0D) {
         player.field_70181_x += 0.06D;
      }

   }

   public void onFalling(World world, EntityPlayer player, LivingFallEvent event) {
      if (!world.field_72995_K) {
         Witchery.packetPipeline.sendToAllAround(new PacketSound(SoundEffect.MOB_SLIME_BIG, player), TargetPointUtil.from(player, 8.0D));
         event.distance = 0.0F;
      }

   }
}
