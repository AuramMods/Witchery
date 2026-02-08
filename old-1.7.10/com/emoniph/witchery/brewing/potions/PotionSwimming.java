package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.KeyBindHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionSwimming extends PotionBase implements IHandleLivingUpdate {
   public PotionSwimming(int id, int color) {
      super(id, color);
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if (world.field_72995_K && player.func_70090_H() && !player.func_70644_a(Witchery.Potions.PARALYSED)) {
            Minecraft minecraft = Minecraft.func_71410_x();
            if (KeyBindHelper.isKeyBindDown(Minecraft.func_71410_x().field_71474_y.field_74351_w)) {
               int amplifier = 3;
               player.field_70159_w *= 1.15D + 0.03D * (double)amplifier;
               player.field_70179_y *= 1.15D + 0.03D * (double)amplifier;
            }
         }
      } else if (world.field_72995_K && world.func_82737_E() % 10L == 3L && entity.func_70090_H() && entity.func_70644_a(Witchery.Potions.PARALYSED)) {
         entity.field_70159_w *= 1.15D + 0.03D * (double)amplifier;
         entity.field_70179_y *= 1.15D + 0.03D * (double)amplifier;
      }

   }
}
